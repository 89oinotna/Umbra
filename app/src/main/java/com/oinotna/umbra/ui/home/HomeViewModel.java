package com.oinotna.umbra.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.db.ServerPcRepository;
import com.oinotna.umbra.thread.Finder;
import com.oinotna.umbra.thread.MyInterruptThread;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class HomeViewModel extends AndroidViewModel {
    private MyInterruptThread thFinder;

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

    public void searchForServers(InetAddress broadcast)  {
        //clear recycler view
        serversList.clear();
        serversLiveData.postValue(true);

        if(thFinder==null){
            try {
                //todo lasciare finder e inviare solo nuovo broadcast
                //todo finder gestisce tutto in modo trasparente
                thFinder=new MyInterruptThread(new Finder(broadcast, serversList, serversLiveData));
                thFinder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            thFinder.interrupt();
            thFinder=null;
            searchForServers(broadcast);
        }
    }

    /**
     * Termina il thread in ascolto dei server
     */
    public void endSearch() {
        if(thFinder!=null)
            thFinder.interrupt();
        thFinder=null;
    }

    public LiveData<ServerPc> getPC(String name) {
        return mServerPcRepository.getPc(name);
    }

    public void storePc(ServerPc pc) {
        mServerPcRepository.insert(pc);
    }
}