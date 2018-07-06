package com.arcao.geocaching4locus.authentication.fragment;

import android.content.Intent;

public interface OAuthLoginDialogListener {
    void onLoginFinished(Intent errorIntent);
}
