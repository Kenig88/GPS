package com.kenig.gps.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


//19.1

@Dao
interface Dao {

    @Insert //(для записывания в БД)
    suspend fun insertTrack(trackItem: TrackItem)

    @Query("SELECT * FROM TRACK") //(нужно для считывания с БД (поиск). Сюда писать запросы SQLite)
    fun getAllTracks(): Flow <List<TrackItem>> //(Flow будет выдавать список из TrackItem постоянно если есть изменения)

    @Delete //20.10 (для удаления сохранённых треков)
    suspend fun deleteTrack(trackItem: TrackItem)
}