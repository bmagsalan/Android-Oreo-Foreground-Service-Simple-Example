package com.data.studysensor.timetracker


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.data.studysensor.androidoreoforegroundtest.R
import kotlin.collections.ArrayList

const val REQ_CODE_DETAILED_HOURS = 1

class TimeSheetDayActivity : Activity(), AdapterView.OnItemClickListener {

    lateinit var listView: ListView
    var dayEntries:ArrayList<DayEntry> = ArrayList()
    var timeEntries:ArrayList<TimeEntry> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_sheet)

        listView  = findViewById(R.id.listview) as ListView


        var listItems = (application as MyApplication).getTimeEntrys()



        for( (index, str) in listItems.withIndex() ){
            var entry = str.split(" ")
            var timeEntry:TimeEntry? = null
            var month = entry[0].split("-")[1].toInt()
            var day = entry[0].split("-")[2].toInt()
            var year = entry[0].split("-")[0].toInt()
            timeEntry = TimeEntry(entry[1].toInt() , entry[2].toInt() , if( entry[3].contains("in") ) LOGGED_IN else STEPPED_OUT, month, day, year, index)
            timeEntries.add(timeEntry)
        }

        var stringItems = ArrayList<String>()

        var lastEntry:TimeEntry? = null
        var dayEntry = DayEntry()
        for( entry in timeEntries){
            lastEntry?.let{ last ->
                if( entry.day != lastEntry!!.day ){
                    dayEntry.year = lastEntry!!.year
                    dayEntry.day = lastEntry!!.day
                    dayEntry.month = lastEntry!!.month
                    dayEntries.add( dayEntry )
                    dayEntry = DayEntry()
                }
            }
            dayEntry.timeEntries.add(entry)

            lastEntry = entry
        }

        lastEntry?.let{
            dayEntry.year = it.year
            dayEntry.day = it.day
            dayEntry.month = it.month
            dayEntries.add( dayEntry )

        }

        lastEntry = null

        for( day in dayEntries ){
            UtilsDebug.debug("${day.year} ${day.month} ${day.day} ${day.timeEntries.size}")
            for( timeEntry in day.timeEntries){
                UtilsDebug.debug("timeEntries: ${timeEntry.year} ${timeEntry.month} ${timeEntry.day} ${timeEntry.hours}")


                if(timeEntry.state == STEPPED_OUT){

                    var startTimeInMinutes = lastEntry!!.hours * 60 + lastEntry!!.minutes
                    var isNewDay = lastEntry.day != timeEntry.day
                    var endTimeInMinutes = 0
//                    if( isNewDay )
//                    {
//                        UtilsDebug.debug("new day")
//                        endTimeInMinutes = (timeEntry.hours + 24) * 60 + timeEntry.minutes
//                    }else{
                        endTimeInMinutes = timeEntry.hours * 60 + timeEntry.minutes
//                    }

                    var elapsedInMinutes = endTimeInMinutes - startTimeInMinutes

                    day.totalMinutes += elapsedInMinutes
                    stringItems.add("${lastEntry.year}-${lastEntry.month}-${lastEntry.day} ${lastEntry.hours}:${if(lastEntry.minutes < 10) "0${lastEntry.minutes}" else "${lastEntry.minutes}"} ${timeEntry.hours}:${if(timeEntry.minutes < 10) "0${timeEntry.minutes}" else "${timeEntry.minutes}"} ${String.format("%dh %dm", elapsedInMinutes /60, elapsedInMinutes % 60)}")

                }

                day.hourEntries.add(timeEntry.index)
                lastEntry = timeEntry
            }

//            stringItems.add("${day.year}-${day.month}-${day.day} -> ${day.totalMinutes /60}h ${day.totalMinutes % 60}m")
        }





        val itemsAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringItems)


        listView.adapter = itemsAdapter
        listView.setOnItemClickListener(this)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

        val text = listView.getItemAtPosition(position)

        UtilsDebug.debug("${text}")
//        var intent = Intent(this, TimeSheetDayDetailedActivity::class.java)
//        intent.putIntegerArrayListExtra("hourEntries", dayEntries.get(position).hourEntries)
//        startActivityForResult(intent, REQ_CODE_DETAILED_HOURS)
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode == REQ_CODE_DETAILED_HOURS ){
            if( resultCode == RESULT_OK ){
                UtilsDebug.debug("Result: $resultCode")
                recreate()
            }

        }
    }

}

