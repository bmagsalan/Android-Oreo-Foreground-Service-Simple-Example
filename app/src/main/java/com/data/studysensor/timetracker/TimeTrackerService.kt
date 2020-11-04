package com.data.studysensor.timetracker

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import java.util.*

/**
 * Service to catch every notification
 *
 * @author ISSIST
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class TimeTrackerService : AccessibilityService() {

    val LOG_TAG = TimeTrackerService::class.java.simpleName

    private var tempMenitor: Timer? = null
    var lastState:Boolean = false
    var debugCounter:Int = 0

    lateinit var safeSoundPool:SafeSoundPool

    override fun onServiceConnected() {
        super.onServiceConnected()

        safeSoundPool = SafeSoundPool.getSoundPool(applicationContext)

        startTempMonitory()

        UtilsDebug.debug("Service connected")
    }

    private fun startTempMonitory() {
        if (tempMenitor != null) {
            tempMenitor!!.cancel()
            tempMenitor = null
        }
        tempMenitor = Timer()
        tempMenitor!!.schedule(object : TimerTask() {
            override fun run() {
                val wifiMgr: WifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                var startScan = wifiMgr.startScan()
                if(startScan){
                    val scanResults: List<ScanResult> = wifiMgr.getScanResults()

                    var connected = false
                    for( (index, value) in scanResults.withIndex()){
                        UtilsDebug.debug("$index ${value.SSID}")
                        if( value.SSID.contains("Acanac") && value.SSID.contains("_05374") ) {
                            connected = true
                            break
                        }
                    }


                    if( connected != lastState ){
                        var current = if( connected ) "logged_in" else "stepped_out"

                        UtilsDebug.debug("${current}")
                        UtilsDebug.writeWithStamp("${current}")
                        safeSoundPool.playShutdownSound()
                    }

                    lastState = connected
                }



            }
        }, 0, 60_000)
    }

    override fun onInterrupt() {}

    /**
     * Constructor
     */
    @SuppressLint("ServiceCast")
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

    }


}