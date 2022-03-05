package com.github.zoommaxdecentralnetwork.decentralmessenger;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import ru.zoommax.hul.HexUtils;

public class BlockCrypt {
        static String privkey, data;
        static int blockSize = 100;
        public BlockCrypt(String privkey, String data){
            this.privkey = privkey;
            this.data = data;
        }

        public String encrypt(){
            byte[] d = HexUtils.fromString(data);
            byte[][] dat = blocksClear(d);
            int lastBlock = d.length%blockSize;
            String result = "";
            for (int i = 0; i < dat.length; i++){
                result += encryptRSA(dat[i]);
            }
            return result+":"+lastBlock+":";
        }

        public String decrypt(int lastBlockSize){
            //lastBlockSize *= 2;
            byte[] d = HexUtils.fromString(data);
            byte[][] dat = blocksEncrypt(d);
            String result = "";
            for (int i = 0; i < dat.length; i++){
                if ((i) >= dat.length-1){
                    byte[] tmp = new byte[lastBlockSize];
                    byte[] tmp1 = HexUtils.fromString(decryptRSA(dat[i]));
                    int y = tmp1.length - 100;
                    for(int t = 0; t<lastBlockSize; t++){
                        tmp[t] = tmp1[y+t];
                    }
                    result += HexUtils.toString(tmp);
                }else {
                    byte[] tmp = new byte[100];
                    byte[] tmp1 = HexUtils.fromString(decryptRSA(dat[i]));
                    int y = tmp1.length - 100;
                    for(int t = 0; t<100; t++){
                        tmp[t] = tmp1[y+t];
                    }
                    result += HexUtils.toString(tmp);
                }
            }
            return result;
        }

        public static String toString(String hex){
            return new String(HexUtils.fromString(hex), StandardCharsets.UTF_8);
        }

        private static byte[][] blocksEncrypt(byte[] d){
            int blockCount = d.length/128;
            if (blockCount < 1){
                blockCount = 1;
            }
            byte[][] blockss = new byte[blockCount][128];
            for(int i = 0; i<blockCount; i++){
                for(int j = 0; j<128; j++){
                    blockss[i][j] = 0x00;
                }
            }
            int x = 0;
            int y = 0;
            for(byte b : d){
                blockss[y][x] = b;
                x++;
                if (x >= 128){
                    x = 0;
                    y++;
                }
            }
            return blockss;
        }

        private static byte[][] blocksClear(byte[] d){
            int blockCount = d.length/blockSize;
            if (d.length%blockSize != 0){
                blockCount += 1;
            }
            byte[][] blockss = new byte[blockCount][blockSize];
            for(int i = 0; i<blockCount; i++){
                for(int j = 0; j<blockSize; j++){
                    blockss[i][j] = 0x00;
                }
            }
            int x = 0;
            int y = 0;
            for(byte b : d){
                blockss[y][x] = b;
                x++;
                if (x >= blockSize){
                    x = 0;
                    y++;
                }
            }
            return blockss;
        }

        private static String encryptRSA(byte[] b){
            try {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                PublicKey privateKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(HexUtils.fromString(privkey)));
                //PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(HexUtils.fromString(privkey)));
                cipher.init(Cipher.ENCRYPT_MODE, privateKey);
                String ret = HexUtils.toString(cipher.doFinal(b));
                return ret;
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return null;
            }
        }

        private static String decryptRSA(byte[] b){
            try{
                Cipher cipher = Cipher.getInstance("RSA");
                PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(HexUtils.fromString(privkey)));
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                String ret = HexUtils.toString(cipher.doFinal(b));
                return ret;
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                return null;
            }
        }
}
