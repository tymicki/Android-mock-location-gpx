package com.mockloc.jtymicki.androidmocklocation

import android.content.Context
import android.location.LocationManager
import android.os.Environment
import android.os.Handler
import android.util.Log
import java.io.BufferedReader
import java.io.File

class MockRoute {
    fun pushMockRoute(context: Context) {
        if (isExternalStorageReadable()) {
            Log.i(MainActivity.TAG, "externals storage is readable")
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath;
            val file = File("""${downloadsPath}/${MainActivity.MOCK_TRACK_DATA_FILENAME}""")
            if (file?.exists()) {
                Log.i(MainActivity.TAG, "data file exists")
                val bufferedReader: BufferedReader = file.bufferedReader()
                val inputString = bufferedReader.use { it.readText() }
                Log.i(MainActivity.TAG, inputString)
                val parseGPX = ParseGPX()
                parseGPX.parse(inputString)
                val mockGPS = MockLocationProvider(LocationManager.GPS_PROVIDER, context)
                val mockWifi = MockLocationProvider(LocationManager.NETWORK_PROVIDER, context)
                val handler = Handler()
                for (item in parseGPX.items) {
                    Log.i(MainActivity.TAG, "pointDelay=${item.pointDelay}")
                    handler.postDelayed({
                        Log.i(MainActivity.TAG, "pushing mock location")
                        Log.i(MainActivity.TAG, "lat= ${item.lat}")
                        Log.i(MainActivity.TAG, "lon= ${item.lon}")
                        Log.i(MainActivity.TAG, "ele= ${item.ele}")
                        mockGPS?.pushLocation(item.lat, item.lon, item.ele, 0f)
                        mockWifi?.pushLocation(item.lat, item.lon, item.ele, 0f)
                    }, item.pointDelay)
                }
            } else {
                Log.i(MainActivity.TAG, "data file doesn't exist")
            }
        }
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}