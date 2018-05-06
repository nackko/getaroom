package com.ludoscity.getaroom

import android.util.Log
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import java.io.IOException

/**
 * Created by @f8full on 2018-05-06. This code is MIT licensed
 * You may use the code only in compliance with the license
 * Helper class managing Google Calendar API client
 */
class GoogleCalendarHelper {

    companion object {
        private const val TAG = "GoogleCalendarHelper"

        private var mInstance: GoogleCalendarHelper? = null
        private lateinit var mActivityModel: MainActivityViewModel

        val instance: GoogleCalendarHelper
            get() {
                if (mInstance == null)
                    throw RuntimeException("You must call init(...) first")

                return mInstance as GoogleCalendarHelper
            }

        fun init(activityModel: MainActivityViewModel) {

            if (mInstance == null)
                mInstance = GoogleCalendarHelper(activityModel)
        }
    }

    /**
     * Fetch a list of the next 10 events from the primary calendar.
     * @return List of Strings describing returned events.
     * @throws IOException
     */
    // List the next 10 events from the primary calendar.
    // All-day events don't have start times, so just use
    // the start date.
    val dataFromApi: List<String> = ArrayList()
        @Throws(IOException::class)
        get() {
            val now = DateTime(System.currentTimeMillis())
            val eventStrings = ArrayList<String>()
            try {
                val events = calendarAPIClient.events().list("primary") //User primary calendar
                        .setMaxResults(10)
                        .setTimeMin(now)
                        .setOrderBy("startTime")
                        .setSingleEvents(true)
                        .execute()  //network operation
                val items = events.items

                for (event in items) {
                    var start = event.start.dateTime
                    if (start == null) {
                        start = event.start.date
                    }
                    eventStrings.add(
                            String.format("%s (%s)", event.summary, start))
                }
            } catch (e: GoogleJsonResponseException){
                Log.e(TAG, "Error", e)
                if (e.statusCode == 401){
                    Log.w("proutprout", "Token expired, trying a refresh")
                    return field
                }
            }
            return eventStrings
        }

    lateinit var calendarAPIClient : com.google.api.services.calendar.Calendar

    //To protect against external construction
    @Suppress("unused")
    private constructor()

    private constructor(activityModel: MainActivityViewModel) {

        mActivityModel = activityModel

        mActivityModel.getLoginState.observeForever({ loginState ->
            if (loginState == MainActivityViewModel.LoginState.LOGGED_IN) {

                Log.i(TAG, "Login detected, building client")
                calendarAPIClient = Calendar(NetHttpTransport(), JacksonFactory(), AuthHelper.instance.googleCredential)


                //Now we're ready to use the api, here reading the 10 next events
                Log.i(TAG, "client built, next 10 events of user primary calendar :$dataFromApi")
            }
        })
    }
}