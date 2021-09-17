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

import java.lang.Exception
import java.util.*

data class CustomCalendar (val calendar: Calendar, var yearNA : Boolean) {

    constructor( day : Int, month : Int, year : Int?, seconds : Int = 0 ) :
            this(Builder.createCalendar(day, month, year, seconds), year?.let { false } ?: true )

    var day : Int
        get() = calendar.get(Calendar.DAY_OF_MONTH)
        set(value) {
            calendar.set(Calendar.DAY_OF_MONTH, value)
        }

    var month : Int
        get() = calendar.get(Calendar.MONTH) + 1
        set(value) {
            calendar.set(Calendar.MONTH, value - 1)
        }

    var year : Int?
        get() {
            if (yearNA)
                return null
            return calendar.get(Calendar.YEAR)
        }
        set(value) {
            value?.let {
                calendar.set(Calendar.YEAR, it)
                yearNA = false
                return
            }
            calendar.set(Calendar.YEAR, 2000)
            yearNA = true
        }

    var hour : Int
        get() = calendar.get(Calendar.HOUR_OF_DAY)
        set(value) {calendar.set(Calendar.HOUR_OF_DAY, value)}

    var minute : Int
        get() = calendar.get(Calendar.MINUTE)
        set(value) {calendar.set(Calendar.MINUTE, value)}

    var second : Int
        get() = calendar.get(Calendar.SECOND)
        set(value) {calendar.set(Calendar.SECOND, value)}

    override fun toString(): String {
        return "${day}/${month}/${year} ${hour}:${minute}:${second}"
    }

    fun copy() : CustomCalendar{
        return CustomCalendar(calendar.clone() as Calendar, yearNA)
    }

    operator fun compareTo(b : CustomCalendar) : Int{
        if (year == null || b.year == null)
            throw (Exception())
        return calendar.compareTo(b.calendar)
    }

    fun getDate() : CustomCalendar {
        val tmp = this.copy()
        tmp.hour = 0
        tmp.minute = 0
        tmp.second = 0
        return tmp
    }

    fun toMillis() : Long? {
        year?.let {
            return calendar.timeInMillis
        }
        return null
    }

    fun getNearestDate() : CustomCalendar {
        val tmp = this.copy()
        if (year == null) {
            tmp.year = Builder.getCurrent().year
        }
        if (isValid())
            return tmp
        if ( !checkDayValid() ) {
            tmp.month += 1
            tmp.day = 1
        }
        if (tmp.month !in 1..12) {
            tmp.year = tmp.year!! + 1
            tmp.month = 1
            tmp.day = 1
        }
        return tmp
    }

    fun getNextNearestDate() : CustomCalendar {
        val tmp = this.copy()
        val now = Builder.getCurrent()
        if (year == null)
            tmp.year = now.year
        if (tmp <= now)
            tmp.year = now.year
        if (tmp <= now)
            tmp.year = now.year!! + 1
        return tmp.getNearestDate()
    }

    fun isValid() : Boolean
    {
        if (month !in 1..12)
            return false

        return checkDayValid()
    }

    private fun checkDayValid() : Boolean{
        val y = year
        if (y != null && y >= 1600)
        {
            //31 day in these months
            if (month in listOf(1, 3, 5, 7, 8, 10, 12))
                return true

            //30 days
            if (month in listOf(4, 6, 9, 12))
                return day <= 30

            return when {
                y % 400 == 0 -> day <= 29
                y % 100 == 0 -> day <= 28
                y % 4 == 0 -> day <= 29
                else -> day <= 28
            }
        }
        return day in 1 .. 31
    }

    object Builder {
        var forcedCalendar : CustomCalendar? = null

        fun fromInts(day : Int, month : Int, year : Int, hour : Int, minute : Int, second : Int) : CustomCalendar {
            val c = Calendar.getInstance()
            c.set(year,month - 1, day, hour, minute, second)
            c.set(Calendar.MILLISECOND, 0)
            return CustomCalendar(c, false)
        }

        fun createCalendar(day : Int, month : Int, year : Int?, seconds: Int = 0) : Calendar {
            return if (year == null) {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.set(2000, month - 1, day, 0, 0, seconds)
                calendar
            } else {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.set(year, month - 1, day , 0, 0, seconds)
                calendar
            }
        }

        fun getCurrent() : CustomCalendar {
            val calendar = forcedCalendar?.let { it } ?: CustomCalendar(Calendar.getInstance(), false)
            calendar.calendar.set(Calendar.MILLISECOND, 0)
            return calendar
        }

        fun fromMillis(millis : Long) : CustomCalendar {
            val c = Calendar.getInstance()
            c.time = Date(millis)
            return CustomCalendar(c, false)
        }

        fun fromAndroidCalendar(calendar : Calendar) : CustomCalendar {
            return CustomCalendar(calendar, false)
        }
    }
}