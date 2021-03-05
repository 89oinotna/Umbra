package com.oinotna.umbra.ui.keyboard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.oinotna.umbra.R;
import com.oinotna.umbra.input.MySocket;
import com.oinotna.umbra.input.MySocketService;
import com.oinotna.umbra.input.MySocketViewModel;

import java.util.Objects;

public class KeyboardFragment extends Fragment implements Observer<Byte> {

    private KeyboardViewModel mViewModel;
    private MySocketViewModel mySocketViewModel;

    private BroadcastReceiver br;
    public static KeyboardFragment newInstance() {
        return new KeyboardFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(KeyboardViewModel.class);
        mySocketViewModel=new ViewModelProvider(requireActivity()).get(MySocketViewModel.class);
        if(MySocket.isConnected()){
            requireActivity().startService(new Intent(requireActivity().getApplicationContext(), MySocketService.class)
                    .setAction(MySocketService.START_SERVICE)
                    .putExtra("pcName", mySocketViewModel.getPc().getFullName()));

            mySocketViewModel.getConnection().observe(getViewLifecycleOwner(), this);
        }
        return inflater.inflate(R.layout.fragment_keyboard, container, false);
    }


    @Override
    public void onChanged(Byte aByte) {
        if(aByte == MySocket.DISCONNECTED){
            mySocketViewModel.getConnection().removeObserver(this);

            ActionBar ab=((AppCompatActivity)requireActivity()).getSupportActionBar();
            Objects.requireNonNull(ab).setTitle(getString(R.string.title_keyboard));
        }
    }
}