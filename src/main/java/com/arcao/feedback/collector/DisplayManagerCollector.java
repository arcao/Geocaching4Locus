package com.arcao.feedback.collector;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import timber.log.Timber;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DisplayManagerCollector extends Collector {
	private final Context mContext;

	public DisplayManagerCollector(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public String getName() {
		return "DisplayManager";
	}

	@Override
	protected String collect() {
		Display[] displays = null;
		final StringBuilder result = new StringBuilder();

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			// Before Android 4.2, there was a single display available from the
			// window manager
			final WindowManager windowManager = (WindowManager) mContext.getSystemService(android.content.Context.WINDOW_SERVICE);
			displays = new Display[1];
			displays[0] = windowManager.getDefaultDisplay();
		} else {
			// Since Android 4.2, we can fetch multiple displays with the
			// DisplayManager.
			try {
				DisplayManager displayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
				displays = displayManager.getDisplays();
			} catch (IllegalArgumentException e) {
				Timber.e(e, "Error while collecting DisplayManager data");
			}
		}

		if (displays != null) {
			for (Display display : displays) {
				result.append(collectDisplayData(display));
			}
		}

		return result.toString();
	}

	@SuppressWarnings("deprecation")
	private static Object collectDisplayData(Display display) {
		final DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);

		return collectCurrentSizeRange(display) +
						collectFlags(display) +
						display.getDisplayId() + ".height=" + display.getHeight() + '\n' +
						collectMetrics(display, "getMetrics") +
						collectName(display) +
						display.getDisplayId() + ".orientation=" + display.getRotation() + '\n' +
						collectMetrics(display, "getRealMetrics") +
						collectSize(display, "getRealSize") +
						collectRectSize(display) +
						display.getDisplayId() + ".refreshRate=" + display.getRefreshRate() + '\n' +
						collectRotation(display) +
						collectSize(display, "getSize") +
						display.getDisplayId() + ".width=" + display.getWidth() + '\n' +
						collectIsValid(display);
	}

	private static String collectIsValid(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v17
			Method isValid = display.getClass().getMethod("isValid");
			Boolean value = (Boolean) isValid.invoke(display);
			result.append(display.getDisplayId()).append(".isValid=").append(value).append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			Timber.e(e, e.getMessage());
		}

		return result.toString();
	}

	private static String collectRotation(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v8
			int rotation = display.getRotation();
			switch (rotation) {
				case Surface.ROTATION_0:
					result.append("ROTATION_0");
					break;
				case Surface.ROTATION_90:
					result.append("ROTATION_90");
					break;
				case Surface.ROTATION_180:
					result.append("ROTATION_180");
					break;
				case Surface.ROTATION_270:
					result.append("ROTATION_270");
					break;
				default:
					result.append(rotation);
					break;
			}
			result.append('\n');
		} catch (SecurityException | IllegalArgumentException e) {
			Timber.e(e, e.getMessage());
		}

		return result.toString();
	}

	private static String collectRectSize(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v13
			Rect size = new Rect();
			display.getRectSize(size);
			result.append(display.getDisplayId()).append(".rectSize=[").append(size.top).append(',').append(size.left)
							.append(',').append(size.width()).append(',').append(size.height()).append(']').append('\n');
		} catch (SecurityException | IllegalArgumentException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
	}

	private static String collectSize(Display display, String methodName) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v17
			Method getRealSize = display.getClass().getMethod(methodName, Point.class);
			Point size = new Point();
			getRealSize.invoke(display, size);
			result.append(display.getDisplayId()).append('.').append(methodName).append("=[").append(size.x)
							.append(',').append(size.y).append(']').append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
	}

	private static String collectCurrentSizeRange(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v16
			Method getCurrentSizeRange = display.getClass().getMethod("getCurrentSizeRange", Point.class, Point.class);
			Point smallest = new Point(), largest = new Point();
			getCurrentSizeRange.invoke(display, smallest, largest);
			result.append(display.getDisplayId()).append(".currentSizeRange.smallest=[").append(smallest.x).append(',')
							.append(smallest.y).append(']').append('\n');
			result.append(display.getDisplayId()).append(".currentSizeRange.largest=[").append(largest.x).append(',')
							.append(largest.y).append(']').append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
	}

	private static String collectFlags(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v17
			Method getFlags = display.getClass().getMethod("getFlags");
			int flags = (Integer) getFlags.invoke(display);

			SparseArray<String> mFlagsNames = new SparseArray<>();
			for (Field field : display.getClass().getFields()) {
				if (field.getName().startsWith("FLAG_")) {
					mFlagsNames.put(field.getInt(null), field.getName());
				}
			}

			result.append(display.getDisplayId()).append(".flags=").append(activeFlags(mFlagsNames, flags))
							.append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
	}

	private static String collectName(Display display) {
		StringBuilder result = new StringBuilder();
		try {
			// since API v17
			Method getName = display.getClass().getMethod("getName");
			String name = (String) getName.invoke(display);

			result.append(display.getDisplayId()).append(".name=").append(name).append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
	}

	private static String collectMetrics(Display display, String methodName) {
		StringBuilder result = new StringBuilder();
		try {
			Method getMetrics = display.getClass().getMethod(methodName);
			DisplayMetrics metrics = (DisplayMetrics) getMetrics.invoke(display);

			result.append(display.getDisplayId()).append('.').append(methodName).append(".density=")
							.append(metrics.density).append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append(".densityDpi=")
							.append(metrics.getClass().getField("densityDpi")).append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append("scaledDensity=x")
							.append(metrics.scaledDensity).append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append(".widthPixels=")
							.append(metrics.widthPixels).append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append(".heightPixels=")
							.append(metrics.heightPixels).append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append(".xdpi=").append(metrics.xdpi)
							.append('\n');
			result.append(display.getDisplayId()).append('.').append(methodName).append(".ydpi=").append(metrics.ydpi)
							.append('\n');
		} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchFieldException e) {
			Timber.e(e, e.getMessage());
		}
		return result.toString();
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
			final int value = bitfield & maskValue;
			if (value > 0) {
				if (result.length() > 0) {
					result.append('+');
				}
				result.append(valueNames.get(value));
			}
		}
		return result.toString();
	}
}
