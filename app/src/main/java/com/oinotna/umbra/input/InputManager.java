package com.oinotna.umbra.input;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.mouse.Mouse;

import java.io.IOException;

public class InputManager {
    private static MySocket mSocket;
    private static Mouse mMouse;
    private static MutableLiveData<Byte> mConnection;

    public static boolean connect(ServerPc pc){
        if(mSocket==null){
            try {
                mSocket=new MySocket();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        mSocket.connect(pc, mConnection);
        if(mMouse==null)
            mMouse=new Mouse();
        return true;
    }

    public static void disconnect(){
        if(mSocket!=null)
            mSocket.disconnect();
        mSocket=null;
    }

    public static void push(byte action){
        if(mSocket!=null)
            mSocket.push(action);
    }

    public static void push(byte action, float[] coord) {
        if(mSocket!=null)
            mSocket.push(action, coord);
    }

    public static void usePassword(ServerPc pc) {
        if(mSocket!=null)
            mSocket.usePassword(pc);
    }

    public static boolean mouse(Mouse.Type type, Object event){
        if(mMouse==null) return false;
        switch (type){
            case LEFT:
                mMouse.left((MotionEvent) event);
                break;
            case RIGHT:
                mMouse.right((MotionEvent)event);
                break;
            case PAD:
                mMouse.move((MotionEvent)event);
                break;
            case SENSOR:
                mMouse.move((SensorEvent)event);
                break;
            case WHEEL:
                mMouse.wheel((MotionEvent)event);
                break;
            default:
                break;

        }
        return true;
    }

    public static void setMouseSensitivity(Mouse.Type type, int value){
        if(mMouse==null) return;
        switch (type){
            case PAD:
                mMouse.setPadSensitivity(value);
                break;
            case WHEEL:
                mMouse.setWheelSensitivity(value);
                break;
            case SENSOR:
                mMouse.setSensorSensitivity(value);
                break;
        }
    }

    public static Mouse getMouse(){
        return mMouse;
    }

    public static MutableLiveData<Byte> getConnection() {
        if(mConnection==null)
            mConnection= new MutableLiveData<>();
        return mConnection;
    }
}
