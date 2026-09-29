package ru.sber.transport.integrations.encryption;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;

/**
 * Компонент шифрования пароля.
 */
@Component
public class PasswordEncryption {

    private static final List<String> KEY_PARTS = List.of("mZvxZ", "uRSkE7ydYy", "BxavriRwX", "W7uVfNZf");

    private static final byte[] KEY = String.join("", KEY_PARTS).getBytes(StandardCharsets.US_ASCII);

    private static final byte[] VECTOR = {8, 7, 5, 6, 4, 1, 2, 3, 18, 17, 15, 16, 14, 11, 12, 13};

    private static final String ALGO = "AES";

    /**
     * Зашифровать.
     *
     * @param password пароль.
     * @return зашифрованный пароль.
     */
    @SneakyThrows({IllegalBlockSizeException.class, BadPaddingException.class})
    public String encode(String password) {
        var cipher = initCipher(Cipher.ENCRYPT_MODE);
        var encoded = Base64.getEncoder().encode(cipher.doFinal((password).getBytes(StandardCharsets.UTF_8)));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    /**
     * Расшифровать.
     *
     * @param ciphered зашифрованный пароль,
     * @return пароль.
     */
    @SuppressWarnings("java:S3958")
    @SneakyThrows({IllegalBlockSizeException.class, BadPaddingException.class})
    public String decode(String ciphered) {
        var decoded = Base64.getDecoder().decode(ciphered);
        var cipher = initCipher(Cipher.DECRYPT_MODE);
        return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
    }

    @SneakyThrows({NoSuchAlgorithmException.class, NoSuchPaddingException.class, InvalidKeyException.class, InvalidAlgorithmParameterException.class})
    private Cipher initCipher(int encryptMode) {
        var key = new SecretKeySpec(KEY, ALGO);

        var cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5Padding");
        cipher.init(encryptMode, key, new IvParameterSpec(VECTOR)); // NOSONAR

        return cipher;
    }

}
