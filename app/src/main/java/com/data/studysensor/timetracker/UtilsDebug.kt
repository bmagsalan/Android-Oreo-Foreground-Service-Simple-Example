package com.data.studysensor.timetracker

import android.os.Environment
import android.text.format.Time
import android.util.Log
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

object UtilsDebug {
    private const val TAG = "UtilsTimer"
    var FILENAME = "DEFAULT.txt"
    val DATE_FORMAT = "yyyy-MM-dd HH mm"

    @Synchronized
    fun debug( text: String?) {
        try {
            Log.e(TAG, text)
        } catch (e: Exception) {
            // TODO: handle exception
            Log.e(TAG, "debugFailed")
        }
    }


    fun writeWithStamp(text: String) {
        val now = Time()
        now.setToNow()
        val mFilewriter: FileWriter
        val mFile: File
        try {
            mFile = File(
                    Environment.getExternalStoragePublicDirectory(""),
                    FILENAME
            )
            val path = File(mFile.parent)
            if (!path.exists()) path.mkdirs()
            if (mFile.exists()) {
                mFilewriter = FileWriter(mFile, true)
            } else {
                mFilewriter = FileWriter(mFile)
                mFile.createNewFile()
            }
            mFilewriter.write("$currentTimeStamp $text\n")
            mFilewriter.flush()
            mFilewriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            debug(e.message)
        }
    }
    fun write(text: String, filename: String) {
        val now = Time()
        now.setToNow()
        val mFilewriter: FileWriter
        val mFile: File
        try {
            mFile = File(
                    Environment.getExternalStoragePublicDirectory(""),
                    filename
            )
            val path = File(mFile.parent)
            if (!path.exists()) path.mkdirs()
            if (mFile.exists()) {
                mFilewriter = FileWriter(mFile, false)
            } else {
                mFilewriter = FileWriter(mFile)
                mFile.createNewFile()
            }
            mFilewriter.write("$text")
            mFilewriter.flush()
            mFilewriter.close()
        } catch (e: IOException) {
            e.printStackTrace()
            debug(e.message)
        }
    }



    fun read():String {
        val now = Time()
        now.setToNow()
        val mFilewriter: FileWriter
        val mFile: File
        try {
            mFile = File(
                Environment.getExternalStoragePublicDirectory(""),
                FILENAME
            )
            val path = File(mFile.parent)

            if (mFile.exists()) {
                //Read text from file

                //Read text from file
                val text = StringBuilder()

                    val br = BufferedReader(FileReader(mFile))
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        text.append(line)
                        text.append('\n')
                    }
                    br.close()

                return text.toString()
            } else {
                UtilsDebug.debug("File doesn't exist")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            debug(e.message)
        }
        return ""
    }

    fun delete() {
        val mFile: File
        try {
            mFile = File(
                Environment.getExternalStoragePublicDirectory(""),
                FILENAME
            )
            val path = File(mFile.parent)
            if (!path.exists()) path.mkdirs()
            if (mFile.exists()) {
                mFile.delete()
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }// Find todays date

    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    val currentTimeStamp: String?
        get() = try {
            val dateFormat =
                SimpleDateFormat(DATE_FORMAT)
            dateFormat.format(Date())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }


}