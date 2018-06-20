package com.mockloc.jtymicki.androidmocklocation

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


private val i: Int by lazy {
    1
}

/**
 * Interface to the SendMockLocationService that sends mock locations into Location Services.
 *
 * This Activity collects parameters from the UI, sends them to the Service, and receives back
 * status messages from the Service.
 * <p>
 * The following parameters are sent:
 * <ul>
 * <li><b>Type of test:</b> one-time cycle through the mock locations, or continuous sending</li>
 * <li><b>Pause interval:</b> Amount of time (in seconds) to wait before starting mock location
 * sending. This pause allows the tester to switch to the app under test before sending begins.
 * </li>
 * <li><b>Send interval:</b> Amount of time (in seconds) before sending a new location.
 * This time is unrelated to the update interval requested by the app under test. For example, the
 * app under test can request updates every second, and the tester can request a mock location
 * send every five seconds. In this case, the app under test will receive the same location 5
 * times before a new location becomes available.
 * </li>
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private val MOCK_TRACK_DATA_FILENAME = "mock_track.gpx" // /sdcard/Download/mock_track.gpx
    private val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableMockLocation.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        // open file /sdcard/Download/mock_track.gpx
    }


    override fun onStart() {
        super.onStart()
        handleMockLocationAccess()

        handleStorage()
    }

    private fun handleStorage() {
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_EXTERNAL_STORAGE granted ")
            if (isExternalStorageReadable()) {
                Log.i(TAG, "externals storage is readable")
                val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath;
                val file = File("""${downloadsPath}/${MOCK_TRACK_DATA_FILENAME}""")
                if (file?.exists()) {
                    Log.i(TAG, "data file exists")
                } else {
                    Log.i(TAG, "data file doesn't exist")
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
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
            //if marshmallow
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
