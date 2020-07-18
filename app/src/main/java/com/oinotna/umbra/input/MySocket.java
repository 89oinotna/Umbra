package com.oinotna.umbra.input;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.mouse.Mouse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MySocket implements Runnable {
    //todo header object

    private ServerPc pc;

    private SecretKey k;

    private MutableLiveData<Byte> mConnection;
    private ExecutorService executor;
    private InetAddress ipAddress;

    private int portPc=4513; //UDP mouse
    private int portConnectionPc=4512; //TCP connection

    //connection request
    public static byte CONNECTION=0x04;
    public static byte CONNECTION_PASSWORD=0x05;

    //Connection status
    public static byte CONNECTED=0x01;
    public static byte DISCONNECTED=0x02;
    public static byte CONNECTED_PASSWORD=0x03;

    //response
    public static byte CONNECTION_ERROR=0x04;
    public static byte REQUIRE_PASSWORD=0x05;
    public static byte WRONG_PASSWORD=0x06;

    private DatagramSocket socketUdp;
    private Socket socketTcp;

    private Thread mThread;

    public boolean connect(ServerPc pc, MutableLiveData<Byte> mConnection) {
        //Se mi voglio connettere allo stesso
        if(mConnection.getValue()!=null && this.pc!=null &&
                (mConnection.getValue() == MySocket.CONNECTED_PASSWORD || mConnection.getValue() == MySocket.CONNECTED)
                && this.pc.getName().equals(pc.getName())){
            mConnection.postValue(mConnection.getValue());
            return false;
        }

        this.pc=pc;
        this.k=null;
        this.mConnection=mConnection;

        if(mThread!=null) {
            disconnect();
        }

        mThread = new Thread(this);
        mThread.start();

        return true;
    }


    public ServerPc getPc() {
        return  pc;
    }



    private class Command{
        private byte[] command;

        /**
         * Principale per invio comandi (Mouse)
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
                command[1]= Mouse.NULL;
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

    public MySocket(/*ServerPc pc, MutableLiveData<Byte> mConnection*/) throws IOException {
        //this.mConnection=mConnection;
        //this.pc=pc;
        this.socketUdp=new DatagramSocket();
        this.executor= Executors.newSingleThreadExecutor();
    }

    /**
     * Sends a connection request
     * Initialize secret key if connected with password
     */
    public void usePassword(ServerPc pc) {
        executor.execute(() -> {
            OutputStream writer = null;
            try{
                writer=socketTcp.getOutputStream();
                if(pc.getPassword()!=null){
                    this.k= decodeKeyFromBase64(pc.getPassword());
                }
                //genero il comando per la connessione
                byte[] s = new Command(pc.getPassword(), k).command;
                writer.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Effettua la disconnessione
     */
    public void disconnect() {
        if (mConnection.getValue() != null) {
            mThread.interrupt();
            mThread=null;
            executor.shutdownNow();
            socketUdp.close();
            try {
                socketTcp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Genera secretKey dai byte della key in base64
     * @param password
     * @return
     */
    private SecretKey decodeKeyFromBase64(String password){
        byte[] decoded;
        // get base64 encoded version of the key
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decoded = Base64.getDecoder().decode(password);
        }
        else{
            decoded = android.util.Base64.decode(password, android.util.Base64.DEFAULT);
        }
        return new SecretKeySpec(decoded, "AES");
    }

    /**
     * Comandi (tasti)
     * @param action
     */
    public void push(byte action){
        push(action, null);
    }

    /**
     * Comandi con coordinate (pad, sensore, wheel)
     * @param action
     * @param coord
     */
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
                    socketUdp.send(dp);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Prova la connessione e poi entra in un ciclo bloccante sulla read
     * sul canale utilizzato per i comandi di connessione
     */
    @Override
    public void run() {
        //todo verify ip

        byte[] rb=new byte[1024];
        try {
            ipAddress= Inet4Address.getByName(pc.getIp());
            socketTcp=new Socket(ipAddress, portConnectionPc);
            InputStream reader =socketTcp.getInputStream();
            OutputStream writer = socketTcp.getOutputStream();

            if(pc.getPassword()!=null){
                this.k= decodeKeyFromBase64(pc.getPassword());
            }
            //genero il comando per la connessione
            byte[] s = new Command(pc.getPassword(), k).command;
            writer.write(s);
            int readBytes;
            while(!Thread.interrupted() && (readBytes=reader.read(rb)) > -1) {
                //socketUdp.receive(dp);
                //r = dp.getData();
                Log.d("Received", new String(rb));
                if (rb[0] == CONNECTED) {
                    mConnection.postValue(CONNECTED);
                } else if (rb[0] == REQUIRE_PASSWORD) {
                    mConnection.postValue(REQUIRE_PASSWORD);
                } else if (rb[0] == WRONG_PASSWORD) {
                    mConnection.postValue(WRONG_PASSWORD);
                } else if (rb[0] == CONNECTED_PASSWORD) {
                    mConnection.postValue(CONNECTED_PASSWORD);
                }else if (rb[0] == CONNECTION_ERROR) {
                    mConnection.postValue(CONNECTION_ERROR);
                }
            }
            //se mi disconnetto lato server read restituisce -1 e vado qua
            mConnection.postValue(DISCONNECTED);
        } catch (IOException e) {
            //se mi disconnetto lato app vado qua perchè chiudo il socket
            e.printStackTrace();
            mConnection.postValue(DISCONNECTED);
        }


    }


}
