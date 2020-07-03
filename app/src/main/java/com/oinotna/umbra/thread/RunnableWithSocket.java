package com.oinotna.umbra.thread;

import java.net.DatagramSocket;

/**
 * Oggetto runnable con DatagramSocket da passare a MyInterruptThread
 */
public interface RunnableWithSocket extends Runnable{
    DatagramSocket getSocket();
}
