package com.arcao.feedback.collector;

import android.content.Context;
import android.content.res.Configuration;
import android.util.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class ConfigurationCollector extends Collector {
	private static final String SUFFIX_MASK = "_MASK";
	private static final String FIELD_SCREENLAYOUT = "screenLayout";
	private static final String FIELD_UIMODE = "uiMode";
	private static final String FIELD_MNC = "mnc";
	private static final String FIELD_MCC = "mcc";
	private static final String PREFIX_UI_MODE = "UI_MODE_";
	private static final String PREFIX_TOUCHSCREEN = "TOUCHSCREEN_";
	private static final String PREFIX_SCREENLAYOUT = "SCREENLAYOUT_";
	private static final String PREFIX_ORIENTATION = "ORIENTATION_";
	private static final String PREFIX_NAVIGATIONHIDDEN = "NAVIGATIONHIDDEN_";
	private static final String PREFIX_NAVIGATION = "NAVIGATION_";
	private static final String PREFIX_KEYBOARDHIDDEN = "KEYBOARDHIDDEN_";
	private static final String PREFIX_KEYBOARD = "KEYBOARD_";
	private static final String PREFIX_HARDKEYBOARDHIDDEN = "HARDKEYBOARDHIDDEN_";

	private static final SparseArray<String> mHardKeyboardHiddenValues = new SparseArray<>();
	private static final SparseArray<String> mKeyboardValues = new SparseArray<>();
	private static final SparseArray<String> mKeyboardHiddenValues = new SparseArray<>();
	private static final SparseArray<String> mNavigationValues = new SparseArray<>();
	private static final SparseArray<String> mNavigationHiddenValues = new SparseArray<>();
	private static final SparseArray<String> mOrientationValues = new SparseArray<>();
	private static final SparseArray<String> mScreenLayoutValues = new SparseArray<>();
	private static final SparseArray<String> mTouchScreenValues = new SparseArray<>();
	private static final SparseArray<String> mUiModeValues = new SparseArray<>();

	private static final Map<String, SparseArray<String>> mValueArrays = new HashMap<>();

	// Static init
	static {
		mValueArrays.put(PREFIX_HARDKEYBOARDHIDDEN, mHardKeyboardHiddenValues);
		mValueArrays.put(PREFIX_KEYBOARD, mKeyboardValues);
		mValueArrays.put(PREFIX_KEYBOARDHIDDEN, mKeyboardHiddenValues);
		mValueArrays.put(PREFIX_NAVIGATION, mNavigationValues);
		mValueArrays.put(PREFIX_NAVIGATIONHIDDEN, mNavigationHiddenValues);
		mValueArrays.put(PREFIX_ORIENTATION, mOrientationValues);
		mValueArrays.put(PREFIX_SCREENLAYOUT, mScreenLayoutValues);
		mValueArrays.put(PREFIX_TOUCHSCREEN, mTouchScreenValues);
		mValueArrays.put(PREFIX_UI_MODE, mUiModeValues);

		for (final Field f : Configuration.class.getFields()) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
				final String fieldName = f.getName();
				try {
					if (fieldName.startsWith(PREFIX_HARDKEYBOARDHIDDEN)) {
						mHardKeyboardHiddenValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_KEYBOARD)) {
						mKeyboardValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_KEYBOARDHIDDEN)) {
						mKeyboardHiddenValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_NAVIGATION)) {
						mNavigationValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_NAVIGATIONHIDDEN)) {
						mNavigationHiddenValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_ORIENTATION)) {
						mOrientationValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_SCREENLAYOUT)) {
						mScreenLayoutValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_TOUCHSCREEN)) {
						mTouchScreenValues.put(f.getInt(null), fieldName);
					} else if (fieldName.startsWith(PREFIX_UI_MODE)) {
						mUiModeValues.put(f.getInt(null), fieldName);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Timber.w(e, "Error while inspecting device configuration: ");
				}
			}
		}
	}

	private final Context mContext;

	public ConfigurationCollector(Context context) {
		this.mContext = context.getApplicationContext();
	}

	@Override
	public String getName() {
		return "CONFIGURATION";
	}


	@Override
	protected String collect() {
		try {
			final Configuration crashConf = mContext.getResources().getConfiguration();
			return ConfigurationCollector.toString(crashConf);
		} catch (RuntimeException e) {
			Timber.w(e, "Couldn't retrieve ConfigurationCollector for : " + mContext.getPackageName());
			return "Couldn't retrieve crash config";
		}
	}

	/**
	 * Use this method to generate a human readable String listing all values
	 * from the provided Configuration instance.
	 *
	 * @param conf
	 *            The Configuration to be described.
	 * @return A String describing all the fields of the given Configuration,
	 *         with values replaced by constant names.
	 */
	private static String toString(Configuration conf) {
		final StringBuilder result = new StringBuilder();
		for (final Field f : conf.getClass().getFields()) {
			try {
				if (!Modifier.isStatic(f.getModifiers())) {
					final String fieldName = f.getName();
					result.append(fieldName).append('=');
					if (f.getType().equals(int.class)) {
						result.append(getFieldValueName(conf, f));
					} else if(f.get(conf) != null){
						result.append(f.get(conf).toString());
					}
					result.append('\n');
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				Timber.e(e, "Error while inspecting device configuration: ");
			}
		}
		return result.toString();
	}

	/**
	 * Retrieve the name of the constant defined in the {@link Configuration}
	 * class which defines the value of a field in a {@link Configuration}
	 * instance.
	 *
	 * @param conf
	 *            The instance of {@link Configuration} where the value is
	 *            stored.
	 * @param f
	 *            The {@link Field} to be inspected in the {@link Configuration}
	 *            instance.
	 * @return The value of the field f in instance conf translated to its
	 *         constant name.
	 * @throws IllegalAccessException if the supplied field is inaccessible.
	 */
	private static String getFieldValueName(Configuration conf, Field f) throws IllegalAccessException {
		final String fieldName = f.getName();
		switch (fieldName) {
			case FIELD_MCC:
			case FIELD_MNC:
				return Integer.toString(f.getInt(conf));
			case FIELD_UIMODE:
				return activeFlags(mValueArrays.get(PREFIX_UI_MODE), f.getInt(conf));
			case FIELD_SCREENLAYOUT:
				return activeFlags(mValueArrays.get(PREFIX_SCREENLAYOUT), f.getInt(conf));
			default:
				final SparseArray<String> values = mValueArrays.get(fieldName.toUpperCase() + '_');
				if (values == null) {
					// Unknown field, return the raw int as String
					return Integer.toString(f.getInt(conf));
				}

				final String value = values.get(f.getInt(conf));
				if (value == null) {
					// Unknown value, return the raw int as String
					return Integer.toString(f.getInt(conf));
				}
				return value;
		}
	}

	/**
	 * Some fields contain multiple value types which can be isolated by
	 * applying a bitmask. That method returns the concatenation of active
	 * values.
	 *
	 * @param valueNames
	 *            The array containing the different values and names for this
	 *            field. Must contain mask values too.
	 * @param bitfield
	 *            The bitfield to inspect.
	 * @return The names of the different values contained in the bitfield,
	 *         separated by '+'.
	 */
	private static String activeFlags(SparseArray<String> valueNames, int bitfield) {
		final StringBuilder result = new StringBuilder();

		// Look for masks, apply it an retrieve the masked value
		for (int i = 0; i < valueNames.size(); i++) {
			final int maskValue = valueNames.keyAt(i);
			if (valueNames.get(maskValue).endsWith(SUFFIX_MASK)) {
				final int value = bitfield & maskValue;
				if (value > 0) {
					if (result.length() > 0) {
						result.append('+');
					}
					result.append(valueNames.get(value));
				}
			}
		}
		return result.toString();
	}
}
