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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_settings.*

const val IMPORT_ID = 1
const val EXPORT_ID = 2

class Settings : AppCompatActivity() {

    lateinit var currentSettings : SettingsRecord
    private val db = DatabaseHelper(this)

    class DelimiterClickListener(private val parentActivity : Settings) : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selected = parentActivity.dateTypeSpinner.selectedItemId
            parentActivity.currentSettings.delimiter = position
            parentActivity.createDateTypes()
            parentActivity.dateTypeSpinner.setSelection(selected.toInt())
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            parentActivity.dateDelimeter.setSelection(0)
        }
    }

    private val delimiterSelectListener = DelimiterClickListener(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        currentSettings = Api.settings
        createDelimiters()
        createDateTypes()
        createIconSizes()
        createSuppressZeroes()
        dateTypeSpinner.setSelection(currentSettings.dateType)
        dateDelimeter.setSelection(currentSettings.delimiter)
        iconSize.setSelection(currentSettings.iconSize)
        var suppressZeroesIndex = 0
        if (currentSettings.suppressZeroes)
            suppressZeroesIndex = 1
        suppressZeroesSpinner.setSelection(suppressZeroesIndex)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.settingsmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.Import_database_id -> requestWritePermissions(IMPORT_ID)
            R.id.Export_database_id -> requestWritePermissions(EXPORT_ID)
        }
        return true
    }

    private fun requestWritePermissions(id : Int) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),id)
                // Permission is not granted
            } else {
                dbExecute(id, true)
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        var granted = false
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            granted = true
            dbExecute(requestCode, granted)
    }

    private fun dbExecute(id : Int, granted : Boolean) {
        when (id) {
            EXPORT_ID -> {
                if (granted) {
                    chooseExportFolder()
                } else {
                    Toast.makeText(
                        this,
                        "Insufficient permissions to save database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            IMPORT_ID -> {
                if (granted) {
                    chooseImportFile()
                } else {
                    Toast.makeText(
                        this,
                        "Insufficient permissions to import database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun chooseExportFolder(){
        val intent = Intent(Intent.ACTION_PICK)
        val uri = Uri.parse("folder://")
        //intent.setType("folder")
        intent.data = uri
        try {
            startActivityForResult(intent, EXPORT_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun chooseImportFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "file/*"
        try {
            startActivityForResult(intent, IMPORT_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data?.data?.path?.let {
            when (requestCode) {

                IMPORT_ID -> db.importFrom(it)

                EXPORT_ID -> db.saveTo(it)
            }
        }
    }

    private fun createDateTypes(){
        val adapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item)
        for ( i in 0 until SettingsRecord.Data.dateTypesCount)
            adapter.add(SettingsRecord.Data.getDateTypeString(currentSettings.getDelimiterString(), i))
        dateTypeSpinner.adapter = adapter
    }

    private fun createDelimiters(){
        val adapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item)
        for ( i in SettingsRecord.Data.delimiters ) {
            if (i != " ")
                adapter.add(i)
            else
                adapter.add(resources.getString(R.string.Space))
        }
        dateDelimeter.adapter = adapter
        dateDelimeter.onItemSelectedListener = delimiterSelectListener
    }

    private fun createIconSizes(){
        val adapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item)
        for ( i in SettingsRecord.Data.IconSizes)
            adapter.add(i.str)
        iconSize.adapter = adapter
    }

    private fun createSuppressZeroes(){
        val adapter = ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_dropdown_item)
        adapter.add(resources.getString(R.string.No))
        adapter.add(resources.getString(R.string.Yes))
        suppressZeroesSpinner.adapter = adapter
    }

    fun saveClicked(view: View){
        currentSettings.dateType = dateTypeSpinner.selectedItemId.toInt()
        currentSettings.delimiter = dateDelimeter.selectedItemId.toInt()
        currentSettings.iconSize = iconSize.selectedItemId.toInt()
        currentSettings.suppressZeroes = suppressZeroesSpinner.selectedItemId.toInt() != 0
        db.updateSettings(currentSettings)
        Api.readSettings(db)
        val intent = Intent()
        intent.putExtra("updated", true)
        setResult(RESULT_OK, intent)
        finish()
    }

}
