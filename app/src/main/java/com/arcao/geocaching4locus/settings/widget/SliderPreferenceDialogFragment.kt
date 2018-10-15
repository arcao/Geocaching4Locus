package com.arcao.geocaching4locus.settings.widget

import android.os.Bundle
import android.text.*
import android.text.method.NumberKeyListener
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.preference.PreferenceDialogFragmentCompat
import com.arcao.geocaching4locus.R
import org.apache.commons.lang3.StringUtils

class SliderPreferenceDialogFragment : PreferenceDialogFragmentCompat() {
    private var value: Int = 0
    private var min: Int = 0
    private var max: Int = 0
    private var step: Int = 0
    private var dialogMessage: CharSequence? = null

    private val sliderPreference: SliderPreference
        get() = preference as SliderPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        dialogMessage = sliderPreference.dialogMessage
        min = sliderPreference.min
        max = sliderPreference.max
        step = sliderPreference.step
        value = sliderPreference.progress

        if (savedInstanceState != null) {
            value = savedInstanceState.getInt(SAVE_STATE_VALUE, value)
        }
    }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVE_STATE_VALUE, value)
    }

    override fun onBindDialogView(@NonNull v: View) {
        val messageView = v.findViewById<TextView>(R.id.message)
        val seekBar = v.findViewById<SeekBar>(R.id.seekbar)
        val editText = v.findViewById<EditText>(R.id.input)

        if (value % step != 0) {
            value = value / step * step
        }

        if (value < min) {
            value = min
        }

        if (!TextUtils.isEmpty(dialogMessage)) {
            messageView.visibility = View.VISIBLE
            messageView.text = dialogMessage
        } else {
            messageView.visibility = View.GONE
        }

        seekBar.max = (max - min) / step
        seekBar.progress = (value - min) / step
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                value = progress * step + min
                if (fromUser) {
                    editText.setText(value.toString())
                    editText.setSelection(editText.text.length)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        editText.setText(value.toString())
        editText.setSelection(editText.text.length)
        editText.filters = arrayOf(InputTextFilter(editText, min, max, step))
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                value = min
                try {
                    value = Integer.parseInt(s.toString())
                } catch (e: NumberFormatException) {
                    // fall trough
                }

                seekBar.progress = (value - min) / step
            }
        })
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            if (sliderPreference.callChangeListener(value)) {
                sliderPreference.progress = value
            }
        }
    }

    private class InputTextFilter internal constructor(internal val editText: EditText, private val min: Int, private val max: Int, step: Int) : NumberKeyListener() {
        private val availableValues: Array<String>?

        init {
            if (step == 1) {
                availableValues = null
            } else {
                val values = arrayOfNulls<String>((max - min) / step + 1)

                for (i in 0 until values.size) {
                    values[i] = (min + i * step).toString()
                }
                @Suppress("UNCHECKED_CAST")
                availableValues = values as Array<String>
            }
        }

        override fun getInputType(): Int {
            return InputType.TYPE_CLASS_TEXT
        }

        @NonNull
        override fun getAcceptedChars(): CharArray {
            return DIGIT_CHARACTERS
        }

        override fun filter(@NonNull source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
            if (availableValues == null || availableValues.isEmpty()) {
                var filtered: CharSequence? = super.filter(source, start, end, dest, dstart, dend)
                if (filtered == null) {
                    filtered = source.subSequence(start, end)
                }

                val result = (dest.subSequence(0, dstart).toString() + filtered
                        + dest.subSequence(dend, dest.length))

                if (TextUtils.isEmpty(result)) {
                    return result
                }

                var value = min
                try {
                    value = Integer.parseInt(result)
                } catch (e: NumberFormatException) {
                    // do nothing
                }

                return if (value > max) {
                    StringUtils.EMPTY
                } else {
                    filtered
                }
            } else {
                val filtered = source.subSequence(start, end).toString()
                if (TextUtils.isEmpty(filtered)) {
                    return StringUtils.EMPTY
                }
                val result = (dest.subSequence(0, dstart).toString() + filtered
                        + dest.subSequence(dend, dest.length))

                for (value in availableValues) {
                    if (value.startsWith(result)) {
                        postSetSelection(result.length, value.length)
                        return value.subSequence(dstart, value.length)
                    }
                }
                return StringUtils.EMPTY
            }
        }

        private fun postSetSelection(start: Int, stop: Int) {
            editText.post {
                val len = editText.text.length
                editText.setSelection(Math.min(start, len), Math.min(stop, len))
            }
        }

        companion object {
            private val DIGIT_CHARACTERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        }
    }

    companion object {
        private const val SAVE_STATE_VALUE = "SliderPreferenceDialogFragment.value"

        fun newInstance(key: String): SliderPreferenceDialogFragment {
            val fragment = SliderPreferenceDialogFragment()
            val b = Bundle(1)
            b.putString(PreferenceDialogFragmentCompat.ARG_KEY, key)
            fragment.arguments = b
            return fragment
        }
    }
}
