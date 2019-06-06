package com.mockloc.jtymicki.androidmocklocation

import android.R
import android.content.Context
import android.location.LocationManager
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import java.io.BufferedReader
import java.io.File

private const val TAG = "MockRoute"
private const val MOCK_TRACK_DATA_FILENAME = "mock_track.gpx"

class MockRoute {
    fun pushMockRoute(context: Context) {
        if (isExternalStorageReadable()) {
            Log.i(TAG, "externals storage is readable")
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath;
            val file = File("""${downloadsPath}/${MOCK_TRACK_DATA_FILENAME}""")
            if (file?.exists()) {
                Log.i(TAG, "data file exists")
                val bufferedReader: BufferedReader = file.bufferedReader()
                val inputString = bufferedReader.use { it.readText() }
                Log.i(TAG, inputString)
                val parseGPX = ParseGPX()
                parseGPX.parse(inputString)
                val mockGPS = MockLocationProvider(LocationManager.GPS_PROVIDER, context)
                val mockWifi = MockLocationProvider(LocationManager.NETWORK_PROVIDER, context)
                val handler = Handler()
                for (item in parseGPX.items) {
                    Log.i(TAG, "pointDelay=${item.pointDelay}")
                    handler.postDelayed({
                        Log.i(TAG, "pushing mock location")
                        Log.i(TAG, "lat= ${item.lat}")
                        Log.i(TAG, "lon= ${item.lon}")
                        Log.i(TAG, "ele= ${item.ele}")
                        mockGPS?.pushLocation(item.lat, item.lon, item.ele, 0f)
                        mockWifi?.pushLocation(item.lat, item.lon, item.ele, 0f)
                    }, item.pointDelay)
                }
            } else {
                Log.i(TAG, "data file doesn't exist ${file.absolutePath}")
                Toast.makeText(context, "data file doesn't exist", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}