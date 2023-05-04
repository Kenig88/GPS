package com.kenig.gps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kenig.gps.location.LocationModel


//14

class MainViewModel : ViewModel(){
    val timeData = MutableLiveData<String>() //9.3
    val locationUpdates = MutableLiveData<LocationModel>()
}