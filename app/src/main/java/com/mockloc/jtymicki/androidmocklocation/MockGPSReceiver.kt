package com.mockloc.jtymicki.androidmocklocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log

private const val TAG = "MockGPSReceiver"

class MockGPSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "on receive invoked")
        var mockGPS: MockLocationProvider? = null
        var mockWifi: MockLocationProvider? = null
        if (intent.getAction() == "stop.mock") {
            if (mockGPS != null) {
                mockGPS.shutdown()
            }
            if (mockWifi != null) {
                mockWifi.shutdown()
            }
        } else if (intent.getAction() == "send.mock") {
            mockGPS = MockLocationProvider(LocationManager.GPS_PROVIDER, context)
            mockWifi = MockLocationProvider(LocationManager.NETWORK_PROVIDER, context)
            val lat: Double
            val lon: Double
            val alt: Double
            val accuracy: Float
            lat = java.lang.Double.parseDouble(if (intent.getStringExtra("lat") != null) intent.getStringExtra("lat") else "0")
            lon = java.lang.Double.parseDouble(if (intent.getStringExtra("lon") != null) intent.getStringExtra("lon") else "0")
            alt = java.lang.Double.parseDouble(if (intent.getStringExtra("alt") != null) intent.getStringExtra("alt") else "0")
            accuracy = java.lang.Float.parseFloat(if (intent.getStringExtra("accuracy") != null) intent.getStringExtra("accuracy") else "0")
            Log.i(TAG, String.format("setting mock to Latitude=%f, Longitude=%f Altitude=%f Accuracy=%f", lat, lon, alt, accuracy))
            mockGPS.pushLocation(lat, lon, alt, accuracy)
            mockWifi.pushLocation(lat, lon, alt, accuracy)
        } else if (intent.getAction() == "send.mock.route") {
            MockRoute().pushMockRoute(context)
        }
    }
}