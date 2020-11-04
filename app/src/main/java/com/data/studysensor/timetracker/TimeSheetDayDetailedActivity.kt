package com.data.studysensor.timetracker


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.data.studysensor.androidoreoforegroundtest.R
import kotlin.collections.ArrayList


class TimeSheetDayDetailedActivity : Activity(), AdapterView.OnItemClickListener {

    private var list: java.util.ArrayList<Int>? = null
    lateinit var listView: ListView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_sheet)

        listView  = findViewById(R.id.listview) as ListView

        var listItems = (application as MyApplication).getTimeEntrys()

        intent?.let{ i ->
            list = i.getIntegerArrayListExtra("hourEntries")
            var stringList = ArrayList<String>()
            for( i in list!! ){
//                UtilsDebug.debug("$i ${listItems.size}")
                stringList.add("$i ${listItems.get(i)}")

            }

            val itemsAdapter: ArrayAdapter<String> =
                    ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stringList)


            listView.adapter = itemsAdapter
            listView.setOnItemClickListener(this)
        }



    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        var listItems = (application as MyApplication).getTimeEntrys()
//        listItems.removeAt(list!![position])

//        (application as MyApplication).save()
        setResult(RESULT_OK)
        finish()
    }

    override fun onBackPressed() {

        setResult(RESULT_OK)
        super.onBackPressed()
    }


}

