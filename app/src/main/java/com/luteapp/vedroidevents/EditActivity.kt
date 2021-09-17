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
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_edit.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import jasuramme.dateapi.CustomCalendar


class EditActivity() : AppCompatActivity(),DatePickerDialog.OnDateSetListener,
    AdapterView.OnItemSelectedListener, Parcelable {


    ///> current dialog type
    private var type = CREATE_NEW_EVENT_DIALOG

    private lateinit var event : EventRecord
    private val db = DatabaseHelper(this)

    constructor(parcel: Parcel) : this() {
        type = parcel.readInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        // Create spinner choices
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.events_types, android.R.layout.simple_spinner_dropdown_item )
        typeSpinner.adapter = adapter
        typeSpinner.onItemSelectedListener = this

        Api.settings.let {
            dateEdit.dateFormat = SettingsRecord.Data.getDateTypeString(
                it.getDelimiterString(), it.dateType)
        }

        // If create new dialog
        type = intent.getIntExtra("EDIT_EVENT_TYPE", CREATE_NEW_EVENT_DIALOG)
        if ( type == CREATE_NEW_EVENT_DIALOG ) {
            dateEdit.setText( Api.calendarToString( CustomCalendar.Builder.getCurrent(), true ) )
            imageButton.setImageResource(Pics.plus)
            eventOkButton.setText(R.string.create)
            val currentCal = CustomCalendar.Builder.getCurrent()
            event = EventRecord(null, "", "", "", BIRTHDAY_EVENT_TYPE,
                currentCal, currentCal.getNextNearestDate(), null, false)
        } else {
            event = EventRecord.Builder.fromIntent(intent)
            setEventtype(event.type)
            val dText = Api.calendarToString(event.date, true)
            dateEdit.setText( dText )
            nameEdit.setText( event.name )
            commentsEdit.setText(event.comments )
            imageButton.setImageResource(Pics.getIdByName(event.picture))
            typeSpinner.setSelection(event.type)
            setAppointmentDateLabel()
            if (type == EDIT_EVENT_DIALOG)
                eventOkButton.text = resources.getString(R.string.change)
            else {
                dateEdit.isEnabled = false
                commentsEdit.isEnabled = false
                nameEdit.isEnabled = false
                typeSpinner.isEnabled = false
                imageButton.isEnabled = false
                val blackColor = ContextCompat.getColor(this,R.color.colorBlack)
                dateEdit.setTextColor(blackColor)
                nameEdit.setTextColor(blackColor)
                commentsEdit.setTextColor(blackColor)
                if (event.type == ONCE_EVENT_TYPE && event.date <= CustomCalendar.Builder.getCurrent())
                    eventOkButton.text = resources.getString(R.string.delete)
                else {
                    Api.eventTapped(event, db)
                    eventOkButton.text = resources.getString(R.string.close)
                }
            }
        }
        nameEdit.requestFocus()
        dateEdit.setFilterState(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (type != ACKNOWLEDGE_EVENT_DIALOG) {
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.menu_edit, menu)
        }
        return true
    }

    private fun getDate() = Api.stringToCalendar(
        dateEdit.text.toString(),
        typeSpinner.selectedItemPosition != YEARLY_EVENT_TYPE
    )

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.Menu_delete_id -> {
                event.index?.let {
                    db.deleteEvent(it)
                }
                finish()
                true
            }
            R.id.Menu_event_settings_id -> {
                val intent = Intent(this, EventSettingsActivity::class.java)
                event.date = getDate()?.let { it } ?: CustomCalendar.Builder.getCurrent()
                event.putToIntent(intent)
                startActivityForResult(intent, EVENT_SETTINGS_REQUEST)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    /**
     * Save button click handler
     */
    fun saveClicked(view : View) {
        if (type == ACKNOWLEDGE_EVENT_DIALOG) {
            if (event.type == ONCE_EVENT_TYPE &&
            event.date <= CustomCalendar.Builder.getCurrent()) {
                Api.removeNotification(event, this)
                Api.eventTapped(event, db)
            }
            finish()
            return
        }
        val date = getDate()

        if (date == null || !date.isValid()){
            Api.showSimpleAlert(this,resources.getString(R.string.wrong_date))
            return
        }

        if (nameEdit.text.toString() == "") {
            Api.showSimpleAlert(this,resources.getString(R.string.name_empty))
            return
        }

        if (typeSpinner.selectedItemPosition == ONCE_EVENT_TYPE &&
                date <= CustomCalendar.Builder.getCurrent()
        ) {
            Api.showSimpleAlert(this,resources.getString(R.string.event_date_before_error))
            return
        }

        event.name = nameEdit.text.toString()
        event.date = date
        event.comments = commentsEdit.text.toString()
        event.setShowDate()
        //event.showDate = CustomCalendar.Builder.getCurrent() // костыль для отладки
        if (type == EDIT_EVENT_DIALOG)
            db.updateEvent(event)
        else
            db.addEvent(event)

        Api.startNotificationAlarm(this, db)
        finish()
    }


    /**
     * Opens choose Icon Dialog
     */
    fun chooseIconClicked(view : View) {
        val intent = Intent(this, IconChooseActivity::class.java)
        intent.putExtra("CREATE_NEW", true)
        startActivityForResult(intent, ICON_REQUEST)
    }

    /**
     * Update icon when it is chosen in the another dialog
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ICON_REQUEST) {
            data?.let {
                event.picture = it.getStringExtra("dbString")!!
                val imageId = Pics.getIdByName(event.picture)
                imageButton.setImageResource(imageId)
            }
        } else if (requestCode == EVENT_SETTINGS_REQUEST) {
            data?.let {
                val eventReturn = EventRecord.Builder.fromIntent(it)
                event.yearlyAddAppointment = eventReturn.yearlyAddAppointment
                event.addAppointment = eventReturn.addAppointment
                event.setShowDate()
                setAppointmentDateLabel()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAppointmentDateLabel() {
        if (event.showDate == event.date.getNextNearestDate())
            appointmentDateLabel.text = ""
        else
            appointmentDateLabel.text = resources.getString(R.string.Appointment) +
                    " " + Api.calendarToString(event.showDate) + " " +
                    Api.calendarToTime(event.showDate)
    }

    /**
     * Opens calendar to choose date
     */
    fun calendarClicked(view : View) {
        if (type == ACKNOWLEDGE_EVENT_DIALOG)
            return
        val c = CustomCalendar.Builder.getCurrent()
        val datePicker = DatePickerFragment(this, this, c.year!!,
            c.month, c.day )
        datePicker.show(supportFragmentManager, "datePicker")
    }

    /**
     * Callback that recieves date from calendar date picker
     */
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val c = CustomCalendar(dayOfMonth, month + 1, year)
        dateEdit.setText(Api.calendarToString(c,true))
    }

    /**
     * It's called when event changes. It changes dateEdit when necessary and
     * its format string
     */
    private fun setEventtype(type : Int){
        val text = dateEdit.text.toString()
        if (event.type == type)
            return
        if (type == YEARLY_EVENT_TYPE) {
            var cal = Api.stringToCalendar(text,true)
            if (cal == null) {
                cal = CustomCalendar.Builder.getCurrent()
            }
            cal.year = null
            dateEdit.dateFormat = Api.getRepresentation().getFormatString(false)
            dateEdit.fullDate = false
            dateEdit.setText(Api.calendarToString(cal, true))
        }
        if (event.type == YEARLY_EVENT_TYPE) {
            var cal = Api.stringToCalendar(text,false)
            if (cal == null) {
                cal = CustomCalendar.Builder.getCurrent()
            } else {
                cal.year = CustomCalendar.Builder.getCurrent().year
            }
            dateEdit.dateFormat = Api.getRepresentation().getFormatString(true)
            dateEdit.fullDate = true
            dateEdit.setText(Api.calendarToString(cal, true))
        }
    event.type = type
    }

    /**
     * Callback when event type changed
     */
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        setEventtype(position)
    }

    ///> Don't allow nothing to be selected
    override fun onNothingSelected(parent: AdapterView<*>?) {
        typeSpinner.setSelection(BIRTHDAY_EVENT_TYPE)
    }


    //Sub class to open date picker fragment
    class DatePickerFragment(
        private val pContext : Context, private val listener : DatePickerDialog.OnDateSetListener,
        private val year : Int, private val month : Int, private val day : Int) : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Create a new instance of TimePickerDialog and return it
            return DatePickerDialog(pContext, listener, year, month, day)
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EditActivity> {
        override fun createFromParcel(parcel: Parcel): EditActivity {
            return EditActivity(parcel)
        }

        override fun newArray(size: Int): Array<EditActivity?> {
            return arrayOfNulls(size)
        }
    }
}
