package com.ludoscity.getaroom

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View

/**
 * Created by @f8full on 2018-05-05. This code is MIT licensed.
 * You may use the code only in compliance with the license
 */
class MobileMainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
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
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            Log.i(TAG, "Sign in result available, processing...")
            GoogleSignInHelper.instance.processSigninRequestResult(data)
        }
    }
}
