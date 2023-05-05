package com.kenig.gps.location

import org.osmdroid.util.GeoPoint
import java.io.Serializable


//8

data class LocationModel(
    val speed: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>

) : Serializable
