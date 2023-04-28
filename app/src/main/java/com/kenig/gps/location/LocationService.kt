package com.kenig.gps.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.kenig.gps.MainActivity
import com.kenig.gps.R
import com.kenig.gps.fragments.MainFragment


//8.1

class LocationService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        isRunning = true
        return START_STICKY //(сервис будет перезапущен после того, как был убит системой из за недостатка памяти)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun startNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val nChannel = NotificationChannel(
                CHANNEL_ID,
                "Location service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val nManager = getSystemService(NotificationManager::class.java) as NotificationManager
            nManager.createNotificationChannel(nChannel)
        }

        val nIntent = Intent(this, MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(
            this,
            10,
            nIntent,
            FLAG_MUTABLE
        )
        val notification = NotificationCompat.Builder( //(само уведомление в статус-баре)
            this,
            CHANNEL_ID
        ).setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tracker Running!")
            .setContentIntent(pIntent).build()
        startForeground(99, notification)
    }

    companion object{
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
    }
}