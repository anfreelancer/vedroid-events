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
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.DisplayMetrics
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import jasuramme.dateapi.CustomCalendar
import jasuramme.dateapi.DateRepresentation
import java.text.SimpleDateFormat
import java.util.*

const val ICON_REQUEST = 1
const val EDIT_EVENT = 2
const val EVENT_SETTINGS_REQUEST = 3

const val CREATE_NEW_EVENT_DIALOG = 1
const val EDIT_EVENT_DIALOG = 2
const val ACKNOWLEDGE_EVENT_DIALOG = 3

const val UNDEFINED_EVENT_TYPE = -1
const val BIRTHDAY_EVENT_TYPE = 0
const val YEARLY_EVENT_TYPE = 1
const val ONCE_EVENT_TYPE = 2


object Api {
    private const val channelId = "VedroidEvents-VedroidEvents"
    private var dpCoeff: Float? = null
    private var notifyChannelConfigured = false
    private var notificationAlarmSet = false
    private lateinit var dateRep : DateRepresentation
    lateinit var settings : SettingsRecord


    val currentTime : CustomCalendar
        get() = CustomCalendar.Builder.getCurrent()

    fun getRepresentation() : DateRepresentation {
        return dateRep
    }

    private fun setDateRep() {
        dateRep = SettingsRecord.Data.getRepresentator(
            settings.getDelimiterString(),
            settings.suppressZeroes,
            settings.dateType
            )
    }

    fun readSettings(db : DatabaseHelper) {
        settings = db.getSettings()
        setDateRep()
    }

    fun init(aContext: Context, db : DatabaseHelper) {
        readSettings(db)
        dpCoeff =
            aContext.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT
    }

    fun dpToPixels(dp: Int): Int {
        return (dp * dpCoeff!!).toInt()
    }

    fun getDateFormat(): String {
        return "dd/MM/yyyy"
    }

    @SuppressLint("SimpleDateFormat")
    fun calendarToTime(calendar : CustomCalendar) : String {
        return SimpleDateFormat("HH:mm").format(calendar.calendar.time)
    }

    fun showSimpleAlert(context: Context, text: String) {
        val alert = AlertDialog.Builder( context )
        alert.setMessage(text)
            .setCancelable(false)
            .setPositiveButton("OK", null)
        alert.create().show()
    }

    fun calendarToString(calendar : CustomCalendar, forceZeroes : Boolean = false)
            = getRepresentation().getString(calendar, forceZeroes)

    fun calendarToVerboseString(calendar : CustomCalendar) =
        getRepresentation().getVerboseString(calendar)

    fun stringToCalendar(string : String, full : Boolean) = getRepresentation().getCalendar(string, full)

    private fun createNotificationChannel(context: Context) {
        if (notifyChannelConfigured)
            return
        notifyChannelConfigured = true
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "VedroidEvents"
            val descriptionText = "Vedroid Events"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager =
                getSystemService(context, NotificationManager::class.java) as NotificationManager
            // Register the channel with the system
            //val notificationManager: NotificationManager = getSystemService() as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun removeNotification(event: EventRecord, context: Context){
        //if (!notifyChannelConfigured)
        //    createNotificationChannel(context)
        event.index?.let {
            NotificationManagerCompat.from(context).cancel("Vedroid Events", it.toInt())
        }
    }

    private fun createNotification(event: EventRecord, context: Context) {
        if (!notifyChannelConfigured)
            createNotificationChannel(context)

        // Action for click
        val clickIntent = Intent(context, EditActivity::class.java)
        clickIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        clickIntent.putExtra("EDIT_EVENT_TYPE", ACKNOWLEDGE_EVENT_DIALOG)
        event.putToIntent(clickIntent)
        val clickPendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Action for swipe off
        val removeIntent = Intent (context, MainService::class.java)
        removeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        event.putToIntent(removeIntent)
        val removePendingIntent: PendingIntent = PendingIntent.getService(context,
            0, removeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        // We don't cancel on once swipe event
        var autoCancel = true
        if (event.type == ONCE_EVENT_TYPE)
            autoCancel = false

        var pic = R.drawable.whitecross
        if (event.picture != "")
            pic = Pics.getIdByName(event.picture)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(pic)
            .setContentTitle("Vedroid Events")
            .setContentText(
                event.name +
                    "\n${getRepresentation().getString(event.date, false)}")
            .setContentIntent(clickPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDeleteIntent(removePendingIntent)
            .setAutoCancel(autoCancel)
        val a = NotificationManagerCompat.from(context)
        event.index?.let {
            a.notify("Vedroid Events", it.toInt(), builder.build())
        }
    }

    fun checkForNotifications(context : Context, db : DatabaseHelper) {
        val events = db.getEventsToBeShown()
        for (event in events) {
            createNotification(event, context)
        }
    }

    fun startNotificationAlarm(context : Context, db : DatabaseHelper){
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(context, EventBroadcastReceiver::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, intent, 0)
        val cal = db.getNextAppointmentDate()
        if (notificationAlarmSet)
            am.cancel(pi)
        notificationAlarmSet = true
        am.set(android.app.AlarmManager.RTC_WAKEUP, cal.toMillis()!!, pi)
    }

    fun eventTapped(event : EventRecord, db : DatabaseHelper){
        val now = CustomCalendar.Builder.getCurrent()
        event.addAppointment?.let {
            if (!event.yearlyAddAppointment && it <= now)
                event.addAppointment = null
        }
        db.acknowledgeEvent(event)
    }

    fun todayMidnight(addHour : Int = 0) : Date {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0 + addHour)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.time
    }

}