package com.data.studysensor.androidoreoforegroundtest

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import com.data.studysensor.timetracker.MyApplication
import com.data.studysensor.timetracker.TimeSheetDayActivity
import com.data.studysensor.timetracker.TimeSheetMonthActivity
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class TimeTrackerActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_timetracker)


    }

    override fun onResume() {
        super.onResume()

//        startHoursActivity(null)

    }

    fun sendEmail(view: View) {
        (application as MyApplication).save()

        val filelocation =
            File(Environment.getExternalStoragePublicDirectory(""), MyApplication.EXPORT_FILE)
        val path: Uri = Uri.fromFile(filelocation)
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "vnd.android.cursor.dir/email"
        val to = arrayOf("accounts@trysight.com","brian@issist.com")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to)
        emailIntent.putExtra(Intent.EXTRA_STREAM, path)
        val dateFormat: DateFormat = SimpleDateFormat("MM")
        val date = Date()

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Time Sheet")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    fun goToAccessibilityServices(view: View) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, 0)

    }

    fun startHoursActivity(view: View?) {
        val intent = Intent(this, TimeSheetDayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun startMonthlyHoursActivity(view: View) {
        val intent = Intent(this, TimeSheetMonthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun createEntry(view: View) {
        (application as MyApplication).save()

    }

}