package com.ludoscity.getaroom

import android.Manifest
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import pub.devrel.easypermissions.EasyPermissions
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.Strategy.P2P_STAR
import pub.devrel.easypermissions.AfterPermissionGranted

/**
 * Created by @f8full on 2018-05-05. This code is MIT licensed.
 * You may use the code only in compliance with the license
 */
class MobileMainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks {
    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRationaleDenied(requestCode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRationaleAccepted(requestCode: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001

        const val RC_COARSE_LOCATION_PERM  = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_main)

        val activityModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        GoogleSignInHelper.init(this, activityModel)

        val v : View = findViewById(R.id.sign_in_button)
        v.setOnClickListener({
            Log.i(TAG, "Auth flow started")
            startActivityForResult(GoogleSignInHelper.instance.signInIntent, RC_SIGN_IN)
        })

        NearbyHelper.init(this, activityModel,
                NearbyHelper.NearbyConnectionLifecycleCallback(NearbyHelper.NearbyPayloadCallback()),
                AdvertisingOptions.Builder().setStrategy(P2P_STAR).build(),
                NearbyHelper.NearbyAdvertisingStartSuccessListener(),
                NearbyHelper.NearbyAdvertisingStartFailureListener())

        activityModel.getAuthCode.observeForever({ authCodeString ->
            if (!authCodeString.isNullOrEmpty()){
                onAuthCodeAvailable()
            }
        })
    }

    private fun hasCoarseLocationPermission(): Boolean{
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @AfterPermissionGranted(RC_COARSE_LOCATION_PERM)
    fun onAuthCodeAvailable() {
        if (hasCoarseLocationPermission()) {
            // Have permission, do the thing!
            NearbyHelper.instance.startAdvertising()
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                    this,
                    "Code exchange requires location permission",
                    RC_COARSE_LOCATION_PERM,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Log.i(TAG, "Sign in result available, processing...")
            GoogleSignInHelper.instance.processSigninRequestResult(data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}
