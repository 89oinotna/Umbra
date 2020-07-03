package com.oinotna.umbra;

import android.content.SharedPreferences;

import androidx.lifecycle.ViewModel;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SecretKeyViewModel extends ViewModel {

    private SecretKey key;

    public SecretKey getAESKey(){
       return key;
    }

    public void setAESKey(SecretKey key){
        this.key=key;
    }

    public String encrypt(byte[] encoded) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] ciphertext = cipher.doFinal(encoded);
            //byte[] iv = cipher.getIV();
            return new String(ciphertext);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
