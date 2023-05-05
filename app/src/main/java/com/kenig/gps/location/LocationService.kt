package com.kenig.gps.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_MUTABLE
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.kenig.gps.MainActivity
import com.kenig.gps.R
import org.osmdroid.util.GeoPoint


//8.1

class LocationService : Service() {
    private var distance = 0.0f //11.2
    private var lastLocation: Location? = null //11
    private lateinit var locProvider: FusedLocationProviderClient //10 (с помощью него сохраняются координаты для проложения маршрута за пользователем)
    private lateinit var locRequest: LocationRequest //10.4
    private lateinit var geoPointsList: ArrayList<GeoPoint>//12.1

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startNotification()
        startLocationUpdates() //10.3.1
        isRunning = true
        return START_STICKY //(сервис будет перезапущен после того, как был убит системой из за недостатка памяти)
    }

    override fun onCreate() {
        super.onCreate()
        geoPointsList = ArrayList() //12.2
        initLocation() //10.2
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        locProvider.removeLocationUpdates(locCallback) //(убрать обновления местоположения при разрушении сервиса(его отключении))
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

    private fun initLocation(){ //10.1
        locRequest = LocationRequest.create() //10.5
        locRequest.interval = 1 //(интервал обновления местоположения пользователя в миллисекундах)
        locRequest.fastestInterval = 1
        locRequest.priority = PRIORITY_HIGH_ACCURACY //(высокая точность местоположения)
        locProvider = LocationServices.getFusedLocationProviderClient(baseContext) //(инициализировал класс Fused, который даёт возможность получатьсведения о местоположении (просто доступ))
    }

    private val locCallback = object : LocationCallback(){ //10.6 в этот коллбэк locProvider будет передавать данные о местоположении)
        override fun onLocationResult(locResult: LocationResult) { //(в этот коллбэк буду получать сведения о местоположении)
            super.onLocationResult(locResult)
            val currentLocation = locResult.lastLocation
            if(lastLocation != null && currentLocation != null){ //11.1
                distance += lastLocation?.distanceTo(currentLocation)!! //11.2.1 (таким образом накапливается дистанция + если погрешность спутника 0.2мс то дистанция не увеличивается)
                geoPointsList.add(GeoPoint(currentLocation.latitude, currentLocation.longitude)) //12.3
                val locModel = LocationModel( //12
                    currentLocation.speed,
                    distance,
                    geoPointsList
                )
                sendLocData(locModel) //13.1
            }
            lastLocation = currentLocation
        }
    }

    private fun sendLocData(locModel: LocationModel){ //13 (BroadcastIntent)
        val intent = Intent(LOC_MODEL_INTENT) //(создал интент, в скобках дал название)
        intent.putExtra(LOC_MODEL_INTENT, locModel) //(под ключевым словом передастся locModel (весь класс), котоырй буду получать в MainFragment)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent) //(с помощью этого инициализирую данный класс и через sendBroadcast передаю весь класс locModel(Intent))
    }

    private fun startLocationUpdates(){ //10.3 (тут подписываюсь на получение сведений о местоположений)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED //(если != (нет) разрешения, то запускается return и код дальше не проходит, если разрешение есть, то запускается locProvider)
        ) return

        locProvider.requestLocationUpdates( //(так же не забыть добавить AddPermissionCheck)
            locRequest, //10.5.1 (просто настройки)
            locCallback, //10.6.1 (в этот коллбэк locProvider и будет передавать данные)
            Looper.myLooper() //(получаю местоположение не 1 раз, а постоянно)
        )
    }

    companion object{
        const val LOC_MODEL_INTENT = "loc_intent" //13.2
        const val CHANNEL_ID = "channel_1"
        var isRunning = false
        var startTime = 0L //9.5
    }
}