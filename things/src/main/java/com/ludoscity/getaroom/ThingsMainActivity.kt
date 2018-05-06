package com.ludoscity.getaroom

import android.app.Activity
import android.os.Bundle
import android.os.StrictMode

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class ThingsMainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //TODO: production code would perform network operations on a background thread
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val activityModel = MainActivityViewModel()

        AuthHelper.init(activityModel)
        GoogleCalendarHelper.init(activityModel)

        NearbyHelper.init(this, activityModel, NearbyHelper.NearbyConnectionLifecycleCallback(NearbyHelper.NearbyPayloadCallback()),
                NearbyHelper.NearbyEndpointDiscoveryCallback(NearbyHelper.NearbyConnectionLifecycleCallback(NearbyHelper.NearbyPayloadCallback())),
                NearbyHelper.NearbyDiscoveryStartSuccessListener(),
                NearbyHelper.NearbyDiscoveryStartFailureListener())

        NearbyHelper.instance.startDiscovery()
    }
}
