package com.oinotna.umbra.input;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.input.MySocket;

public class MySocketViewModel extends ViewModel {

    private ServerPc pc;

    private boolean usingSensor;

    public ServerPc getPc() {
        return pc;
    }
    public void setPc(ServerPc pc) {
        this.pc=pc;
    }

    public MySocketViewModel() {
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    /**
     * Returns the current connection used by the socket if any
     * @return connection (can be null if no socket)
     */
    public LiveData<Byte> getConnection() {
        if(MySocket.getInstance()!=null)
            return MySocket.getInstance().getConnection();
        return null;
    }







}