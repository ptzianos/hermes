package org.hermes

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
//import com.squareup.leakcanary.LeakCanary

import org.hermes.di.DaggerHermesComponent




class HermesClientApp : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerHermesComponent
            .builder()
            .application(this)
            .build()
    }

//    override fun onCreate() {
//        super.onCreate()
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this)
//        }
//    }

}
