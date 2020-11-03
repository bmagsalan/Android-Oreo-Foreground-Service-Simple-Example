package com.data.studysensor.androidoreoforegroundtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private NotificationManager mNotificationManager;
    private Handler handler;
    private int count = 0;
    private static int stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
    private static boolean connected = false;
    private static boolean laststate = connected;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
    }

    @Override
    public void onDestroy() {
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Handler handler = new Handler();
        int count = 0;

        final Runnable runnable = new Runnable() {
            public void run() {
                // need to do tasks on the UI thread
                List<ScanResult> wifiScanResults = MainActivity.getWifiScanResults(true, getApplicationContext());
                String result = "stepped_out";
                connected = false;
                for( ScanResult s : wifiScanResults ){

                    if( s.SSID.contains("Acanac")){
                        result = "logged_in";
                        connected = true;
                        break;
                    }
                }

                if( laststate != connected ){
                    UtilsTextWriter.write(UtilsTextWriter.getCurrentTimeStamp() + " " + result);
                }


                laststate = connected;


                handler.postDelayed(this, 30_000);
            }
        };

// trigger first time
        handler.post(runnable);

        if (intent == null) {
            stopForeground(true);
            stopSelf();

            return START_NOT_STICKY;
        }

        // if user starts the service
        switch (intent.getAction()) {
            case Constants.ACTION.START_ACTION:
                Log.d(TAG, "Received user starts foreground intent");
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                // Start the locker receiver
                final ScreenActionReceiver screenactionreceiver = new ScreenActionReceiver();
                registerReceiver(screenactionreceiver, screenactionreceiver.getFilter());

                connect();
                break;
            case Constants.ACTION.STOP_ACTION:
                stopForeground(true);
                stopSelf();
                break;
            default:
                stopForeground(true);
                stopSelf();
        }

        return START_NOT_STICKY;
    }

    // its connected, so change the notification text
    private void connect() {
        // after 10 seconds its connected
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        Log.d(TAG, "Bluetooth Low Energy device is connected!!");
                        Toast.makeText(getApplicationContext(),"Connected!",Toast.LENGTH_SHORT).show();
                        stateService = Constants.STATE_SERVICE.CONNECTED;
                        startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                    }
                }, 10000);

    }


    private Notification prepareNotification() {
        // handle build version above android oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            CharSequence name = getString(R.string.text_name_notification);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // if min sdk goes below honeycomb
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }*/

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a stop intent
        Intent stopIntent = new Intent(this, MyService.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);

        // if it is connected
        switch(stateService) {
            case Constants.STATE_SERVICE.NOT_CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "DISCONNECTED");
                break;
            case Constants.STATE_SERVICE.CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "CONNECTED");

                break;
        }

        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }


}
