package com.luteapp.vedroidevents

import jasuramme.dateapi.CustomCalendar
import org.junit.Assert
import org.junit.Test

class EventRecord1 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(15, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2016),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(4, 2, 2016),
            yearlyAddAppointment = false

        )
        val check = CustomCalendar(25,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}

class EventRecord2 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(15, 3, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2016),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(4, 2, 2016),
            yearlyAddAppointment = false

        )
        val check = CustomCalendar(25,2,2019)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}


class EventRecord3 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(15, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2018),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(22, 2, 2018),
            yearlyAddAppointment = false

        )
        val check = CustomCalendar(22,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}

class EventRecord4 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(26, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2018),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(28, 2, 2018),
            yearlyAddAppointment = false

        )
        val check = CustomCalendar(28,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}


class EventRecord5 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(22, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2016),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(28, 2, 2018),
            yearlyAddAppointment = false

        )
        val check = CustomCalendar(25,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}


class EventRecord6 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(22, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2016),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(24, 2, 2015),
            yearlyAddAppointment = true

        )
        val check = CustomCalendar(24,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}


class EventRecord7 {
    @Test
    fun addition_isCorrect() {
        CustomCalendar.Builder.forcedCalendar = CustomCalendar(22, 2, 2018)
        val tmp = EventRecord(
            index = null,
            name = "test",
            comments = "No comments",
            picture = "",
            type = BIRTHDAY_EVENT_TYPE,
            date = CustomCalendar(25, 2, 2016),
            showDate = CustomCalendar(0,0,0),
            addAppointment = CustomCalendar(28, 2, 2016),
            yearlyAddAppointment = true

        )
        val check = CustomCalendar(25,2,2018)
        tmp.setShowDate()
        Assert.assertEquals(tmp.showDate, check)
    }
}