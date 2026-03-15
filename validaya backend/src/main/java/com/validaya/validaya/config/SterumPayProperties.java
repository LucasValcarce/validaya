package com.validaya.validaya.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "stereum")
public class SterumPayProperties {

    private String baseUrl;

    private String username;

    private String claveIntegracion;

    private String publicKey;

    private String callbackUrl;

    private int connectTimeout = 5000;

    private int readTimeout = 10000;

    private long tokenRefreshThreshold = 300000;

    private String apiKey;

    private int maxRetries = 3;

    private long retryDelay = 1000;

    private long webhookMaxAge = 120;

    private String defaultCurrency = "BOB";

    private String defaultCountry = "BO";

    private String defaultNetwork = "POLYGON";

    private String defaultBlockchainCurrency = "USDT";
}
