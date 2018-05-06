package com.ludoscity.getaroom

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

/**
 * Created by @f8full on 2018-05-05. This code is MIT licensed.
 * You may use the code only in compliance with the license
 * ViewModel class for data exchange
 */

class MainActivityViewModel : ViewModel() {

    private val mAuthCode = MutableLiveData<String>()
    val getAuthCode: LiveData<String>
        get() = mAuthCode

    fun setAuthCode(toSet: String){
        mAuthCode.postValue(toSet)
    }

    enum class NearbyConnectionState{
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private val mNearbyConnectionState = MutableLiveData<NearbyConnectionState>()
    val getNearbyConnectionState: LiveData<NearbyConnectionState>
        get() = mNearbyConnectionState

    fun setNearbyConnectionState(toSet: NearbyConnectionState){
        mNearbyConnectionState.postValue(toSet)
    }
}
