package com.ludoscity.getaroom

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.calendar.CalendarScopes

/**
 * Created by @f8full on 2018-05-05. This code is part of #getaroom and is MIT licensed
 * You may use this code only in compliance with the license.
 * Helper class managing a GoogleSignIn object set for offline access
 */

class GoogleSignInHelper {

    companion object {
        private const val TAG = "GoogleSignInHelper"

        private var mInstance: GoogleSignInHelper? = null
        private var mActivityModel: MainActivityViewModel? = null

        val instance: GoogleSignInHelper
            get() {
                if (mInstance == null)
                    throw RuntimeException("You must call init(...) first")

                return mInstance as GoogleSignInHelper
            }

        fun init(activity: Activity, activityModel: MainActivityViewModel) {

            if (mInstance == null)
                mInstance = GoogleSignInHelper(activity, activityModel)
        }
    }

    private var mGoogleSignInClient: GoogleSignInClient? = null

    val signInIntent: Intent
        get() = mGoogleSignInClient!!.signInIntent

    //To protect against external construction
    @Suppress("unused")
    private constructor()

    private constructor(activity: Activity, activityModel: MainActivityViewModel) {
        //Options setup to use offline access, as described here
        //https://developers.google.com/identity/sign-in/android/offline-access
        //to manage calendars
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Scope(CalendarScopes.CALENDAR))
                .requestServerAuthCode("818157017237-hidphgq8htcqmc03bjn8r112buvbtdmn.apps.googleusercontent.com")
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)

        mActivityModel = activityModel

    }

    fun processSigninRequestResult(resultData: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(resultData)
        try {
            val account = task.getResult(ApiException::class.java)
            Log.i(TAG, "Authentication successful, writing auth code to model")
            mActivityModel?.setAuthCode(account.serverAuthCode!!)
        } catch (e: ApiException) {
            // Google Sign In failed, update UI appropriately
            Log.w(TAG, "Google sign in failed", e)
        }
    }

    fun signOut(activity: Activity) {
        mGoogleSignInClient!!.signOut().addOnCompleteListener(activity) { _ ->  }
    }
}