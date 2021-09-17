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

import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import jasuramme.dateapi.CustomCalendar

data class EventRecord(val index : Long?,
                       var name : String,
                       var comments : String,
                       var picture : String,
                       var type : Int,
                       var date : CustomCalendar,
                       var showDate : CustomCalendar,
                       var addAppointment : CustomCalendar?,
                       var yearlyAddAppointment : Boolean
){

    fun putToIntent(intent: Intent){
        intent.putExtra("index", index)
        intent.putExtra("name", name)
        intent.putExtra("comments", comments)
        intent.putExtra("picture", picture)
        intent.putExtra("type", type)
        intent.putExtra("day", date.day)
        intent.putExtra("month", date.month)
        intent.putExtra("showDate", showDate.toMillis())
        if (date.year == null)
            intent.putExtra("yearNA", true)
        else
            intent.putExtra("year", date.year!!)
        intent.putExtra( "addAppointment", addAppointment?.toMillis() ?: 0)
        intent.putExtra("yearlyAddAppointment", yearlyAddAppointment)
    }

    fun getValues() : ContentValues {
        val values = ContentValues()
        values.put("NAME", name)
        values.put("COMMENTS", comments)
        values.put("PICTURE", picture)
        values.put("TYPE", type)
        values.put("DAY", date.day)
        values.put("MONTH", date.month)
        values.put("YEAR", date.year?.let { it } ?: 0)
        values.put("YEARNA", date.year?.let { 0 } ?: 1)
        values.put("SHOWDATE", showDate.toMillis()?.let { it } ?: 0)
        values.put("ADDAPPOINTMENT", addAppointment?.toMillis() ?: 0)
        values.put("YEARLYADDAPPOINTMENT", yearlyAddAppointment)
        return values
    }

    fun setShowDate(){
        val nextDate = date.getNextNearestDate()
        val now = CustomCalendar.Builder.getCurrent()
        if(addAppointment?.let { it <= now && !yearlyAddAppointment } != false) {
            // If single additional appointment is set, but it is already past, or no additional appointment set
            showDate = nextDate
            return
        } else
            addAppointment?.let {
                var tmp = it
                if (yearlyAddAppointment) {
                    tmp.year = 1600
                    tmp = it.getNextNearestDate()
                }
                showDate = if (nextDate < tmp && nextDate.getDate() != tmp.getDate())
                    nextDate
                else
                    tmp
            }
    }

    object Builder{
        fun fromIntent(intent : Intent) : EventRecord{
            val day = intent.getIntExtra("day", 0)
            val month = intent.getIntExtra("month", 0)
            var year : Int? = null
            year = intent.getIntExtra("year", 0)
            if (intent.getBooleanExtra("yearNA",false)){
                year = null
            }
            var addApp : CustomCalendar? = null
            val addAppointmentNum = intent.getLongExtra("addAppointment", 0)
            if (addAppointmentNum != 0.toLong())
                addApp = CustomCalendar.Builder.fromMillis(addAppointmentNum)



            return EventRecord(
                index = intent.getLongExtra("index", 0),
                name = intent.getStringExtra( "name").let{ it } ?: "",
                comments = intent.getStringExtra("comments").let{ it } ?: "",
                picture = intent.getStringExtra("picture").let{ it } ?: "",
                type = intent.getIntExtra("type", 0),
                date = CustomCalendar(day, month, year),
                showDate = CustomCalendar.Builder.fromMillis(intent.getLongExtra("showDate", 0)),
                addAppointment = addApp,
                yearlyAddAppointment = intent.getBooleanExtra("yearlyAddAppointment", false)
            )
        }

        fun fromCursor(cursor : Cursor, ignoreShowDate : Boolean = false) : EventRecord {
            val day = cursor.getInt(5)
            val month = cursor.getInt(6)
            var year : Int? = cursor.getInt(7)
            if (cursor.getInt(8) != 0) {
                year = null
            }
            val showDate : CustomCalendar = if (ignoreShowDate) {
                CustomCalendar(0,0,0)
            } else {
                CustomCalendar.Builder.fromMillis(cursor.getLong(9))
            }

            var addApp : CustomCalendar? = null
            val addAppointmentNum = cursor.getLong(10)
            if (addAppointmentNum != 0.toLong())
                addApp = CustomCalendar.Builder.fromMillis(addAppointmentNum)

            return EventRecord(
                index = cursor.getLong(0),
                name = cursor.getString(1),
                comments = cursor.getString(2),
                picture = cursor.getString(3),
                type = cursor.getInt(4),
                date = CustomCalendar(day, month, year),
                showDate = showDate,
                addAppointment = addApp,
                yearlyAddAppointment = cursor.getInt(11) != 0
            )
        }
    }
}