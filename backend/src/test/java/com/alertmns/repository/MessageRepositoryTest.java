package com.alertmns.repository;

import com.alertmns.entity.Canal;
import com.alertmns.entity.Message;
import com.alertmns.entity.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CanalRepository canalRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private Canal canal;
    private Utilisateur user;

    @BeforeEach
    void setUp() {
        user = utilisateurRepository.save(Utilisateur.builder()
                .nom("Dupont")
                .prenom("Marie")
                .email("marie.msg@alertmns.fr")
                .mdp("hash")
                .role("RH")
                .isActive(true)
                .build());

        canal = canalRepository.save(Canal.builder()
                .nom("general-test")
                .typeCanal("groupe")
                .build());
    }

    private Message saveMessage(String contenu) {
        return messageRepository.save(Message.builder()
                .canal(canal)
                .utilisateur(user)
                .contenu(contenu)
                .dateEnvoi(LocalDateTime.now())
                .build());
    }

    @Test
    void findLatestByIdCanalPaged_returnsMessagesInDescendingIdOrder() {
        Message m1 = saveMessage("premier");
        Message m2 = saveMessage("deuxième");
        Message m3 = saveMessage("troisième");

        List<Message> results = messageRepository.findLatestByIdCanalPaged(
                canal.getIdCanal(), PageRequest.of(0, 10));

        assertThat(results).hasSize(3);
        // DESC order → most recent (highest id) first
        assertThat(results.get(0).getIdMessage()).isGreaterThan(results.get(1).getIdMessage());
        assertThat(results.get(1).getIdMessage()).isGreaterThan(results.get(2).getIdMessage());
    }

    @Test
    void findLatestByIdCanalPaged_respectsPageLimit() {
        for (int i = 0; i < 5; i++) {
            saveMessage("message " + i);
        }

        List<Message> results = messageRepository.findLatestByIdCanalPaged(
                canal.getIdCanal(), PageRequest.of(0, 3));

        assertThat(results).hasSize(3);
    }

    @Test
    void findBeforeIdPaged_returnsOnlyMessagesWithIdStrictlyBeforeGiven() {
        Message m1 = saveMessage("ancien");
        Message m2 = saveMessage("milieu");
        Message m3 = saveMessage("récent");

        List<Message> results = messageRepository.findBeforeIdPaged(
                canal.getIdCanal(), m3.getIdMessage(), PageRequest.of(0, 10));

        assertThat(results)
                .extracting(Message::getIdMessage)
                .doesNotContain(m3.getIdMessage())
                .contains(m1.getIdMessage(), m2.getIdMessage());
    }

    @Test
    void findBeforeIdPaged_returnsEmptyWhenNoOlderMessages() {
        Message m1 = saveMessage("seul message");

        List<Message> results = messageRepository.findBeforeIdPaged(
                canal.getIdCanal(), m1.getIdMessage(), PageRequest.of(0, 10));

        assertThat(results).isEmpty();
    }

    @Test
    void findLatestByIdCanalPaged_returnsEmptyForUnknownCanal() {
        List<Message> results = messageRepository.findLatestByIdCanalPaged(
                999999L, PageRequest.of(0, 10));

        assertThat(results).isEmpty();
    }
}
