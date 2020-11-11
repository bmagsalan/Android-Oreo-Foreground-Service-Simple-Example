package com.data.studysensor.androidoreoforegroundtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MyService extends Service {

    private static final String TAG = MyService.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private static final String KEY_WIFI_CONNECTED = "KEY_WIFI_CONNECTED";
    private static final String KEY_LAST_STATE = "KEY_LAST_STATE";
    private static final long LOOP_DELAY = 10_000;
    private NotificationManager mNotificationManager;
    private static int counter = 0;
    private static boolean stateService;
    private static boolean started = false;
    private static Handler handler = new Handler();
    private static ScreenActionReceiver screenactionreceiver;
    private static String lastState = "";

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Location mLocation;


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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onNewLocation(locationResult.getLastLocation());
            }
        };

        stateService = getIsConnected(getApplicationContext()) ;
        lastState = getLastState(getApplicationContext());
    }

    private void onNewLocation(Location lastLocation) {
        Log.e(TAG, "New location: " + lastLocation);

        mLocation = lastLocation;

        double curLat = 0;
        double curLong = 0;
        try{
//            Log.e(TAG, String.format("%f, %f", mLocation.getLatitude() + 90d, mLocation.getLongitude() + 180d  ) );

            curLat = mLocation.getLatitude() + 90d;
            curLong = mLocation.getLongitude() + 180d;
        }catch (Exception e){
            e.printStackTrace();
        }


        double tarLat = 43.635589 + 90d;
        double tarLong = -79.644247 + 180d;
        double distance = 0.002d;

        String result = "";

        boolean connected = false;

        if( (Math.abs(tarLat - curLat) < distance) && (Math.abs(tarLong - curLong) < distance) ){
            result = "logged_in";
            connected = true;
        }else{
            result = "stepped_out";
            connected = false;
        }

        if( stateService != connected ){
            UtilsTextWriter.write(UtilsTextWriter.getCurrentTimeStamp() + " " + result);
            lastState = UtilsTextWriter.getCurrentTimeStamp() + " " + result;
        }
        mNotificationManager.notify(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

        stateService = connected;

        setIsConnected(getApplicationContext(), stateService);
        setLastState(getApplicationContext(), lastState);

    }

    @Override
    public void onDestroy() {
        setIsConnected(getApplicationContext(), stateService);
        setLastState(getApplicationContext(), lastState);
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
                started = true;

                Log.d(TAG, "Received user starts foreground intent");
                int count = 0;

                createLocationRequest();

                mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());

                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());

                // Start the locker receiver
                screenactionreceiver = new ScreenActionReceiver();

                registerReceiver(screenactionreceiver, screenactionreceiver.getFilter());

                connect();
                break;
            case Constants.ACTION.STOP_ACTION:
                started = false;
                handler.removeCallbacksAndMessages(0);
                unregisterReceiver(screenactionreceiver);
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

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a stop intent
        Intent stopIntent = new Intent(this, MyService.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);

        // if it is connected



        double curLat = 0;
        double curLong = 0;
        try{
//            Log.e(TAG, String.format("%f, %f", mLocation.getLatitude() + 90d, mLocation.getLongitude() + 180d  ) );

            curLat = mLocation.getLatitude() + 90d;
            curLong = mLocation.getLongitude() + 180d;
        }catch (Exception e){
            e.printStackTrace();

        }


        String text = UtilsTextWriter.getCurrentTimeStamp() + "\n" + lastState + "\n" + String.format("%f, %f",curLat,curLong);
        remoteViews.setTextViewText(R.id.tv_state, text);

        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .addAction(R.drawable.ic_launcher, "Cancel",
                        pendingStopIntent)
//                .setContentText(text)
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

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static String getLastState(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LAST_STATE, "first_run");
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setLastState(Context context, String text) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(KEY_LAST_STATE, text)
                .apply();
    }

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
