package com.oinotna.umbra.thread;

import androidx.annotation.Nullable;

import java.net.DatagramSocket;

/**
 * Mi permette di essere interrotto se è bloccato sulla read
 */
public class MyInterruptThread extends Thread {
    private final DatagramSocket socket;

    public MyInterruptThread(@Nullable RunnableWithSocket target) {
        super(target);
        this.socket = target.getSocket();
    }

    /**
     * Interrupt the current thread and close it's socket
     */
    @Override
    public void interrupt(){
        super.interrupt();
        this.socket.close(); //mi sblocca la read (IOException)
    }

}
