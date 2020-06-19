package com.oinotna.umbra.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oinotna.umbra.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ServersAdapter mAdapter;
    private RecyclerView rv;
    private ArrayList<ServerPc> serversList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        serversList= homeViewModel.getServersList(); //prendo la lista dal viewmodel

        mAdapter=new ServersAdapter(serversList);

        rv=root.findViewById(R.id.rv_servers);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 3));
        rv.setAdapter(mAdapter);
        //rv.setNestedScrollingEnabled(true);

        //mi permette di aggiornare la recycler view
        homeViewModel.getLiveServersList().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean serverPcs) {
                mAdapter.notifyDataSetChanged();
            }

        });

        /*Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                serversList.add(new ServerPc("ciao", "1234"));
                homeViewModel.postServer();


            }
        }, 1000, 5000);*/


        return root;
    }


}