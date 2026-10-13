package com.example.swiftyproteins

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.swiftyproteins.util.AppLifecycleObserver

class SwiftyProteinsApp : Application() {

    companion object {
        var isSessionLocked: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        val lifecycleObserver = AppLifecycleObserver {
            // Triggered whenever the entire application goes into background
            isSessionLocked = true
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }
}
