package com.validaya.validaya.controller;

import com.validaya.validaya.entity.Notification;
import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<Notification>>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.getUnread(userId)));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<ApiResponse<Long>> countUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.countUnread(userId)));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída", null));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("Todas las notificaciones marcadas como leídas", null));
    }
}