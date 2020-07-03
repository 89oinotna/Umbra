package com.oinotna.umbra.mouse;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.thread.RunnableWithSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MouseSocket implements RunnableWithSocket {
    //todo header object

    private ServerPc pc;

    private SecretKey k;

    private MutableLiveData<Byte> mConnection;
    private Executor executor;
    private InetAddress ipAddress;

    private int portPc=4511;
    private int portApp=4512;

    //connection request
    public static byte CONNECTION=0x04;
    public static byte CONNECTION_PASSWORD=0x05;
    public static byte DISCONNECT=0x06;
    public static byte DISCONNECT_PASSWORD=0x07;

    //Connection status
    public static byte CONNECTED=0x01;
    public static byte DISCONNECTED=0x02;
    public static byte CONNECTED_PASSWORD=0x03;

    //response
    public static byte CONNECTION_ERROR=0x04;
    public static byte REQUIRE_PASSWORD=0x05;
    public static byte WRONG_PASSWORD=0x06;


    private DatagramSocket socket;

    public DatagramSocket getSocket() {
        return socket;
    }

    public ServerPc getPc() {
        return  pc;
    }


    private class Command{
        private byte[] command;

        /**
         * Principale per invio comandi
         * @param connectionType
         * @param action
         * @param coord
         */
        public Command(byte connectionType, byte action, float[] coord){
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
        }

        /**
         * Per la connessione
         * Metto la password in base64 e la cripto
         * @param k può essere null se non uso la password
         */
        public Command(String password, SecretKey k){
            if(k!=null){
                String c=":"+password;
                byte[] cb=c.getBytes();
                command=new byte[2 + cb.length];
                command[0]=CONNECTION_PASSWORD;
                command[1]=Mouse.NULL;
                System.arraycopy(cb, 0, command, 2, cb.length);
                encrypt(k);
            }
            else{
                command=new byte[]{CONNECTION, Mouse.NULL};
            }
        }

        /**
         * Encrypt with key
         * @param k
         */
        public void encrypt(SecretKey k){
            //todo aggiungere timestamp per evitare replay
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
                //todo store iv safely in sharedprefs instead of hardcoded
                IvParameterSpec ivSpec = new IvParameterSpec("1111111111111111".getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, k, ivSpec);
                //todo criptare da [3] byte in poi
                byte[] tmp=new byte[command.length-1];
                System.arraycopy(command, 1, tmp, 0, tmp.length);
                tmp = cipher.doFinal(tmp);
                byte[] finalCom= new byte[1+tmp.length];
                System.arraycopy(command, 0, finalCom, 0, 1);
                System.arraycopy(tmp, 0, finalCom, 1, tmp.length);
                command=finalCom;
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        //TODO password
        if (mConnection.getValue() == null) {
            return;
        }
        else if (mConnection.getValue() == CONNECTED
                || mConnection.getValue() == CONNECTED_PASSWORD) {
            executor.execute(() -> {
                try {
                    byte[] s = new byte[]{MouseSocket.DISCONNECT};
                    DatagramPacket dp = new DatagramPacket(s, s.length, ipAddress, portPc);
                    socket.send(dp);
                } catch (IOException e) {
                    e.printStackTrace();
                    mConnection.postValue(CONNECTION_ERROR);
                }
                socket.close();
            });
        /*}else if(mConnection.getValue()==CONNECTED){
            push(DISCONNECT_PASSWORD);
        }*/
        }
        else{
            socket.close();
        }
    }

    public MouseSocket(ServerPc pc, MutableLiveData<Byte> mConnection) throws SocketException {
        this.mConnection=mConnection;
        this.pc=pc;
        this.socket=new DatagramSocket(portApp);
        this.executor= Executors.newCachedThreadPool();
    }

    /**
     * Invia una richiesta di connessione
     * Istanzia la secret key se mi connetto con password
     */
    public void tryConnection() {
        executor.execute(() -> {
            try {
                //se è la prima connessione risolvo l'indirizzo
                if(ipAddress==null){
                    ipAddress= Inet4Address.getByName(pc.getIp());
                }
                if(pc.getPassword()!=null){
                    this.k=generateKeyFromBase64(pc.getPassword());
                }
                //genero il comando per la connessione
                byte[] s = new Command(pc.getPassword(), k).command;
                DatagramPacket dp = new DatagramPacket(s, s.length, ipAddress, portPc);
                socket.send(dp);
            } catch (IOException e) {
                e.printStackTrace();
                mConnection.postValue(CONNECTION_ERROR);
            }
        });
    }

    /**
     * Genera secretKey dai byte della key in base64
     * @param password
     * @return
     */
    private SecretKey generateKeyFromBase64(String password){
        byte[] decoded;
        // get base64 encoded version of the key
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decoded = Base64.getDecoder().decode(password);
            //todo use sha
        }
        else{
            decoded = android.util.Base64.decode(password, android.util.Base64.DEFAULT);
        }
        return new SecretKeySpec(decoded, "AES");
    }

    public void push(byte action){
        push(action, null);
    }

    //todo usare queue????
    public void push(byte action, float[] coord){
        executor.execute(()->{
            try {
                if(ipAddress!=null) {
                    Command cmd=new Command(mConnection.getValue(), action, coord);
                    if(mConnection.getValue()==CONNECTED_PASSWORD){
                        cmd.encrypt(k);
                    }
                    byte[] s = cmd.command;
                    DatagramPacket dp = new DatagramPacket(s, s.length, ipAddress, portPc);
                    socket.send(dp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        //todo verify ip
        byte[] r=new byte[1024];
        try {
            DatagramPacket dp=new DatagramPacket(r, r.length);
            while(!Thread.interrupted()) {
                socket.receive(dp);
                r = dp.getData();
                Log.d("Received", new String(r));
                if (r[0] == CONNECTED) {
                    mConnection.postValue(CONNECTED);
                } else if (r[0] == REQUIRE_PASSWORD) {
                    mConnection.postValue(REQUIRE_PASSWORD);
                } else if (r[0] == WRONG_PASSWORD) {
                    mConnection.postValue(WRONG_PASSWORD);
                } else if (r[0] == CONNECTED_PASSWORD) {
                    mConnection.postValue(CONNECTED_PASSWORD);
                }else if (r[0] == CONNECTION_ERROR) {
                    mConnection.postValue(CONNECTION_ERROR);
                }
            }
        } catch (IOException e) {
            //mConnection.postValue(DISCONNECTED);
            e.printStackTrace();
        }
    }


}
