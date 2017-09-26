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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class DataCollectionSettings {

    private static final String TAG = "DataCollectionSettings";

    /**
     * Get user configurations that are related to data collection from the shared preference,
     * and set these values to the system.
     * @param none
     */
    public static void setDataCollectionConfiguration(Context context) {
    	SharedPreferences sp = context.getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE);
    	
    	String SHARED_FILE_INSPIRE = "inspireasus";
    	String SHAREDKEY_INSPIREASUS = "sharekey_inspireasus";//key
        boolean isOpenAnalytics = true;
        try{
        	Context otherContext = context.createPackageContext("com.android.settings", Context.CONTEXT_IGNORE_SECURITY);
        	SharedPreferences sp1 = otherContext.getSharedPreferences(SHARED_FILE_INSPIRE, Context.MODE_MULTI_PROCESS);
        	if(sp1!=null){
             	isOpenAnalytics = sp1.getBoolean(SHAREDKEY_INSPIREASUS, true);
             	Log.d(TAG, "the sharekey_inspireasus is: " + String.valueOf(isOpenAnalytics));
            }           
        }catch(Exception exp){
        	Log.e(TAG, "the sharekey_inspireasus is: " + exp.getMessage().toString());
        }

        if(!CheckDeviceProperty.isSpecificSKU("ATT") && isOpenAnalytics){
        	boolean setSuccess = setAsusAnalyticsEnabled(context, sp.getBoolean(Constants.SP_EULA_CHECKBOX, true));
        	Log.v(TAG, "Set AsusAnalytics configuration success ? " + setSuccess);
        }
        Log.v(TAG, "AsusAnalytics enabled ? " + isAsusAnalyticsEnabled(context));
    }

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
}
