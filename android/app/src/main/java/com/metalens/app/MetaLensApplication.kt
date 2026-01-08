package com.metalens.app

import android.app.Application

class MetaLensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: MetaLensApplication
            private set
    }
}

