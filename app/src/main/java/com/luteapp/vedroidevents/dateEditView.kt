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
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.core.content.ContextCompat

const val TYPE_CLASS_DATETIME = 4

class DateEditView(context : Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatEditText(context, attrs), TextWatcher{

    var dateFormat = "dd/MM/YYYY"
    var fullDate = true
    private var ignore = true

    fun setFilterState(state : Boolean) {
        ignore = !state
    }

    private fun isCharNDigit(num : Int) : Boolean{
        return try {
            dateFormat[num].toLowerCase() in listOf('d', 'y', 'm')
        } catch (e : Exception) {
            false
        }
    }

    private fun isDigit(ch : Char) = ch.toLowerCase() in listOf('d', 'y', 'm')

    private fun getEmptyString() : String {
        var ret = ""
        for ( ch in dateFormat) {
            ret += if (isDigit(ch)) {
                '_'
            } else {
                ch
            }
        }
        return ret
    }

    init {
        ignore = true
        this.addTextChangedListener( this )
        this.inputType = TYPE_CLASS_DATETIME
    }

    override fun afterTextChanged(s: Editable?) {
        if (ignore)
            return
        Api.stringToCalendar(text.toString(), fullDate)?.let {
            if (it.isValid())
            {
                this.setCompoundDrawables (null, null, null, null)
                return
            }
        }
        val errorPic = ContextCompat.getDrawable(context, Pics.errorPic)!!
        errorPic.setBounds(0, 0, 20, 20)
        this.setCompoundDrawables (null, null,errorPic, null)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (text == null || ignore)
            return
        if (lengthBefore > lengthAfter)
            deleteChars(start + lengthAfter, start + lengthBefore, text.toMutableList())
        if (lengthAfter - lengthBefore != 1)
            return
        var ret = text.toMutableList()

        val pos = start + lengthBefore
        if (pos < dateFormat.length) {
            if (isCharNDigit(pos)) {
                if (!text[pos].isDigit()) {
                    ret.removeAt(pos)
                } else {
                    if (pos + 1 < ret.size)
                        ret.removeAt(pos + 1)
                }
            } else {
                if (ret[pos].isDigit()) {
                    ret[pos + 1] = ret[pos]
                    if (pos + 2 < ret.size)
                        ret.removeAt(pos + 2)
                }
                else
                    ret.removeAt(pos)
                ret[pos] = dateFormat[pos]
            }
        }
        if (dateFormat.length <= ret.size)
            ret = ret.subList(0, dateFormat.length)
        ignore = true
        this.setText(String(ret.toCharArray()))
        ignore = false
        try {
            if (isCharNDigit(pos))
                this.setSelection(pos + 1)
            else
                this.setSelection(pos + 2)
        }
        catch (e : Exception) {
            this.text?.let {
                this.setSelection(it.length)
            }
        }
        //super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    private fun deleteChars(from : Int, to : Int, text : MutableList<Char>) {
        if (to <= 0)
            return
        for (i in from until to){
            if (isCharNDigit(i))
                text.add(i, '_')
            else
                if (i < dateFormat.length)
                    text.add(i, dateFormat[i])
        }
        ignore = true
        this.setText(String(text.toCharArray()))
        ignore = false
        this.setSelection(from)
    }

}