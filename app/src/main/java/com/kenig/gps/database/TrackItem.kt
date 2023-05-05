package com.kenig.gps.database

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.io.Serializable


//18

@Entity(tableName = "track_item")
data class TrackItem(

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "time")
    val time: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "av_speed")
    val av_speed: String,

    @ColumnInfo(name = "distance")
    val distance: String,

    @ColumnInfo(name = "geo_points")
    val geoPoints: String,

) : Serializable
