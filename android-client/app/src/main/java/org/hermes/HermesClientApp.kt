package org.hermes

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
//import com.squareup.leakcanary.LeakCanary

import org.hermes.di.DaggerHermesComponent
import org.hermes.di.HermesComponent
import javax.inject.Inject


class HermesClientApp : DaggerApplication(), HasSupportFragmentInjector {

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>

    lateinit var daggerHermesComponent: HermesComponent


    val toastHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            try {
                Toast.makeText(applicationContext, inputMessage.obj as String, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {}
        }
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        daggerHermesComponent = DaggerHermesComponent
            .builder()
            .application(this)
            .app(this)
            .build()
        return daggerHermesComponent
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return supportFragmentInjector
    }
}
