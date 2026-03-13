package com.alertmns.service;

import com.alertmns.entity.Canal;
import com.alertmns.entity.MembreCanal;
import com.alertmns.entity.MembreCanalId;
import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.CanalRepository;
import com.alertmns.repository.MembreCanalRepository;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CanalService {

    private final CanalRepository canalRepository;
    private final MembreCanalRepository membreCanalRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional(readOnly = true)
    public List<Canal> getAllCanaux() {
        return canalRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Canal getCanalById(Long id) {
        return canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé: " + id));
    }

    @Transactional(readOnly = true)
    public List<Canal> getCanauxForUser(Long userId) {
        return canalRepository.findCanauxForUser(userId);
    }

    @Transactional
    public Canal createCanal(String nom, Boolean estPrive, Long creatorUserId) {
        Canal canal = Canal.builder()
                .nom(nom)
                .estPrive(estPrive != null ? estPrive : false)
                .build();
        canal = canalRepository.save(canal);

        // Add creator as admin member
        Utilisateur creator = utilisateurRepository.findById(creatorUserId)
                .orElseThrow(() -> new RuntimeException("Utilisateur créateur non trouvé"));

        MembreCanal membre = MembreCanal.builder()
                .id(new MembreCanalId(creatorUserId, canal.getIdCanal()))
                .utilisateur(creator)
                .canal(canal)
                .role("admin")
                .build();
        membreCanalRepository.save(membre);

        return canal;
    }

    @Transactional
    public Canal updateCanal(Long id, String nom, Boolean estPrive) {
        Canal canal = canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé: " + id));
        if (nom != null) canal.setNom(nom);
        if (estPrive != null) canal.setEstPrive(estPrive);
        return canalRepository.save(canal);
    }

    @Transactional
    public void deleteCanal(Long id) {
        canalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé: " + id));
        canalRepository.deleteById(id);
    }

    @Transactional
    public MembreCanal addMember(Long canalId, Long userId, String role) {
        Canal canal = canalRepository.findById(canalId)
                .orElseThrow(() -> new RuntimeException("Canal non trouvé: " + canalId));
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        MembreCanalId membreId = new MembreCanalId(userId, canalId);
        if (membreCanalRepository.existsByIdIdUserAndIdIdCanal(userId, canalId)) {
            throw new RuntimeException("L'utilisateur est déjà membre de ce canal");
        }

        MembreCanal membre = MembreCanal.builder()
                .id(membreId)
                .utilisateur(utilisateur)
                .canal(canal)
                .role(role != null ? role : "member")
                .build();
        return membreCanalRepository.save(membre);
    }

    @Transactional
    public void removeMember(Long canalId, Long userId) {
        MembreCanal membre = membreCanalRepository.findByIdIdUserAndIdIdCanal(userId, canalId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé dans ce canal"));
        membreCanalRepository.delete(membre);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMembres(Long canalId) {
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
                    return m;
                })
                .collect(Collectors.toList());
    }
}
