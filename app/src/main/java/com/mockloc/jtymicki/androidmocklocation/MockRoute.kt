package com.mockloc.jtymicki.androidmocklocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import java.io.BufferedReader
import java.lang.Exception

private const val TAG = "MockRoute"

class MockRoute {
    @SuppressLint("MissingPermission")
    fun pushMockRoute(context: Context, fileUri: Uri) {
        val inputStream = context.getContentResolver().openInputStream(fileUri)
        if(inputStream == null){
                throw Exception("File is empty")
        }
        val inputString = inputStream.bufferedReader().use(BufferedReader::readText)

        Log.d(TAG, inputString)
        val parseGPX = ParseGPX()
        parseGPX.parse(inputString)
        var fusedLocationProviderClient = getFusedLocationProviderClient(context)

        val handler = Handler()
        for (item in parseGPX.items) {
            Log.i(TAG, "pointDelay=${item.pointDelay}")
            handler.postDelayed({
                try {
                    Log.i(TAG, "pushing mock location")
                    Log.i(TAG, "lat= ${item.lat}")
                    Log.i(TAG, "lon= ${item.lon}")
                    Log.i(TAG, "altitude= ${item.altitude}")
                    Log.i(TAG, "altitude= ${item.accuracy}")
                    if (hasLocationPermission(context)) {
                        val location = Location("MockProvider")
                        location.latitude = item.lat
                        location.longitude = item.lon
                        location.accuracy = item.accuracy
                        location.altitude = item.altitude
                        location.time = System.currentTimeMillis()
                        location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        fusedLocationProviderClient.setMockMode(true)
                        fusedLocationProviderClient.setMockLocation(location)
                    }
                }catch (e : Exception){
                    if("provider is not a test provider".toRegex().containsMatchIn(e.localizedMessage)){
                        Log.i(TAG, "NOT pushing mock location as mock providers not enabled")
                        handler.removeCallbacksAndMessages(null)
                        if (hasLocationPermission(context)) {
                            fusedLocationProviderClient.setMockMode(false)
                        }
                    }else{
                        Log.e(TAG, e.localizedMessage)
                    }
                }
            }, item.pointDelay)
        }
    }

    companion object{
        fun hasLocationPermission(context: Context): Boolean{
            return (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED)
        }
    }
}