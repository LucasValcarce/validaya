package com.validaya.validaya.service.impl;

import com.validaya.validaya.entity.Notification;
import com.validaya.validaya.entity.User;
import com.validaya.validaya.entity.enums.NotificationChannel;
import com.validaya.validaya.repository.NotificationRepository;
import com.validaya.validaya.repository.UserRepository;
import com.validaya.validaya.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void send(Long userId, String title, String message,
                     NotificationChannel channel, String referenceType, Long referenceId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("No se pudo enviar notificación: usuario {} no encontrado", userId);
            return;
        }

        Notification notification = Notification.builder()
                .user(user)
                .type(referenceType != null ? referenceType : "general")
                .title(title)
                .body(message)
                .channel(channel)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .isRead(false)
                .sentAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        log.info("Notificación enviada a usuario {}: {}", userId, title);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnread(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unread);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}