package com.arcao.geocaching4locus.settings.widget

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.core.content.withStyledAttributes
import androidx.preference.DialogPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arcao.geocaching4locus.R

class SliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.preferenceStyle
) : DialogPreference(context, attrs, defStyleAttr),
    PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    private var value: Int = 0

    var min: Int = 0
    var max: Int = 0
    var step: Int = 0

    var progress: Int
        get() = value
        set(progress) {
            val wasBlocking = shouldDisableDependents()

            value = progress
            persistInt(value)

            val isBlocking = shouldDisableDependents()
            if (isBlocking != wasBlocking) {
                notifyDependencyChange(isBlocking)
            }
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.SliderPreference) {
            min = getInt(R.styleable.SliderPreference_min, 0)
            max = getInt(R.styleable.SliderPreference_max, 100)
            step = getInt(R.styleable.SliderPreference_step, 1)
        }
    }

    override fun getDialogLayoutResource(): Int {
        return R.layout.view_slider
    }

    override fun onSetInitialValue(@Nullable defaultValue: Any?) {
        progress = getPersistedInt(defaultValue as? Int ?: 0)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInt(index, 0)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        if (isPersistent) {
            // No need to save instance state since it's persistent
            return superState
        }

        // Save the instance state
        val myState = SavedState(superState)
        myState.value = value
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state.javaClass != SavedState::class.java) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state)
            return
        }

        // Restore the instance state
        val myState = state as SavedState
        super.onRestoreInstanceState(myState.superState)
        value = myState.value
    }

    override fun onPreferenceDisplayDialog(caller: PreferenceFragmentCompat, preference: Preference): Boolean {
        // check if dialog is already showing
        val fragmentManager = caller.requireFragmentManager()
        if (fragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) return true

        if (preference !is SliderPreference) return false

        val f = SliderPreferenceDialogFragment.newInstance(preference.getKey())
        f.setTargetFragment(caller, 0)
        f.show(fragmentManager, DIALOG_FRAGMENT_TAG)
        return false
    }

    private class SavedState : BaseSavedState {
        internal var value: Int = 0

        internal constructor(source: Parcel) : super(source) {
            value = source.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(value)
        }

        internal constructor(superState: Parcelable) : super(superState)

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    companion object {
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }
}
