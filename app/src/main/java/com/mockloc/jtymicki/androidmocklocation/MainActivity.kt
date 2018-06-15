package com.mockloc.jtymicki.androidmocklocation

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
