package com.validaya.validaya.crypto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Wrapper dedicado al cifrado/descifrado del vector facial de los usuarios.
 * El vector se serializa a JSON, se cifra con AES-256-GCM y se almacena como String.
 *
 * La entidad User guarda face_vector como List<Double> en memoria, pero
 * antes de persistir se debe cifrar usando este componente (vía un @Converter
 * o en la capa de servicio según el flujo elegido).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FaceVectorCrypto {

    private final CryptoAES cryptoAES;
    private final ObjectMapper objectMapper;

    /**
     * Serializa y cifra el vector facial para persistencia.
     *
     * @param vector lista de doubles (128 o 512 componentes)
     * @return String Base64 cifrado listo para guardar en BD
     */
    public String encryptVector(List<Double> vector) throws Exception {
        String json = objectMapper.writeValueAsString(vector);
        return cryptoAES.encrypt(json);
    }

    /**
     * Descifra y deserializa el vector facial almacenado en BD.
     *
     * @param encryptedBase64 valor almacenado en la columna face_vector
     * @return lista de doubles con el vector original
     */
    public List<Double> decryptVector(String encryptedBase64) throws Exception {
        String json = cryptoAES.decrypt(encryptedBase64);
        return objectMapper.readValue(json, new TypeReference<List<Double>>() {});
    }

    /**
     * Calcula la similitud coseno entre dos vectores faciales.
     * Devuelve un valor entre -1 y 1 (1 = idénticos).
     * Umbral sugerido para match: >= 0.80
     */
    public double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1.size() != v2.size()) {
            throw new IllegalArgumentException("Los vectores deben tener la misma dimensión");
        }
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot   += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
