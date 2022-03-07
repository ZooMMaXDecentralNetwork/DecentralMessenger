package com.github.zoommaxdecentralnetwork.decentralmessenger;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ru.zoommax.hul.HexUtils;

public class Crypto {

    public static byte[] encryptData(PublicKey publicKey, byte[] data){
        try {
            Cipher iesCipher = Cipher.getInstance("ECIES", BouncyCastleProvider.PROVIDER_NAME);
            iesCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return iesCipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptData(PrivateKey privateKey, byte[] data){
        try {
            Cipher iesCipher = Cipher.getInstance("ECIES", BouncyCastleProvider.PROVIDER_NAME);
            iesCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return iesCipher.doFinal(data);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HashMap<String, String> getKeys(){
        HashMap<String, String> result = new HashMap<>();
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            kpg.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();
            result.put("pubkey", HexUtils.toString(keyPair.getPublic().getEncoded()));
            result.put("privkey", HexUtils.toString(keyPair.getPrivate().getEncoded()));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            result.put("error", e.toString());
        }
        return result;
    }

    public static PublicKey publicKey(String keyHex){
        EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(HexUtils.fromString(keyHex));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            PublicKey publicKey = keyFactory.generatePublic(encodedKeySpec);
            return publicKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey privateKey(String keyHex){
        EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(HexUtils.fromString(keyHex));
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME);
            PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
            return privateKey;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            e.printStackTrace();
            return null;
        }
    }

}
