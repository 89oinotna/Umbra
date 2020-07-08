package com.oinotna.umbra.thread;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.thread.RunnableWithSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Finder implements RunnableWithSocket {
    private int portPc=4513;
    private int portFinder=4513;

    private InetAddress broadcast;
    private DatagramSocket socket;

    private static byte BROADCAST=0x00;
    private static byte ALIVE=0x01;

    private List<ServerPc> serversList;
    private MutableLiveData<Boolean> serversLiveData;

    public Finder(InetAddress broadcast, ArrayList<ServerPc> serversList, MutableLiveData<Boolean> serversLiveData) throws SocketException {
        this.serversList=serversList;
        this.serversLiveData=serversLiveData;
        socket=new DatagramSocket(portFinder);
        this.broadcast=broadcast;
    }

    @Override
    public void run() {
        byte[] receiveData = new byte[512];
        try {
            send();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!Thread.interrupted()){
            try {
                DatagramPacket dp=new DatagramPacket(receiveData, receiveData.length);
                socket.receive(dp);
                byte[] received= dp.getData();
                try {
                    String receivedString = new String(received);
                    //TODO use byte instead of "server"
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

    public void send() throws IOException {
        final byte[] message = new byte[]{BROADCAST};
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, broadcast, portPc);
        socket.send(sendPacket);
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }
}
