package com.validaya.validaya.integracion.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacialVerifyResponse {
    private boolean success;
    private boolean match;
    private double confidence;
    private double distance;
    private double threshold;
    private String person_id;
    private String timestamp;
    private String message;
}
