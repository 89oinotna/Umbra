package com.oinotna.umbra.input;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.oinotna.umbra.MainActivity;
import com.oinotna.umbra.MyBroadcastReceiver;
import com.oinotna.umbra.R;

public class MySocketService extends Service {

    private static MySocket mSocket;

    public static final String START_SERVICE="com.oinotna.umbra.action.START_SERVICE";
    public static final String STOP_SERVICE="com.oinotna.umbra.action.STOP_SERVICE";

    private static boolean started=false;


    @Override
    public void onCreate() {
        mSocket=MySocket.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action=intent.getAction();
        if(START_SERVICE.equals(action) && !started){
            Notification n=buildNotification(intent.getStringExtra("pcName"));
            startForeground(1, n);
            mSocket.setOnDisconnectListener(()->{
                stopSelf();
                started=false;
            });
            started=true;
        }
        else if(STOP_SERVICE.equals(action)){
            //todo oppure in onDestroy() ?
            mSocket.disconnect();
            /*
            stopSelf();
            started=false;*/
        }
        return START_NOT_STICKY;
    }

    /**
     * Build the notification for foreground
     * @return the builded notification
     */
    private Notification buildNotification(String pcName){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        // Set the action and category so it appears that the app is being launched
        intent.setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Pending intent per pulsante disconnessione da notifica
        Intent disconnectIntent = new Intent(MyBroadcastReceiver.ACTION_DISCONNECT); //todo
        disconnectIntent.setAction(MyBroadcastReceiver.ACTION_DISCONNECT);
        PendingIntent disconnectPendingIntent =
                PendingIntent.getBroadcast(getApplicationContext(), 0, disconnectIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "com.oinotna.umbra.NOTIFICATION")
                .setSmallIcon(R.drawable.card_view_computer)
                .setContentTitle(getString(R.string.notification_connected))
                .setOngoing(true)
                .setAutoCancel(false)
                .setContentText(getString(R.string.notification_connected_to)+pcName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.notification_connected_to)+pcName))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_menu_mouse_full, getString(R.string.notification_button_disconnect), disconnectPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return  builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
