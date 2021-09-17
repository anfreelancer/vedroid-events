/*
 * This program source code file is part of Vedroid Events application
 *
 * Copyright (C) 2020 Alexander Shuklin jasuramme@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, you may find one here:
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * or you may search the http://www.gnu.org website for the version 2 license,
 * or you may write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */

package com.luteapp.vedroidevents

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.view.isVisible

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.text.SimpleDateFormat
import java.util.*
import jasuramme.dateapi.CustomCalendar


const val EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE"

class MainActivity : AppCompatActivity() {

    data class EventShowField(
        val event: EventRecord,
        val tableRow: TableRow,
        val text: TextView,
        val date: DateField
    )

    data class DateField(val dt: CustomCalendar, val tableRow: TableRow, val text: TextView)

    @SuppressLint("SimpleDateFormat")
    val df = SimpleDateFormat("MMdd")
    private val db = DatabaseHelper(this)
    private val eventsList = mutableListOf<EventShowField>()
    private val dateList = mutableListOf<DateField>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        Api.init(this, db)

        fab.setOnClickListener {
            if (BillingClientSetup.isUpgraded(applicationContext)) {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("EDIT_EVENT_TYPE", CREATE_NEW_EVENT_DIALOG)
                startActivityForResult(intent, EDIT_EVENT)
            }

        }
        updateEvents()
        Api.checkForNotifications(this, db)
        Api.startNotificationAlarm(this, db)
        val searchWatcher = SearchWatcher(this)
        Search.addTextChangedListener(searchWatcher)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    private fun openEditor(event: EventRecord) {
        val intent = Intent(this, EditActivity::class.java)
        event.putToIntent(intent)
        intent.putExtra("EDIT_EVENT_TYPE", EDIT_EVENT_DIALOG)
        startActivityForResult(intent, EDIT_EVENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        updateEvents()
    }

    private fun getDay(date: Date) = df.format(date)

    private fun updateEvents() {
        val today = CustomCalendar.Builder.getCurrent().getDate()
        val afterYear = today.copy().apply { year = year!! + 1 }
        eventsList.clear()
        var gray = false
        BirthdaysList.removeAllViews()
        val events = db.getAllEvents()
        if (events.isEmpty())
            return
        var showDate = CustomCalendar(32, 32, 0)
        lateinit var currentDateRow: DateField
        for (i in 1..3) {
            for (event in events) {
                var tmpDate = event.date.copy()
                tmpDate.year = today.year
                tmpDate = tmpDate.getNearestDate()
                if (event.type == ONCE_EVENT_TYPE && event.date >= afterYear) {
                    if (i != 3)
                        continue
                } else {
                    if (i == 3)
                        continue
                }
                if (i == 1 && tmpDate < today) {
                    continue
                } else if (i == 2 && tmpDate >= today) {
                    continue
                }
                if (showDate != event.showDate) {
                    val tmp = event.showDate.copy()
                    if (i != 3)
                        tmp.year = null
                    val daterow = createDateRow(tmp, gray)
                    BirthdaysList.addView(daterow.tableRow)
                    dateList.add(daterow)
                    currentDateRow = daterow
                }
                showDate = event.showDate
                val tablerow = createRow(event, gray, currentDateRow)
                BirthdaysList.addView(tablerow.tableRow)
                eventsList.add(tablerow)
                gray = !gray
            }
        }
    }

    private fun createDateRow(date: CustomCalendar, gray: Boolean): DateField {
        val row = TableRow(this)
        Api.dpToPixels(5).let {
            row.setPadding(it, 0, 0, 0)
        }
        val field = TextView(this)
        field.text = Api.getRepresentation().getVerboseString(date)
        field.textSize = 14.toFloat()
        row.addView(field)
        setGray(row, gray)
        return DateField(date, row, field)
    }

    @SuppressLint("SetTextI18n")
    fun createRow(event: EventRecord, gray: Boolean, dateField: DateField): EventShowField {
        val row = TableRow(this)
        val vp = Api.dpToPixels(3)
        Api.dpToPixels(16).let {
            row.setPadding(it, vp, it, vp)
        }
        val field = TextView(this)
        when (event.type) {
            BIRTHDAY_EVENT_TYPE -> {
                val lang = Locale.getDefault().language
                val currentYear = CustomCalendar.Builder.getCurrent().year!!
                var years = currentYear - event.date.year!!
                event.showDate.year?.let {
                    if (it > currentYear)
                        years += 1
                }
                if (lang == "ru") {
                    setEventText(event.name, "$years ${getRuYears(years)}", field)
                } else {
                    if (years == 1) {
                        setEventText(
                            event.name,
                            "$years ${resources.getString(R.string.year)}",
                            field
                        )
                    } else {
                        setEventText(
                            event.name,
                            "$years ${resources.getString(R.string.years)}",
                            field
                        )
                    }
                }
            }
            //ONCE_EVENT_TYPE -> setEventText(event.name, date, field)
            else -> field.text = "${event.name}\n"
        }
        field.textSize = 18.toFloat()
        field.setTextColor(Color.BLACK)
        val lp = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER_VERTICAL
        lp.weight = 1.0f
        field.layoutParams = lp
        setGray(row, gray)
        row.addView(field, lp)
        val image = ImageView(this)
        Pics.res[event.picture]?.let {
            image.setImageResource(it)
        }
        val iconSize = Api.settings.getIconSizePixels()
//        row.addView(image, iconSize,iconSize)
        row.setOnClickListener { openEditor(event) }
        val params = TableRow.LayoutParams(iconSize, iconSize)
        params.gravity = Gravity.CENTER_VERTICAL
        image.layoutParams = params
        row.addView(image, params)
        return EventShowField(event, row, field, dateField)
    }

    fun filterEvents(line: String) {
        var gray = false
        for (date in dateList) {
            val index = date.text.text.toString().indexOf(line)
            if (index >= 0) {
                setHighlight(date.text, index, line.length)
                date.tableRow.isVisible = true
            } else {
                date.tableRow.isVisible = false
                removeHighlights(date.text)
            }
        }
        for (event in eventsList) {
            val index = event.text.text.toString().indexOf(line)
            if (index < 0) {
                if (event.date.text.text.toString().contains(line)) {
                    removeHighlights(event.text)
                    setGray(event.tableRow, gray)
                    gray = !gray
                    event.tableRow.isVisible = true
                } else {
                    event.tableRow.isVisible = false
                }
            } else { //index >= 0
                setHighlight(event.text, index, line.length)
                setGray(event.tableRow, gray)
                gray = !gray
                event.tableRow.isVisible = true
                event.date.tableRow.isVisible = true
            }
        }
    }

    private fun setGray(tr: TableRow, gray: Boolean) {
        if (gray)
            tr.setBackgroundColor(Color.argb(10, 0, 0, 0))
        else
            tr.setBackgroundColor(Color.argb(0, 0, 0, 0))
    }

    private fun removeHighlights(tv: TextView) {
        val str = SpannableString(tv.text)
        val spans = str.getSpans(0, str.length, BackgroundColorSpan::class.java)
        for (span in spans)
            str.removeSpan(span)
        tv.text = str
    }

    private fun setHighlight(tv: TextView, from: Int, len: Int) {
        val str = SpannableString(tv.text)
        val spans = str.getSpans(0, str.length, BackgroundColorSpan::class.java)
        for (span in spans)
            str.removeSpan(span)
        str.setSpan(
            BackgroundColorSpan(Color.LTGRAY),
            from,
            from + len,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = str
    }

    private fun getRuYears(years: Int): String {
        if (years == 1 || (years.rem(10) == 1 && years.rem(100) != 11))
            return "год"
        if (years in 5..20)
            return "лет"
        years.rem(10).let {
            return if (it > 4 || it == 0)
                "лет"
            else
                "года"
        }
    }


    private fun setEventText(str1: String, str2: String, tv: TextView) {
        val str = SpannableString(str1 + '\n' + str2)
        str.setSpan(
            ForegroundColorSpan(Color.GRAY),
            str1.length,
            str.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tv.text = str
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                if (!BillingClientSetup.isUpgraded(applicationContext)) return true
                val intent = Intent(this, Settings::class.java)
                startActivityForResult(intent, EVENT_SETTINGS_REQUEST)
                true
            }
            R.id.action_premium -> {
                val intent = Intent(this, ShopActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

class SearchWatcher(val parent: MainActivity) : TextWatcher {

    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        parent.filterEvents(s.toString())
    }
}