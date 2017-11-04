package com.arcao.geocaching4locus.tests;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.arcao.geocaching4locus.tests.util.UiUtil;

import org.junit.Before;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)
@LargeTest
public abstract class AbstractBaseTest {
    UiDevice mDevice;

    @Before
    public void prepareTest() {
        // prepare device
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        assertThat(mDevice, notNullValue());
        UiUtil.showAppLauncher(mDevice);
    }
}
