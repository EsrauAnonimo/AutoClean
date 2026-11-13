package com.autoclean.security;

import com.autoclean.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de generar y validar tokens JWT firmados con HMAC-SHA256.
 */
@Service
public class JwtService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final byte[] secretKeyBytes;
    private final long expirationMs;
    private final ObjectMapper objectMapper;

    /**
     * Inicializa el servicio JWT con la clave y tiempo de expiración configurados.
     *
     * @param secret clave secreta de firma (propiedad {@code jwt.secret})
     * @param expirationMs duración de validez del token en milisegundos (propiedad {@code jwt.expiration})
     * @param objectMapper serializador JSON de Jackson
     */
    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs,
            ObjectMapper objectMapper) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("La propiedad jwt.secret no puede estar vacía");
        }
        if (expirationMs <= 0) {
            throw new IllegalStateException("La propiedad jwt.expiration debe ser mayor que cero");
        }
        this.secretKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationMs = expirationMs;
        this.objectMapper = objectMapper;
    }

    /**
     * Genera un token JWT firmado para el usuario autenticado.
     *
     * @param user entidad de usuario persistida
     * @return token JWT compacto en formato {@code header.payload.signature}
     */
    public String generateToken(User user) {
        if (user == null || user.getEmail() == null || user.getRole() == null) {
            throw new IllegalArgumentException("El usuario debe tener correo y rol para emitir un token");
        }

        Instant now = Instant.now();
        Instant expiresAt = now.plusMillis(expirationMs);

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", user.getEmail());
        payload.put("role", user.getRole().name());
        if (user.getId() != null) {
            payload.put("userId", user.getId().toString());
        }
        if (user.getCustomer() != null && user.getCustomer().getId() != null) {
            payload.put("customerId", user.getCustomer().getId().toString());
        }
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        try {
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = URL_ENCODER.encodeToString(sign(signingInput));
            return signingInput + "." + signature;
        } catch (Exception ex) {
            throw new IllegalStateException("Error al generar el token JWT", ex);
        }
    }

    /**
     * Extrae el correo (claim {@code sub}) de un token JWT válido y vigente.
     *
     * @param token token JWT compacto
     * @return correo electrónico contenido en {@code sub}
     */
    public String extractEmail(String token) {
        Map<String, Object> claims = parseAndVerifyClaims(token);
        Object subject = claims.get("sub");
        if (subject == null || subject.toString().isBlank()) {
            throw new IllegalArgumentException("El token JWT no contiene sujeto válido");
        }
        return subject.toString();
    }

    /**
     * Valida la firma, vigencia y pertenencia del token al correo esperado.
     *
     * @param token token JWT compacto
     * @param expectedEmail correo esperado del usuario autenticado
     * @return {@code true} si el token es auténtico, no ha expirado y coincide con el correo
     */
    public boolean isTokenValid(String token, String expectedEmail) {
        if (expectedEmail == null || expectedEmail.isBlank()) {
            return false;
        }
        try {
            String subject = extractEmail(token);
            return subject.equalsIgnoreCase(expectedEmail.trim());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Valida la firma y vigencia del token sin comparar contra un usuario específico.
     *
     * @param token token JWT compacto
     * @return {@code true} si el token es auténtico y vigente
     */
    public boolean isTokenValid(String token) {
        try {
            parseAndVerifyClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private Map<String, Object> parseAndVerifyClaims(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token JWT no puede estar vacío");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Formato de token JWT inválido");
        }

        try {
            Map<String, Object> header = objectMapper.readValue(URL_DECODER.decode(parts[0]), MAP_TYPE);
            if (!"HS256".equals(header.get("alg"))) {
                throw new IllegalArgumentException("Algoritmo JWT no soportado");
            }

            String signingInput = parts[0] + "." + parts[1];
            byte[] expectedSig = sign(signingInput);
            byte[] actualSig = URL_DECODER.decode(parts[2]);
            // Comparación en tiempo constante para evitar ataques de temporización
            if (!MessageDigest.isEqual(expectedSig, actualSig)) {
                throw new IllegalArgumentException("Firma JWT inválida");
            }

            Map<String, Object> claims = objectMapper.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            Object expObj = claims.get("exp");
            if (!(expObj instanceof Number expNumber)) {
                throw new IllegalArgumentException("El token JWT no contiene fecha de expiración");
            }
            if (Instant.now().getEpochSecond() >= expNumber.longValue()) {
                throw new IllegalArgumentException("El token JWT ha expirado");
            }
            return claims;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Token JWT inválido o malformado", ex);
        }
    }

    private String encodeJson(Map<String, Object> data) throws Exception {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(data);
        return URL_ENCODER.encodeToString(jsonBytes);
    }

    private byte[] sign(String data) throws GeneralSecurityException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(new SecretKeySpec(secretKeyBytes, HMAC_SHA256));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }
}

