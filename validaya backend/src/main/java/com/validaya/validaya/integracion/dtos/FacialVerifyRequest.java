package com.validaya.validaya.integracion.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request para verificar rostro en el servicio de modelado facial
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacialVerifyRequest {
    private String person_id;
    private String image;
}
