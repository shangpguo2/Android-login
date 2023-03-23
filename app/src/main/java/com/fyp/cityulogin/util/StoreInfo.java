package com.fyp.cityulogin.util;

import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class StoreInfo {
    // The secret key used to encrypt the password
    private static final String SECRET_KEY = "getHighGrade";
    // The key bytes truncated to 128 bits (16 bytes)
    private static byte[] truncatedKeyBytes;
    // The SecretKeySpec object used to encrypt and decrypt the password
    private static SecretKeySpec secretKeySpec;
    // The Cipher object used to encrypt and decrypt the password
    private static Cipher cipher;

    public StoreInfo() {
        // Create a key using the secret key
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        // Use only the first 128 bits (16 bytes) of the key
        truncatedKeyBytes = new byte[16];
        System.arraycopy(keyBytes, 0, truncatedKeyBytes, 0, Math.min(keyBytes.length, truncatedKeyBytes.length));
        try {
            // Create a SecretKeySpec object using the key bytes
            secretKeySpec = new SecretKeySpec(truncatedKeyBytes, "AES");
            // Encrypt the password using AES
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    // Encrypt the info
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String encryptInfo(String info) {
        try {
            // Encrypt the password using the SecretKeySpec object
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedPassword = cipher.doFinal(info.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Retrieve the encrypted info
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String decryptInfo(String encryptedInfo) {
        try {
            // Decrypt the password using the SecretKeySpec object
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedByte = cipher.doFinal(Base64.getDecoder().decode(encryptedInfo));
            String decryptedInfo = new String(decryptedByte, StandardCharsets.UTF_8);
            return decryptedInfo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // The password is stored in the SharedPreferences as an encrypted string
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void storeInfo(String key, String userPassword, SharedPreferences preferences) {
        // Create an editor to modify the SharedPreferences
        SharedPreferences.Editor editor = preferences.edit();

        // Encrypt the password using a secure encryption algorithm, such as AES
        String encryptedPassword = encryptInfo(userPassword);

        // Add the encrypted password to the SharedPreferences
        editor.putString(key, encryptedPassword);

        // Commit the changes to the SharedPreferences
        editor.apply();
    }

    // Retrieve the encrypted information from the SharedPreferences
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String getInfo(String key, SharedPreferences preferences) {
        String encryptedInfo = "";
        try {
            // Retrieve the encrypted password from the SharedPreferences
            encryptedInfo = preferences.getString(key, "");
            // Decrypt the information using the same encryption algorithm used to encrypt it
            return decryptInfo(encryptedInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
