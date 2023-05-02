package com.kenig.gps.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*


//9

object TimeUtils {
    @SuppressLint("SimpleDateFormat")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss")

    fun getTime(timeInMillis: Long): String{
        val cv = Calendar.getInstance() //(Calendar превращает системное время в нужный формат)
        timeFormatter.timeZone = TimeZone.getTimeZone("UTC") //(благодаря этому отсчёт будет начинаться с нуля)
        cv.timeInMillis = timeInMillis
        return timeFormatter.format(cv.time)
    }
}