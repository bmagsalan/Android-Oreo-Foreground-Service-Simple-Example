package com.data.studysensor.androidoreoforegroundtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class ScreenActionReceiver extends BroadcastReceiver {

    public static final String START_GPS_SERVICE = "START_GPS_SERVICE";
    private String TAG = "ScreenActionReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {


        //LOG
        StringBuilder sb = new StringBuilder();
        sb.append("Action: " + intent.getAction() + "\n");
        sb.append("URI: " + intent.toUri(Intent.URI_INTENT_SCHEME).toString() + "\n");
        String log = sb.toString();
        Log.e(TAG, log);
//        Toast.makeText(context, log, Toast.LENGTH_LONG).show();







        String action = intent.getAction();

        if(Intent.ACTION_SCREEN_ON.equals(action))
        {
            Log.e(TAG, "screen is on...");
//            Toast.makeText(context,"screen ON",Toast.LENGTH_LONG);

            //Run the locker

//            context.startService(new Intent(context, FloatingWindow.class));


        }

        else if(Intent.ACTION_SCREEN_OFF.equals(action))
        {
            Log.e(TAG, "screen is off...");
//            Toast.makeText(context,"screen OFF",Toast.LENGTH_LONG);

        }

        else if(Intent.ACTION_USER_PRESENT.equals(action))
        {
            Log.e(TAG, "screen is unlock...");
//            Toast.makeText(context,"screen UNLOCK",Toast.LENGTH_LONG);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(new Intent(context, FloatingWindow.class));
//            } else {
//                context.startService(new Intent(context, FloatingWindow.class));
//            }


        }

        else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
            Log.e(TAG, "boot completed...");
//            Toast.makeText(context,"BOOTED..",Toast.LENGTH_LONG);
            //Run the locker
/*            Intent i = new Intent(context, FloatingWindow.class);
            context.startService(i);

*/
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(new Intent(context, FloatingWindow.class));
//            } else {
//                context.startService(new Intent(context, FloatingWindow.class));

//            }

            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(START_GPS_SERVICE);
            context.startActivity(i);
        }
        else{
            Log.e(TAG, action);
        }

    }

    public IntentFilter getFilter(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        return filter;
    }

}