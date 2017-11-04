package com.arcao.geocaching4locus.tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.Until;

import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.tests.util.LocusUiUtil;
import com.arcao.geocaching4locus.tests.util.UiUtil;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ImportFromGcTest extends AbstractBaseTest {
    @Test
    public void importFromGcFromAppLauncher() throws UiObjectNotFoundException {
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(UiUtil.createAppIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        mDevice.findObject(By.res(UiUtil.APP_PACKAGE, "db_import_gc")).click();

        assertEquals(InstrumentationRegistry.getTargetContext().getString(R.string.dialog_gc_number_input_title),
                mDevice.wait(Until.findObject(By.res(UiUtil.APP_PACKAGE, "md_title")), 500).getText());
    }

    @Test
    public void importFromGcFromLocusMapTest() throws UiObjectNotFoundException {
        // Launch the Locus Map app
        Context context = InstrumentationRegistry.getContext();
        context.startActivity(LocusUiUtil.createLocusMapIntent());

        // Wait for the app to appear
        UiUtil.waitForApp(mDevice, LocusUiUtil.APP_PACKAGE);

        // check if Locus base activity is running
        assertTrue(mDevice.hasObject(By.res(LocusUiUtil.APP_PACKAGE, "btn_title_text")));

        // run function Import from GC code
        LocusUiUtil.startFunction(mDevice, InstrumentationRegistry.getTargetContext().getString(R.string.launcher_import_from_gc));

        UiUtil.waitForApp(mDevice, UiUtil.APP_PACKAGE);

        assertEquals(InstrumentationRegistry.getTargetContext().getString(R.string.dialog_gc_number_input_title),
                mDevice.wait(Until.findObject(By.res(UiUtil.APP_PACKAGE, "md_title")), 500).getText());
    }
}
