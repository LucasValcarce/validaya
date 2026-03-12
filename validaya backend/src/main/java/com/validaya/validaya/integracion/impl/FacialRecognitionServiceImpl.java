package com.validaya.validaya.integracion.impl;

import com.validaya.validaya.integracion.FacialRecognitionService;
import com.validaya.validaya.integracion.dtos.FacialRegisterRequest;
import com.validaya.validaya.integracion.dtos.FacialRegisterResponse;
import com.validaya.validaya.integracion.dtos.FacialVerifyRequest;
import com.validaya.validaya.integracion.dtos.FacialVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FacialRecognitionServiceImpl implements FacialRecognitionService {

    private final RestTemplate restTemplate;

    @Value("${facial.recognition.api.url:http://localhost:5000}")
    private String facialApiBaseUrl;

    @Override
    public FacialVerifyResponse verifyFace(String identification, String faceBase64) {
        try {
            String verifyUrl = facialApiBaseUrl + "/api/verify";
            
            FacialVerifyRequest request = FacialVerifyRequest.builder()
                    .person_id(identification)
                    .image(faceBase64)
                    .build();

            log.info("Solicitando verificación facial para: {}", identification);
            
            FacialVerifyResponse response = restTemplate.postForObject(
                    verifyUrl,
                    request,
                    FacialVerifyResponse.class
            );

            if (response != null) {
                log.info("Verificación facial completada para {}: match={}, confidence={}",
                        identification, response.isMatch(), response.getConfidence());
                return response;
            } else {
                log.warn("Respuesta nula del servicio de verificación facial para: {}", identification);
                return FacialVerifyResponse.builder()
                        .success(false)
                        .match(false)
                        .confidence(0.0)
                        .person_id(identification)
                        .message("Error: respuesta nula del servicio de verificación")
                        .build();
            }
        } catch (RestClientException e) {
            log.error("Error al conectar con el servicio de verificación facial ({}): {}",
                    facialApiBaseUrl, e.getMessage());
            return FacialVerifyResponse.builder()
                    .success(false)
                    .match(false)
                    .confidence(0.0)
                    .person_id(identification)
                    .message("Error: no se pudo conectar con el servicio de verificación facial")
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado en verificación facial para {}: {}", identification, e.getMessage(), e);
            return FacialVerifyResponse.builder()
                    .success(false)
                    .match(false)
                    .confidence(0.0)
                    .person_id(identification)
                    .message("Error interno: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public FacialRegisterResponse registerFace(String identification, String faceBase64) {
        try {
            String registerUrl = facialApiBaseUrl + "/api/register";
            
            FacialRegisterRequest request = FacialRegisterRequest.builder()
                    .person_id(identification)
                    .image(faceBase64)
                    .build();

            log.info("Solicitando registro facial para: {}", identification);
            
            FacialRegisterResponse response = restTemplate.postForObject(
                    registerUrl,
                    request,
                    FacialRegisterResponse.class
            );

            if (response != null) {
                log.info("Registro facial completado para {}: success={}", 
                        identification, response.isSuccess());
                return response;
            } else {
                log.warn("Respuesta nula del servicio de registro facial para: {}", identification);
                return FacialRegisterResponse.builder()
                        .success(false)
                        .person_id(identification)
                        .error("Respuesta nula del servicio")
                        .build();
            }
        } catch (RestClientException e) {
            log.error("Error al conectar con el servicio de registro facial ({}): {}",
                    facialApiBaseUrl, e.getMessage());
            return FacialRegisterResponse.builder()
                    .success(false)
                    .person_id(identification)
                    .error("No se pudo conectar con el servicio: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado en registro facial para {}: {}", identification, e.getMessage(), e);
            return FacialRegisterResponse.builder()
                    .success(false)
                    .person_id(identification)
                    .error("Error interno: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean deleteFace(String identification) {
        try {
            String deleteUrl = facialApiBaseUrl + "/api/delete/" + identification;
            
            log.info("Solicitando eliminación de registro facial para: {}", identification);
            
            restTemplate.delete(deleteUrl);
            
            log.info("Registro facial eliminado exitosamente para: {}", identification);
            return true;
        } catch (RestClientException e) {
            log.error("Error al conectar con el servicio de eliminación facial ({}): {}",
                    facialApiBaseUrl, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al eliminar registro facial para {}: {}", identification, e.getMessage(), e);
            return false;
        }
    }
}
