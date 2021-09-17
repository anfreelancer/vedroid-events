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

import android.app.Service
import android.content.Intent
import android.os.IBinder


class MainService : Service() {
    private val db = DatabaseHelper(this)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Api.init(this, db)
        intent?.let {
            Api.eventTapped(EventRecord.Builder.fromIntent(it), db)
        }
        Thread(Runnable {
            stopSelf()
        }).start()

        return START_STICKY
    }
}