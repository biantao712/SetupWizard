package com.asus.cnsetupwizard.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.provider.Settings;
import android.text.TextUtils.SimpleStringSplitter;

public class AccessibilityUtils {
	// Auxiliary members.
    public final static SimpleStringSplitter sStringColonSplitter =
            new SimpleStringSplitter(':');
	 /**
     * @return the set of enabled accessibility services. If there are not services
     * it returned the unmodifiable {@link Collections#emptySet()}.
     * 
     */
    public static Set<ComponentName> getEnabledServicesFromSettings(Context context) {
        final String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabledServicesSetting == null) {
            return Collections.emptySet();
        }

        final Set<ComponentName> enabledServices = new HashSet<ComponentName>();
        final SimpleStringSplitter colonSplitter = sStringColonSplitter;
        colonSplitter.setString(enabledServicesSetting);

        while (colonSplitter.hasNext()) {
            final String componentNameString = colonSplitter.next();
            final ComponentName enabledService = ComponentName.unflattenFromString(
                    componentNameString);
            if (enabledService != null) {
                enabledServices.add(enabledService);
            }
        }
        return enabledServices;
    }

    /**
     * @return a localized version of the text resource specified by resId
     */
    public static CharSequence getTextForLocale(Context context, Locale locale, int resId) {
        final Resources res = context.getResources();
        final Configuration config = res.getConfiguration();
        final Locale prevLocale = config.locale;
        try {
            config.locale = locale;
            res.updateConfiguration(config, null);
            return res.getText(resId);
        } finally {
            config.locale = prevLocale;
            res.updateConfiguration(config, null);
        }
    }

}
