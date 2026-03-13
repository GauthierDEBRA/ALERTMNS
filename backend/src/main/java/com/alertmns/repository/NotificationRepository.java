package com.alertmns.repository;

import com.alertmns.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.utilisateur.idUser = :userId AND n.isLu = :isLu ORDER BY n.idNotif DESC")
    List<Notification> findByIdUserAndIsLu(@Param("userId") Long userId, @Param("isLu") Boolean isLu);

    @Query("SELECT n FROM Notification n WHERE n.utilisateur.idUser = :userId ORDER BY n.idNotif DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.utilisateur.idUser = :userId AND n.isLu = false")
    Long countUnreadByUserId(@Param("userId") Long userId);
}
