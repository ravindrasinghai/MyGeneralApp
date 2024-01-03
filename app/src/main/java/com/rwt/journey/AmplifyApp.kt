package com.rwt.mygeneral

/*
import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify

class AmplifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MyGeneral", "AmplifyApp: Application triggered...");

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.configure(applicationContext)
            Log.i("MyGeneral", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyGeneral", "Could not initialize Amplify", error)
        }
    }
}
*/


import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.datastore.AWSDataStorePlugin
import com.amplifyframework.kotlin.core.Amplify.Companion.addPlugin
import com.amplifyframework.kotlin.core.Amplify.Companion.configure

class AmplifyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MyGeneral", "AmplifyApp: Application triggered...");

        try {
            addPlugin(AWSDataStorePlugin())
            addPlugin(AWSApiPlugin())
            addPlugin(AWSCognitoAuthPlugin())
            configure(applicationContext)
            Log.i("MyGeneral", "Plugins added successfully");
        } catch (e : AmplifyException) {
            Log.e("MyGeneral", "oops, something went wrong");
            e.printStackTrace();
        }
    }
}