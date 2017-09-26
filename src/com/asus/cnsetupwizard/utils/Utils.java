package com.asus.cnsetupwizard.utils;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TtsSpan;
import android.util.Log;

import com.android.internal.util.ArrayUtils;
import com.asus.cnsetupwizard.SetupWizardCompleteActivity;


/**
 * Created by czy on 17-4-5.
 */

public class Utils {
    public static int getCredentialOwnerUserId(Context context) {
        return getCredentialOwnerUserId(context, UserHandle.myUserId());
    }

    public static int getCredentialOwnerUserId(Context context, int userId) {
        UserManager um = getUserManager(context);
        return um.getCredentialOwnerProfile(userId);
    }

    public static UserManager getUserManager(Context context) {
        UserManager um = UserManager.get(context);
        if (um == null) {
            throw new IllegalStateException("Unable to load UserManager");
        }
        return um;
    }

    public static int enforceSameOwner(Context context, int userId) {
        final UserManager um = getUserManager(context);
        final int[] profileIds = um.getProfileIdsWithDisabled(UserHandle.myUserId());
        if (ArrayUtils.contains(profileIds, userId)) {
            return userId;
        }
        throw new SecurityException("Given user id " + userId + " does not belong to user "
                + UserHandle.myUserId());
    }

    public static int getUserIdFromBundle(Context context, Bundle bundle) {
        if (bundle == null) {
            return getCredentialOwnerUserId(context);
        }
        int userId = bundle.getInt(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        return enforceSameOwner(context, userId);
    }

    public static boolean isATT() {
        boolean result = false;
        String SKU = SystemProperties.get("ro.product.name", "");
        Log.d("Utils", "isATT(): SKU=" + SKU);
        if (SKU != null) {
            result = SKU.toLowerCase().startsWith("att_");
        }
        return result;
    }

    public static boolean isVerizonSKU() {
        boolean result = false;
        String SKU = SystemProperties.get("ro.product.name", "");
        return SKU.toLowerCase().startsWith("vzw");
    }

    public static boolean isManagedProfile(UserManager userManager) {
        return isManagedProfile(userManager, UserHandle.myUserId());
    }

    public static SpannableString createAccessibleSequence(CharSequence displayText,
                                                           String accessibileText) {
        SpannableString str = new SpannableString(displayText);
        str.setSpan(new TtsSpan.TextBuilder(accessibileText).build(), 0,
                displayText.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return str;
    }

    public static boolean isManagedProfile(UserManager userManager, int userId) {
        if (userManager == null) {
            throw new IllegalArgumentException("userManager must not be null");
        }
        UserInfo userInfo = userManager.getUserInfo(userId);
        return (userInfo != null) ? userInfo.isManagedProfile() : false;
    }

    public static void startCompleteActivity(Context context){
        Intent intent = new Intent(SetupWizardCompleteActivity.ACTION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void finishSetupWizard(Context context){
        Constants.adjustStatusBarLocked(context,StatusBarManager.DISABLE_NONE);
        PackageManager pm = context.getPackageManager();

        // Under eng-build, things that are related to data collection won't
        // be displayed.
        // Hence, do not need to set any data collection configurations if
        // under eng-build.
        DataCollectionSettings.setDataCollectionConfiguration(context);
        // Disable the GMS Setup wizard when system is first running
        // Under eng-build, they will not include GMS cnsetupwizard by
        // themselves.
        // No need to disable the GMS cnsetupwizard for them.
        try {

            pm.setComponentEnabledSetting(
                    new ComponentName("com.google.android.setupwizard",
                            "com.google.android.setupwizard.MobileConnectivityChangeReceiver"),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
        } catch (IllegalArgumentException e) {
            //Log.w(TAG,
           //         "Google Mobile Services has not installed on this device");
        }

        // Set setup wizard has run
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SETUP_WIZARD_HAS_RUN, 1);
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.LAST_SETUP_SHOWN, "honeycomb_epad_1");

        // New value to notify the system the user setup has completed after
        // JellyBean 4.2
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 1);

        // Set a persistent setting to notify other apps the device has been
        // provisioned
        Settings.Global.putInt(context.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED, 1);

        // Send intent to tell KeyguardUpdateMonitor to update device provision
        // value
        Intent intent = new Intent("com.asus.cnsetupwizard.Finished");
        context.sendBroadcast(intent);

        // The flag "DONT_KILL_APP" is set for waiting that startLauncher()
        // could be called
        // If the flag is set to 0, the process will be killed immediately
        pm.setComponentEnabledSetting(new ComponentName(context.getPackageName(),
                        "com.asus.cnsetupwizard.WelcomeActivity"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                //        0);
                PackageManager.DONT_KILL_APP);
        Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(homeIntent);
    }
}
