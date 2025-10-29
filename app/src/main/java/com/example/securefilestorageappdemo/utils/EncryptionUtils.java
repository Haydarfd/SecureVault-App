package com.example.securefilestorageappdemo.utils;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    private static final byte[] SECRET_KEY = "1234567890123456".getBytes(); // 16-byte key

    public static byte[] encrypt(byte[] inputBytes) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION); // Now includes mode and padding
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inputBytes);
    }

    public static byte[] decrypt(byte[] encryptedBytes) throws Exception {
        Key key = new SecretKeySpec(SECRET_KEY, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION); // Now includes mode and padding
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedBytes);
    }
}
