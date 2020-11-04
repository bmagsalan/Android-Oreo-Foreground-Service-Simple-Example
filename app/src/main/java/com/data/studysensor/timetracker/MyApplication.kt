package com.data.studysensor.timetracker

import android.app.Application

class MyApplication:Application {

    lateinit var listItems:MutableList<String>
    var dayEntries:ArrayList<DayEntry> = ArrayList()
    var timeEntries:ArrayList<TimeEntry> = ArrayList()

    constructor(){
        var string = UtilsDebug.read()
        listItems = string.split("\n").toMutableList()

        for( (index, value) in listItems.withIndex()){
            if( value.isNullOrEmpty() ){
                listItems.removeAt(index)
            }
        }

        UtilsDebug.debug("ListItems ${listItems.size}")
    }

    fun getTimeEntrys():MutableList<String>{
        var string = UtilsDebug.read()
        listItems = string.split("\n").toMutableList()

        for( (index, value) in listItems.withIndex()){
            if( value.isNullOrEmpty() ){
                listItems.removeAt(index)
            }
        }

        UtilsDebug.debug("ListItems ${listItems.size}")
        return listItems
    }

    fun save(){
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

        stringItems.add("Date , In , Out , Hours , Minutes")

        for( day in dayEntries ){
            UtilsDebug.debug("${day.year} ${day.month} ${day.day} ${day.timeEntries.size}")

            for( timeEntry in day.timeEntries){
                UtilsDebug.debug("timeEntries: ${timeEntry.year} ${timeEntry.month} ${timeEntry.day} ${timeEntry.hours}")


                if(timeEntry.state == STEPPED_OUT){

                    var startTimeInMinutes = lastEntry!!.hours * 60 + lastEntry!!.minutes
                    var isNewDay = lastEntry.day != timeEntry.day
                    var endTimeInMinutes = 0
                    if( isNewDay )
                    {
                        UtilsDebug.debug("new day")
                        endTimeInMinutes = (timeEntry.hours + 24) * 60 + timeEntry.minutes
                    }else{
                        endTimeInMinutes = timeEntry.hours * 60 + timeEntry.minutes
                    }

                    var elapsedInMinutes = endTimeInMinutes - startTimeInMinutes

                    day.totalMinutes += elapsedInMinutes
                    stringItems.add("${lastEntry.year}-${lastEntry.month}-${lastEntry.day} , ${lastEntry.hours}:${if(lastEntry.minutes < 10) "0${lastEntry.minutes}" else "${lastEntry.minutes}"} , ${timeEntry.hours}:${if(timeEntry.minutes < 10) "0${timeEntry.minutes}" else "${timeEntry.minutes}"} , ${String.format("%d , %.2f", elapsedInMinutes /60, (elapsedInMinutes % 60)/60.0)}")

                }

                day.hourEntries.add(timeEntry.index)
                lastEntry = timeEntry
            }


        }

        var text = ""
        for( str in stringItems ){
            text += "$str\n"
        }
        UtilsDebug.debug(text)
        UtilsDebug.write(text, EXPORT_FILE)
    }

    fun deleteItem(index:Int){
        listItems.removeAt(index)
    }

    companion object{
        val EXPORT_FILE: String = "timesheet.csv"
    }
}