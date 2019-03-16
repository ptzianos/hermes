package org.hermes.client

import android.app.Application
import com.squareup.leakcanary.LeakCanary

class HermesClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Probably put some leak canary calls over here
    }
}