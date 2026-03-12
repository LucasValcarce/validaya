package com.validaya.validaya.integracion;

import com.validaya.validaya.integracion.dtos.FacialRegisterRequest;
import com.validaya.validaya.integracion.dtos.FacialRegisterResponse;
import com.validaya.validaya.integracion.dtos.FacialVerifyRequest;
import com.validaya.validaya.integracion.dtos.FacialVerifyResponse;

public interface FacialRecognitionService {

    FacialVerifyResponse verifyFace(String identification, String faceBase64);

    FacialRegisterResponse registerFace(String identification, String faceBase64);

    boolean deleteFace(String identification);
}
