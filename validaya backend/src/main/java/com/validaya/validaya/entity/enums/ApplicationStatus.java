package com.validaya.validaya.entity.enums;

public enum ApplicationStatus {
    pending_documents,    // Usuario aún no ha adjuntado todos los documentos requeridos
    documents_verified,   // Sistema verificó automáticamente que tiene todos los documentos
    payment_pending,      // Esperando que se realice el pago
    payment_confirmed,    // Pago confirmado
    scheduled,            // Cita agendada
    completed,            // Trámite completado
    rejected,             // Solicitud rechazada
    cancelled             // Solicitud cancelada por el usuario
}
