package com.oinotna.umbra.ui.home;

import android.widget.ArrayAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.oinotna.umbra.R;

import java.util.ArrayList;

public class HomeViewModel extends ViewModel {

    MutableLiveData<Boolean> serversLiveData; //livedata che uso solo per far triggerare gli observer
    //TODO cuncurrent hashmap
    ArrayList<ServerPc> serversList;    //lista contenente i server che trovo durante la ricerca

    public HomeViewModel() {
        serversLiveData = new MutableLiveData<>();
        serversList = new ArrayList<>();
        serversLiveData.setValue(true);
    }


    public LiveData<Boolean> getLiveServersList() {
        return serversLiveData;
    }

    public ArrayList<ServerPc> getServersList() {
        return serversList;
    }

    /**
     * Per triggerare gli observer sul livedata
     */
    public void postServer(){
        serversLiveData.postValue(true);
    }
}