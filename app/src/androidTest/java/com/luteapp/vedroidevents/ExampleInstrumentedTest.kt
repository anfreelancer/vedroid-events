package com.luteapp.vedroidevents

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import jasuramme.dateapi.CustomCalendar

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.myapplication", appContext.packageName)
        val db = DatabaseHelper(appContext)
        db.reCreate()
        val event = EventRecord(
            null,
            "test1",
            "no comments",
            "",
            BIRTHDAY_EVENT_TYPE,
            CustomCalendar.Builder.getCurrent(),
            CustomCalendar.Builder.getCurrent(),
            null,
            false
        )
        db.addEvent(event)
        val toShow = db.getEventsToBeShown()
        assertEquals(toShow.size, 1)
    }
}
