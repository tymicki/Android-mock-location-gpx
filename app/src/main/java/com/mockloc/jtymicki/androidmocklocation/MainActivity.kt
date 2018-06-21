package com.mockloc.jtymicki.androidmocklocation

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.File

data class TrackingPoint(var lat: Double = 0.0, var lon: Double = 0.0, var ele: Double = 0.0,
                         var pointDelay: Long = 0)

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val MOCK_TRACK_DATA_FILENAME = "mock_track.gpx"
        const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1
        val handler = Handler()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableMockLocation.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
    }


    override fun onStart() {
        super.onStart()
        handleMockLocationAccess()

        handleStorage()
    }

    private fun handleStorage() {
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_EXTERNAL_STORAGE granted ")
            readGPXData()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE &&
                permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readGPXData()
        }
    }

    private fun readGPXData() {
        if (isExternalStorageReadable()) {
            Log.i(TAG, "externals storage is readable")
            val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath;
            val file = File("""${downloadsPath}/${MOCK_TRACK_DATA_FILENAME}""")
            if (file?.exists()) {
                Log.i(TAG, "data file exists")

                val bufferedReader: BufferedReader = file.bufferedReader()
                val inputString = bufferedReader.use { it.readText() }
                Log.i(TAG, inputString)
                val parseItems = ParseItems()
                parseItems.parse(inputString)
                for (item in parseItems.items) {
                    Log.i(TAG, "lat= ${item.lat}")
                    Log.i(TAG, "lon= ${item.lon}")
                    Log.i(TAG, "ele= ${item.ele}")
                    Log.i(TAG, "pointDelay=${item.pointDelay}")
                    handler.postDelayed({
                        val mockGPS = MockLocationProvider(LocationManager.GPS_PROVIDER, this)
                        val mockWifi = MockLocationProvider(LocationManager.NETWORK_PROVIDER, this)
                        Log.i(TAG, "pushing mock location")
                        mockGPS.pushLocation(item.lat, item.lon, item.ele, 0f)
                        mockWifi.pushLocation(item.lat, item.lon, item.ele, 0f)
                    }, item.pointDelay)
                }

            } else {
                Log.i(TAG, "data file doesn't exist")
            }
        }
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    private fun handleMockLocationAccess() {
        if (isMockSettingsON()) {
            mockLocationPermission.setText(R.string.mock_location_granted)
            mockLocationPermission.setTextColor(Color.GREEN)
            enableMockLocation.visibility = View.GONE
        } else {
            mockLocationPermission.setText(R.string.mock_location_not_granted)
            mockLocationPermission.setTextColor(Color.RED)
            enableMockLocation.visibility = View.VISIBLE
        }
    }

    private fun isMockSettingsON(): Boolean {
        var isMockLocation = false
        isMockLocation = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val opsManager = this.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION,
                        android.os.Process.myUid(),
                        BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED
            } else {
                // in marshmallow this will always return true
                android.provider.Settings.Secure.getString(this.contentResolver, "mock_location") != "0"
            }
        } catch (e: Exception) {
            return isMockLocation
        }
        return isMockLocation

    }
}
