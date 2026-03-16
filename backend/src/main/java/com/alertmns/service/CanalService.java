package com.alertmns.service;

import com.alertmns.entity.Canal;
import com.alertmns.entity.MembreCanal;
import com.alertmns.entity.MembreCanalId;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.CanalRepository;
import com.alertmns.repository.MessageRepository;
import com.alertmns.repository.MembreCanalRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CanalService {

    private final CanalRepository canalRepository;
    private final MembreCanalRepository membreCanalRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MessageRepository messageRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<Canal> getAllCanaux() {
        return enrichMemberCounts(canalRepository.findByTypeCanalNotOrderByNomAsc("direct"));
    }

    @Transactional(readOnly = true)
    public Canal getCanalById(Long id) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + id));
        return enrichMemberCount(canal);
    }

    @Transactional(readOnly = true)
    public Canal getCanalByIdForUser(Long id, Long userId) {
        Canal canal = getCanalById(id);
        assertUserHasAccess(id, userId);
        return canal;
    }

    @Transactional(readOnly = true)
    public List<Canal> getCanauxForUser(Long userId) {
        return enrichMemberCounts(canalRepository.findCanauxForUser(userId));
    }

    @Transactional
    public Canal createCanal(String nom, Boolean estPrive, Long creatorUserId) {
        String canalName = sanitizeCanalName(nom);
        if (canalRepository.existsByNomIgnoreCase(canalName)) {
            throw new RuntimeException("Un canal avec ce nom existe deja");
        }

        Canal canal = Canal.builder()
                .nom(canalName)
                .estPrive(estPrive != null ? estPrive : false)
                .typeCanal("canal")
                .build();
        canal = canalRepository.save(canal);

        Utilisateur creator = utilisateurRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur createur non trouve"));

        MembreCanal membre = MembreCanal.builder()
                .id(new MembreCanalId(creatorUserId, canal.getIdCanal()))
                .utilisateur(creator)
                .canal(canal)
                .role("admin")
                .build();
        membreCanalRepository.save(membre);

        auditLogService.logAction(creatorUserId, "CANAL_CREATED", "CANAL", canal.getIdCanal(),
                "Canal créé : " + canalName + (Boolean.TRUE.equals(estPrive) ? " (privé)" : " (public)"));
        return enrichMemberCount(canal);
    }

    @Transactional
    public Canal createDirectCanal(Long requesterUserId, Long otherUserId) {
        if (requesterUserId == null || otherUserId == null) {
            throw new RuntimeException("Utilisateurs requis");
        }
        if (Objects.equals(requesterUserId, otherUserId)) {
            throw new RuntimeException("Vous ne pouvez pas demarrer une conversation avec vous-meme");
        }

        Utilisateur requester = utilisateurRepository.findById(requesterUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur courant non trouve"));
        Utilisateur otherUser = utilisateurRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve: " + otherUserId));

        if (!Boolean.TRUE.equals(requester.getIsActive())) {
            throw new RuntimeException("Votre compte n'est pas actif");
        }
        if (!Boolean.TRUE.equals(otherUser.getIsActive())) {
            throw new RuntimeException("Impossible d'ouvrir une conversation avec un utilisateur inactif");
        }

        Canal existing = canalRepository.findDirectCanalBetweenUsers(requesterUserId, otherUserId)
                .map(this::enrichMemberCount)
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        Long lowId = Math.min(requesterUserId, otherUserId);
        Long highId = Math.max(requesterUserId, otherUserId);
        Canal canal = Canal.builder()
                .nom("__direct__" + lowId + "_" + highId)
                .estPrive(true)
                .typeCanal("direct")
                .build();
        canal = canalRepository.save(canal);

        membreCanalRepository.save(MembreCanal.builder()
                .id(new MembreCanalId(requesterUserId, canal.getIdCanal()))
                .utilisateur(requester)
                .canal(canal)
                .role("membre")
                .build());

        membreCanalRepository.save(MembreCanal.builder()
                .id(new MembreCanalId(otherUserId, canal.getIdCanal()))
                .utilisateur(otherUser)
                .canal(canal)
                .role("membre")
                .build());

        return enrichMemberCount(canal);
    }

    @Transactional
    public Canal updateCanal(Long id, String nom, Boolean estPrive) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + id));
        ensureRegularChannel(canal);

        if (nom != null) {
            String canalName = sanitizeCanalName(nom);
            if (canalRepository.existsByNomIgnoreCaseAndIdCanalNot(canalName, id)) {
                throw new RuntimeException("Un canal avec ce nom existe deja");
            }
            canal.setNom(canalName);
        }
        if (estPrive != null) {
            canal.setEstPrive(estPrive);
        }
        return enrichMemberCount(canalRepository.save(canal));
    }

    @Transactional
    public void deleteCanal(Long id) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + id));
        ensureRegularChannel(canal);
        auditLogService.logAction(null, "CANAL_DELETED", "CANAL", id,
                "Canal supprimé : " + canal.getNom());
        canalRepository.deleteById(id);
    }

    @Transactional
    public MembreCanal addMember(Long canalId, Long userId, String role) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + canalId));
        ensureRegularChannel(canal);

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve: " + userId));

        if (membreCanalRepository.existsByIdIdUserAndIdIdCanal(userId, canalId)) {
            throw new RuntimeException("L'utilisateur est deja membre de ce canal");
        }

        MembreCanal membre = MembreCanal.builder()
                .id(new MembreCanalId(userId, canalId))
                .utilisateur(utilisateur)
                .canal(canal)
                .role(normalizeMemberRole(role))
                .build();
        MembreCanal saved = membreCanalRepository.save(membre);
        auditLogService.logAction(null, "CANAL_MEMBER_ADDED", "CANAL", canalId,
                "Utilisateur " + userId + " ajouté au canal " + canal.getNom() + " (rôle : " + normalizeMemberRole(role) + ")");
        return saved;
    }

    @Transactional
    public MembreCanal updateMemberRole(Long canalId, Long userId, String role) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + canalId));
        ensureRegularChannel(canal);

        MembreCanal membre = membreCanalRepository.findByIdIdUserAndIdIdCanal(userId, canalId)
                .orElseThrow(() -> new RuntimeException("Membre non trouve dans ce canal"));

        String normalizedRole = normalizeMemberRole(role);
        String currentRole = membre.getRole() != null ? membre.getRole() : "membre";

        if (currentRole.equals(normalizedRole)) {
            return membre;
        }

        if ("admin".equals(currentRole) && !"admin".equals(normalizedRole)) {
            ensureAnotherAdminExists(canalId);
        }

        membre.setRole(normalizedRole);
        return membreCanalRepository.save(membre);
    }

    @Transactional
    public void removeMember(Long canalId, Long userId) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouve: " + canalId));
        ensureRegularChannel(canal);

        MembreCanal membre = membreCanalRepository.findByIdIdUserAndIdIdCanal(userId, canalId)
                .orElseThrow(() -> new RuntimeException("Membre non trouve dans ce canal"));

        if ("admin".equals(membre.getRole())) {
            ensureAnotherAdminExists(canalId);
        }

        membreCanalRepository.delete(membre);
        auditLogService.logAction(null, "CANAL_MEMBER_REMOVED", "CANAL", canalId,
                "Utilisateur " + userId + " retiré du canal " + canal.getNom());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMembres(Long canalId, Long requesterUserId) {
        assertUserHasAccess(canalId, requesterUserId);
        return membreCanalRepository.findByCanalId(canalId).stream()
                .map(mc -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id", mc.getUtilisateur().getIdUser());
                    m.put("userId", mc.getUtilisateur().getIdUser());
                    m.put("nom", mc.getUtilisateur().getNom());
                    m.put("prenom", mc.getUtilisateur().getPrenom());
                    m.put("email", mc.getUtilisateur().getEmail());
                    m.put("role", mc.getRole() != null ? mc.getRole() : "membre");
                    m.put("msgAbsence", mc.getUtilisateur().getMsgAbsence());
                    m.put("avatarUrl", mc.getUtilisateur().getAvatarUrl());
                    return m;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Utilisateur getDirectPeer(Long canalId, Long requesterUserId) {
        assertUserHasAccess(canalId, requesterUserId);
        Canal canal = getCanalById(canalId);
        if (!"direct".equalsIgnoreCase(canal.getTypeCanal())) {
            return null;
        }

        Long peerId = membreCanalRepository.findByCanalId(canalId).stream()
                .map(membre -> membre.getUtilisateur().getIdUser())
                .filter(id -> !Objects.equals(id, requesterUserId))
                .findFirst()
                .orElse(null);

        if (peerId == null) {
            return null;
        }

        return utilisateurRepository.findById(peerId).orElse(null);
    }

    @Transactional(readOnly = true)
    public void assertUserHasAccess(Long canalId, Long userId) {
        if (!canalRepository.existsById(canalId)) {
            throw new RuntimeException("Canal non trouve: " + canalId);
        }
        if (userId == null || !membreCanalRepository.existsByIdIdUserAndIdIdCanal(userId, canalId)) {
            throw new SecurityException("Acces refuse a cette conversation");
        }
    }

    private String sanitizeCanalName(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new RuntimeException("Le nom du canal est obligatoire");
        }
        return nom.trim();
    }

    private List<Canal> enrichMemberCounts(List<Canal> canaux) {
        canaux.forEach(this::enrichMemberCount);
        enrichLastMessageDates(canaux);
        return canaux;
    }

    private Canal enrichMemberCount(Canal canal) {
        if (canal != null && canal.getIdCanal() != null) {
            canal.setMembresCount(membreCanalRepository.countByIdIdCanal(canal.getIdCanal()));
            enrichLastMessageDates(List.of(canal));
        }
        return canal;
    }

    private void enrichLastMessageDates(List<Canal> canaux) {
        if (canaux == null || canaux.isEmpty()) {
            return;
        }

        List<Long> canalIds = canaux.stream()
                .map(Canal::getIdCanal)
                .filter(Objects::nonNull)
                .toList();

        if (canalIds.isEmpty()) {
            return;
        }

        Map<Long, java.time.LocalDateTime> lastMessageDates = new HashMap<>();
        for (Object[] row : messageRepository.findLastMessageDates(canalIds)) {
            if (row.length >= 2 && row[0] instanceof Long canalId && row[1] instanceof java.time.LocalDateTime dateTime) {
                lastMessageDates.put(canalId, dateTime);
            }
        }

        canaux.forEach(canal -> canal.setLastMessageAt(lastMessageDates.get(canal.getIdCanal())));
    }

    private void ensureAnotherAdminExists(Long canalId) {
        long adminCount = membreCanalRepository.countByIdIdCanalAndRole(canalId, "admin");
        if (adminCount <= 1) {
            throw new RuntimeException("Le canal doit conserver au moins un admin");
        }
    }

    private void ensureRegularChannel(Canal canal) {
        if (canal != null && "direct".equalsIgnoreCase(canal.getTypeCanal())) {
            throw new RuntimeException("Cette conversation privee ne peut pas etre geree comme un canal");
        }
    }

    private String normalizeMemberRole(String role) {
        if (role == null || role.isBlank()) {
            return "membre";
        }

        String normalizedRole = role.trim().toLowerCase();
        return "member".equals(normalizedRole) ? "membre" : normalizedRole;
    }
}
