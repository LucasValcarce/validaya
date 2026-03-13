package com.validaya.validaya.integracion.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacialRegisterRequest {
    private String person_id;      // CI del usuario
    private String image_base64;          // Base64 de la imagen del rostro
}
