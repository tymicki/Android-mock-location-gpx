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
private const val MIN_UPDATE_INTERVAL_MS = 500

class MockRoute {
    var timeFactor = 1
    var lastLocationTimeMs: Long = -1
    lateinit var handler: Handler

    fun clearRoute(){
        if(::handler.isInitialized){
            handler.removeCallbacksAndMessages(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun pushMockRoute(context: Context, fileUri: Uri) {
        val inputStream = context.getContentResolver().openInputStream(fileUri)
        if(inputStream == null){
                throw Exception("File is empty")
        }
        val inputString = inputStream.bufferedReader().use(BufferedReader::readText)

//        Log.d(TAG, inputString)
        val parseGPX = ParseGPX()
        parseGPX.parse(inputString)
        var fusedLocationProviderClient = getFusedLocationProviderClient(context)

        clearRoute();
        handler = Handler()
        for (item in parseGPX.items) {
            val delay = item.pointDelay/timeFactor;

//            Log.d(TAG, "pointDelay=${item.pointDelay}")
//            Log.d(TAG, "timeFactor=${timeFactor}")
//            Log.d(TAG, "delay=${delay}")

            handler.postDelayed({
                try {
                    val currentTimeMs = System.currentTimeMillis()
                    if (hasLocationPermission(context)
                        && (lastLocationTimeMs < 0 || currentTimeMs - lastLocationTimeMs > MIN_UPDATE_INTERVAL_MS)
                    ) {
                      Log.i(TAG, "pushing mock location lat=${item.lat} lon=${item.lon} alt=${item.altitude} acc=${item.accuracy} dT=${currentTimeMs - lastLocationTimeMs}")
//                    Log.i(TAG, "lat= ${item.lat}")
//                    Log.i(TAG, "lon= ${item.lon}")
//                    Log.i(TAG, "alt=${item.altitude}")
//                    Log.i(TAG, "acc=${item.accuracy}")

                        val location = Location("MockProvider")
                        location.latitude = item.lat
                        location.longitude = item.lon
                        location.accuracy = item.accuracy
                        location.altitude = item.altitude
                        location.time = currentTimeMs
                        location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                        fusedLocationProviderClient.setMockMode(true)
                        fusedLocationProviderClient.setMockLocation(location)
                        lastLocationTimeMs = currentTimeMs
                    }
                }catch (e : Exception){
                    if("provider is not a test provider".toRegex().containsMatchIn(e.localizedMessage)){
                        Log.w(TAG, "NOT pushing mock location as mock providers not enabled")
                        handler.removeCallbacksAndMessages(null)
                        if (hasLocationPermission(context)) {
                            fusedLocationProviderClient.setMockMode(false)
                        }
                    }else{
                        Log.e(TAG, e.localizedMessage)
                    }
                }
            }, delay)
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