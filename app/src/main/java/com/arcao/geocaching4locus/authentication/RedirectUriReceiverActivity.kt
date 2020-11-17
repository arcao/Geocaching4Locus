package com.arcao.geocaching4locus.authentication

import android.app.Activity
import android.os.Bundle

class RedirectUriReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // redirect to LoginActivity with FLAG_SINGLE_TOP and FLAG_CLEAR_TOP to close Chrome Custom
        // Tab
        startActivity(LoginActivity.createResponseHandlingIntent(this, this.intent.data))
        finish()
    }
}
