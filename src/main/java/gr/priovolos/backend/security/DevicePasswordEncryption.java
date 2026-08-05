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

/**
 * Service responsible for encrypting and decrypting SSH passwords
 * stored for managed network devices.
 *
 * <p>The service uses the AES algorithm operating in Galois/Counter
 * Mode (AES-256-GCM), which provides both confidentiality and
 * integrity protection for encrypted data.</p>
 *
 * <p>Each encryption operation generates a new cryptographically
 * secure random Initialization Vector (IV). The IV is prefixed to
 * the encrypted data and stored together with the ciphertext,
 * allowing successful decryption without storing the IV separately.</p>
 *
 * <p>The encryption key is supplied through the application
 * configuration and must be a 256-bit hexadecimal value.</p>
 *
 * <p>This service is intended exclusively for protecting device SSH
 * credentials. User passwords are <strong>not</strong> encrypted
 * using this service; instead, they are securely one-way hashed
 * using BCrypt as required for user authentication.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
@Slf4j
public class DevicePasswordEncryption {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;

    private final SecureRandom secureRandom = new SecureRandom();

    private final SecretKey secretKey;

    /**
     * Creates a new encryption service using the configured
     * application encryption key.
     *
     * <p>The key must be provided as a hexadecimal string
     * representing exactly 32 bytes (256 bits).</p>
     *
     * @param hexKey the hexadecimal AES-256 encryption key
     * @throws IllegalArgumentException if the supplied key does not
     *                                  contain exactly 32 bytes
     */
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

    /**
     * Logs successful initialization of the encryption service.
     */
    @PostConstruct
    void initialized() {
        log.info("Device password encryption initialized.");
    }

    /**
     * Encrypts a plaintext device password.
     *
     * <p>A new random Initialization Vector (IV) is generated for
     * every encryption operation. The IV is prepended to the
     * ciphertext and the combined byte array is Base64 encoded for
     * storage.</p>
     *
     * @param plainText the plaintext password to encrypt
     * @return the Base64-encoded encrypted password
     * @throws IllegalStateException if encryption fails
     */
    public String encrypt(String plainText) {

        try {

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to encrypt device password", e);
        }
    }

    /**
     * Decrypts an encrypted device password.
     *
     * <p>The method extracts the Initialization Vector (IV) from the
     * beginning of the Base64-decoded data before performing AES-GCM
     * decryption.</p>
     *
     * @param encryptedText the Base64-encoded encrypted password
     * @return the decrypted plaintext password
     * @throws IllegalStateException if decryption fails or the
     *                               encrypted data has been altered
     */
    public String decrypt(String encryptedText) {

        try {

            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] iv = Arrays.copyOfRange(decoded, 0, IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(decoded, IV_LENGTH, decoded.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(decrypted, java.nio.charset.StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to decrypt device password", e);
        }
    }
}