package com.oinotna.umbra.ui.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.InputManager;
import com.oinotna.umbra.input.mouse.Mouse;
import com.oinotna.umbra.input.mouse.MouseControl;
import com.oinotna.umbra.input.MySocket;

import java.io.IOException;
import java.net.SocketException;

public class MouseViewModel extends ViewModel implements MouseControl {

    private MutableLiveData<Byte> mConnection;

    private ServerPc pc;


    private boolean useSensor;

    public MouseViewModel() {
        mConnection = InputManager.getConnection();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        //disconnect();
    }

    public LiveData<Byte> getConnection() {
        return mConnection;
    }

    /**
     * Send connection request to the server
     * @param pc
     * @return
     * @throws SocketException
     */
    //todo è utile avere boolean?
    public boolean connect(ServerPc pc) throws IOException {
        //todo refactor spostare tutto in mouse
        InputManager.connect(pc);


        //Se mi voglio connettere allo stesso
        /*if(mouse!=null && mConnection.getValue()!=null &&
                (mConnection.getValue() == MySocket.CONNECTED_PASSWORD || mConnection.getValue() == MySocket.CONNECTED)
                && mouse.getPc().getName().equals(pc.getName())){
            mConnection.postValue(mConnection.getValue());
            return true;
        }*/

        this.pc=pc;

        /*if(mouse!=null)
            mouse.close();
        mouse = new Mouse(pc, mConnection);*/
        return true;
    }

    /**
     * Should be called after connect if password is required
     * @param password base64 of the password
     */
    public void usePassword(CharSequence password) {
        pc.setPassword((String) password);
        InputManager.usePassword(pc);

        //mouse.tryConnection();
    }

    @Override
    public boolean left(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.LEFT, action);
        /*if(mouse!=null)
            return mouse.left(action);
        return false;*/
    }

    @Override
    public boolean right(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.RIGHT, action);
        /*if(mouse!=null)
            return mouse.right(action);
        return false;*/
    }

    @Override
    public boolean wheel(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.WHEEL, action);
        /*if(mouse!=null)
            return mouse.wheel(action);
        return false;*/
    }

    @Override
    public boolean move(MotionEvent action) {
        return InputManager.mouse(Mouse.Type.PAD, action);
        /*if(mouse!=null)
            return mouse.move(action);
        return false;*/
    }

    @Override
    public void move(SensorEvent action) {
        InputManager.mouse(Mouse.Type.SENSOR, action);
        /*if(mouse!=null)
            mouse.move(action);*/
    }

    @Override
    public void setPadSensitivity(int padSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.PAD, padSensitivity);
        /*if(mouse!=null)
            mouse.setPadSensitivity(padSensitivity);*/
    }

    @Override
    public void setWheelSensitivity(int wheelSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.WHEEL, wheelSensitivity);
        /*if(mouse!=null)
            mouse.setWheelSensitivity(wheelSensitivity);*/
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        InputManager.setMouseSensitivity(Mouse.Type.SENSOR, sensorSensitivity);
        /*if(mouse!=null)
            mouse.setSensorSensitivity(sensorSensitivity);*/
    }

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

    public ServerPc getPc() {
        return pc;
    }

    public boolean isConnected() {
        return mConnection.getValue() != null &&
                (mConnection.getValue() == MySocket.CONNECTED || mConnection.getValue() == MySocket.CONNECTED_PASSWORD);
    }

    /**
     * Disconnect the mouse from server
     */
    public void disconnect() {
        /*if(mouse!=null){
            mouse.close();
            pc=null;
            mouse=null;
        }*/
        InputManager.disconnect();
        /*if(mConnection.getValue()!=null && mConnection.getValue()!=MySocket.DISCONNECTED)
            mConnection.postValue(MySocket.DISCONNECTED);*/
    }

 }