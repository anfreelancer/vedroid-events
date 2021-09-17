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
import android.app.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import jasuramme.dateapi.CustomCalendar
import kotlinx.android.synthetic.main.activity_event_settings.*

//Sub class to open date picker fragment
class TimePickerFragment(
    private val listener: TimePickerDialog.OnTimeSetListener, private val hour: Int,
    private val minute: Int
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(context, listener, hour, minute, true)
    }

}

class EventSettingsActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener,
    TimePickerDialog.OnTimeSetListener {

    private lateinit var event : EventRecord

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_settings)

        event = EventRecord.Builder.fromIntent(this.intent)
        setAdditionalAppointmentState(event.addAppointment != null)
        useAdditionalAppointment.setOnCheckedChangeListener { _, isChecked ->
            setAdditionalAppointmentState(isChecked)
        }
        everyYearCheckbox.setOnCheckedChangeListener { _, isChecked ->
            onYearlyClicked(isChecked)
        }

        if (event.addAppointment == null) {
            dateView.text = Api.calendarToString( event.date.getNextNearestDate() )
            timeView.text = "00:00"
        } else {
            if ( event.yearlyAddAppointment )
                event.addAppointment!!.yearNA = true
            dateView.text = Api.calendarToString( event.addAppointment!!)
            timeView.text = Api.calendarToTime(event.addAppointment!!)
        }

        everyYearCheckbox.isChecked = event.yearlyAddAppointment
    }


    private fun setAdditionalAppointmentState(state : Boolean){
        useAdditionalAppointment.isChecked = state
        if (!state) {
            appointmentLabel.setTextColor(ContextCompat.getColor(this, R.color.colorInhibit))
            dateView.setTextColor(ContextCompat.getColor(this, R.color.colorInhibit))
            timeView.setTextColor(ContextCompat.getColor(this, R.color.colorInhibit))
            everyYearCheckbox.setTextColor(ContextCompat.getColor(this, R.color.colorInhibit))
            calendarButton.isEnabled = false
            timeButton.isEnabled = false
            everyYearCheckbox.isEnabled = false
        } else {
            appointmentLabel.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            dateView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            timeView.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            everyYearCheckbox.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            calendarButton.isEnabled = true
            timeButton.isEnabled = true
            everyYearCheckbox.isEnabled = true
        }
    }

    fun timeClicked(view : View) {
        val str = timeView.text.toString().split(":")
        val timePicker = TimePickerFragment(this, str[0].toInt(), str[1].toInt())
        timePicker.show(supportFragmentManager, "timePicker")
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        var hPrefix =""
        if (hourOfDay < 10) hPrefix = "0"
        var mPrefix =""
        if (minute < 10) mPrefix = "0"
        timeView.text = "${hPrefix}${hourOfDay}:${mPrefix}${minute}"
    }

    private fun getDate() = Api.stringToCalendar(dateView.text.toString(), !everyYearCheckbox.isChecked)

    fun calendarClicked(view : View) {
        val c = getDate()?.getNextNearestDate()
            ?: CustomCalendar.Builder.getCurrent()
        val datePicker = EditActivity.DatePickerFragment(
            this, this, c.year!!, c.month - 1, c.day
        )
        datePicker.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = CustomCalendar(dayOfMonth, month + 1, year)
        if (everyYearCheckbox.isChecked)
            c.yearNA = true
        dateView.text = Api.calendarToString(c)
    }

    private fun onYearlyClicked(state : Boolean) {
        if (state) {
            val c = Api.stringToCalendar(dateView.text.toString(), true)
            c?.let {
                it.yearNA = true
                dateView.text = Api.calendarToString(c)
            }
        } else {
            val c = Api.stringToCalendar(dateView.text.toString(), false)
            c?.let {
                it.yearNA = false
                it.year = CustomCalendar.Builder.getCurrent().year
                dateView.text = Api.calendarToString(c)
            }
        }
    }

    override fun onBackPressed() {
        saveClicked()
        super.onBackPressed()
    }

    private fun saveClicked() {
        val intent = Intent()
        val yearlyRepeat = everyYearCheckbox.isChecked
        if (useAdditionalAppointment.isChecked) {
            val date = getDate()?.let{
                if (yearlyRepeat)
                    it.getNextNearestDate()
                else
                    it.getNearestDate()
            } ?: return

            val time = timeView.text.toString().split(":")
            date.hour = time[0].toInt()
            date.minute = time[1].toInt()
            if (!yearlyRepeat && date < Api.currentTime) {
                Api.showSimpleAlert(
                   this,
                   resources.getString(R.string.event_appointment_before_error)
                )
                return
            }

            event.yearlyAddAppointment = everyYearCheckbox.isChecked
            event.addAppointment = if (yearlyRepeat) date.getNextNearestDate() else date
        } else {
            event.addAppointment = null
            event.yearlyAddAppointment = false
        }
        event.putToIntent(intent)
        setResult(RESULT_OK, intent)
        finish()
    }
}
