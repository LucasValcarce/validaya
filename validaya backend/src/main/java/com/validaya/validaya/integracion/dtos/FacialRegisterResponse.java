package com.validaya.validaya.integracion.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacialRegisterResponse {
    private boolean success;
    private String person_id;
    private Integer total_encodings;
    private String message;
    private String error;
}
