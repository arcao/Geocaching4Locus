package com.arcao.geocaching4locus.tests;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObjectNotFoundException;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.tests.util.LocusUiUtil;
import com.arcao.geocaching4locus.tests.util.UiUtil;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class AppStartTest extends AbstractBaseTest {
    @Test
    public void startAppFromLauncherTest() {
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(UiUtil.createAppIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        // check if dashboard activity is running
        assertTrue(mDevice.hasObject(By.res(UiUtil.APP_PACKAGE, "db_live_map")));
    }

    @Test
    public void startAppFromLocusTest() throws UiObjectNotFoundException {
        // Launch the Locus Map app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(LocusUiUtil.createLocusMapIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, LocusUiUtil.APP_PACKAGE);

        // check if Locus base activity is running
        assertTrue(mDevice.hasObject(By.res(LocusUiUtil.APP_PACKAGE, "btn_title_text")));

        // run Geocaching4Locus
        LocusUiUtil.startFunction(mDevice, InstrumentationRegistry.getTargetContext().getString(R.string.app_name));

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        // check if dashboard activity is running
        assertTrue(mDevice.hasObject(By.res(UiUtil.APP_PACKAGE, "db_live_map")));
    }
}