package com.arcao.geocaching4locus.base.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.NumberKeyListener
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.arcao.geocaching4locus.R
import java.lang.ref.WeakReference

class SliderDialogFragment : AbstractDialogFragment(), SeekBar.OnSeekBarChangeListener, TextWatcher {

    @BindView(R.id.seekbar)
    internal lateinit var seekBarView: SeekBar
    @BindView(R.id.input)
    internal lateinit var editTextView: EditText
    @BindView(R.id.message)
    internal lateinit var messageView: TextView

    private var minValue: Int = 0
    private var maxValue = 100
    private var valueStep = 1
    internal var value = minValue
    private var newValue = value

    private lateinit var dialogListenerRef: WeakReference<DialogListener>

    interface DialogListener {
        fun onDialogClosed(fragment: SliderDialogFragment)
    }

    fun getValue(): Int {
        return newValue
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        try {
            dialogListenerRef = WeakReference(requireActivity() as DialogListener)
        } catch (e: ClassCastException) {
            throw ClassCastException("${requireActivity()} must implement DialogListeners")
        }
    }

    private fun fireDialogClose() {
        val listener = dialogListenerRef.get()
        listener?.onDialogClosed(this)
    }

    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val arguments = requireArguments()

        arguments.apply {
            minValue = getInt(PARAM_MIN, minValue)
            maxValue = getInt(PARAM_MAX, maxValue)
            valueStep = getInt(PARAM_STEP, valueStep)
            value = getInt(PARAM_DEFAULT_VALUE, value)
        }

        if (value % valueStep != 0) {
            value = value / valueStep * valueStep
        }

        if (value < minValue) {
            value = minValue
        }

        newValue = value

        if (savedInstanceState != null) {
            value = savedInstanceState.getInt(PARAM_DEFAULT_VALUE, value)
        }

        return MaterialDialog.Builder(requireActivity())
                .title(arguments.getInt(PARAM_TITLE))
                .customView(prepareView(), false)
                .positiveText(R.string.button_ok)
                .negativeText(R.string.button_cancel)
                .onAny { _, dialogAction ->
                    if (dialogAction == DialogAction.POSITIVE)
                        newValue = value

                    fireDialogClose()
                }
                .build()
    }

    override fun onSaveInstanceState(@NonNull outState: Bundle) {
        outState.putInt(PARAM_DEFAULT_VALUE, value)
        super.onSaveInstanceState(outState)
    }

    private fun prepareView(): View {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(activity).inflate(R.layout.view_slider, null, false)
        ButterKnife.bind(this, view)

        val message = requireArguments().getInt(PARAM_MESSAGE)
        messageView.apply {
            if (message != 0) {
                visibility = View.VISIBLE
                setText(message)
            } else {
                visibility = View.GONE
            }
        }

        seekBarView.apply {
            max = (maxValue - minValue) / valueStep
            progress = (value - minValue) / valueStep
            setOnSeekBarChangeListener(this@SliderDialogFragment)
        }

        editTextView.apply {
            setText(value.toString())
            setSelection(text.length)
            filters = arrayOf<InputFilter>(InputTextFilter(this, minValue, maxValue, valueStep))
            addTextChangedListener(this@SliderDialogFragment)
        }

        return view
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        value = progress * valueStep + minValue
        if (fromUser) {
            editTextView.apply {
                setText(value.toString())
                setSelection(text.length)
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        value = minValue
        try {
            value = s.toString().toInt()
        } catch (e: NumberFormatException) {
            // fall trough
        }

        seekBarView.progress = (value - minValue) / valueStep
    }

    private class InputTextFilter internal constructor(internal val editTextView: EditText, private val min: Int, private val max: Int, step: Int) : NumberKeyListener() {
        @Nullable
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
                this.availableValues = values as Array<String>
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

                val result = (dest.subSequence(0, dstart).toString() + filtered +
                        dest.subSequence(dend, dest.length))

                if (result.isEmpty()) {
                    return result
                }

                var value = min
                try {
                    value = result.toInt()
                } catch (e: NumberFormatException) {
                    // do nothing
                }

                return if (value > max) "" else filtered
            } else {
                val filtered = source.subSequence(start, end)
                if (filtered.isEmpty()) {
                    return ""
                }
                val result = (dest.subSequence(0, dstart).toString() + filtered +
                        dest.subSequence(dend, dest.length))

                for (value in availableValues) {
                    if (value.startsWith(result)) {
                        postSetSelection(result.length, value.length)
                        return value.subSequence(dstart, value.length)
                    }
                }
                return ""
            }
        }

        private fun postSetSelection(start: Int, stop: Int) {
            editTextView.post {
                val len = editTextView.text.length
                editTextView.setSelection(Math.min(start, len), Math.min(stop, len))
            }
        }

        companion object {
            private val DIGIT_CHARACTERS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        }
    }

    companion object {
        private const val PARAM_TITLE = "TITLE"
        private const val PARAM_MESSAGE = "MESSAGE"
        private const val PARAM_DEFAULT_VALUE = "DEFAULT_VALUE"
        private const val PARAM_MIN = "MIN_VALUE"
        private const val PARAM_MAX = "MAX_VALUE"
        private const val PARAM_STEP = "STEP"

        @JvmStatic
        @JvmOverloads
        fun newInstance(@StringRes title: Int, @StringRes message: Int, min: Int, max: Int, defaultValue: Int, step: Int = 1): SliderDialogFragment {
            val fragment = SliderDialogFragment()

            val args = bundleOf(
                PARAM_TITLE to title,
                PARAM_MESSAGE to message,
                PARAM_MIN to min,
                PARAM_MAX to max,
                PARAM_DEFAULT_VALUE to defaultValue,
                PARAM_STEP to step
            )

            fragment.arguments = args
            fragment.isCancelable = false

            return fragment
        }
    }
}