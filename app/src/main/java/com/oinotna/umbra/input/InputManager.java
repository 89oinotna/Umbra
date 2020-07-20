package com.oinotna.umbra.input;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import com.oinotna.umbra.input.mouse.Mouse;

public class InputManager {

    /**
     * Push a static action to server
     * @see MySocket#push(byte)
     */
    public static void push(byte action){
        if(MySocket.isConnected())
            MySocket.getInstance().push(action);
    }

    /**
     * Push a dynamic action to server
     * @see MySocket#push(byte, float[])
     */
    public static void push(byte action, float[] coord) {
        if(MySocket.isConnected())
            MySocket.getInstance().push(action, coord);
    }

    /*          MOUSE           */

    /**
     * Used to provide mouse movements
     * @param type {@link Mouse.Type}
     * @param event {@link MotionEvent} or {@link SensorEvent}
     */
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

    /**
     * Sets sensitivity for the mouse
     * @param type {@link Mouse.Type}
     * @param value
     */
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

    /**
     * Reset mouse sensor values
     */
    public static void resetSensor(){
        Mouse.getInstance().resetSensor();
    }

}
