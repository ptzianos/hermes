package org.hermes.activities

import android.app.Application
//import com.squareup.leakcanary.LeakCanary

class HermesClientApp : Application() {
    override fun onCreate() {
        super.onCreate()
//        if (!LeakCanary.isInAnalyzerProcess(this)) {
//            LeakCanary.install(this)
//        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Probably put some leak canary calls over here
    }
}

//import android.util.Log;
//
//public class App extends Application {
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        System.loadLibrary("dummy");
//        Log.e("IOTA", "Library loaded.");
//
