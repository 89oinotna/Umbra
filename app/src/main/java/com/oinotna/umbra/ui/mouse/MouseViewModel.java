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

public class MouseViewModel extends ViewModel{

    private ServerPc pc;

    private boolean usingSensor;

    public ServerPc getPc() {
        return pc;
    }
    public void setPc(ServerPc pc) {
        this.pc=pc;
    }

    public MouseViewModel() {
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<Byte> getConnection() {
        return MySocket.getInstance().getConnection();
    }

    /*          SENSOR          */

    /**
     * Set using sensor value
     * @param sensor
     */
    public void setSensor(boolean sensor) {
        this.usingSensor=sensor;
    }

    public boolean usingSensor(){
        return usingSensor;
    }





}