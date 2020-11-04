package com.data.studysensor.timetracker

data class DayEntry(var timeEntries:ArrayList<TimeEntry> = ArrayList(), var totalMinutes:Int = 0, var month:Int = 0, var day:Int = 0, var year:Int = 0, var hourEntries:ArrayList<Int> = ArrayList()) {
}