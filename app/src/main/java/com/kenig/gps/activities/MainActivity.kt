package com.kenig.gps.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kenig.gps.R
import com.kenig.gps.databinding.ActivityMainBinding
import com.kenig.gps.fragments.MainFragment
import com.kenig.gps.fragments.SettingsFragment
import com.kenig.gps.fragments.TracksFragment
import com.kenig.gps.utils.openFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBottomNavClicks()
        openFragment(MainFragment.newInstance())
    }

    private fun onBottomNavClicks(){ //1(слушатель нажатий bNav)
        binding.bNav.setOnItemSelectedListener{
            when(it.itemId) {
                R.id.home -> openFragment(MainFragment.newInstance())
                R.id.tracks -> openFragment(TracksFragment.newInstance())
                R.id.settings -> openFragment(SettingsFragment())
            }
            true
        }
    }
}