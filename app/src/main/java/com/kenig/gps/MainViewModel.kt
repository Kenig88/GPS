package com.kenig.gps

import androidx.lifecycle.*
import com.kenig.gps.database.MainDb
import com.kenig.gps.database.TrackItem
import com.kenig.gps.location.LocationModel
import kotlinx.coroutines.launch


//14

@Suppress("UNCHECKED_CAST")
class MainViewModel(db: MainDb) : ViewModel(){
    val dao = db.getDao()
    val timeData = MutableLiveData<String>() //9.3
    val locationUpdates = MutableLiveData<LocationModel>()
    val tracks = dao.getAllTracks().asLiveData()

    fun insertTrack(trackItem: TrackItem) = viewModelScope.launch{ //19.4
        dao.insertTrack(trackItem)
    }

    class ViewModelFactory(private val db: MainDb) : ViewModelProvider.Factory{ //19.3 (этот класс будет создавать класс MainViewModel (тот что выше) и отсюда я смогу создать базу данных)
        override fun <T : ViewModel> create(modelClass: Class<T>): T { //(пока можно не понимать зачем это, просто ViewModelFactort Создаёт класс MainViewModel и через его конструктор я смогу передать Dao)
            if(modelClass.isAssignableFrom(MainViewModel::class.java)){
                return MainViewModel(db) as T
            }
            throw IllegalArgumentException("Unknown viewModelClass")
        }
    }
}