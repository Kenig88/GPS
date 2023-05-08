package com.kenig.gps.utils

import android.app.Application
import com.kenig.gps.database.MainDb


//19.2

class MainApp : Application() {
    val database by lazy {  //(by lazy означает что запустится только 1 раз)
        MainDb.getDatabase(this) //(всё это означает что database инициализируется только 1 раз пр запуске приложения)
    }
}