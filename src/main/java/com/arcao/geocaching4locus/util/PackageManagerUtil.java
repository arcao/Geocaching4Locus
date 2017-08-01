package com.arcao.geocaching4locus.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PackageManagerUtil {
  public static boolean isComponentEnabled(Context context, Class<?> componentClass) {
    PackageManager packageManager = context.getPackageManager();

    ComponentName componentName = new ComponentName(context, componentClass);
    int componentEnabledSetting = packageManager.getComponentEnabledSetting(componentName);

    switch (componentEnabledSetting) {
      case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
        return false;
      case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
        return true;
      case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
      default:
        // We need to get the application info to get the component's default state
        try {
          PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES
              | PackageManager.GET_RECEIVERS
              | PackageManager.GET_SERVICES
              | PackageManager.GET_PROVIDERS
              | PackageManager.GET_DISABLED_COMPONENTS);

          List<ComponentInfo> components = new ArrayList<>();
          if (packageInfo.activities != null) Collections.addAll(components, packageInfo.activities);
          if (packageInfo.services != null) Collections.addAll(components, packageInfo.services);
          if (packageInfo.providers != null) Collections.addAll(components, packageInfo.providers);

          final String name = componentClass.getName();

          for (ComponentInfo componentInfo : components) {
            if (componentInfo.name.equals(name)) {
              return componentInfo.isEnabled();
            }
          }

          // the component is not declared in the AndroidManifest
          return false;
        } catch (PackageManager.NameNotFoundException e) {
          // the package isn't installed on the device
          return false;
        }
    }
  }
}
