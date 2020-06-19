package com.oinotna.umbra.ui.mouse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.oinotna.umbra.R;
import com.oinotna.umbra.ui.home.HomeViewModel;

public class MouseFragment extends Fragment {

    private MouseViewModel mouseViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mouseViewModel =new ViewModelProvider(this).get(MouseViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mouse, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        mouseViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}