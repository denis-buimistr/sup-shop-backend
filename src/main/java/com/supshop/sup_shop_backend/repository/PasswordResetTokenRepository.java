package com.supshop.sup_shop_backend.repository;

import com.supshop.sup_shop_backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    // сколько ссылок юзер уже просил за последний час, чтобы не дать заспамить его почту
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    // гашу все живые токены юзера: при новом запросе рабочей остаётся только последняя ссылка
    @Modifying
    @Query("update PasswordResetToken t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
    int invalidateAll(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}