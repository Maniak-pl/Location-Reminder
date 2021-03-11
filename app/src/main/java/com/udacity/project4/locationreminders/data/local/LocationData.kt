package com.udacity.project4.locationreminders.data.local

import com.google.android.gms.maps.model.PointOfInterest

data class LocationData(
    var poi: PointOfInterest, var lat: Double, var long: Double, var title: String
)