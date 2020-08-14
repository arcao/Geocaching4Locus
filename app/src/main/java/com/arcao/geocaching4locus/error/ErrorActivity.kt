package com.arcao.geocaching4locus.error

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.checkbox.isCheckPromptChecked
import com.arcao.geocaching4locus.R
import com.arcao.geocaching4locus.base.fragment.AbstractDialogFragment
import com.arcao.geocaching4locus.base.util.getText
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.oshkimaadziig.george.androidutils.SpanFormatter

class ErrorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            showErrorDialog()
    }

    private fun showErrorDialog() {
        val args = intent.extras
        if (args == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        ErrorDialogFragment.newInstance(args).show(supportFragmentManager)
    }

    class ErrorDialogFragment : AbstractDialogFragment() {
        @SuppressLint("PrivateResource")
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val args = requireArguments()

            val title = args.getCharSequence(KEY_TITLE)
            val message = args.getCharSequence(KEY_MESSAGE) ?: ""
            val positiveAction = args.getParcelable(KEY_POSITIVE_ACTION) as Intent?
            val positiveButtonText = args.getCharSequence(KEY_POSITIVE_BUTTON_TEXT)
            val negativeButtonText = args.getCharSequence(KEY_NEGATIVE_BUTTON_TEXT)
            val t = args.getSerializable(KEY_EXCEPTION) as? Throwable

            val context = requireContext()

            val md = MaterialDialog(requireContext())
                .message(text = message)
                .positiveButton(text = positiveButtonText ?: context.getString(R.string.button_ok)) { dialog ->
                    if (dialog.isCheckPromptChecked()) {
                        t?.let {
                            FirebaseCrashlytics.getInstance().recordException(it)
                        }
                        Toast.makeText(
                            context,
                            R.string.toast_error_report_sent,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    if (positiveAction != null) {
                        startActivity(positiveAction)
                    }

                    requireActivity().apply {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }

            title?.let {
                md.title(text = title.toString())
            }

            negativeButtonText?.let {
                md.negativeButton(text = negativeButtonText) {
                    requireActivity().apply {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }
            }

            t?.let {
                md.checkBoxPrompt(R.string.button_send_error_report, onToggle = null)
            }

            return md
        }

        companion object {
            internal fun newInstance(args: Bundle): ErrorDialogFragment {
                return ErrorDialogFragment().apply {
                    isCancelable = false
                    arguments = args
                }
            }
        }
    }

    class IntentBuilder(private val context: Context) {
        private var title: CharSequence? = null
        private var message: CharSequence? = null
        private var positiveAction: Intent? = null
        private var positiveButtonText: CharSequence? = null
        private var negativeButtonText: CharSequence? = null
        private var exception: Throwable? = null

        fun title(@StringRes resTitle: Int): IntentBuilder {
            this.title = context.getText(resTitle)
            return this
        }

        fun message(@StringRes message: Int, vararg params: Any): IntentBuilder {
            this.message = context.getText(message, *params)
            return this
        }

        fun message(message: CharSequence, vararg params: Any): IntentBuilder {
            this.message = SpanFormatter.format(message, *params)
            return this
        }

        fun positiveAction(positiveAction: Intent): IntentBuilder {
            this.positiveAction = positiveAction
            return this
        }

        fun positiveButtonText(@StringRes positiveButtonText: Int): IntentBuilder {
            this.positiveButtonText = context.getText(positiveButtonText)
            return this
        }

        fun negativeButtonText(@StringRes negativeButtonText: Int): IntentBuilder {
            this.negativeButtonText = context.getText(negativeButtonText)
            return this
        }

        fun negativeButtonText(negativeButtonText: CharSequence): IntentBuilder {
            this.negativeButtonText = negativeButtonText
            return this
        }

        fun clearNegativeButtonText(): IntentBuilder {
            this.negativeButtonText = null
            return this
        }

        fun exception(exception: Throwable): IntentBuilder {
            this.exception = exception
            return this
        }

        fun build(): Intent {
            return Intent(context, ErrorActivity::class.java)
                .putExtra(KEY_TITLE, title)
                .putExtra(KEY_MESSAGE, message)
                .putExtra(KEY_POSITIVE_ACTION, positiveAction)
                .putExtra(KEY_POSITIVE_BUTTON_TEXT, positiveButtonText)
                .putExtra(KEY_NEGATIVE_BUTTON_TEXT, negativeButtonText)
                .putExtra(KEY_EXCEPTION, exception)
        }
    }

    companion object {
        private const val KEY_TITLE = "TITLE"
        private const val KEY_MESSAGE = "MESSAGE"
        internal const val KEY_POSITIVE_ACTION = "POSITIVE_ACTION"
        private const val KEY_POSITIVE_BUTTON_TEXT = "POSITIVE_BUTTON_TEXT"
        private const val KEY_NEGATIVE_BUTTON_TEXT = "NEGATIVE_BUTTON_TEXT"
        private const val KEY_EXCEPTION = "EXCEPTION"
    }
}

internal fun Intent.hasPositiveAction() =
    getParcelableExtra<Parcelable?>(ErrorActivity.KEY_POSITIVE_ACTION) != null
