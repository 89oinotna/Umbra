package com.oinotna.umbra;

import androidx.lifecycle.ViewModel;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SecretKeyViewModel extends ViewModel {

    private SecretKey key;

    public SecretKey getAESKey(){
       return key;
    }

    public void setAESKey(SecretKey key){
        this.key=key;
    }

    /**
     * Encrypt password to get stored in db
     * @param encoded
     * @return Base64 encrypted password
     */
    public String encrypt(byte[] encoded) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            IvParameterSpec ivSpec = new IvParameterSpec("1111111111111111".getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ciphertext = cipher.doFinal(encoded);
            //byte[] iv = cipher.getIV();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                return new String(Base64.getEncoder().encode(ciphertext));
            }
            else{
                return new String(android.util.Base64.encode(ciphertext, android.util.Base64.DEFAULT));
            }

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decrypt stored password from db
     * @param encrypted
     * @return Base64 decrypted key
     */
    public String decrypt(byte[] encrypted) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            IvParameterSpec ivSpec = new IvParameterSpec("1111111111111111".getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] ciphertext = cipher.doFinal(encrypted);
            //byte[] iv = cipher.getIV();
            return new String(ciphertext);

        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }
}
