package com.github.zoommaxdecentralnetwork.decentralmessenger;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ru.zoommax.hul.HexUtils;

public class Crypto {
    public static String encryptAES(String data, String userKey){
        try {
            SecretKey secKey = new SecretKeySpec(HexUtils.fromString(userKey), 0, HexUtils.fromString(userKey).length, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] byteCipherText = aesCipher.doFinal(data.getBytes());
            return HexUtils.toString(byteCipherText);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decryptAES(String data, String userKey){
        try {
            SecretKey originalKey = new SecretKeySpec(HexUtils.fromString(userKey), 0, HexUtils.fromString(userKey).length, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, originalKey);
            byte[] bytePlainText = aesCipher.doFinal(HexUtils.fromString(data));
            return new String(bytePlainText, StandardCharsets.UTF_8);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

}
