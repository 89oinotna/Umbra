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
import android.util.Log;
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
import com.oinotna.umbra.input.InputManager;
import com.oinotna.umbra.input.MySocket;

import java.util.Objects;

public class MouseFragment extends Fragment implements MySocket.OnDisconnectListener, SensorEventListener, View.OnTouchListener, SharedPreferences.OnSharedPreferenceChangeListener, Observer<Byte> {

    private MouseViewModel mouseViewModel;

    private Button btnMouseLeft;
    private Button btnMouseRight;
    private Button btnMouseWheel;
    private Button btnMousePad;
    private SensorManager sm;

    private BroadcastReceiver br;
    private Notification mNotification;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_mouse, container, false);
        mouseViewModel = new ViewModelProvider(requireActivity()).get(MouseViewModel.class);

        btnMouseLeft = root.findViewById(R.id.btn_mouse_left);
        btnMouseRight = root.findViewById(R.id.btn_mouse_right);
        btnMouseWheel = root.findViewById(R.id.btn_mouse_wheel);
        btnMousePad = root.findViewById(R.id.btn_mouse_pad);
        Log.d("DISCONNECT", ""+mouseViewModel.isConnected());
        if(mouseViewModel.isConnected()){
            InputManager.setOnDisconnectListener(this);
            setBroadcastReceiver();
            if(mNotification==null) {
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity().getApplicationContext());
                // notificationId is a unique int for each notification that you must define
                mNotification=buildNotification();
                notificationManager.notify(1, buildNotification());
            }


            mouseViewModel.getConnection().observe(requireActivity(), this);
        }

        return root;
    }

    /**
     * Set MyBroadcastReceiver that wait for the notification broadcast
     * Disconnect when broadcast is received
     */
    private void setBroadcastReceiver(){
        if(br==null) {
            br = new MyBroadcastReceiver(intent -> {
                //todo controllare intent
                if (intent != null && MyBroadcastReceiver.ACTION_DISCONNECT.equals(intent.getAction())) {
                    mouseViewModel.disconnect();
                }
            });

            //registro l'intent filter
            IntentFilter filter = new IntentFilter(MyBroadcastReceiver.ACTION_DISCONNECT);

            //registro il broadcast receiver
            /* Dalla doc:   Context-registered receivers receive broadcasts as long as their registering
                            context is valid. For an example, if you register within an Activity context,
                            you receive broadcasts as long as the activity is not destroyed. If you register
                            with the Application context, you receive broadcasts as long as the app is running.*/
            requireActivity().getApplicationContext().registerReceiver(br, filter);
        }
    }

    /**
     * Build the App notification when connected
     * @return the builded notification
     */
    private Notification buildNotification(){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(requireActivity().getApplicationContext(), MainActivity.class);
        // Set the action and category so it appears that the app is being launched
        intent.setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(requireActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent per pulsante disconnessione da notifica
        Intent disconnectIntent = new Intent(MyBroadcastReceiver.ACTION_DISCONNECT); //todo
        disconnectIntent.setAction(MyBroadcastReceiver.ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent =
                PendingIntent.getBroadcast(requireActivity().getApplicationContext(), 0, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireActivity().getApplicationContext(), "com.oinotna.umbra.NOTIFICATION")
                .setSmallIcon(R.drawable.card_view_computer)
                .setContentTitle(getString(R.string.notification_connected))
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentText(getString(R.string.notification_connected_to)+mouseViewModel.getPc().getFullName())
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notification_connected_to)+mouseViewModel.getPc().getFullName()))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_menu_mouse_full, getString(R.string.notification_button_disconnect), disconnectPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return  builder.build();
    }

    /**
     * onTouch for mouse buttons
     */
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
        return false; //for button press animation
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //TODO use key
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        if(mouseViewModel.usingSensor()) {
            sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));
        }
    }

    @Override
    public void onResume() {
        if(mouseViewModel.isConnected()) {
            ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            Objects.requireNonNull(ab).setTitle(mouseViewModel.getPc().getName().trim());

            sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

            btnMouseLeft.setOnTouchListener(this);
            btnMouseRight.setOnTouchListener(this);
            btnMouseWheel.setOnTouchListener(this);
            btnMousePad.setOnTouchListener(this);

        }

        super.onResume();
        if(mouseViewModel.isConnected()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            mouseViewModel.setPadSensitivity(prefs.getInt("mouse_sensitivity", 50));
            mouseViewModel.setWheelSensitivity(prefs.getInt("wheel_sensitivity", 50));
            mouseViewModel.setSensorSensitivity(prefs.getInt("sensor_sensitivity", 50));
            mouseViewModel.setSensor(prefs.getBoolean("use_sensor", false));
            prefs.registerOnSharedPreferenceChangeListener(this);
            if (mouseViewModel.usingSensor()) {
                mouseViewModel.resetSensor();
                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
            }
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

    /**
     * Observer that manage the connection status when disconnected
     * @param aByte
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onChanged(Byte aByte) {
        if(aByte == MySocket.DISCONNECTED){
            mouseViewModel.getConnection().removeObserver(this);

            mouseViewModel.disconnect();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            prefs.unregisterOnSharedPreferenceChangeListener(this);

            btnMouseLeft.setOnTouchListener(null);
            btnMouseRight.setOnTouchListener(null);
            btnMouseWheel.setOnTouchListener(null);
            btnMousePad.setOnTouchListener(null);

            sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));

            ActionBar ab=((AppCompatActivity)requireActivity()).getSupportActionBar();
            Objects.requireNonNull(ab).setTitle(getString(R.string.title_mouse));
        }
    }

    /**
     * Used to remove notification when disconnected and app is in background
     */
    @Override
    public void onDisconnect() {
        requireActivity().runOnUiThread(() -> {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireActivity().getApplicationContext());
            notificationManager.cancel(1);
            mNotification=null;
        });

    }
}