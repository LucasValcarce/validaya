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

    private String password;

    private String publicKey;

    private String webhookSecret;

    private int connectTimeout = 5000;

    private int readTimeout = 10000;

    private long tokenRefreshThreshold = 300000; // 5 minutes

    private int maxRetries = 3;

    private long retryDelay = 1000;

    private long webhookMaxAge = 300; // 5 minutes

    private String defaultCurrency = "BOB";

    private String defaultCountry = "BO";

    private String defaultNetwork = "POLYGON";

    private String defaultBlockchainCurrency = "USDT";
}
