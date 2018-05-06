package com.ludoscity.getaroom

import android.content.*
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_ERROR
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes.STATUS_OK
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.lang.Exception
import kotlin.text.Charsets.UTF_8

/**
 * Created by @f8full on 2018-05-05. This code is part of #getaroom and is MIT licensed
 * You may use this code only in compliance with the license.
 * Helper class managing Nearby
 */

class NearbyHelper : ContextWrapper {

    open class NearbyEndpointDiscoveryCallback(private val mConnectionLifecycleCallback: NearbyConnectionLifecycleCallback) : EndpointDiscoveryCallback(){
        override fun onEndpointFound(endpointId: String?, p1: DiscoveredEndpointInfo?) {
            Log.i(TAG, "Nearby endpoint found, requesting connection...")
            Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).requestConnection(
                    "getUserEmail",
                    endpointId!!,
                    mConnectionLifecycleCallback)
            mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.CONNECTING)
        }

        override fun onEndpointLost(p0: String?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class NearbyPayloadCallback : PayloadCallback(){
        override fun onPayloadReceived(p0: String, p1: Payload) {
            val authcode = String(p1.asBytes()!!,UTF_8)
            Log.i(TAG, "BytePayload received :$authcode")

            mMainActivityModel.setAuthCode(authcode)

            Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).stopDiscovery()
            //Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).disconnectFromEndpoint(p0)
        }
        override fun onPayloadTransferUpdate(p0: String, p1: PayloadTransferUpdate) {
        }
    }

    open class NearbyConnectionLifecycleCallback(private var mPayloadCallback: NearbyPayloadCallback) : ConnectionLifecycleCallback(){

        override fun onConnectionInitiated(p0: String, p1: ConnectionInfo) {
            Log.i(TAG, "Nearby connection initiated - accepting")
            Nearby.getConnectionsClient(NearbyHelper.instance.applicationContext).acceptConnection(p0, mPayloadCallback)
        }

        override fun onConnectionResult(p0: String, p1: ConnectionResolution) {
            when(p1.status.statusCode){
                STATUS_OK -> {
                    Log.i(TAG, "Nearby connection successfully established")
                    mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.CONNECTED)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED, STATUS_ERROR ->{
                    Log.w(TAG, "connection could NOT be established !")
                }
            }
        }

        override fun onDisconnected(p0: String) {
            Log.i(TAG, "disconnected")
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class NearbyDiscoveryStartSuccessListener : OnSuccessListener<Void>{
        override fun onSuccess(p0: Void?) {
            Log.i(TAG, "...successfully started !")
        }
    }

    open class NearbyDiscoveryStartFailureListener : OnFailureListener{
        override fun onFailure(p0: Exception) {
            Log.i(TAG, "...failed to start ! :(")
        }
    }

    private lateinit var mConectionLifecycleCallback : NearbyConnectionLifecycleCallback
    private lateinit var mEndpointDiscoveryCallback: EndpointDiscoveryCallback

    //To protect against external construction
    @Suppress("unused")
    private constructor() : super(null)

    private lateinit var mDiscoveryStartSuccessListener: NearbyDiscoveryStartSuccessListener

    private lateinit var mDiscoveryStartFailureListener: NearbyDiscoveryStartFailureListener

    private constructor(ctx: Context, mainActivityModel: MainActivityViewModel,
                        nearbyConnectionLifecycleCallback: NearbyConnectionLifecycleCallback,
                        nearbyEndpointDiscoveryCallback: NearbyEndpointDiscoveryCallback,
                        discoveryStartSuccessListener: NearbyDiscoveryStartSuccessListener,
                        discoveryStartFailureListener: NearbyDiscoveryStartFailureListener) : super(ctx) {

        mMainActivityModel = mainActivityModel
        mMainActivityModel.setNearbyConnectionState(MainActivityViewModel.NearbyConnectionState.DISCONNECTED)
        mEndpointDiscoveryCallback = nearbyEndpointDiscoveryCallback
        mConectionLifecycleCallback = nearbyConnectionLifecycleCallback
        mDiscoveryStartSuccessListener = discoveryStartSuccessListener
        mDiscoveryStartFailureListener = discoveryStartFailureListener


    }

    fun startDiscovery() {
        Log.i(TAG, "Starting discovery...")
        Nearby.getConnectionsClient(applicationContext).startDiscovery(
                "com.ludoscity.getaroom", //SERVICE_ID
                mEndpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        ).addOnSuccessListener(mDiscoveryStartSuccessListener)
                .addOnFailureListener(mDiscoveryStartFailureListener)
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
                 nearbyEndpointDiscoveryCallback: NearbyEndpointDiscoveryCallback,
                 discoveryStartSuccessListener: NearbyDiscoveryStartSuccessListener,
                 discoveryStartFailureListener: NearbyDiscoveryStartFailureListener) {
            if (mInstance == null)
                mInstance = NearbyHelper(ctx, mainActivityModel,
                        nearbyConnectionLifecycleCallback,
                        nearbyEndpointDiscoveryCallback,
                        discoveryStartSuccessListener, discoveryStartFailureListener)
            else {

            }
        }

        fun reinit(ctx: Context, mainActivityModel: MainActivityViewModel,
                   nearbyConnectionLifecycleCallback: NearbyConnectionLifecycleCallback,
                   nearbyEndpointDiscoveryCallback: NearbyEndpointDiscoveryCallback,
                   discoveryStartSuccessListener: NearbyDiscoveryStartSuccessListener,
                   discoveryStartFailureListener: NearbyDiscoveryStartFailureListener) {
            Log.w(TAG, "reinit shouldn't be called in normal execution context")

            if (mInstance == null)
                init(ctx, mainActivityModel, nearbyConnectionLifecycleCallback,
                        nearbyEndpointDiscoveryCallback,
                        discoveryStartSuccessListener, discoveryStartFailureListener)
            else {
                mInstance = NearbyHelper(ctx, mainActivityModel, nearbyConnectionLifecycleCallback,
                        nearbyEndpointDiscoveryCallback,
                        discoveryStartSuccessListener, discoveryStartFailureListener)
            }
        }

        val instance: NearbyHelper
            get() = if (mInstance == null)
                throw RuntimeException("You must call init(...) first")
            else
                mInstance!!
    }
}