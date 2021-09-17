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
import jasuramme.dateapi.DateRepresentation
import jasuramme.dateapi.DateRepresentationDDMMYY
import jasuramme.dateapi.DateRepresentationMMDDYY
import jasuramme.dateapi.DateRepresentationYYMMDD

data class SettingsRecord(var dateType : Int,
                          var delimiter : Int,
                          var iconSize : Int,
                          var suppressZeroes: Boolean)
{

    fun putToIntent(intent : Intent) {
        intent.putExtra("dateType", dateType)
        intent.putExtra("delimiter", delimiter)
        intent.putExtra("iconSize", iconSize)
        intent.putExtra("suppressZeroes", suppressZeroes)
    }

    fun toValues() : ContentValues {
        val ret = ContentValues()
        ret.put("dateType", dateType)
        ret.put("delimiter", delimiter)
        ret.put("iconSize", iconSize)
        ret.put("suppressZeroes", suppressZeroes)
        return ret
    }

    fun getIconSizePixels() : Int {
        if (iconSize >= Data.IconSizes.size)
            iconSize = 0
        return Api.dpToPixels( Data.IconSizes[iconSize].dpi )
    }

    fun getDelimiterString() : String {
        return Data.delimiters[delimiter]
    }

    object Builder {
        fun fromIntent(intent : Intent) : SettingsRecord {
            return SettingsRecord(
                intent.getIntExtra("dateType", 0),
                intent.getIntExtra("delimiter", 0),
                intent.getIntExtra("iconSize", 100),
                intent.getBooleanExtra("suppressZeroes", false)
            )
        }

        fun fromCursor(cursor : Cursor) : SettingsRecord {
            return SettingsRecord(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getInt(2),
                cursor.getInt(3) != 0
            )
        }

    }

    object Data {
        val delimiters = listOf( "/", ".", "-", ":", " " )
        const val dateTypesCount = 3

        fun getDateTypeString(d : String, dateType : Int) : String{
            return when (dateType) {
                0 -> "DD${d}MM${d}YYYY"
                1 -> "YYYY${d}MM${d}DD"
                2 -> "MM${d}DD${d}YYYY"
                else -> "DD${d}MM${d}YYYY"
            }
        }

        fun getRepresentator(
            d: String, suppressZeroes : Boolean, dateType : Int ) : DateRepresentation {
            return when (dateType) {
                0 -> DateRepresentationDDMMYY(d, suppressZeroes)
                1 -> DateRepresentationYYMMDD(d, suppressZeroes)
                2 -> DateRepresentationMMDDYY(d, suppressZeroes)
                else -> DateRepresentationDDMMYY(d, suppressZeroes)
            }
        }

        data class IconSize(val dpi : Int, val str : String)
        val IconSizes = listOf( IconSize(15, "15dp"),
            IconSize(30, "30dp"),
            IconSize(50, "50dp"),
            IconSize( 75, "75dp"),
            IconSize(100, "100dp") )
    }
}