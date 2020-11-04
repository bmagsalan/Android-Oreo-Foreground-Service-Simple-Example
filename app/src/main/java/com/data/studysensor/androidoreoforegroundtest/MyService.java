package com.data.studysensor.androidoreoforegroundtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.List;

public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private static final String KEY_WIFI_CONNECTED = "KEY_WIFI_CONNECTED";
    private static final long LOOP_DELAY = 10_000;
    private NotificationManager mNotificationManager;
    private static int counter = 0;
    private static boolean stateService;
    final Handler handler = new Handler();

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

        stateService = getIsConnected(getApplicationContext()) ;
    }

    @Override
    public void onDestroy() {
        setIsConnected(getApplicationContext(), stateService);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        if (intent == null) {
            stopForeground(true);
            stopSelf();

            return START_NOT_STICKY;
        }

        // if user starts the service
        switch (intent.getAction()) {
            case Constants.ACTION.START_ACTION:
                Log.d(TAG, "Received user starts foreground intent");
                int count = 0;

                final Runnable runnable = new Runnable() {
                    public void run() {
                        // need to do tasks on the UI thread
                        List<ScanResult> wifiScanResults = MainActivity.getWifiScanResults(true, getApplicationContext());
                        String result = "stepped_out";
                        boolean connected = false;
                        for( ScanResult s : wifiScanResults ){

                            if( s.SSID.contains("Acanac")){
                                result = "logged_in";
                                connected = true;
                                break;
                            }

                        }

                        if( stateService != connected ){
                            UtilsTextWriter.write(UtilsTextWriter.getCurrentTimeStamp() + " " + result);
                        }

                        Log.e( TAG, "" + UtilsTextWriter.readInternal(getApplicationContext()));

                        mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                        stateService = connected;

                        handler.postDelayed(this, LOOP_DELAY);
                    }
                };

// trigger first time
                handler.postDelayed(runnable, LOOP_DELAY);


                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                // Start the locker receiver
                final ScreenActionReceiver screenactionreceiver = new ScreenActionReceiver();
                registerReceiver(screenactionreceiver, screenactionreceiver.getFilter());

                connect();
                break;
            case Constants.ACTION.STOP_ACTION:
                handler.removeCallbacksAndMessages(0);
                stopForeground(true);
                stopSelf();
                break;
            default:
                handler.removeCallbacksAndMessages(0);
                stopForeground(true);
                stopSelf();
                break;
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
        remoteViews.setTextViewText(R.id.tv_state, UtilsTextWriter.getCurrentTimeStamp() + ": " + (stateService ? "Logged In" : "Out of Office"));

        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setContentTitle("Wifi Time Tracker")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }
    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean getIsConnected(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_WIFI_CONNECTED, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setIsConnected(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_WIFI_CONNECTED, requestingLocationUpdates)
                .apply();
    }

}
