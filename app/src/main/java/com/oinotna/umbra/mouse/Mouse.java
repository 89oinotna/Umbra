package com.oinotna.umbra.mouse;

import android.hardware.SensorEvent;
import android.view.MotionEvent;

import androidx.lifecycle.MutableLiveData;

import com.oinotna.umbra.db.ServerPc;
import com.oinotna.umbra.thread.MyInterruptThread;

import java.net.SocketException;

public class Mouse implements MouseControl {

    public static byte NULL=0x00;
    public static byte LEFT_DOWN=0x01;
    public static byte LEFT_UP=0x02;
    public static byte RIGHT_DOWN=0x03;
    public static byte RIGHT_UP=0x04;
    public static byte WHEEL_MOVE=0x05;
    public static byte PAD_MOVE=0x06;
    public static byte SENSOR_MOVE=0x07;

    private MouseSocket socket;
    private MyInterruptThread thSocket;

    private float[] lastWheel;
    private float[] lastPad;
    private float[] lastSensor;

    private float wheelSensitivity;
    private float padSensitivity;
    private float sensorSensitivity;


    public Mouse(ServerPc pc, MutableLiveData<Byte> mConnection) throws SocketException {
        //todo single thread executor per run di mousesocket???
        this.lastWheel=new float[]{-1,-1};
        this.lastPad=new float[]{-1,-1};
        this.lastSensor=new float[]{-1,-1};

        //creo il socket i mi metto in ascolto
        this.socket=new MouseSocket(pc, mConnection);
        this.thSocket=new MyInterruptThread(socket);
        this.thSocket.start();
    }

    public void tryConnection(){
        this.socket.tryConnection();
    }

    public void close() {
        socket.disconnect();
        thSocket.interruptOnly();
    }

    @Override
    public boolean left(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                socket.push(LEFT_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                socket.push(LEFT_UP);
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
                socket.push(RIGHT_DOWN);
                break;
            case MotionEvent.ACTION_UP:
                socket.push(RIGHT_UP);
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
                lastWheel[0]=event.getX();
                lastWheel[1]=event.getY();
            case MotionEvent.ACTION_MOVE:
                // new position
                //final float x = event.getX();
                final float y = event.getY();

                final float deltaY = (y-lastWheel[1])*wheelSensitivity;
                //TODO maybe sampling for better performance
                socket.push(WHEEL_MOVE, new float[]{0, deltaY});
                break;
            case MotionEvent.ACTION_UP:
                //reset (do i need this?)
                lastWheel[0]=-1;
                lastWheel[1]=-1;
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean move(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //initialize the point
                lastPad[0]=event.getX();
                lastPad[1]=event.getY();
            case MotionEvent.ACTION_MOVE:
                // new position
                final float x = event.getX();
                final float y = event.getY();

                final float deltaX = (x-lastPad[0])*padSensitivity;
                final float deltaY = (y-lastPad[1])*padSensitivity;
                //TODO maybe sampling for better performance
                lastPad[0]=x;
                lastPad[1]=y;
                socket.push(PAD_MOVE, new float[]{deltaX, deltaY});
                break;
            case MotionEvent.ACTION_UP:
                //reset (do i need this?)
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
        if(Math.abs(deltaX)-event.sensor.getResolution()>event.sensor.getResolution()&&Math.abs(deltaY)-event.sensor.getResolution()>event.sensor.getResolution())
            socket.push(SENSOR_MOVE, new float[]{deltaX*sensorSensitivity*-1,deltaY*sensorSensitivity*-1});
        //if((int) Math.abs(x*gyroscopeSensitivity)>0&&(int)Math.abs(y*gyroscopeSensitivity)>0)
         //   socket.push(SENSOR_MOVE, new float[]{x*gyroscopeSensitivity*-1,y*gyroscopeSensitivity*-1});
        //TODO RESET
    }

    public void setSensorSensitivity(int sensorSensitivity) {
        this.sensorSensitivity=sensorSensitivity*1000;
    }

    public void resetSensor(){
        lastSensor[0]=-1;
        lastSensor[1]=-1;
    }

    public ServerPc getPc() {
        return socket.getPc();
    }
}
