package com.arcao.geocaching4locus.base.util

import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

/**
 * Two-way binding between an [EditText] and a [LiveData] value.
 */
fun EditText.bindText(
    owner: LifecycleOwner,
    source: LiveData<out CharSequence>,
    onTextChanged: (String) -> Unit
) {
    source.observe(owner) { value ->
        val newText = value ?: ""
        if (!newText.contentEquals(text)) {
            setText(newText)
        }
    }
    doAfterTextChanged { onTextChanged(it?.toString().orEmpty()) }
}
