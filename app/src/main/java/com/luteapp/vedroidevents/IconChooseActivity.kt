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

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TableRow
import kotlinx.android.synthetic.main.activity_icon_choose.*
import android.util.DisplayMetrics


class IconChooseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_choose)
        createIcons()

    }

    private fun iconClicked(dbString : String, id : Int){
        val intent = Intent()
        intent.putExtra("dbString", dbString)
        intent.putExtra("id", id)
        setResult(RESULT_OK, intent)
        finish()
    }


    private fun createIcons() {
        val size = Api.dpToPixels(100)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val iconsInRow = displayMetrics.widthPixels / size
        val padding = (displayMetrics.widthPixels % size) / 2
        var currentIcon = 0
        var row = TableRow( this )
        row.setPadding(padding, 0,padding, 0)
        for ( i in Pics.res) {
            if (currentIcon++ >= iconsInRow) {
                iconsTable.addView(row)
                row = TableRow(this)
                row.setPadding(padding, 0,padding, 0)
                currentIcon = 1
            }
            val icon = ImageButton(this)
            if (i.key != "")
                icon.setImageResource(i.value)
            icon.scaleType = ImageView.ScaleType.FIT_XY
            icon.setOnClickListener { this.iconClicked(i.key, i.value) }
            row.addView(icon, size, size)
            //iconsTable.add
        }
        iconsTable.addView(row)
    }

}
