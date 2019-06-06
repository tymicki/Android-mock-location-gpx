package com.mockloc.jtymicki.androidmocklocation

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.SystemClock

internal class MockLocationProvider(private val providerName: String, private val context: Context) {

    init {
        val locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager
        locationManager.addTestProvider(providerName, false, false, false, false, false,
                true, true, 0, 5)
        locationManager.setTestProviderEnabled(providerName, true)
    }

    fun pushLocation(lat: Double, lon: Double, alt: Double, accuracy: Float) {
        val locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager

        val mockLocation = Location(providerName)
        mockLocation.latitude = lat
        mockLocation.longitude = lon
        mockLocation.altitude = alt
        mockLocation.time = System.currentTimeMillis()
        mockLocation.accuracy = accuracy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
        }
        locationManager.setTestProviderLocation(providerName, mockLocation)
    }

    fun shutdown() {
        val locationManager = context.getSystemService(
                Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeTestProvider(providerName)
    }
}
