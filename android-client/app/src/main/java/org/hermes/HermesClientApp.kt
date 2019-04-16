package org.hermes

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
//import com.squareup.leakcanary.LeakCanary

import org.hermes.di.DaggerHermesComponent
import org.hermes.di.HermesComponent


class HermesClientApp : DaggerApplication() {

    lateinit var daggerHermesComponent: HermesComponent

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        daggerHermesComponent = DaggerHermesComponent
            .builder()
            .application(this)
            .app(this)
            .build()
        return daggerHermesComponent
    }

}
