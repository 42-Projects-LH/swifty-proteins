package com.example.swiftyproteins.util

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(private val onBackgrounded: () -> Unit) : DefaultLifecycleObserver {
    override fun onStop(owner: LifecycleOwner) {
        // Triggered whenever the app goes into the background
        onBackgrounded()
    }
}
