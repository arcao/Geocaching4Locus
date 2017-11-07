package com.arcao.geocaching4locus.authentication.util;

import com.github.scribejava.core.model.OAuthAsyncRequestCallback;

public class OAuthAsyncRequestCallbackAdapter<T> implements OAuthAsyncRequestCallback<T> {
    @Override
    public void onCompleted(T response) {
    }

    @Override
    public void onThrowable(Throwable t) {
    }
}
