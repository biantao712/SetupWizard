/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.asus.cnsetupwizard.unbundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.android.internal.telephony.ITelephony;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.util.Log;

public class Reflection {

    private static final String TAG = "Reflection";

    /**
     * Check if AsusAnalytics is enabled.
     * In order to be framework-independent, use java reflection mechanism to invoke the method at run-time.
     * @return True, if it is enabled. False, if it is not enabled or the framework does not have data collection mechanism.
     */
    public static boolean isAsusAnalyticsEnabled(Context context) {

        // Set the default value to false in case of the occurrence of exception
        Boolean isAsusAnalyticsEnabled = false;

        try {
            Class<?> c = Class.forName("com.asus.analytics.AnalyticsSettings");

            // Declare the parameter type
            Class<?>[] paramType = {Context.class};

            // Assign the parameter content
            Object[] paramContent = {context};

            // Get method
            Method getEnableAsusAnalytics = c.getMethod("getEnableAsusAnalytics", paramType);

            // Invoke the method
            isAsusAnalyticsEnabled = (Boolean) getEnableAsusAnalytics.invoke(
                null, // When the callee is a static function, pass null to this parameter
                paramContent);

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
            Log.w(TAG, "getAsusAnalytics failed ! " + e.toString());
            e.printStackTrace();
        }

        return isAsusAnalyticsEnabled.booleanValue();
    }

    /**
     * Set AsusAnalytics is enabled.
     * In order to be framework-independent, use java reflection mechanism to invoke the method at run-time.
     * @param val The boolean value to be set
     * @return True, if the value was set, false on database errors
     */
    public static boolean setAsusAnalyticsEnabled(Context context, boolean val) {

        Boolean setSucess = false;
        try {
            Class<?> c = Class.forName("com.asus.analytics.AnalyticsSettings");

            // Declare the parameter type
            Class<?>[] paramType = new Class<?>[2];
            paramType[0] = Context.class;
            paramType[1] = Boolean.TYPE;

            // Assign the parameter content
            Object[] paramContent = new Object[2];
            paramContent[0] = context;
            paramContent[1] = new Boolean(val);

            // Get method
            Method setEnableAsusAnalytics = c.getMethod("setEnableAsusAnalytics", paramType);

            // Invoke the method
            setSucess = (Boolean) setEnableAsusAnalytics.invoke(
                null, // When the callee is a static function, pass null to this parameter
                paramContent);

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
        } catch(Exception e) {
            Log.w(TAG, "setAsusAnalytics failed ! " + e.toString());
            e.printStackTrace();
        }

        return setSucess.booleanValue();
    }

    public static void deleteBundledSystemPackages(PackageManager pm, String[] pkgNames) {
        if (pm == null) return;

        try {
            // Get class object of package manager
            Class<?> c = pm.getClass();

            // Declare the parameter type
            Class<?>[] paramType = {pkgNames.getClass()};

            // Assign the parameter content
            Object[] paramContent = {pkgNames};

            // Get method
            Method deleteBundledSystemPackages = c.getMethod("deleteBundledSystemPackages", paramType);

            // Invoke the method
            deleteBundledSystemPackages.invoke(pm, paramContent);

        } catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "deleteBundledSystemPackages failed ! " + e.toString());
            e.printStackTrace();
        }
    }
    
    public static boolean getDataEnabled(IBinder b){
    	ITelephony it = ITelephony.Stub.asInterface(b);
    	Class<?> cls = it.getClass();
    	boolean result = true;
    	//5.1
    	try {
			Method method = cls.getMethod("getDataEnabled", Integer.TYPE);
			result = (Boolean) method.invoke(it, new Object[]{0});
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "deleteBundledSystemPackages failed ! " + e.toString());
            e.printStackTrace();
        }
    	
    	//5.0
    	try {
			Method method = cls.getMethod("getDataEnabled");
			result = (Boolean) method.invoke(it);
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "getDataEnabled failed ! " + e.toString());
            e.printStackTrace();
        }
    	return result;
    }
    
    public static void setDataEnabled(IBinder b,boolean enabled){
    	ITelephony it = ITelephony.Stub.asInterface(b);
    	Class<?> cls = it.getClass();
    	//5.1
    	try {
			Method method = cls.getMethod("setDataEnabled", new Class<?>[]{Integer.TYPE,boolean.class});
			method.invoke(it, new Object[]{0,enabled});
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "deleteBundledSystemPackages failed ! " + e.toString());
            e.printStackTrace();
        }
    	
    	//5.0
    	try {
			Method method = cls.getMethod("setDataEnabled",new Class<?>[]{boolean.class});
			method.invoke(it,new Object[]{enabled});
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "setDataEnabled failed ! " + e.toString());
            e.printStackTrace();
        }
    }
    
    public static void restoreCustHiddenPackage(PackageManager pm,String param){
    	Class<?> c = pm.getClass();
    	try {
			Method method = c.getMethod("restoreCustHiddenPackage", String.class);
			method.invoke(pm, param);
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "restoreCustHiddenPackage failed ! " + e.toString());
            e.printStackTrace();
        }
    }
    
    public static boolean isSnapView(UserInfo user){
    	Class<?> c = user.getClass();
    	boolean result = false;
    	try {
			Method method = c.getMethod("isSnapView");
			result = (Boolean)method.invoke(user);
		} catch (IllegalArgumentException e){
            Log.w(TAG, "IllegalArgumentException !");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException !");
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.w(TAG, "InvocationTargetException !");
        } catch(Exception e) {
            Log.w(TAG, "isSnapView failed ! " + e.toString());
            e.printStackTrace();
        }
    	return result;
    }
    
    public static boolean enable(NfcAdapter adapter){
		Class<?> c = adapter.getClass();
		try {
			Method m = c.getMethod("enable");
			boolean result = (Boolean) m.invoke(adapter);
			return result;
		} catch (NoSuchMethodException e) {
			Log.w(TAG, "NoSuchMethodException !");
		} catch (IllegalAccessException e) {
			Log.w(TAG, "IllegalAccessException !");
		} catch (IllegalArgumentException e) {
			Log.w(TAG, "IllegalArgumentException !");
		} catch (InvocationTargetException e) {
			Log.w(TAG, "InvocationTargetException !");
		} catch(Exception e) {
            Log.w(TAG, "enable failed ! " + e.toString());
        }
		return false;
	}
}
