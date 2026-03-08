package com.validaya.validaya.integracion.impl.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.validaya.validaya.integracion.impl.dtos.StereumUtils.StereumUserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StereumAuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String id_token;
    private String refresh_token;
    private String token_type;
    private String expires_in;
    private Long expires_at;
    private String provider_id;
    private String sign_in_provider;
    private StereumUserInfo user;
    private String session_id;
}
