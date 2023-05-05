package com.kenig.gps.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*


//9

@SuppressLint("SimpleDateFormat")
object TimeUtils {
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm") //18.4

    fun getTime(timeInMillis: Long): String{
        val cv = Calendar.getInstance() //(Calendar превращает системное время в нужный формат)
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC") //(благодаря этому отсчёт будет начинаться с нуля)
        cv.timeInMillis = timeInMillis
        return timeFormatter.format(cv.time) //(форматирует системное время в то, что указано в переменной timeFormatter)
    }

    fun getDate(): String{ //18.4.1
        val cv = Calendar.getInstance()
        return dateFormatter.format(cv.time) //(форматирует системное время в то, что указано в переменной dateFormatter)
    }
}