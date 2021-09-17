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

object Pics{
    val res : Map<String, Int> = mapOf(
        "@Pic1" to R.drawable.pic1,
        "@Pic2" to R.drawable.pic2,
        "@Pic3" to R.drawable.pic3,
        "@Pic4" to R.drawable.pic4,
        "@Pic5" to R.drawable.pic5,
        "@Pic6" to R.drawable.pic6,
        "@Pic7" to R.drawable.pic7,
        "@Pic8" to R.drawable.pic8,
        "@Pic9" to R.drawable.pic9,
        "@Pic10" to R.drawable.pic10,
        "@Pic11" to R.drawable.pic11,
        "@Pic12" to R.drawable.pic12,
        "@Pic13" to R.drawable.pic13,
        "@Pic14" to R.drawable.pic14,
        "@Pic15" to R.drawable.pic15,
        "@Pic16" to R.drawable.pic16,
        "@Pic17" to R.drawable.pic17,
        "@Pic18" to R.drawable.pic18,
        "@Pic19" to R.drawable.pic19,
        "@Pic20" to R.drawable.pic20,
        "@Pic21" to R.drawable.pic21,
        "@Pic22" to R.drawable.pic22,
        "" to 0
        )

    const val errorPic = R.drawable.error
    const val plus = R.drawable.plus

    fun getIdByName(name : String) : Int{
        res[name]?.let{ return it }
        return 0
    }
}