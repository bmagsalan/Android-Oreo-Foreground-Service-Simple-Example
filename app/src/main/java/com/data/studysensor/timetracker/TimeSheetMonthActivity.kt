package com.data.studysensor.timetracker

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.data.studysensor.androidoreoforegroundtest.R

class TimeSheetMonthActivity : Activity() {

    lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_sheet_month)

        listView  = findViewById(R.id.listview_month) as ListView

        var string = UtilsDebug.read()
        string = string.replace(";", "")

        var listItems = string.split("\n").toMutableList()

        for( (index, value) in listItems.withIndex()){
            if( value.isNullOrEmpty() ){
                listItems.removeAt(index)
            }
        }

        var dayEntries:ArrayList<DayEntry> = ArrayList()
        var timeEntries:ArrayList<TimeEntry> = ArrayList()

        for( str in listItems ){
            var entry = str.split(" ")
            var timeEntry:TimeEntry? = null
            var month = entry[0].split("-")[1].toInt()
            var day = entry[0].split("-")[2].toInt()
            var year = entry[0].split("-")[0].toInt()
            timeEntry = TimeEntry(entry[1].toInt() , entry[2].toInt() , if( entry[3].contains("in") ) LOGGED_IN else STEPPED_OUT, month, day, year)
            timeEntries.add(timeEntry)
        }

        listItems.clear()


        var lastEntry:TimeEntry? = null
        var dayEntry = DayEntry()
        for( entry in timeEntries){
            lastEntry?.let{ last ->
                if( entry.day != lastEntry!!.day ){
                    dayEntries.add( dayEntry )
                    dayEntry = DayEntry()
                }
            }
            dayEntry.timeEntries.add(entry)

            lastEntry = entry
        }
        dayEntries.add( dayEntry )




        var getFirstLoginEntry:TimeEntry? = null
        var getLastOutEntry:TimeEntry? = null


        for( day in dayEntries ){
            for( entries in day.timeEntries){
                if( entries.state == LOGGED_IN ){
                    getFirstLoginEntry = entries;
                    break
                }
            }

            for( n in (day.timeEntries.size - 1) downTo 0 ){
                if( day.timeEntries[n].state == STEPPED_OUT){
                    getLastOutEntry = day.timeEntries[n];
                    break
                }
            }

            if( getFirstLoginEntry != null && getLastOutEntry != null ){
                var startTimeInMinutes = getFirstLoginEntry.hours * 60 + getFirstLoginEntry.minutes
                var endTimeInMinutes = getLastOutEntry.hours * 60 + getLastOutEntry.minutes
                var elapsedInMinutes = endTimeInMinutes - startTimeInMinutes
                day.totalMinutes = elapsedInMinutes
                day.month = getLastOutEntry.month
            }
        }

        var lastDayEntry:DayEntry? = null

        var totalMinutesPerMonth:Int = 0
        for( day in dayEntries ){
            if( lastDayEntry == null ){
                lastDayEntry = day
                totalMinutesPerMonth += day.totalMinutes
                continue
            }else{
                if( day.month == lastDayEntry.month ){
                    totalMinutesPerMonth += day.totalMinutes
                }else{

                    listItems.add("${lastDayEntry.month} -> ${String.format("%dh %dm", totalMinutesPerMonth/60, totalMinutesPerMonth % 60)}")

                    totalMinutesPerMonth = 0
                    totalMinutesPerMonth += day.totalMinutes
                }
            }

            lastDayEntry = day
        }

        listItems.add("${lastDayEntry!!.month} -> ${String.format("%dh %dm", totalMinutesPerMonth/60, totalMinutesPerMonth % 60)}")


        val itemsAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems)

        listView.adapter = itemsAdapter
    }
}
