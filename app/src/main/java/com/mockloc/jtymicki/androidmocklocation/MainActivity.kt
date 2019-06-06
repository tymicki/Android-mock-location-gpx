package com.mockloc.jtymicki.androidmocklocation

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

data class TrackingPoint(var lat: Double = 0.0, var lon: Double = 0.0, var ele: Double = 0.0,
                         var pointDelay: Long = 0)

private const val TAG = "MainActivity"
private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableMockLocation.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        runGPXMockLocations.setOnClickListener {
            loadGPXMockLocations()
        }
    }


    override fun onStart() {
        super.onStart()
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_EXTERNAL_STORAGE granted ")
            readExternalStoragePermissionStatus.setText(R.string.read_external_storage_permission_granted)
            readExternalStoragePermissionStatus.setTextColor(Color.GREEN)
        } else {
            readExternalStoragePermissionStatus.setText(R.string.read_external_storage_permission_not_granted)
            readExternalStoragePermissionStatus.setTextColor(Color.RED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }
        handleMockLocationAccess()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE &&
                permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            readExternalStoragePermissionStatus.setText(R.string.read_external_storage_permission_granted)
            readExternalStoragePermissionStatus.setTextColor(Color.GREEN)
        }
    }

    private fun loadGPXMockLocations() {
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_EXTERNAL_STORAGE granted ")
            MockRoute().pushMockRoute(this)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        }
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
