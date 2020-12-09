package com.github.cache.crypto;

import com.github.cache.utils.EncryptionUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public abstract class AESEncryptionService implements EncryptionService {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    //    private final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private final String algorithm;

    private final int keyBitLength; // must be one of {128, 120, 112, 104, 96}
    private final int ivByteLength;
    private final int saltByteLength;

    private final String password;

    public AESEncryptionService(String algorithm,
                                int keyBitLength,
                                int ivByteLength,
                                int saltByteLength,
                                String password) {
        this.algorithm = algorithm;
        this.keyBitLength = keyBitLength;
        this.ivByteLength = ivByteLength;
        this.saltByteLength = saltByteLength;
        this.password = password;
    }

    // return a base64 encoded AES encrypted text
    public byte[] encrypt(byte[] pText) throws Exception {
        byte[] salt = EncryptionUtils.getRandomNonce(saltByteLength);

        // GCM recommended 12 bytes iv?
        byte[] iv = EncryptionUtils.getRandomNonce(ivByteLength);

        // secret key from password
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt, keyBitLength);

        Cipher cipher = Cipher.getInstance(algorithm);

        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(keyBitLength, iv));

        byte[] cipherText = cipher.doFinal(pText);

        // prefix IV and Salt to cipher text
        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherText.length)
                .put(iv)
                .put(salt)
                .put(cipherText)
                .array();

        // string representation, base64, send this string to other for decryption.
        return Base64.getEncoder().encode(cipherTextWithIvSalt);
    }

    // we need the same password, salt and iv to decrypt it
    public byte[] decrypt(byte[] cText) throws Exception {

        byte[] decode = Base64.getDecoder().decode(cText);

        // get back the iv and salt from the cipher text
        ByteBuffer bb = ByteBuffer.wrap(decode);

        byte[] iv = new byte[ivByteLength];
        bb.get(iv);

        byte[] salt = new byte[saltByteLength];
        bb.get(salt);

        byte[] cipherText = new byte[bb.remaining()];
        bb.get(cipherText);

        // get back the aes key from the same password and salt
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt, keyBitLength);

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(keyBitLength, iv));

        return cipher.doFinal(cipherText);

    }

    // Password derived AES 256 bits secret key
    private SecretKey getAESKeyFromPassword(char[] password, byte[] salt, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        KeySpec spec = new PBEKeySpec(password, salt, 65536, keyLength);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
}
