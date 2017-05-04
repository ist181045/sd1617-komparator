package org.komparator.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoUtil {
    private static final String RSA_PKCS1_TRANSFORMATION =
            "RSA/ECB/PKCS1Padding";

    public static byte[] asymCipher(Key key, byte[] plainBytes) {
        return doRSACipher(Cipher.ENCRYPT_MODE, key, plainBytes);
    }

    public static byte[] asymDecipher(Key key, byte[] encryptedBytes) {
        return doRSACipher(Cipher.DECRYPT_MODE, key, encryptedBytes);
    }

    private static byte[] doRSACipher(int opMode, Key key, byte[] bytes) {
        return doCipherOperation(opMode, RSA_PKCS1_TRANSFORMATION, key, bytes);
    }

    private static byte[] doCipherOperation(int opMode, String transformation,
            Key key, byte[] bytes) {
        try {
            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(opMode, key);

            return cipher.doFinal(bytes);
        } catch (InvalidKeyException | IllegalBlockSizeException |
                NoSuchAlgorithmException | BadPaddingException |
                NoSuchPaddingException e) {
            StringBuilder esb = new StringBuilder("Error ");

            switch (opMode) {
                case Cipher.DECRYPT_MODE:
                    esb.append("decrypting ");
                    break;
                case Cipher.ENCRYPT_MODE:
                    esb.append("encrypting ");
                    break;
            }
            esb.append(
                    String.format("bytes, caught %s: %s%n", e, e.getMessage()));

            System.err.println(esb);
        }

        return null;
    }
}
