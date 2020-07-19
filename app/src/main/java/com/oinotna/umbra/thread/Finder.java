package com.oinotna.umbra.thread;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Finder implements RunnableWithSocket {
    private int portPc=4513;
    private int portFinder=4513;

    //private InetAddress broadcast;
    private DatagramSocket socket;

    private static byte BROADCAST=0x00;
    private static byte ALIVE=0x01;

    private List<ServerPc> serversList;
    private MutableLiveData<Boolean> serversLiveData;

    private Executor executor;

    private static Finder instance;
    private static MyInterruptThread mThread;

    public Finder() throws SocketException {
        socket=new DatagramSocket(portFinder);
        executor= Executors.newSingleThreadExecutor();
    }

    private static Finder getInstance(ArrayList<ServerPc> serversList, MutableLiveData<Boolean> serversLiveData) throws SocketException {
        if(instance==null){
            instance=new Finder();
        }
        instance.setServersList(serversList);
        instance.setServersLiveData(serversLiveData);
        return instance;
    }

    /**
     * Search for servers
     * @param broadcast
     * @param serversList
     * @param serversLiveData
     * @throws SocketException
     */
    public static void search(InetAddress broadcast, ArrayList<ServerPc> serversList, MutableLiveData<Boolean> serversLiveData) throws SocketException {
        Finder f=getInstance(serversList, serversLiveData);
        if(mThread==null){
            mThread=new MyInterruptThread(f);
            mThread.start();
        }
        f.send(broadcast);
    }

    public static void endSearch() {
        if(mThread!=null){
            mThread.interrupt();
            mThread=null;
            instance=null;
        }
    }

    /**
     * Used to notify changes
     * @param serversLiveData
     */
    private void setServersLiveData(MutableLiveData<Boolean> serversLiveData) {
        this.serversLiveData=serversLiveData;
    }

    /**
     * Used to store alive servers
     * @param serversList
     */
    private void setServersList(ArrayList<ServerPc> serversList) {
        this.serversList=serversList;
    }

    @Override
    public void run() {
        byte[] receiveData = new byte[512];
        while(!Thread.interrupted()){
            try {
                DatagramPacket dp=new DatagramPacket(receiveData, receiveData.length);
                socket.receive(dp);
                byte[] received= dp.getData();
                try {
                    String receivedString = new String(received);
                    String[] tokens = receivedString.split(":");
                    ServerPc pc = new ServerPc(tokens[1], dp.getAddress().toString().split("/")[1]);
                    if (received[0] == ALIVE && !serversList.contains(pc)) {
                        serversList.add(pc);
                        serversLiveData.postValue(true);
                    }
                }catch (ArrayIndexOutOfBoundsException ignore){

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Send broadcast message
     * @param broadcast address
     */
    public void send(InetAddress broadcast)  {
        executor.execute(()->{
            final byte[] message = new byte[]{BROADCAST};
            DatagramPacket sendPacket = new DatagramPacket(message, message.length, broadcast, portPc);
            try {
                socket.send(sendPacket);
            }catch(IOException e){
                e.printStackTrace();
            }
        });
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }
}
