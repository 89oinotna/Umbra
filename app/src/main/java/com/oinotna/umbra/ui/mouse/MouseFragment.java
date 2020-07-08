package com.oinotna.umbra.ui.mouse;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.oinotna.umbra.MainActivity;
import com.oinotna.umbra.MyBroadcastReceiver;
import com.oinotna.umbra.R;
import com.oinotna.umbra.mouse.MouseSocket;

public class MouseFragment extends Fragment implements SensorEventListener, View.OnTouchListener, SharedPreferences.OnSharedPreferenceChangeListener, Observer<Byte> {

    private MouseViewModel mouseViewModel;

    private Button btnMouseLeft;
    private Button btnMouseRight;
    private Button btnMouseWheel;
    private Button btnMousePad;
    private SensorManager sm;

    private BroadcastReceiver br;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mouse, container, false);
        mouseViewModel = new ViewModelProvider(requireActivity()).get(MouseViewModel.class);

        btnMouseLeft = root.findViewById(R.id.btn_mouse_left);
        btnMouseRight = root.findViewById(R.id.btn_mouse_right);
        btnMouseWheel = root.findViewById(R.id.btn_mouse_wheel);
        btnMousePad = root.findViewById(R.id.btn_mouse_pad);

        if(mouseViewModel.isConnected()){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            mouseViewModel.setPadSensitivity(prefs.getInt("mouse_sensitivity", 50));
            mouseViewModel.setWheelSensitivity(prefs.getInt("wheel_sensitivity", 50));
            mouseViewModel.setSensorSensitivity(prefs.getInt("sensor_sensitivity", 50));
            mouseViewModel.setSensor(prefs.getBoolean("use_sensor", false));
            prefs.registerOnSharedPreferenceChangeListener(this);

            sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

            btnMouseLeft.setOnTouchListener(this);
            btnMouseRight.setOnTouchListener(this);
            btnMouseWheel.setOnTouchListener(this);
            btnMousePad.setOnTouchListener(this);

            ActionBar ab=((AppCompatActivity)requireActivity()).getSupportActionBar();
            ab.setTitle(mouseViewModel.getPc().getName());

            setBroadcastReceiver();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity().getApplicationContext());
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, buildNotification());

            mouseViewModel.getConnection().observe(getViewLifecycleOwner(), this);

        }
        return root;

    }

    private void setBroadcastReceiver(){
        br=new MyBroadcastReceiver(intent -> {
            //todo controllare intent
            if(intent!=null && MyBroadcastReceiver.ACTION_DISCONNECT.equals(intent.getAction())) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity().getApplicationContext());
                notificationManager.cancel(1);//1: notification id from notify
                mouseViewModel.disconnect();
            }
        });

        //registro l'intent filter
        IntentFilter filter = new IntentFilter(MyBroadcastReceiver.ACTION_DISCONNECT);

        //registro il broadcast receiver
        requireActivity().registerReceiver(br, filter);
    }

    private Notification buildNotification(){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(requireActivity().getApplicationContext(), MainActivity.class);
        // Set the action and category so it appears that the app is being launched
        intent.setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent per pulsante disconnessione da notifica
        Intent disconnectIntent = new Intent(MyBroadcastReceiver.ACTION_DISCONNECT);
        disconnectIntent.setAction(MyBroadcastReceiver.ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent =
                PendingIntent.getBroadcast(requireActivity(), 0, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireActivity().getApplicationContext(), "com.oinotna.umbra.NOTIFICATION")
                .setSmallIcon(R.drawable.card_view_computer)
                .setContentTitle("Umbra is connected")
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentText("Connected to: "+mouseViewModel.getPc().getFullName())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Connected to: "+mouseViewModel.getPc().getFullName()))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_menu_mouse_full, "Disconnect", disconnectPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return  builder.build();
    }

    @Override
    public boolean onTouch(View v, MotionEvent e) {
        v.performClick();
        switch (v.getId()){
            case R.id.btn_mouse_left:
                mouseViewModel.left(e);
                break;
            case R.id.btn_mouse_right:
                mouseViewModel.right(e);
                break;
            case R.id.btn_mouse_wheel:
                mouseViewModel.wheel(e);
                break;
            case R.id.btn_mouse_pad:
                mouseViewModel.move(e);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO maybe use key???
        mouseViewModel.setPadSensitivity(sharedPreferences.getInt("mouse_sensitivity", 50));
        mouseViewModel.setWheelSensitivity(sharedPreferences.getInt("wheel_sensitivity", 50));
        mouseViewModel.setSensorSensitivity(sharedPreferences.getInt("sensor_sensitivity", 50));
        mouseViewModel.setSensor(sharedPreferences.getBoolean("use_sensor", false));
        if (mouseViewModel.usingSensor()) {
            mouseViewModel.resetSensor();
            Sensor sensor=sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        }
        else{
            sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mouseViewModel.usingSensor()) {
            sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mouseViewModel.isConnected() && mouseViewModel.usingSensor()) {
            mouseViewModel.resetSensor();
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(event.sensor.getName(), event.values[0]+", "+event.values[1]+", "+event.values[2]);
        mouseViewModel.move(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    @Override
    public void onChanged(Byte aByte) {
        if(aByte== MouseSocket.DISCONNECTED){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            mouseViewModel.getConnection().removeObserver(this);

            btnMouseLeft.setOnTouchListener(null);
            btnMouseRight.setOnTouchListener(null);
            btnMouseWheel.setOnTouchListener(null);
            btnMousePad.setOnTouchListener(null);
            prefs.registerOnSharedPreferenceChangeListener(null);



            mouseViewModel.disconnect();

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity().getApplicationContext());
            notificationManager.cancel(1);

            ActionBar ab=((AppCompatActivity)requireActivity()).getSupportActionBar();
            ab.setTitle(getString(R.string.title_mouse));
        }
    }
}