package com.validaya.validaya.crypto;

import lombok.Getter;

@Getter
public enum CipherSuite {
    AESGCMNoPadding256("AES/GCM/NoPadding", 256, 32, 12, 128),
    RSA("RSA", 256, 32, 12, 128),
    AESCBCNoPadding256("AES/CBC/NoPadding", 256, 32, 0, 0);

    private final String algorithm;
    private final int keySize;
    private final int keySizeByte;
    private final int ivSize;
    private final int tagLength;

    CipherSuite(String algorithm, int keySize, int keySizeByte, int ivSize, int tagLength) {
        this.algorithm = algorithm;
        this.keySize = keySize;
        this.keySizeByte = keySizeByte;
        this.ivSize = ivSize;
        this.tagLength = tagLength;
    }
}