package com.oinotna.umbra.input;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import com.oinotna.umbra.input.mouse.Mouse;

public class InputManager {

    public static void push(byte action){
        if(MySocket.isConnected())
            MySocket.getInstance().push(action);
    }

    public static void push(byte action, float[] coord) {
        if(MySocket.isConnected())
            MySocket.getInstance().push(action, coord);
    }

    /*          MOUSE           */
    public static boolean mouse(Mouse.Type type, Object event){
        switch (type){
            case LEFT:
                Mouse.getInstance().left((MotionEvent) event);
                break;
            case RIGHT:
                Mouse.getInstance().right((MotionEvent)event);
                break;
            case PAD:
                Mouse.getInstance().move((MotionEvent)event);
                break;
            case SENSOR:
                Mouse.getInstance().move((SensorEvent)event);
                break;
            case WHEEL:
                Mouse.getInstance().wheel((MotionEvent)event);
                break;
            default:
                break;
        }
        return true;
    }

    public static void setMouseSensitivity(Mouse.Type type, int value){
        switch (type){
            case PAD:
                Mouse.getInstance().setPadSensitivity(value);
                break;
            case WHEEL:
                Mouse.getInstance().setWheelSensitivity(value);
                break;
            case SENSOR:
                Mouse.getInstance().setSensorSensitivity(value);
                break;
        }
    }

    public static void resetSensor(){
        Mouse.getInstance().resetSensor();
    }

}
