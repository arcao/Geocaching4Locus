package com.arcao.geocaching4locus.tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.base.util.Coordinates;
import com.arcao.geocaching4locus.tests.util.LocusUiUtil;
import com.arcao.geocaching4locus.tests.util.UiUtil;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class SearchNearestTest extends AbstractBaseTest {
    @Test
    public void searchNearestFromAppLauncher() throws UiObjectNotFoundException {
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(UiUtil.createAppIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        // click on dashboard button Nearest caches
        mDevice.findObject(By.res(UiUtil.APP_PACKAGE, "db_search_nearest")).clickAndWait(Until.newWindow(), 500);

        // cancel acquiring GPS waiting dialog
        assertTrue(mDevice.wait(Until.hasObject(By.text(
                InstrumentationRegistry.getTargetContext().getString(R.string.progress_acquire_gps_location))), 500));
        mDevice.findObject(By.res(UiUtil.APP_PACKAGE, "md_buttonDefaultNegative")).click();

        // check window title
        assertTrue(mDevice.wait(Until.hasObject(By.text(InstrumentationRegistry.getTargetContext().getString(R.string.launcher_nearest_geocaches))), 500));
    }


    @Test
    public final void searchNearestFromLocusMapTest() throws UiObjectNotFoundException {
        // generate random point around N50, E14
        double latitude = Coordinates.roundDouble(50D + Math.random(), 3);
        double longitude = Coordinates.roundDouble(14D + Math.random(), 3);

        // Launch the Locus Map app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(LocusUiUtil.createLocusMapIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, LocusUiUtil.APP_PACKAGE);

        // check if Locus base activity is running
        assertTrue(mDevice.hasObject(By.res(LocusUiUtil.APP_PACKAGE, "btn_title_text")));

        // set location in Locus Map
        LocusUiUtil.setLocation(mDevice, latitude, longitude);

        // run function Nearest caches
        LocusUiUtil.startFunction(mDevice, InstrumentationRegistry.getTargetContext().getString(R.string.launcher_nearest_geocaches));

        // wait for app
        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        // check correct coordinates
        assertEquals(Coordinates.convertDoubleToDeg(latitude, false), mDevice.findObject(By.res(UiUtil.APP_PACKAGE, "latitude")).getText());
        assertEquals(Coordinates.convertDoubleToDeg(longitude, true), mDevice.findObject(By.res(UiUtil.APP_PACKAGE, "longitude")).getText());
    }
}
