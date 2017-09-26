/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asus.cnsetupwizard.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class CheckDeviceProperty {

    private static final String TAG = "CheckDeviceProperty";

    public static boolean isMobileNetworkSupported(Context c){

        if(c == null) {
            Log.d(TAG, "Context is null in isMobileNetworkSupported(Context c) !");
            return false;
        }

        // Check the connectivity manager
        ConnectivityManager connectManager = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        // NO connectivity manager
        if(connectManager == null) {
            Log.d(TAG, "ConnectivityManager is null in isMobileNetworkSupported(Context c) !");
            return false;
        } else {
            boolean isNetworkSupported = connectManager.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
            Log.d(TAG, "The device is mobile network supported : " + isNetworkSupported);
            return isNetworkSupported;
        }
    }

    public static boolean isAbleToMakePhoneCall(Context c){

        if(c == null) {
            Log.d(TAG, "Context is null in isAbleToMakePhoneCall(Context c) !");
            return false;
        }

        // Check the telephony manager
        TelephonyManager telManager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);

        // NO telephony manager
        if(telManager == null) {
            Log.d(TAG, "TelephonyManager is null in isAbleToMakePhoneCall(Context c) !");
            return false;
        } else {
            boolean isVoiceCapable = telManager.isVoiceCapable();
            Log.d(TAG, "The device is voice capable : " + isVoiceCapable);
            return isVoiceCapable;
        }
    }

    public static boolean isWifiOnly() {

        // Check system property. You can see the property by "adb shell getprop"
        String ro_carrier = SystemProperties.get("ro.carrier");

        if(TextUtils.isEmpty(ro_carrier)) {
            Log.d(TAG, "System property ro.carrier is empty ! ");
            return true;
        } else {
            Log.d(TAG, "System property ro.carrier is: " + ro_carrier);
            return "wifi-only".equals(ro_carrier);
        }
    }
    
    public static boolean isNewIMEInterface(Context c){
    	if(isSpecificSKU("ATT")){
    		return false;
    	}
    	PackageInfo info; 
    	int versionCode =0;
        try {    
            info = c.getPackageManager().getPackageInfo("com.asus.ime", 0);     
            //String packageName = info.packageName;
            //String versionName = info.versionName;      
            versionCode = info.versionCode;
            Log.d(TAG, "Asus keyboard versioncode is: " + Integer.toString(versionCode));
            if(versionCode >= Constants.ASUS_KEYBOARD_VERSIONCODE)
            	return true;            
        } catch (Exception e) {    
            e.printStackTrace();    
        }
        return false;
    }

    /**
     * Check if ASUS analytics setting exist
     * @param c Context
     * @return True, if exists
     */
    public static boolean isAsusAnalyticsSettingExist(Context c) {
       /* try {
            Settings.System.getInt(c.getContentResolver(), Settings.System.ASUS_ANALYTICS);
        } catch (SettingNotFoundException e) {
            Log.d(TAG, "SettingNotFoundException occurs when get ASUS_ANALYTICS from system !");
            return false;
        }*/
        return true;
    }

    /**
     * Check if the device is Asus keyboard available
     * @param c Context
     * @return True, if AsusKeyBoard is available
     */
    public static boolean isAsusKeyBoardAvailable(Context c) {

        final String IMIID = "com.asus.ime/.IME";
        InputMethodManager imm = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> inputMethodProperties = imm.getEnabledInputMethodList();
        int num = (inputMethodProperties == null ? 0 : inputMethodProperties.size());
        for (int i = 0; i < num; ++i) {
            final InputMethodInfo imi = inputMethodProperties.get(i);
            if(IMIID.equals(imi.getId())) {
                if (Constants.DEBUG) Log.i(TAG, "Asus keyboard is available");
                return true;
            }
        }
        if (Constants.DEBUG) Log.i(TAG, "Asus keyboard is unavailable");
        return false;
    }

    /**
     * Call MSimTelephonyManager.getDefault().isMultiSimEnabled()
     * @return True if the device is multi-SIM cards enabled.
     */
    public static boolean isMultiSimEnabled() {

        // Set the default value to false in case of the occurrence of exception
        Boolean isMultiSimEnabled = false;

        try {
            Class<?> c = Class.forName("android.telephony.MSimTelephonyManager");

            // Declare the parameter type
            Class<?>[] paramType = null;

            // Assign the parameter content
            Object[] paramContent = null;

            // Get method
            Method getDefault = c.getMethod("getDefault", paramType);

            // Invoke the method
            Object multiSimTelephonyManager = getDefault.invoke(null, paramContent);

            // Get method
            Method isMultiSimEnabledFunc = c.getMethod("isMultiSimEnabled", paramType);

            // Invoke the method
            isMultiSimEnabled = (Boolean )isMultiSimEnabledFunc.invoke(multiSimTelephonyManager,paramContent);

        } catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "ClassNotFoundException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch (Exception e) {
            Log.w(TAG, "isMultiSimEnabled failed ! " + e.toString());
            e.printStackTrace();
        }

        Log.i(TAG, "The device is multiSim enabled? " + isMultiSimEnabled.booleanValue());
        return isMultiSimEnabled.booleanValue();
    }

    public static boolean isAsusAccountTypeExists(Context c){
        AuthenticatorDescription[] authDescs;
        Boolean isAsusAccountType = false;

        authDescs = AccountManager.get(c).getAuthenticatorTypes();
        for (int i = 0; i < authDescs.length; i++) {
            if(Constants.ASUS_ACCOUNT_TYPE.equals(authDescs[i].type)) {
                isAsusAccountType = true;
            }
        }

        return isAsusAccountType;
    }
    
    public static boolean hasAsusAccountLogin(Context context){
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(Constants.ASUS_ACCOUNT_TYPE);
        return accounts != null && accounts.length != 0;
    }

    public static boolean isSpecificSKU(String sku) {
        String systemSku = SystemProperties.get("ro.build.asus.sku", "");
        Log.d(TAG, "SKU is " + systemSku);
        return systemSku.toLowerCase().startsWith(sku.toLowerCase());
    }
}
