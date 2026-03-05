package com.validaya.validaya.controller;

import com.validaya.validaya.entity.UserDocument;
import com.validaya.validaya.entity.dto.ApiResponse;
import com.validaya.validaya.entity.dto.UserDocumentDto;
import com.validaya.validaya.service.UserDocumentService;
import com.validaya.validaya.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-documents")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class UserDocumentController {

    private final UserDocumentService userDocumentService;

    /**
     * Obtener todos los documentos del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDocumentDto.Response>>> getMyDocuments() {
        Long userId = getCurrentUserId();
        List<UserDocument> documents = userDocumentService.findByUser(userId);
        List<UserDocumentDto.Response> responses = documents.stream()
                .map(MapperUtil::toUserDocumentResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    /**
     * Obtener un documento específico del usuario autenticado.
     * Valida que el documento pertenezca al usuario actual.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDocumentDto.Response>> getDocument(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        UserDocument document = userDocumentService.findById(id)
                .orElse(null);

        if (document == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Documento no encontrado", null));
        }

        // Validar que el documento pertenezca al usuario actual
        if (!document.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("No tienes permiso para acceder a este documento", null));
        }

        UserDocumentDto.Response response = MapperUtil.toUserDocumentResponse(document);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Método auxiliar para obtener el ID del usuario del JWT actual.
     */
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication auth = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.getPrincipal() instanceof com.validaya.validaya.config.security.UserPrincipal) {
            com.validaya.validaya.config.security.UserPrincipal principal = 
                (com.validaya.validaya.config.security.UserPrincipal) auth.getPrincipal();
            return principal.getId();
        }
        throw new IllegalArgumentException("No se pudo obtener el ID del usuario del JWT");
    }
}
