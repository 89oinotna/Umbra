package com.oinotna.umbra.input.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import com.oinotna.umbra.input.InputManager;

public class Mouse implements MouseControl {

    public enum Type {
        LEFT, RIGHT, PAD, SENSOR, WHEEL
    }

    //action type
    public static byte NULL=0x00;
    public static byte LEFT_DOWN=0x01;
    public static byte LEFT_UP=0x02;
    public static byte RIGHT_DOWN=0x03;
    public static byte RIGHT_UP=0x04;
    public static byte WHEEL_MOVE=0x05;
    public static byte PAD_MOVE=0x06;
    public static byte SENSOR_MOVE=0x07;

    private float[] lastWheel;
    private float[] lastPad;
    private float[] lastSensor;

    private float wheelSensitivity;
    private float padSensitivity;
    private float sensorSensitivity;


    public Mouse() {
        this.lastWheel=new float[]{-1,-1};
        this.lastPad=new float[]{-1,-1};
        this.lastSensor=new float[]{-1,-1};
    }

    @Override
    public boolean left(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                InputManager.push(LEFT_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                InputManager.push(LEFT_UP);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean right(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                InputManager.push(RIGHT_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                InputManager.push(RIGHT_UP);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean wheel(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //initialize the point
                //lastWheel[0]=event.getX();
                lastWheel[1]=event.getY();
            case MotionEvent.ACTION_MOVE:
                // new position
                //final float x = event.getX();
                final float y = event.getY();

                final float deltaY = (y-lastWheel[1])*wheelSensitivity;
                lastWheel[1]=y;
                //TODO maybe sampling for better performance
                InputManager.push(WHEEL_MOVE, new float[]{0, deltaY});
                break;
            case MotionEvent.ACTION_UP:
                //reset (do i need this?)
                //lastWheel[0]=-1;
                lastWheel[1]=-1;
                break;
            default:
                return false;
        }
        return true;
    }

    private long timestampPad;

    /**
     * Moving with pad values
     * @param event
     * @return
     */
    @Override
    public boolean move(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //initialize the point
                lastPad[0]=event.getX();
                lastPad[1]=event.getY();
                timestampPad=System.currentTimeMillis();
            case MotionEvent.ACTION_MOVE:
                // new position
                final float x = event.getX();
                final float y = event.getY();

                final float deltaX = (x-lastPad[0])*padSensitivity;
                final float deltaY = (y-lastPad[1])*padSensitivity;
                lastPad[0]=x;
                lastPad[1]=y;
                InputManager.push(PAD_MOVE, new float[]{deltaX, deltaY});
                break;
            case MotionEvent.ACTION_UP:
                //reset (do i need this?)
                if(System.currentTimeMillis()-timestampPad<100){
                    InputManager.push(LEFT_DOWN);
                    InputManager.push(LEFT_UP);
                }
                lastPad[0]=-1;
                lastPad[1]=-1;
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void setPadSensitivity(int padSensitivity) {
        this.padSensitivity=padSensitivity/50f;
    }

    @Override
    public void setWheelSensitivity(int wheelSensitivity) {
        this.wheelSensitivity=wheelSensitivity/50f;
    }

    /**
     * Moving with sensor values
     * @param event
     */
    @Override
    public void move(SensorEvent event) {
       /* final float x=event.values[0];  //screen y
        final float z=event.values[2];  //screen x
        */
        final float x=event.values[2];
        final float y=event.values[0];
        if(lastSensor[0]==-1 || lastSensor[0]==0){
            lastSensor[0]=x;
            lastSensor[1]=y;
            return;
        }
        final float deltaX=(x-lastSensor[0]);
        final float deltaY=(y-lastSensor[1]);
        lastSensor[0]=x;
        lastSensor[1]=y;
        if(Math.abs(Math.abs(deltaX)-event.sensor.getResolution())>event.sensor.getResolution() &&
                Math.abs(Math.abs(deltaY)-event.sensor.getResolution())>event.sensor.getResolution() &&
                ((int)(deltaX*sensorSensitivity)!=0 || (int)(deltaY*sensorSensitivity)!=0))
            InputManager.push(SENSOR_MOVE, new float[]{deltaX*sensorSensitivity*-1,deltaY*sensorSensitivity*-1});
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        float sensorSensitivityMultiplier = 900f;
        this.sensorSensitivity=sensorSensitivity * sensorSensitivityMultiplier;
    }

    public void resetSensor(){
        lastSensor[0]=-1;
        lastSensor[1]=-1;
    }

}
