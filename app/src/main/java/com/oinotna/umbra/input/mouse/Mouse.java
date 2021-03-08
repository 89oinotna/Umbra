package com.oinotna.umbra.input.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import com.oinotna.umbra.input.Command;
import com.oinotna.umbra.input.InputManager;

import java.nio.ByteBuffer;

public class Mouse implements MouseControl {

    public enum Type {
        LEFT, RIGHT, PAD, SENSOR, WHEEL
    }

    public static class MouseCommand extends Command {
        private final int action;
        private float[] coord;
        public MouseCommand(int action){
            super(InputManager.MOUSE);
            this.action=action;
        }

        public MouseCommand(int action, float[] coord){
            super(InputManager.MOUSE);
            this.action=action;
            this.coord=coord;
        }

        @Override
        public byte[] getCommandBytes() {
            ByteBuffer command;
            if(coord!=null){

                String c=":"+(int)coord[0]+","+(int)coord[1];
                byte[] cb=c.getBytes();
                command = ByteBuffer.allocate(1+4 + cb.length);
                command.put(this.getType());
                command.putInt(action);
                command.put(cb);

            }
            else{
                command = ByteBuffer.allocate(1+4);
                command.put(this.getType());
                command.putInt(action);
            }
            return command.array();
        }
    }

    //action type
    public static byte NULL=0x00;
    public static final byte LEFT_DOWN=0x01;
    public static final byte LEFT_UP=0x02;
    public static final byte RIGHT_DOWN=0x03;
    public static byte RIGHT_UP=0x04;
    public static final byte WHEEL_MOVE=0x05;
    public static final byte PAD_MOVE=0x06;
    public static final byte SENSOR_MOVE=0x07;

    private final float[] lastWheel;
    private final float[] lastPad;
    private final float[] lastSensor;

    private float wheelSensitivity;
    private float padSensitivity;
    private float sensorSensitivity;

    private static Mouse instance;

    public static Mouse getInstance(){
        if(instance==null)
            instance=new Mouse();
        return instance;
    }


    public Mouse() {
        this.lastWheel=new float[]{-1,-1};
        this.lastPad=new float[]{-1,-1};
        this.lastSensor=new float[]{-1,-1};
    }

    /**
     * Used to provide mouse movements
     * @param type {@link Mouse.Type}
     * @param event {@link MotionEvent} or {@link SensorEvent}
     */
    public static boolean mouse(Mouse.Type type, Object event){
        switch (type){
            case LEFT:
                Mouse.getInstance().left((MotionEvent) event);
                break;
            case RIGHT:
                Mouse.getInstance().right((MotionEvent)event);
                break;
            case PAD:
                Mouse.getInstance().move((MotionEvent)event);
                break;
            case SENSOR:
                Mouse.getInstance().move((SensorEvent)event);
                break;
            case WHEEL:
                Mouse.getInstance().wheel((MotionEvent)event);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean left(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                InputManager.push(new MouseCommand(LEFT_DOWN));
                break;
            case MotionEvent.ACTION_UP:
                InputManager.push(new MouseCommand(LEFT_UP));
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
                InputManager.push(new MouseCommand(RIGHT_DOWN));
                break;
            case MotionEvent.ACTION_UP:
                InputManager.push(new MouseCommand(RIGHT_UP));
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
                InputManager.push(new MouseCommand(WHEEL_MOVE, new float[]{0, deltaY}));
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
                InputManager.push(new MouseCommand(PAD_MOVE, new float[]{deltaX, deltaY}));
                break;
            case MotionEvent.ACTION_UP:
                //reset (do i need this?)
                if(System.currentTimeMillis()-timestampPad<100){
                    InputManager.push(new MouseCommand(LEFT_DOWN));
                    InputManager.push(new MouseCommand(LEFT_UP));
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
        this.wheelSensitivity=wheelSensitivity/25f;
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
        //Log.d("SENSOR", x+" , "+y);
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
            InputManager.push(new MouseCommand(SENSOR_MOVE, new float[]{deltaX * sensorSensitivity * -1, deltaY * sensorSensitivity * -1}));
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        this.sensorSensitivity=sensorSensitivity * 900f;
    }

    public void resetSensor(){
        lastSensor[0]=-1;
        lastSensor[1]=-1;
    }

}
