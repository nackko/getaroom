package com.ludoscity.getaroom

import android.util.Log
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import java.io.StringReader

/**
 * Created by @f8full on 2018-05-05. This code is MIT licensed
 * You may use the code only in compliance with the license
 * Helper class managing GoogleCredential object.
 */

class AuthHelper {

    companion object {
        private const val TAG = "AuthHelper"

        private var mInstance: AuthHelper? = null
        private lateinit var mActivityModel: MainActivityViewModel

        val instance: AuthHelper
            get() {
                if (mInstance == null)
                    throw RuntimeException("You must call init(...) first")

                return mInstance as AuthHelper
            }

        fun init(activityModel: MainActivityViewModel) {

            if (mInstance == null)
                mInstance = AuthHelper(activityModel)
        }
    }

    private lateinit var mGoogleCredential : GoogleCredential
    val googleCredential : GoogleCredential
        get() = mGoogleCredential

    val accessToken: String
        get() = mGoogleCredential.accessToken
    //both those are convenience
    val refreshToken: String
        get() = mGoogleCredential.refreshToken


    fun buildCredentialsAndUpdateActivityModel(tokenResponse: TokenResponse?){

        val CLIENT_SECRET_DATA = "{\"web\":{\"client_id\":\"818157017237-hidphgq8htcqmc03bjn8r112buvbtdmn.apps.googleusercontent.com\",\"project_id\":\"getaroom-1525560589369\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"xqvE9W3Go8wJUG5HZhhjAWoz\"}}"
        //Supposed to come from a file read through a FileReader. File is retrieved from https://console.developers.google.com/apis/credentials

        val clientSecrets = GoogleClientSecrets.load(JacksonFactory(), StringReader(CLIENT_SECRET_DATA))

        mGoogleCredential = GoogleCredential.Builder()
                .setJsonFactory(JacksonFactory())
                .setTransport(NetHttpTransport())
                .setClientSecrets(clientSecrets).build().setFromTokenResponse(tokenResponse)

        Log.i(TAG, "Credentials built, we're logged in, updating model Login state")
        Log.i(TAG, "Access token :" + mGoogleCredential.accessToken)
        Log.i(TAG, "Refresh token :" + mGoogleCredential.refreshToken)


        mActivityModel.setLoginState(MainActivityViewModel.LoginState.LOGGED_IN)
    }

    //To protect against external construction
    @Suppress("unused")
    private constructor()

    private constructor(activityModel: MainActivityViewModel) {

        mActivityModel = activityModel

        mActivityModel.getAuthCode.observeForever({ authcode ->
            if (!authcode.isNullOrEmpty()){

                Log.i(TAG, "auth code available, let's exchange it for tokens")

                val CLIENT_SECRET_DATA = "{\"web\":{\"client_id\":\"818157017237-hidphgq8htcqmc03bjn8r112buvbtdmn.apps.googleusercontent.com\",\"project_id\":\"getaroom-1525560589369\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"xqvE9W3Go8wJUG5HZhhjAWoz\"}}"
                //Supposed to come from a file read through a FileReader. File is retrieved from https://console.developers.google.com/apis/credentials

                val clientSecrets = GoogleClientSecrets.load(JacksonFactory(), StringReader(CLIENT_SECRET_DATA))

                val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                        NetHttpTransport(),
                        JacksonFactory(),
                        "https://www.googleapis.com/oauth2/v4/token",
                        clientSecrets.details.clientId,
                        clientSecrets.details.clientSecret,
                        authcode,
                        "" )
                        .execute()  //This is a network operation

                buildCredentialsAndUpdateActivityModel(tokenResponse)
            }
        })
    }
}