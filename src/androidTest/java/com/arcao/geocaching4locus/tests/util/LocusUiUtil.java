package com.arcao.geocaching4locus.tests.util;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import locus.api.android.utils.LocusUtils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by arcao on 04.10.2016.
 */

public final class LocusUiUtil {
    public static final String APP_PACKAGE = getLocusMapPackage();

    public static Intent createLocusMapIntent() {
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager().getLaunchIntentForPackage(APP_PACKAGE);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);    // Clear out any previous instances
        return intent;
    }

    public static void showSideMenu(UiDevice device) {
        device.findObject(By.res(LocusUiUtil.APP_PACKAGE, "btn_logo")).click();
    }

    public static void startFunction(UiDevice device, String functionName) throws UiObjectNotFoundException {
        // navigate to more functions side menu
        LocusUiUtil.showSideMenu(device);
        UiUtil.scrollAnClickOn(device, "More functions");

        // click on Geocaching4Locus
        UiUtil.scrollAnClickOn(device, functionName);
    }

    private static String getLocusMapPackage() {
        LocusUtils.LocusVersion version = LocusUtils.getActiveVersion(InstrumentationRegistry.getTargetContext());
        assertNotNull(version);
        return version.getPackageName();
    }

    public static void setLocation(UiDevice device, double latitude, double longitude) throws UiObjectNotFoundException {
        LocusUiUtil.showSideMenu(device);

        UiUtil.scrollAnClickOn(device, "Search");
        device.wait(Until.findObject(By.clazz(".ListView")), 500)
                .findObject(By.text("Move to …")).click();

        device.wait(Until.findObject(By.text("Coordinates")), 500).click();

        device.wait(Until.findObject(By.res(APP_PACKAGE, "spinner_coordinate_system")), 500).clickAndWait(Until.newWindow(), 500);
        device.pressDPadDown();
        device.pressEnter();
        assertTrue(device.wait(Until.hasObject(By.res(APP_PACKAGE, "edit_text_lat_01")), 500));

        device.findObject(By.res(APP_PACKAGE, "edit_text_lat_01")).setText(String.valueOf(latitude));
        device.findObject(By.res(APP_PACKAGE, "edit_text_lon_01")).setText(String.valueOf(longitude));

        device.findObject(By.res(APP_PACKAGE, "button_positive_material")).click();

        assertTrue(device.wait(Until.hasObject(By.res(LocusUiUtil.APP_PACKAGE, "btn_title_text")), 500));
    }
}

