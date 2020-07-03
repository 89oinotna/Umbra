package com.oinotna.umbra.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

public interface MouseControl {
    void setPadSensitivity(int padSensitivity);
    void setWheelSensitivity(int wheelSensitivity);
    void setSensorSensitivity(int sensorSensitivity);

    //pad
    boolean left(MotionEvent event);
    boolean right(MotionEvent event);
    boolean wheel(MotionEvent event);
    boolean move(MotionEvent event);

    //sensor
    void move(SensorEvent action);
}
