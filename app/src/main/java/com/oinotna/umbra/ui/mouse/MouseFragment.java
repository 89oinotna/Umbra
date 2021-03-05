package com.oinotna.umbra.ui.mouse;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.oinotna.umbra.MyBroadcastReceiver;
import com.oinotna.umbra.R;
import com.oinotna.umbra.input.InputManager;
import com.oinotna.umbra.input.MySocket;
import com.oinotna.umbra.input.MySocketService;
import com.oinotna.umbra.input.mouse.Mouse;
import com.oinotna.umbra.input.MySocketViewModel;

import java.util.Objects;

public class MouseFragment extends Fragment implements SensorEventListener, View.OnTouchListener, Observer<Byte> {

    private MouseViewModel mouseViewModel;
    private MySocketViewModel mySocketViewModel;
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
        mySocketViewModel=new ViewModelProvider(requireActivity()).get(MySocketViewModel.class);
        btnMouseLeft = root.findViewById(R.id.btn_mouse_left);
        btnMouseRight = root.findViewById(R.id.btn_mouse_right);
        btnMouseWheel = root.findViewById(R.id.btn_mouse_wheel);
        btnMousePad = root.findViewById(R.id.btn_mouse_pad);
        //Log.d("DISCONNECT", ""+MySocket.isConnected());
        if(MySocket.isConnected()){
            sm = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

            btnMouseLeft.setOnTouchListener(this);
            btnMouseRight.setOnTouchListener(this);
            btnMouseWheel.setOnTouchListener(this);
            btnMousePad.setOnTouchListener(this);
            //setBroadcastReceiver();
            requireActivity().startService(new Intent(requireActivity().getApplicationContext(), MySocketService.class)
                    .setAction(MySocketService.START_SERVICE)
                    .putExtra("pcName", mySocketViewModel.getPc().getFullName()));

            mySocketViewModel.getConnection().observe(getViewLifecycleOwner(), this);
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
                    if(MySocket.getInstance()!=null)
                        MySocket.getInstance().disconnect();
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
     * onTouch for mouse buttons
     */
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        v.performClick();
        switch (v.getId()){
            case R.id.btn_mouse_left:
                mouseViewModel.mouse(Mouse.Type.LEFT, e);
                break;
            case R.id.btn_mouse_right:
                mouseViewModel.mouse(Mouse.Type.RIGHT, e);
                break;
            case R.id.btn_mouse_wheel:
                mouseViewModel.mouse(Mouse.Type.WHEEL, e);
                break;
            case R.id.btn_mouse_pad:
                mouseViewModel.mouse(Mouse.Type.PAD, e);
                break;
            default:
                return false;
        }
        return false; //for button press animation
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
        if(MySocket.isConnected()) {
            ActionBar ab = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            Objects.requireNonNull(ab).setTitle(mySocketViewModel.getPc().getName().trim());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity());
            InputManager.setMouseSensitivity(Mouse.Type.PAD, prefs.getInt("mouse_sensitivity", 50));
            InputManager.setMouseSensitivity(Mouse.Type.WHEEL, prefs.getInt("wheel_sensitivity", 50));
            InputManager.setMouseSensitivity(Mouse.Type.SENSOR, prefs.getInt("sensor_sensitivity", 50));

            mouseViewModel.setSensor(prefs.getBoolean("use_sensor", false));
            if (mouseViewModel.usingSensor()) {
                InputManager.resetSensor();
                sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
            }

        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d(event.sensor.getName(), event.values[0]+", "+event.values[1]+", "+event.values[2]);
        mouseViewModel.mouse(Mouse.Type.SENSOR, event);
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
            mySocketViewModel.getConnection().removeObserver(this);

            btnMouseLeft.setOnTouchListener(null);
            btnMouseRight.setOnTouchListener(null);
            btnMouseWheel.setOnTouchListener(null);
            btnMousePad.setOnTouchListener(null);

            sm.unregisterListener(this, sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR));

            ActionBar ab=((AppCompatActivity)requireActivity()).getSupportActionBar();
            Objects.requireNonNull(ab).setTitle(getString(R.string.title_mouse));
        }
    }


}