package com.asus.cnsetupwizard.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.os.Process;
import android.preference.Preference;
import android.util.Log;
import android.util.Xml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class APPFilter {
    private final String TAG="APPFilter";
    private final Context mContext;

    //Path for the cust-xml files root
    private final String CUST_ROOT = "/etc/app_filter/";
    private final String CUST_PM = "app_filter.xml";

    private HashMap<String,HashSet> hiddenPackages = new HashMap<String, HashSet>();
    private HashMap<String,HashSet> shownPackages = new HashMap<String, HashSet>();

    public APPFilter(Context context) {
        mContext = context;
        File resCustFile = new File(CUST_ROOT + CUST_PM);     

        if (resCustFile.exists()) {
            Log.d(TAG,"parsing file: " + resCustFile.getPath() + "! ");
            parsingXmlFile(resCustFile);
        }else{
        	Log.d(TAG,"root of customize files is not exist");
        }
    }
    
    private void parsingXmlFile(File res) {
        try {
        	Log.d(TAG,"parsingXmlFile:"+res.getPath());
            InputStream in = new BufferedInputStream(new FileInputStream(res));
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            int type = parser.getEventType();
            String tagName;
            HashSet<String> localeCodes = new HashSet<String>();
            String packageName = null;
            Boolean isHide = false;
            while(type != XmlPullParser.END_DOCUMENT) {
                switch(type) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        Log.d(TAG,"<"+tagName+">");
                        if (tagName != null && tagName.equals("app")) {
                            packageName = parser.getAttributeValue(null, "packageName").trim();
                            Log.d(TAG,packageName);
                            isHide = Boolean.valueOf(parser.getAttributeValue(null,"isHide").trim());
                        } else if (tagName != null && tagName.equals("locale")) {
                            String localeCode = parser.nextText();
                            localeCodes.add(localeCode);
                            Log.d(TAG, localeCode);
                        }
                        break;
                    case XmlPullParser.END_TAG: 
                    	if(localeCodes.size()>0){
                    		if(isHide)
                    			hiddenPackages.put(packageName, localeCodes);
                    		else
                    			shownPackages.put(packageName, localeCodes);
                    	}                       
                        localeCodes.clear();
                        tagName = parser.getName();
                        Log.d(TAG,"</"+tagName+">");
                        break;
                    default:
                        break;
                }
                type = parser.next();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Factory methods for get the lists    
   /* public HashMap<String,HashSet> getHiddenPackages(){
    	return hiddenPackages;
    }
    
    public HashMap<String,HashSet> getShownPackages(){
    	return shownPackages;
    }*/
    
    public void filterAPPsWithLocaleCode(){
    	 PackageManager pm = mContext.getPackageManager();
    	 String locale = Locale.getDefault().toString();
    	 Log.d(TAG, "current locale is:" + locale);
    	 for(String packageName:hiddenPackages.keySet()){
    		 Log.d(TAG, "the hidden packageName is:" + packageName);
    		 HashSet<String> localeCodes = hiddenPackages.get(packageName);
    		 for(String localeCode:localeCodes){
    			 Log.d(TAG, "the hidden localeCode is:" + localeCode);
    		 }
    		 if(localeCodes.contains(locale)){
    			 try{ 	
        			 pm.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
		           			 PackageManager.DONT_KILL_APP);
    			 }catch(Exception exp){
    				 Log.d(TAG, "disable package failed!");
    			 }            			
    		 }    		
    	 }
    	 for(String packageName:shownPackages.keySet()){
    		 Log.d(TAG, "the shown packageName is:" + packageName);
    		 HashSet<String> localeCodes = shownPackages.get(packageName);
    		 for(String localeCode:localeCodes){
    			 Log.d(TAG, "the shown localeCode is:" + localeCode);
    		 }
    		 if(!localeCodes.contains(locale)){
    			 try{ 	
        			 pm.setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
		           			 PackageManager.DONT_KILL_APP);
    			 }catch(Exception exp){
    				 Log.d(TAG, "disable package failed!");
    			 }      
    		 }
    	 }
    }
}

