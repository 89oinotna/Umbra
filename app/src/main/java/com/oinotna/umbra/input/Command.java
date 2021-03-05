package com.oinotna.umbra.input;

import com.oinotna.umbra.input.mouse.Mouse;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public abstract class Command {
    //todo connection type
    private final byte type;

    //todo command abstract con getEncryptedBytes e poi implementazioni per ogni componente (mouse ecc)
    //private byte[] command;

    /**
     * Principale per invio comandi (Mouse)
     * @param connectionType
     * @param action
     * @param coord
     */
    /*public Command(byte connectionType, byte action, float[] coord){
        if(coord!=null){
            String c=":"+(int)coord[0]+","+(int)coord[1];
            byte[] cb=c.getBytes();
            command=new byte[2 + cb.length];
            command[0]=connectionType;
            command[1]=action;
            System.arraycopy(cb, 0, command, 2, cb.length);
        }
        else{
            command=new byte[]{connectionType, action};
        }
    }*/

    public Command(byte type){
        this.type=type;
    }

    public abstract byte[] getCommandBytes();

    public byte getType(){
        return this.type;
    }

    /**
     * Per la connessione
     * Metto la password in base64 e la cripto
     * @param k può essere null se non uso la password
     */
    /*public Command(String password, SecretKey k){
        if(k!=null){
            String c=":"+password;
            byte[] cb=c.getBytes();
            command=new byte[2 + cb.length];
            command[0]=CONNECTION_PASSWORD;
            command[1]= Mouse.NULL;
            System.arraycopy(cb, 0, command, 2, cb.length);
            getEncryptedBytes(k);
        }
        else{
            command=new byte[]{CONNECTION, Mouse.NULL};
        }
    }*/

    /**
     * Encrypt with key
     * @param k
     */
    public byte[] getEncryptedBytes(SecretKey k){
        //todo aggiungere timestamp per evitare replay
        byte[] command=getCommandBytes();
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            //todo store iv safely in sharedprefs instead of hardcoded
            IvParameterSpec ivSpec = new IvParameterSpec("1111111111111111".getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, k, ivSpec);
            //byte[] tmp=new byte[command.length-1];
            //System.arraycopy(command, 1, tmp, 0, tmp.length);
            //tmp = cipher.doFinal(tmp);
            command=cipher.doFinal(command);
            //byte[] finalCom= new byte[1+tmp.length];
            //connection type is not encrypted
            //finalCom[0]=command[0];
            //System.arraycopy(command, 0, finalCom, 0, 1);
            //System.arraycopy(tmp, 0, finalCom, 1, tmp.length);
            return command;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return new byte[1];
    }
}
