package com.oinotna.umbra.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;


public class ServerPcViewModel extends AndroidViewModel {
    private ServerPcRepository mRepository;
    public ServerPcViewModel(@NonNull Application application) {
        super(application);
    }

    public void insert(ServerPc pc){
        mRepository.insert(pc);
    }

    LiveData<ServerPc> getPc(String name){
        return mRepository.getPc(name);
    }
}
