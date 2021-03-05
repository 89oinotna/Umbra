package com.oinotna.umbra.input;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import com.oinotna.umbra.input.mouse.Mouse;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class InputManager {
    public static byte KEYBOARD = 0x01;
    public static byte MOUSE = 0x02;

    /**
     * Push a static action to server
     * @see MySocket#
     */
    /*public static void push(byte type, int action){
        if(MySocket.isConnected())
            MySocket.getInstance().push(type, action);
    }*/

    public static void push(Command cmd){
        if(MySocket.isConnected())
            MySocket.getInstance().push(cmd);
    }

    /**
     * Push a dynamic action to server
     * @see MySocket
     */
    /*public static void push(byte type, int action, float[] coord) {
        if(MySocket.isConnected())
            MySocket.getInstance().push(type, action, coord);
    }*/

    /**
     * Push a dynamic action to server
     * @see MySocket
     */
    /*public static void push(byte type, int action, int val) {
        if(MySocket.isConnected())
            MySocket.getInstance().push(type, action, val);
    }*/

    /*          MOUSE           */



    /**
     * Sets sensitivity for the mouse
     * @param type {@link Mouse.Type}
     * @param value
     */
    /*public static void setMouseSensitivity(Mouse.Type type, int value){
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
    }*/

    /**
     * Reset mouse sensor values
     */
    /*public static void resetSensor(){
        Mouse.getInstance().resetSensor();
    }
*/
}
