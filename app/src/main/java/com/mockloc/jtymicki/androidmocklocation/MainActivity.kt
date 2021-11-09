package com.mockloc.jtymicki.androidmocklocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_main.*

data class TrackingPoint(
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var altitude: Double = 0.0,
    var accuracy: Float = 0f,
    var pointDelay: Long = 0,
    var timestamp: Long = 0
)

private const val TAG = "MainActivity"
private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1
private const val PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 2
private const val PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 3
private const val pickerInitialUri = "content://com.android.externalstorage.documents/document/primary%3AMocks"
private const val OPEN_DOCUMENT_REQUEST_CODE = 10

class MainActivity : AppCompatActivity() {

    var mockRoute = MockRoute()
    lateinit var timeMultiplerSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableMockLocation.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }
        runGPXMockLocations.setOnClickListener {
            loadGPXMockLocations()
        }
        clearGPXMockLocations.setOnClickListener {
            clearGPXMockLocations()
        }
        clearGPXMockLocations.visibility = View.GONE

        timeMultiplerSpinner = findViewById<Spinner>(R.id.timeMultiplerSpinner)
        val items = arrayOf("x1", "x2", "x5", "x10", "x25", "x100")
        val adapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        timeMultiplerSpinner.setAdapter(adapter)
        timeMultiplerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                arg0: AdapterView<*>?,
                arg1: View?,
                arg2: Int,
                arg3: Long
            ) {
                val item = timeMultiplerSpinner.selectedItem.toString().substring(1)
                Log.i(TAG, "set time factor: $item")
                mockRoute.timeFactor = item.toInt()
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }


    override fun onStart() {
        super.onStart()
        onStartOrResume()
    }

    override fun onResume() {
        super.onResume()
        onStartOrResume()
    }

    private fun onStartOrResume() {
        checkReadExternalStoragePermission(true)
        checkCoarseLocationPermission(true)
        checkFineLocationPermission(true)
        handleMockLocationAccess()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkReadExternalStoragePermission()
        checkCoarseLocationPermission()
        checkFineLocationPermission()
    }

    private fun checkReadExternalStoragePermission(requestIfNotGranted: Boolean = false): Boolean {
        var granted = false
        if (checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "READ_EXTERNAL_STORAGE granted ")
            readExternalStoragePermissionStatus.setText(R.string.read_external_storage_permission_granted)
            readExternalStoragePermissionStatus.setTextColor(Color.GREEN)
            granted = true
        } else {
            readExternalStoragePermissionStatus.setText(R.string.read_external_storage_permission_not_granted)
            readExternalStoragePermissionStatus.setTextColor(Color.RED)
            if(requestIfNotGranted) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
            granted = false
        }
        return granted
    }

    private fun checkCoarseLocationPermission(requestIfNotGranted: Boolean = false): Boolean {
        var granted = false
        if (checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "ACCESS_COARSE_LOCATION granted ")
            accessCoarseLocationPermissionStatus.setText(R.string.access_coarse_location_permission_granted)
            accessCoarseLocationPermissionStatus.setTextColor(Color.GREEN)
            granted = true
        } else {
            accessCoarseLocationPermissionStatus.setText(R.string.access_coarse_location_permission_not_granted)
            accessCoarseLocationPermissionStatus.setTextColor(Color.RED)
            if(requestIfNotGranted) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSION_REQUEST_ACCESS_COARSE_LOCATION)
            granted = false
        }
        return granted
    }

    private fun checkFineLocationPermission(requestIfNotGranted: Boolean = false): Boolean {
        var granted = false
        if (checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "ACCESS_COARSE_LOCATION granted ")
            accessFineLocationPermissionStatus.setText(R.string.access_fine_location_permission_granted)
            accessFineLocationPermissionStatus.setTextColor(Color.GREEN)
            granted = true
        } else {
            accessFineLocationPermissionStatus.setText(R.string.access_fine_location_permission_not_granted)
            accessFineLocationPermissionStatus.setTextColor(Color.RED)
            if(requestIfNotGranted) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_ACCESS_COARSE_LOCATION)
            granted = false
        }
        return granted
    }

    private fun loadGPXMockLocations() {
        if(checkReadExternalStoragePermission() && checkCoarseLocationPermission() && checkFineLocationPermission()){
            openDocumentPicker()
        }
    }

    @SuppressLint("MissingPermission")
    private fun clearGPXMockLocations() {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if(MockRoute.Companion.hasLocationPermission(this)){
            fusedLocationProviderClient.setMockMode(false)
            fusedLocationProviderClient.flushLocations()
        }
        mockRoute.clearRoute()
        runGPXMockLocations.visibility = View.VISIBLE
        timeMultiplerSpinner.visibility = View.VISIBLE
        timeMultiplerTextView.visibility = View.VISIBLE
        clearGPXMockLocations.visibility = View.GONE
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

    private fun openDocumentPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)

            /**
             * Because we'll want to use [ContentResolver.openFileDescriptor] to read
             * the data of whatever file is picked, we set [Intent.CATEGORY_OPENABLE]
             * to ensure this will succeed.
             */
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        intent.setType("*/*")
        startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
    }

    private fun openDocument(documentUri: Uri) {
        runGPXMockLocations.visibility = View.GONE
        timeMultiplerSpinner.visibility = View.GONE
        timeMultiplerTextView.visibility = View.GONE
        clearGPXMockLocations.visibility = View.VISIBLE
        mockRoute.pushMockRoute(this, documentUri)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { documentUri ->
                Log.d(TAG, "Loaded document: $documentUri");
                contentResolver.takePersistableUriPermission(
                    documentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                openDocument(documentUri)
            }
        }
    }
}
