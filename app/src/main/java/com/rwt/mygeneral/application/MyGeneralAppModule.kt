package com.rwt.mygeneral.application

import android.content.Context
import com.rwt.mygeneral.interfaces.AuthClientImpl
import com.rwt.mygeneral.interfaces.IAuthClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MyGeneralAppModule {
    @Provides
    fun provideAuthClient(
        @ApplicationContext appContext: Context
    ): IAuthClient {
        return AuthClientImpl(
            appContext
        )
    }
}
