package com.oinotna.umbra.ui.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.mouse.Mouse;
import com.oinotna.umbra.mouse.MouseControl;
import com.oinotna.umbra.mouse.MouseSocket;

import java.net.SocketException;

import javax.crypto.SecretKey;

public class MouseViewModel extends ViewModel implements MouseControl {

    private MutableLiveData<Byte> mConnection;

    private ServerPc pc;

    private Mouse mouse;

    private boolean useSensor;

    public MouseViewModel() {
        mConnection = new MutableLiveData<>();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disconnect();
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
    public boolean connect(ServerPc pc) throws SocketException {

        //Se mi voglio connettere allo stesso
        if(mouse!=null &&
                (mConnection.getValue() == MouseSocket.CONNECTED_PASSWORD || mConnection.getValue() == MouseSocket.CONNECTED)
                && mouse.getPc().getName().equals(pc.getName())){
            mConnection.postValue(mConnection.getValue());
            return true;
        }

        this.pc=pc;

        if(mouse!=null)
            mouse.close();
        mouse = new Mouse(pc, mConnection);
        mouse.tryConnection();
        return true;
    }

    /**
     * Should be called after connect if password is required
     * @param password base64 of the password
     */
    public void usePassword(CharSequence password) {
        pc.setPassword((String) password);
        mouse.tryConnection();
    }

    @Override
    public boolean left(MotionEvent action) {
        if(mouse!=null)
            return mouse.left(action);
        return false;
    }

    @Override
    public boolean right(MotionEvent action) {
        if(mouse!=null)
            return mouse.right(action);
        return false;
    }

    @Override
    public boolean wheel(MotionEvent action) {
        if(mouse!=null)
        return mouse.wheel(action);
        return false;
    }

    @Override
    public boolean move(MotionEvent action) {
        if(mouse!=null)
            return mouse.move(action);
        return false;
    }

    @Override
    public void setPadSensitivity(int padSensitivity) {
        if(mouse!=null)
            mouse.setPadSensitivity(padSensitivity);
    }

    @Override
    public void setWheelSensitivity(int wheelSensitivity) {
        if(mouse!=null)
            mouse.setWheelSensitivity(wheelSensitivity);
    }

    public void setSensor(boolean sensor) {
        this.useSensor=sensor;
    }

    public boolean getSensor(){
        return useSensor;
    }

    public void resetSensor(){
        if(mouse!=null)
            mouse.resetSensor();
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        if(mouse!=null)
            mouse.setSensorSensitivity(sensorSensitivity);
    }

    @Override
    public void move(SensorEvent action) {
        if(mouse!=null)
            mouse.move(action);
    }

    public ServerPc getPc() {
        return pc;
    }

    public boolean isConnected() {
        if(mConnection.getValue() != null &&
                (mConnection.getValue()== MouseSocket.CONNECTED || mConnection.getValue()==MouseSocket.CONNECTED_PASSWORD)){
            return true;
        }
        return false;
    }

    public void disconnect() {
        if(mouse!=null){
            mouse.close();
            mConnection.postValue(MouseSocket.DISCONNECTED);
            pc=null;
            mouse=null;
        }
    }
}