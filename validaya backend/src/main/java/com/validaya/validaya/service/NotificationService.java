package com.validaya.validaya.service;

import com.validaya.validaya.entity.Notification;
import com.validaya.validaya.entity.enums.NotificationChannel;

import java.util.List;

public interface NotificationService {

    void send(Long userId, String title, String message,
              NotificationChannel channel, String referenceType, Long referenceId);

    List<Notification> getUnread(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    long countUnread(Long userId);
}