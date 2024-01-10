package com.rwt.mygeneral.application

import android.app.Application
import android.util.Log
import com.rwt.mygeneral.interfaces.IAuthClient
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyGeneralApp : Application() {

    val TAG = "MyGeneralApp"
    @Inject lateinit var iAuthClient: IAuthClient

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Application triggered...");
        initializeAmplifyPlugins()

    } //end of onCreate

    private fun initializeAmplifyPlugins() {
        Log.i(TAG, "initializeAmplifyPlugins");
        iAuthClient.initPlugins()
    }
}