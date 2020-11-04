package com.data.studysensor.timetracker

const val LOGGED_IN = 1
const val STEPPED_OUT = 0
data class TimeEntry(var hours:Int = 0, var minutes:Int = 0, var state:Int = 0, var month:Int = 0, var day:Int = 0, var year:Int = 0, var index:Int = 0)