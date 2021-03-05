package com.oinotna.umbra.input;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.crypto.SecretKey;
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
    private OnDisconnectListener mListener;

    private static MySocket instance;

    private class ConnectionCommand extends Command{
        private String password;

        public ConnectionCommand(String password){
            super((byte) 0x0);
            this.password=password;
        }

        public ConnectionCommand(){
            super((byte) 0x0);
        }
        @Override
        public byte[] getCommandBytes() {
            ByteBuffer command;
            if(password!=null) {
                String c = ":" + password;
                command = ByteBuffer.allocate(1 + c.length());
                command.put((byte) 0x0);
                command.put(c.getBytes(), 1, c.length());
                return command.array();
            }
            else{
                return new byte[]{0x0};
            }
            //byte[] cb=c.getBytes();
            /*command=new byte[2 + cb.length];
            command[0]=CONNECTION_PASSWORD;
            command[1]= Mouse.NULL;*/
            //System.arraycopy(cb, 0, command, 2, cb.length);
            //getEncryptedBytes(k);

        }
    }


    /**
     * Returns the instance
     * @return can be null
     */
    public static MySocket getInstance() {
        return instance;
    }

    /**
     * Returns the current LiveData used to notify connection changes
     * @return
     */
    public MutableLiveData<Byte> getConnection() {
        if(mConnection==null)
            mConnection= new MutableLiveData<>();
        return mConnection;
    }

    public interface OnDisconnectListener{
        void onDisconnect();
    }

    /**
     * Used to add listener that will be called on disconnection
     * @param listener
     */
    public void setOnDisconnectListener(OnDisconnectListener listener) {
        mListener=listener;
    }




    public MySocket(ServerPc pc) {
        this.pc=pc;
        executor= Executors.newSingleThreadExecutor();
        mThread = new Thread(this);
        mThread.start();
    }

    /**
     * @return true if connection is CONNECTED || CONNECTED_PASSWORD
     *          false otherwise
     */
    public static boolean isConnected() {
        if(instance==null) return false;
        LiveData<Byte> mConnection=instance.getConnection();
        return mConnection.getValue() != null &&
                (mConnection.getValue() == MySocket.CONNECTED || mConnection.getValue() == MySocket.CONNECTED_PASSWORD);
    }

    public ServerPc getPc() {
        return  pc;
    }

    /**
     * Connect to a server
     * If already connected disconnects from previous server
     * @param pc ServerPc to connect to
     * @return LiveData used to notify connection changes for this instance
     */
    public static LiveData<Byte> connect(ServerPc pc) {
        if(instance!=null)
            instance.disconnect();

        instance=new MySocket(pc);

        return instance.getConnection();
    }

    /**
     * Sends a connection request
     * Initialize secret key if connected with password
     */
    public void usePassword(String password) {
        pc.setPassword(password);
        executor.execute(() -> {
            OutputStream writer = null;
            try{
                writer=socketTcp.getOutputStream();
                if(pc.getPassword()!=null){
                    this.k= decodeKeyFromBase64(pc.getPassword());
                }
                //genero il comando per la connessione
                byte[] s = new ConnectionCommand(pc.getPassword()).getEncryptedBytes(k);
                writer.write(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Disconnection
     */
    public void disconnect() {
        if (mConnection.getValue() != null) {
            if(mThread!=null) {
                mThread.interrupt();
                mThread=null;
            }
            executor.shutdownNow();
            if(socketUdp!=null)
                socketUdp.close();
            try {
                if(socketTcp!=null)
                    socketTcp.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates the SecretKey from the key stored in base64
     * @param password
     * @return
     */
    private SecretKey decodeKeyFromBase64(String password){
        //todo spostare da qualche altra parte
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
     * Send static action (button press)
     * @param action
     */
    /*public void push(byte type, int action){
        push(action, null);
    }*/

    /**
     * Send movement action (pad, sensor, wheel)
     * @param action
     * @param coord
     */
    /*public void push(byte type, int action, float[] coord){
        try {
            executor.execute(() -> {
                try {
                    if (ipAddress != null) {
                        Command cmd = new Command(mConnection.getValue(), action, coord);
                        if (mConnection.getValue() == CONNECTED_PASSWORD) {
                            cmd.getEncryptedBytes(k);
                        }
                        byte[] s = cmd.command;
                        DatagramPacket dp = new DatagramPacket(s, s.length, ipAddress, portPc);
                        socketUdp.send(dp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }catch (RejectedExecutionException e){
            e.printStackTrace();
        }
    }*/

    public void push(Command cmd){
        try {
            executor.execute(() -> {
                try {
                    if (ipAddress != null) {
                        //todo add connection type
                        //Command cmd = new Command(mConnection.getValue(), action, coord);
                        byte[] s;
                        if (mConnection.getValue() == CONNECTED_PASSWORD) {
                            s=cmd.getEncryptedBytes(k);
                        }
                        else{
                            s=cmd.getCommandBytes();
                        }
                        s=prefixWithConnection(s);
                        DatagramPacket dp = new DatagramPacket(s, s.length, ipAddress, portPc);
                        socketUdp.send(dp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }catch (RejectedExecutionException e){
            e.printStackTrace();
        }
    }

    public byte[] prefixWithConnection(byte[] toPrefix){
        return prefixWith(toPrefix, mConnection.getValue());
    }

    public byte[] prefixWith(byte[] toPrefix, byte prefix){
        byte[] tmp=new byte[toPrefix.length+1];
        System.arraycopy(toPrefix, 0, tmp, 1, toPrefix.length);
        tmp[0]=prefix;
        return tmp;
    }


    /**
     * Prova la connessione e poi entra in un ciclo bloccante sulla read
     * sul canale utilizzato per i comandi di connessione
     */
    @Override
    public void run() {
        byte[] rb=new byte[1024];
        try {
            socketUdp=new DatagramSocket();
            ipAddress = Inet4Address.getByName(pc.getIp());
            socketTcp = new Socket(ipAddress, portConnectionPc);
            try{
                InputStream reader =socketTcp.getInputStream();
                OutputStream writer = socketTcp.getOutputStream();

                if(pc.getPassword()!=null){
                    this.k= decodeKeyFromBase64(pc.getPassword());
                }
                else{

                }
                //genero il comando per la connessione
                byte[] s;
                if(k!=null) {
                    s = new ConnectionCommand(pc.getPassword()).getEncryptedBytes(k);
                    s=prefixWith(s, CONNECTION_PASSWORD);
                }
                else {
                    s = new ConnectionCommand().getCommandBytes();
                    s=prefixWith(s, CONNECTION);
                }
                writer.write(s);
                int readBytes;
                while(!Thread.interrupted() && (readBytes=reader.read(rb)) > -1) {
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
                Log.d("DISCONNECT", "from server");
            } catch (IOException e) {
                //se mi disconnetto lato app vado qua perchè chiudo il socket
                e.printStackTrace();
                Log.d("DISCONNECT", "from app");
            }
            finally {
                disconnect();
                if (mListener != null) {
                    mListener.onDisconnect();
                    mListener = null;
                }
                mConnection.postValue(DISCONNECTED);
            }
        }catch (IOException e){
            e.printStackTrace();
            disconnect();
            mConnection.postValue(CONNECTION_ERROR);
        }



    }


}
