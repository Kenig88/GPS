package com.kenig.gps.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.kenig.gps.R


//SplashActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var timer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        timer = object : CountDownTimer(1000, 1000) {
            override fun onTick(p0: Long) {
            }
            override fun onFinish() {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }
}