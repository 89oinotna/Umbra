package com.oinotna.umbra.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.db.ServerPcRepository;
import com.oinotna.umbra.thread.Finder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<Boolean> serversLiveData; //livedata che uso solo per far triggerare gli observer

    private ArrayList<ServerPc> serversList;    //lista contenente i server che trovo durante la ricerca

    private ServerPcRepository mServerPcRepository;

    public HomeViewModel(Application application) {
        super(application);

        serversLiveData = new MutableLiveData<>();
        serversList = new ArrayList<>();
        serversLiveData.setValue(true);
        mServerPcRepository=new ServerPcRepository(application);

    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public LiveData<Boolean> getLiveServersList() {
        return serversLiveData;
    }

    public ArrayList<ServerPc> getServersList() {
        return serversList;
    }

    /**
     * Use the Finder to send broadcast
     * @param broadcast
     */
    public void searchForServers(InetAddress broadcast)  {

        try{
            Finder.search(broadcast, serversList, serversLiveData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Search for a server by it's name in database
     * @param name of the server
     * @return livedata on which the query return
     */
    public LiveData<ServerPc> getPC(String name) {
        return mServerPcRepository.getPc(name);
    }

    /**
     * Store pc in database
     * @param pc
     */
    public void storePc(ServerPc pc) {
        mServerPcRepository.insert(pc);
    }
}