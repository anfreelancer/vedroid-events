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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import jasuramme.dateapi.CustomCalendar
import java.io.FileInputStream
import java.io.FileOutputStream

class DatabaseHelper(val context : Context, dbName : String = "events.db") : SQLiteOpenHelper(context, dbName,
    null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        reCreate(db)
    }

    fun reCreate(_db :SQLiteDatabase? = null) {
        val db :SQLiteDatabase = _db?.let{ it } ?: this.writableDatabase
        db.execSQL(SQL_DELETE_EVENTS_TABLE)
        db.execSQL(SQL_CREATE_EVENTS_TABLE)
        db.execSQL(SQL_DELETE_SETTINS_TABLE)
        db.execSQL(SQL_CREATE_SETTINGS_TABLE)
        db.execSQL(SQL_INIT_SETTINGS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        reCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        reCreate(db)
    }

    fun saveTo(path : String) {
        try {
            val fi = FileInputStream(context.getDatabasePath("events.db"))
            val fo = FileOutputStream("$path/events.db")
            fi.channel.let {
                it.transferTo(0, it.size(), fo.channel)
                it.close()
                fo.channel.close()
                Toast.makeText(context, "Database exported to $path/events.db", Toast.LENGTH_SHORT).show()
            }
        } catch (e : Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot export database", Toast.LENGTH_SHORT).show()
        }
    }

    fun importFrom(path : String) {
        val db = writableDatabase
        try {
            db.execSQL("ATTACH DATABASE \"${path}\" AS importdb")
            val cursor = db.rawQuery("SELECT * FROM importdb.${EVENTS_TABLE}", null)
            if (cursor.moveToFirst()) {
                db.execSQL("DELETE FROM $EVENTS_TABLE")
                db.execSQL("INSERT INTO $EVENTS_TABLE SELECT * FROM importdb.$EVENTS_TABLE;")
                Toast.makeText(context, "Database $path imported", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Cannot import database", Toast.LENGTH_SHORT).show()
            }
            cursor.close()
            db.execSQL("DETACH DATABASE importdb")
        } catch (e : Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot import database", Toast.LENGTH_SHORT).show()
        }
        db.close()
    }

    fun addEvent(event : EventRecord) : Boolean {
        val db = writableDatabase
        val success = db.insert(EVENTS_TABLE, null, event.getValues())
        db.close()
        return success != (-1).toLong()
    }

    fun acknowledgeEvent(event : EventRecord ) : Boolean {
        val now = CustomCalendar.Builder.getCurrent()
        return if (event.type == ONCE_EVENT_TYPE && event.date <= now)
            event.index?.let{ deleteEvent(it); true} ?: false
        else {
            event.setShowDate()
            updateEvent(event)
        }
    }

    fun deleteEvent(rowId : Long) : Boolean {
        val db = writableDatabase
        val success = db.delete(EVENTS_TABLE, "ROWID=$rowId", null)
        db.close()
        return success != -1
    }

    /**
     * get mutable list of events to show
     *
     * return all events with showdate <= current time
     */
    fun getEventsToBeShown() : MutableList<EventRecord>{
        val ret = mutableListOf<EventRecord>()
        val db = readableDatabase
        val millis = CustomCalendar.Builder.getCurrent().toMillis()!!
        val cursor = db.rawQuery(SQL_SELECT_EVENTS_TO_SHOW + millis , null )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ret.add(EventRecord.Builder.fromCursor(cursor))
            } while (cursor.moveToNext())
        }
        return ret
    }

    fun updateEvent(event : EventRecord) : Boolean {
        val db = writableDatabase
        val success = db.update(EVENTS_TABLE, event.getValues(),
            "ROWID=${event.index}", null)
        db.close()
        return success != -1
    }

    fun updateSettings(settings : SettingsRecord) : Boolean {
        val db = writableDatabase
        val success = db.update(SETTINGS_TABLE, settings.toValues(), null, null)
        db.close()
        return success != -1
    }

    fun getSettings() : SettingsRecord {
        val db = readableDatabase
        try {
            val cursor = db.rawQuery(SQL_SELECT_SETTINGS, null)
            if (cursor != null && cursor.moveToFirst()) {
                db.close()
                return SettingsRecord.Builder.fromCursor(cursor)
            }
        } catch (e : Exception) {
            e.printStackTrace()
        }
        db.close()
        return SettingsRecord(0,0,100, false)
    }

    fun getEvent(rowId: Long) : EventRecord? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "$SQL_SELECT_ALL_EVENTS WHERE ROWID = $rowId",
            null )
        if (cursor != null && cursor.moveToFirst()) {
            db.close()
            return EventRecord.Builder.fromCursor(cursor)
        }
        db.close()
        return null
    }

    fun getNextAppointmentDate() : CustomCalendar {
        val db = readableDatabase
        val now = CustomCalendar.Builder.getCurrent()
        val cursor = db.rawQuery("SELECT SHOWDATE FROM $EVENTS_TABLE" +
                " WHERE SHOWDATE > ${now.toMillis()} ORDER BY SHOWDATE LIMIT 1", null)
                //" ORDER BY SHOWDATE LIMIT 1", null)
        if (cursor != null && cursor.moveToFirst()) {
            val tmp = CustomCalendar.Builder.fromMillis(cursor.getLong(0))
            cursor.close()
            db.close()
            return tmp
        }
        cursor.close()
        db.close()
        now.day = now.day + 1
        return now.getNearestDate()
    }

    fun getAllEvents() : List<EventRecord>{
        val ret = mutableListOf<EventRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "$SQL_SELECT_ALL_EVENTS ORDER BY MONTH,DAY",
            null )
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ret.add(EventRecord.Builder.fromCursor(cursor))
            } while (cursor.moveToNext())
        }
        db.close()
        return ret
    }

    private companion object {
        const val EVENTS_TABLE = "events"
        const val SETTINGS_TABLE = "settings"
        const val SQL_CREATE_SETTINGS_TABLE = "CREATE TABLE $SETTINGS_TABLE (" +
                "DATETYPE INTEGER, " +
                "DELIMITER INTEGER, " +
                "ICONSIZE INTEGER," +
                "SUPPRESSZEROES INTEGER );"
        const val SQL_INIT_SETTINGS_TABLE = "INSERT INTO $SETTINGS_TABLE" +
                "(DATETYPE, DELIMITER, ICONSIZE, SUPPRESSZEROES) VALUES " +
                "(0, 0, 0, 0);"
        const val SQL_DELETE_SETTINS_TABLE = "DROP TABLE IF EXISTS $SETTINGS_TABLE;"
        const val SQL_SELECT_SETTINGS = "SELECT DATETYPE, DELIMITER, ICONSIZE, SUPPRESSZEROES FROM $SETTINGS_TABLE;"
        const val SQL_CREATE_EVENTS_TABLE =
            "CREATE TABLE $EVENTS_TABLE ( " +
                    "NAME TEXT, " +
                    "COMMENTS TEXT, " +
                    "PICTURE TEXT, " +
                    "TYPE INTEGER, " +
                    "DAY INTEGER," +
                    "MONTH INTEGER," +
                    "YEAR INTEGER," +
                    "YEARNA BOOLEAN," +
                    "SHOWDATE INTEGER," +
                    "ADDAPPOINTMENT INTEGER," +
                    "YEARLYADDAPPOINTMENT INTEGER);"
        const val SQL_FIELDS = "ROWID, NAME, COMMENTS, PICTURE, TYPE, DAY, MONTH, YEAR, YEARNA," +
                "SHOWDATE, ADDAPPOINTMENT, YEARLYADDAPPOINTMENT"
        const val SQL_SELECT_ALL_EVENTS = "SELECT $SQL_FIELDS FROM $EVENTS_TABLE"
        const val SQL_SELECT_EVENTS_TO_SHOW = "SELECT $SQL_FIELDS FROM $EVENTS_TABLE WHERE SHOWDATE <= "
        const val SQL_DELETE_EVENTS_TABLE = "DROP TABLE IF EXISTS $EVENTS_TABLE;"
        const val DATABASE_VERSION = 2
    }
}