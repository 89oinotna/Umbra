package com.oinotna.umbra.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

public class ServerPcRepository {

    private final ServerPcDao mServerPcDao;

    public ServerPcRepository(Application application){
        ServerPcDatabase db = ServerPcDatabase.getDatabase(application);
        mServerPcDao = db.serverPcDao();
    }

    public LiveData<ServerPc> getPc(String name){
        return mServerPcDao.getPc(name);
    }

    public void insert(ServerPc pc) {
        ServerPcDatabase.databaseWriteExecutor.execute(() -> mServerPcDao.insert(pc));
    }
}
