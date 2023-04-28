package com.kenig.gps.location

import org.osmdroid.util.GeoPoint


//8

data class LocationModel(
    val velocity: Float = 0.0f,
    val distance: Float = 0.0f,
    val geoPointsList: ArrayList<GeoPoint>
)
