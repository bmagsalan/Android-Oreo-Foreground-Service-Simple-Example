package com.data.studysensor.androidoreoforegroundtest;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }


        }

        Button startButton = findViewById(R.id.button1);
        Button stopButton = findViewById(R.id.button2);


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

                Intent intent = new Intent(MainActivity.this, TimeTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                finish();
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stopIntent = new Intent(MainActivity.this, MyService.class);
                stopIntent.setAction(Constants.ACTION.STOP_ACTION);
                startService(stopIntent);


            }
        });
    }


    public static WifiManager getWifiManager(Context context) {
        WifiManager wifiManager = null;
        try {

            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return wifiManager;
    }

    public static List<ScanResult> getWifiScanResults(Boolean sorted, Context context) {
        WifiManager wifiManager = getWifiManager(context);
        List<ScanResult> wifiList = wifiManager.getScanResults();

        //Remove results with empty ssid
        List<ScanResult> wifiListNew = new ArrayList<>();
        for (ScanResult scanResult : wifiList) {
            if (!scanResult.SSID.equals(""))
                wifiListNew.add(scanResult);
        }
        wifiList.clear();
        wifiList.addAll(wifiListNew);

        return wifiList;
    }
}
