package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.di.AppContainerImpl
import com.google.android.gms.ads.MobileAds

class SacredWordApplication : Application() {
    lateinit var container: AppContainer

    companion object {
        private var _instance: SacredWordApplication? = null
        val instance: SacredWordApplication
            get() = _instance ?: throw IllegalStateException("Application class not initialized yet")
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        container = AppContainerImpl(this)
    }
}


