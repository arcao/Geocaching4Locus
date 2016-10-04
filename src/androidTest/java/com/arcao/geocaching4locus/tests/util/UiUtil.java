package com.arcao.geocaching4locus.tests.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public final class UiUtil {
    public static final String APP_PACKAGE = InstrumentationRegistry.getTargetContext().getPackageName();
    private static final int LAUNCH_TIMEOUT = 5000;

    public static void showAppLauncher(UiDevice device) {
        // Start from the home screen
        device.pressHome();

        // Wait for launcher
        final String launcherPackage = getLauncherPackageName();
        assertThat(launcherPackage, notNullValue());
        device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), LAUNCH_TIMEOUT);
    }

    public static Intent createAppIntent() {
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        return intent;
    }

    public static void scrollAnClickOn(UiDevice device, String text) throws UiObjectNotFoundException {
        // find Geocaching4Locus item
        UiScrollable functionViews = new UiScrollable(new UiSelector().scrollable(true));
        functionViews.setAsVerticalList();
        functionViews.scrollBackward();
        functionViews.scrollTextIntoView(text);

        // click on item
        device.findObject(By.text(text)).click();
    }

    public static void waitForApp(UiDevice device, String pkg) {
        assertTrue(device.wait(Until.hasObject(By.pkg(pkg).depth(0)), UiUtil.LAUNCH_TIMEOUT));
    }

    /**
     * Uses package manager to find the package name of the device launcher. Usually this package
     * is "com.android.launcher" but can be different at times. This is a generic solution which
     * works on all platforms.
     */
    private static String getLauncherPackageName() {
        // Create launcher Intent
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);

        // Use PackageManager to get the launcher package name
        PackageManager pm = InstrumentationRegistry.getContext().getPackageManager();
        ResolveInfo resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.activityInfo.packageName;
    }
}
