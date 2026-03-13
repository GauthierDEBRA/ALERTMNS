package com.alertmns.security;

import com.alertmns.entity.Utilisateur;
import com.alertmns.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email));

        if (!Boolean.TRUE.equals(utilisateur.getIsActive())) {
            throw new UsernameNotFoundException("Compte désactivé pour: " + email);
        }

        return new User(
                utilisateur.getEmail(),
                utilisateur.getMdp(),
                List.of(new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole()))
        );
    }
}
