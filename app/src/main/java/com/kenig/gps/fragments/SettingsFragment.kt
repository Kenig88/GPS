package com.kenig.gps.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.kenig.gps.R
import com.kenig.gps.utils.showToast


//3

class SettingsFragment : PreferenceFragmentCompat(){ //(все значения класса Preference находятся в папке Res -> XML))
private lateinit var timePref: Preference
private lateinit var colorPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey) //(чтобы передать разметку нужно так прописывать)
        init()
    }

    private fun init(){ //3.1
        timePref = findPreference("update_time_key")!!
        colorPref = findPreference("color_key")!!
        val changeListener = onChangeListener()
        timePref.onPreferenceChangeListener = changeListener
        colorPref.onPreferenceChangeListener = changeListener
        initPrefs()
    }

    private fun onChangeListener(): OnPreferenceChangeListener{
        return Preference.OnPreferenceChangeListener{
            pref, value ->
                when(pref.key){
                    "update_time_key" -> onTimeChange(value.toString())
                    "color_key" -> pref.icon?.setTint(Color.parseColor(value.toString()))
                }
            true
        }
    }

    private fun onTimeChange(value: String){
        val nameArray = resources.getStringArray(R.array.location_time_update_name)
        val valueArray = resources.getStringArray(R.array.location_time_update_value)
        val title = timePref.title.toString().substringBefore(":")
        val pos = valueArray.indexOf(value)
        timePref.title = "$title: ${nameArray[pos]}"
    }

    private fun initPrefs() {
        val pref = timePref.preferenceManager.sharedPreferences
        val nameArray = resources.getStringArray(R.array.location_time_update_name)
        val valueArray = resources.getStringArray(R.array.location_time_update_value)
        val title = timePref.title
        val pos = valueArray.indexOf(pref?.getString("update_time_key", "3000"))
        timePref.title = "$title: ${nameArray[pos]}"

        val trackColor = pref?.getString("color_key", "#1EFF00")
        colorPref.icon?.setTint(Color.parseColor(trackColor))
    }
}