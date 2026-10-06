package com.arcao.geocaching4locus.live_map

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Invisible activity which starts the Live map foreground service and finishes immediately.
 *
 * It is opened by an action of the Live map notification. The foreground service of the `dataSync`
 * type can't be started from a background after its time limit was reached, until the app is
 * visible. Showing this activity makes the app visible for a moment, which allows it.
 */
class LiveMapResumeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LiveMapService.enable(this)
        finish()
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, LiveMapResumeActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION)
    }
}
