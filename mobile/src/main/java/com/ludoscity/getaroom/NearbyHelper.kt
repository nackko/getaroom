package com.ludoscity.getaroom

import android.content.*
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.lang.Exception

/**
 * Created by @f8full on 2018-05-05. This code is part of #getaroom and is MIT licensed
 * You may use this code only in compliance with the license.
 * Helper class managing a Bluetooth LE
 */

class NearbyHelper : ContextWrapper {

    open class NearbyPayloadCallback : PayloadCallback(){
        override fun onPayloadReceived(p0: String, p1: Payload) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        }

    }

    open class NearbyConnectionLifecycleCallback(private var mPayloadCallback: NearbyPayloadCallback) : ConnectionLifecycleCallback(){

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            when(p1.status.statusCode){
                ConnectionsStatusCodes.STATUS_OK -> {
                    Log.i(TAG, "Nearby connection successfully established. updating model")

                    mMainActivityModel.nearbyEndpointId = p0
                    mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.CONNECTED)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED, ConnectionsStatusCodes.STATUS_ERROR ->{
                    Log.w(TAG, "connection could not be established !")
                }
            }
        }

        override fun onDisconnected(p0: String) {
            Log.i(TAG, "Nearby connection disconnected. updating model")

            mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.DISCONNECTED)
        }

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            Log.i(TAG, "Nearby connection initiated - accepting")
            Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).acceptConnection(p0, mPayloadCallback)
            mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.CONNECTING)
        }

    }

    open class NearbyAdvertisingStartSuccessListener : OnSuccessListener<Void>{
        override fun onSuccess(p0: Void?) {
            Log.i(TAG, "...successfully started !")
        }
    }

    open class NearbyAdvertisingStartFailureListener : OnFailureListener{
        override fun onFailure(p0: Exception) {
            Log.i(TAG, "...failed to start ! :(")
        }
    }

    private lateinit var mConectionLifecycleCallback : NearbyConnectionLifecycleCallback

    //To protect against external construction
    @Suppress("unused")
    private constructor() : super(null)

    private lateinit var mAdvertisingOptions: AdvertisingOptions

    private lateinit var mAdvertisingStartSuccessListener: NearbyAdvertisingStartSuccessListener

    private lateinit var mAdvertisingStartFailureListener: NearbyAdvertisingStartFailureListener

    private constructor(ctx: Context, mainActivityModel: MainActivityViewModel,
                        nearbyConnectionLifecycleCallback: NearbyConnectionLifecycleCallback,
                        advertisingOptions: AdvertisingOptions,
                        advertisingStartSuccessListener: NearbyAdvertisingStartSuccessListener,
                        advertisingStartFailureListener: NearbyAdvertisingStartFailureListener) : super(ctx) {

        mMainActivityModel = mainActivityModel
        mConectionLifecycleCallback = nearbyConnectionLifecycleCallback
        mAdvertisingOptions = advertisingOptions
        mAdvertisingStartSuccessListener = advertisingStartSuccessListener
        mAdvertisingStartFailureListener = advertisingStartFailureListener

        mMainActivityModel.getNearbyConnectionState.observeForever({ nearbyConnectionState ->
            Log.i(TAG, "NearbyConnectionState updated in model")
            if (nearbyConnectionState == MainActivityViewModel.NearbyConnectionState.CONNECTED){
                Log.i(TAG, "Connected ! Sending auth code as BytePayload")
                Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).sendPayload(
                        mMainActivityModel.nearbyEndpointId,
                        Payload.fromBytes(mMainActivityModel.getAuthCode.value!!.toByteArray())
                )
            }
        })
    }

    fun startAdvertising() {
        Log.i(TAG, "Starting advertising...")
        Nearby.getConnectionsClient(applicationContext).startAdvertising(
                "getUserEmail",
                "com.ludoscity.getaroom",
                //SERVICE_ID,
                mConectionLifecycleCallback,
                mAdvertisingOptions)
                    .addOnSuccessListener(mAdvertisingStartSuccessListener)
                .addOnFailureListener(mAdvertisingStartFailureListener)
    }

    //TODO: write tests for this method
    fun unregisterResources(){
    }

    fun stopAdvertising() {


    }

    companion object {

        private const val TAG = "NearbyHelper"
        private var mMainActivityModel: MainActivityViewModel = MainActivityViewModel()

        private var mInstance: NearbyHelper? = null

        fun init(ctx: Context, mainActivityModel: MainActivityViewModel,
                 nearbyConnectionLifecycleCallback: NearbyConnectionLifecycleCallback,
                 advertisingOptions: AdvertisingOptions,
                 advertisingStartSuccessListener: NearbyAdvertisingStartSuccessListener,
                 advertisingStartFailureListener: NearbyAdvertisingStartFailureListener) {
            if (mInstance == null)
                mInstance = NearbyHelper(ctx, mainActivityModel,
                        nearbyConnectionLifecycleCallback, advertisingOptions,
                        advertisingStartSuccessListener, advertisingStartFailureListener)
            else {

            }
        }

        fun reinit(ctx: Context, mainActivityModel: MainActivityViewModel,
                   nearbyConnectionLifecycleCallback: NearbyConnectionLifecycleCallback,
                   advertisingOptions: AdvertisingOptions,
                   advertisingStartSuccessListener: NearbyAdvertisingStartSuccessListener,
                   advertisingStartFailureListener: NearbyAdvertisingStartFailureListener) {
            Log.w(TAG, "reinit shouldn't be called in normal execution context")

            if (mInstance == null)
                init(ctx, mainActivityModel, nearbyConnectionLifecycleCallback,
                        advertisingOptions,
                        advertisingStartSuccessListener, advertisingStartFailureListener)
            else {
                mInstance = NearbyHelper(ctx, mainActivityModel, nearbyConnectionLifecycleCallback,
                        advertisingOptions,
                        advertisingStartSuccessListener, advertisingStartFailureListener)
            }
        }

        val instance: NearbyHelper
            get() = if (mInstance == null)
                throw RuntimeException("You must call init(...) first")
            else
                mInstance!!
    }
}