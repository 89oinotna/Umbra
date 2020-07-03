package com.oinotna.umbra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadcastReceiver extends BroadcastReceiver {
    public static String ACTION_DISCONNECT = "com.oinotna.umbra.action.DISCONNECT";
    private OnReceiveListener mListener;

    public interface OnReceiveListener{
        void onReceive(Intent intent);
    }

    public MyBroadcastReceiver(OnReceiveListener mListener){
        this.mListener=mListener;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mListener.onReceive(intent);
    }
}
