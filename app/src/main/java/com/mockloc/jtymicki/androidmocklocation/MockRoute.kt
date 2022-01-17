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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import java.io.BufferedReader

private const val TAG = "MockRoute"
private const val MIN_UPDATE_INTERVAL_MS = 500

class MockRoute {
    var timeFactor = 1

    private var lastLocationTimeMs: Long = -1
    private lateinit var handler: Handler
    private val parseGPX = ParseGPX()
    private lateinit var context: Context
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    fun clearRoute() {
        if (::handler.isInitialized) {
            handler.removeCallbacksAndMessages(null)
        }
        parseGPX.items.clear()
    }

    @SuppressLint("MissingPermission")
    fun nextPoint() {
        if (parseGPX.items.count() > 0) {
            val item = parseGPX.items.removeFirst()

            try {
                val currentTimeMs = System.currentTimeMillis()
                if (hasLocationPermission(context)
                    && (lastLocationTimeMs < 0 || currentTimeMs - lastLocationTimeMs > MIN_UPDATE_INTERVAL_MS)
                ) {
                    Log.i(
                        TAG,
                        "pushing mock location lat=${item.lat} lon=${item.lon} alt=${item.altitude} acc=${item.accuracy} speed=${item.speed} dT=${currentTimeMs - lastLocationTimeMs}"
                    )

                    val location = Location("MockProvider")
                    location.latitude = item.lat
                    location.longitude = item.lon
                    location.accuracy = item.accuracy
                    location.altitude = item.altitude
                    location.speed = item.speed
                    location.time = currentTimeMs
                    location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    fusedLocationProviderClient.setMockMode(true)
                    fusedLocationProviderClient.setMockLocation(location)
                    lastLocationTimeMs = currentTimeMs
                }

                // recurse after delay
                val delay = item.pointDelay / timeFactor
                handler.postDelayed({
                    nextPoint()
                }, delay)

            } catch (e: Exception) {
                if ("provider is not a test provider".toRegex().containsMatchIn(e.localizedMessage)) {
                    Log.w(TAG, "NOT pushing mock location as mock providers not enabled")
                    handler.removeCallbacksAndMessages(null)
                    if (hasLocationPermission(context)) {
                        fusedLocationProviderClient.setMockMode(false)
                    }
                } else {
                    Log.e(TAG, e.localizedMessage)
                }
            }
        } else {
            // end of GPX reached
            clearRoute()
        }
    }

    fun pushMockRoute(context: Context, fileUri: Uri) {
        this.context = context
        val inputStream = context.contentResolver.openInputStream(fileUri) ?: throw Exception("File is empty")

        val inputString = inputStream.bufferedReader().use(BufferedReader::readText)

        clearRoute() // clear existing points
        parseGPX.parse(inputString)
        fusedLocationProviderClient = getFusedLocationProviderClient(context)


        handler = Handler()

        // start recursion over GPX points
        nextPoint()
    }

    companion object {
        fun hasLocationPermission(context: Context): Boolean {
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