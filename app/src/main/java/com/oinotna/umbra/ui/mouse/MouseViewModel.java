package com.oinotna.umbra.ui.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.InputManager;
import com.oinotna.umbra.input.MySocket;
import com.oinotna.umbra.input.mouse.Mouse;
import com.oinotna.umbra.input.mouse.MouseControl;

public class MouseViewModel extends ViewModel implements MouseControl {

    private MutableLiveData<Byte> mConnection;

    private ServerPc pc;

    private boolean useSensor;

    public ServerPc getPc() {
        return pc;
    }
    public void setPc(ServerPc pc) {
        this.pc=pc;
    }

    public MouseViewModel() {
        mConnection = InputManager.getConnection();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<Byte> getConnection() {
        return mConnection;
    }

    public boolean isConnected() {
        //TODO implementarlo in socket
        return mConnection.getValue() != null &&
                (mConnection.getValue() == MySocket.CONNECTED || mConnection.getValue() == MySocket.CONNECTED_PASSWORD);
    }

    /*          SOCKET          */

    /**
     * Send connection request to the server
     * @param pc
     */
    public void connect(ServerPc pc)  {
        this.pc=pc;
        InputManager.connect(pc);

    }

    /**
     * Should be called after connect if password is required
     * @param password base64 of the password
     */
    public void usePassword(CharSequence password) {
        pc.setPassword((String) password);
        InputManager.usePassword(pc);
    }

    /**
     * Disconnect the mouse from server
     */
    public void disconnect() {
        pc=null;
        InputManager.disconnect();
    }

    /*          MOUSE           */

    @Override
    public boolean left(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.LEFT, action);
    }

    @Override
    public boolean right(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.RIGHT, action);
    }

    @Override
    public boolean wheel(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.WHEEL, action);
    }

    @Override
    public boolean move(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.PAD, action);
    }

    @Override
    public void move(SensorEvent action) {
        InputManager.mouse(Mouse.Type.SENSOR, action);
    }

    @Override
    public void setPadSensitivity(int padSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.PAD, padSensitivity);
    }

    @Override
    public void setWheelSensitivity(int wheelSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.WHEEL, wheelSensitivity);
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.SENSOR, sensorSensitivity);
    }

    /*          SENSOR          */

    public void setSensor(boolean sensor) {
        this.useSensor=sensor;
    }

    public boolean usingSensor(){
        return useSensor;
    }

    public void resetSensor(){
        Mouse m=InputManager.getMouse();
        if(m!=null)
            m.resetSensor();
    }


}