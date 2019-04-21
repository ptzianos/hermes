package org.hermes

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject

open class BaseActivity: AppCompatActivity() {

    @Inject
    lateinit var hermesLifeCycleObserver: HermesLifeCycleObserver

    override fun onResume() {
        super.onResume()
        hermesLifeCycleObserver.resume(this)
    }

    override fun onPause() {
        super.onPause()
        hermesLifeCycleObserver.pause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        hermesLifeCycleObserver.destroy(this)
    }

    override fun onStop() {
        super.onStop()
        hermesLifeCycleObserver.stop(this)
    }
}