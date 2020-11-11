package com.data.studysensor.androidoreoforegroundtest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import com.data.studysensor.timetracker.MyApplication;
import com.data.studysensor.timetracker.TimeSheetDayActivity;
import com.data.studysensor.timetracker.TimeSheetMonthActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }

        }

        Button startButton = findViewById(R.id.button1);


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent startIntent = new Intent(MainActivity.this, MyService.class);
                startIntent.setAction(Constants.ACTION.START_ACTION);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startIntent);
                }else{
                    startService(startIntent);
                }


            }
        });

        Intent intent = getIntent();

        if( intent != null ){
            String action = intent.getAction();
            if( action != null){
                if( action.contains(ScreenActionReceiver.START_GPS_SERVICE)){
                    Intent startIntent = new Intent(MainActivity.this, MyService.class);
                    startIntent.setAction(Constants.ACTION.START_ACTION);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(startIntent);
                    }else{
                        startService(startIntent);
                    }

                    finish();
                }

            }


        }else{


        }





    }

    public void sendEmail(View view) {
                ((MyApplication)getApplication()).save();
         StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        File filelocation = new File(Environment.getExternalStoragePublicDirectory(""), "timesheet.csv");
        Uri path  = Uri.fromFile(filelocation);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("vnd.android.cursor.dir/email");
        String[] to = new String[]{"brian@issist.com"};
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_STREAM, path);

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Time Sheet");
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }


    public void startHoursActivity(View view) {
        Intent intent = new Intent(this, TimeSheetDayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startMonthlyHoursActivity(View view) {
        Intent intent = new Intent(this, TimeSheetMonthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }


}
