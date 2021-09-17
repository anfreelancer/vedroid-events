package com.example.myapplication

import jasuramme.dateapi.CustomCalendar
import org.junit.Test
import org.junit.Assert.*
import java.util.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val a = CustomCalendar(30, 2, 2016)
        val b = CustomCalendar(1, 3, 2016)
        assertEquals(a.getNearestDate(), b)
    }
}

class Test2 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(1, 7, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(31, 12, null)
        val b = CustomCalendar(31, 12, 2020)
        assertEquals(a.getNearestDate(), b)
    }
}

class Test3 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(1, 7, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(1, 1, null)
        val b = CustomCalendar(1, 1, 2020)
        assertEquals(a.getNearestDate(), b)
    }
}

class Test4 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(1, 7, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(1, 1, null)
        val b = CustomCalendar(1, 1, 2020)
        assertEquals(a.getNearestDate(), b)
    }
}

class Test6 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(1, 7, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(1, 1, null)
        val b = CustomCalendar(1, 1, 2021)
        assertEquals(a.getNextNearestDate(), b)
    }
}

class Test7 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(1, 7, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(12, 2, 2020)
        val b = CustomCalendar(12, 2, 2021)
        assertEquals(a.getNextNearestDate(), b)
    }
}

class Test8 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(3, 3, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(29, 2, null)
        val b = CustomCalendar(1, 3, 2021)
        assertEquals(a.getNextNearestDate(), b)
    }
}

class Test9 {
    @Test
    fun addition_isCorrect() {
        val c = CustomCalendar(3, 3, 2020)
        CustomCalendar.Builder.forcedCalendar = c
        val a = CustomCalendar(29, 2, null)
        val b = CustomCalendar(29, 2, 2020)
        assertEquals(a.getNearestDate(), b)
    }
}

class Testa {
    @Test
    fun addition_isCorrect() {
        val a = CustomCalendar(30, 2, -15)
        val b = CustomCalendar(30, 2, -15)
        assertEquals(a.getNearestDate(), b)
    }
}

class Testb {
    @Test
    fun addition_isCorrect() {
        val c1 = Calendar.getInstance()
        c1.set(-1200, 1 , 15)
        val c2 = Calendar.getInstance()
        c2.set(1000, 1, 15)
        assertEquals(c1 < c2, true)
    }
}

