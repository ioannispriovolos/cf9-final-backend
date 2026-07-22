package gr.priovolos.backend.security;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

@Service
@Slf4j
public class DevicePasswordEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SecretKey secretKey;

    public DevicePasswordEncryption(
            @Value("${app.security.device-encryption-key}") String hexKey) {

        byte[] keyBytes = HexFormat.of().parseHex(hexKey);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Device encryption key must be exactly 32 bytes (256 bits)."
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @PostConstruct
    void initialized() {
        log.info("Device password encryption initialized.");
    }

    public String encrypt(String plainText) {

        try {

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] encrypted = cipher.doFinal(
                    plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, result, 0, iv.length);

            System.arraycopy(encrypted, 0,
                    result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to encrypt device password", e);
        }
    }

    public String decrypt(String encryptedText) {

        try {

            byte[] decoded = Base64.getDecoder().decode(encryptedText);

            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);

            byte[] cipherText = Arrays.copyOfRange(
                    decoded,
                    IV_LENGTH,
                    decoded.length
            );

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    new GCMParameterSpec(GCM_TAG_LENGTH, iv)
            );

            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(
                    decrypted,
                    java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to decrypt device password", e);
        }
    }
}