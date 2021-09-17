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

package jasuramme.dateapi

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat

abstract class DateRepresentation {

    abstract fun getFormatString(full : Boolean) : String

    abstract fun getCalendar(dateString : String, full: Boolean) : CustomCalendar?

    abstract fun getString(calendar : CustomCalendar, forceZeroes: Boolean) : String

    abstract fun getVerboseString(calendar: CustomCalendar) : String

}

class DateRepresentationDDMMYY(private val delimiter : String, private val suppressZeroes : Boolean) : DateRepresentation(){

    override fun getCalendar(dateString: String, full: Boolean): CustomCalendar? {
        val parts = dateString.split(delimiter)
        if (full && parts.size != 3)
            return null
        if (!full && parts.size != 2)
            return null
        val day = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        if (full) {
            val year = parts[2].toIntOrNull() ?: return null
            return CustomCalendar(day, month, year)
        } else {
            return CustomCalendar(day, month, null)
        }
    }

    override fun getFormatString(full: Boolean): String {
        return if (full)
            "dd${delimiter}MM${delimiter}YYYY"
        else
            "dd${delimiter}MM"
    }

    override fun getString(calendar: CustomCalendar, forceZeroes: Boolean): String {
        var dayPrefix = ""
        var monthPrefix = ""
        if (!suppressZeroes || forceZeroes) {
            if (calendar.day < 10)
                dayPrefix = "0"
            if (calendar.month < 10)
                monthPrefix = "0"
        }

        val dayMonth = dayPrefix + calendar.day.toString() + delimiter + monthPrefix +
                calendar.month.toString()
        return calendar.year?.let {
            dayMonth + delimiter + calendar.year.toString()
        } ?:  dayMonth
    }

    @SuppressLint("SimpleDateFormat")
    override fun getVerboseString(calendar: CustomCalendar): String {
        val c = calendar.calendar
        return if (calendar.year == null) {
            val df = SimpleDateFormat("dd MMMM")
            df.format(c.time)
        } else {
            DateFormat.getDateInstance(DateFormat.LONG).format(c.time)
        }
    }

}


class DateRepresentationYYMMDD(private val delimiter : String, private val suppressZeroes : Boolean) : DateRepresentation(){

    override fun getCalendar(dateString: String, full: Boolean): CustomCalendar? {
        val parts = dateString.split(delimiter)
        if (full && parts.size != 3)
            return null
        if (!full && parts.size != 2)
            return null
        val day = parts[2].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        if (full) {
            val year = parts[0].toIntOrNull() ?: return null
            return CustomCalendar(day, month, year)
        } else {
            return CustomCalendar(day, month, null)
        }
    }

    override fun getFormatString(full: Boolean): String {
        return if (full)
            "YYYY${delimiter}MM${delimiter}dd"
        else
            "MM${delimiter}dd"
    }

    override fun getString(calendar: CustomCalendar, forceZeroes: Boolean): String {
        var dayPrefix = ""
        var monthPrefix = ""
        if (!suppressZeroes || forceZeroes) {
            if (calendar.day < 10)
                dayPrefix = "0"
            if (calendar.month < 10)
                monthPrefix = "0"
        }

        val dayMonth = monthPrefix + calendar.month.toString() + delimiter +
                dayPrefix + calendar.day.toString()
        return calendar.year?.let {
            calendar.year.toString() + delimiter + dayMonth
        } ?:  dayMonth
    }

    @SuppressLint("SimpleDateFormat")
    override fun getVerboseString(calendar: CustomCalendar): String {
        val c = calendar.calendar
        return if (calendar.year == null) {
            val df = SimpleDateFormat("MMMM dd")
            df.format(c.time)
        } else {
            DateFormat.getDateInstance(DateFormat.LONG).format(c.time)
        }
    }

}


class DateRepresentationMMDDYY(private val delimiter : String, private val suppressZeroes : Boolean) : DateRepresentation(){

    override fun getCalendar(dateString: String, full: Boolean): CustomCalendar? {
        val parts = dateString.split(delimiter)
        if (full && parts.size != 3)
            return null
        if (!full && parts.size != 2)
            return null
        val day = parts[1].toIntOrNull() ?: return null
        val month = parts[2].toIntOrNull() ?: return null
        if (full) {
            val year = parts[0].toIntOrNull() ?: return null
            return CustomCalendar(day, month, year)
        } else {
            return CustomCalendar(day, month, null)
        }
    }

    override fun getFormatString(full: Boolean): String {
        return if (full)
            "MM${delimiter}dd${delimiter}YYYY"
        else
            "MM${delimiter}dd"
    }

    override fun getString(calendar: CustomCalendar, forceZeroes: Boolean): String {
        var dayPrefix = ""
        var monthPrefix = ""
        if (!suppressZeroes || forceZeroes) {
            if (calendar.day < 10)
                dayPrefix = "0"
            if (calendar.month < 10)
                monthPrefix = "0"
        }

        val dayMonth = monthPrefix + calendar.month.toString() + delimiter +
                dayPrefix + calendar.day.toString()
        return calendar.year?.let {
            dayMonth + delimiter + calendar.year.toString()
        } ?:  dayMonth
    }

    @SuppressLint("SimpleDateFormat")
    override fun getVerboseString(calendar: CustomCalendar): String {
        val c = calendar.calendar
        return if (calendar.year == null) {
            val df = SimpleDateFormat("MMMM dd")
            df.format(c.time)
        } else {
            DateFormat.getDateInstance(DateFormat.LONG).format(c.time)
        }
    }

}