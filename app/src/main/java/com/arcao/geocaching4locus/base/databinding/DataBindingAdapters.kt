package com.arcao.geocaching4locus.base.databinding

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("goneUnless")
fun View.goneUnless(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

@BindingAdapter("goneIf")
fun View.goneIf(condition: Boolean) {
    visibility = if (condition) View.GONE else View.VISIBLE
}
