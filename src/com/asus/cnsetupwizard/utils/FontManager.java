package com.asus.cnsetupwizard.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Typeface;

public class FontManager {
	
	private static Map<String, Typeface> typefaceMap = new HashMap<String, Typeface>();
	
	public static Typeface getTypefaceByFontName(Context context,String name){
		
		if(typefaceMap.containsKey(name)){
			return typefaceMap.get(name);
		}else{
//			Typeface tf = Typeface.createFromAsset(context.getAssets(), name);
			Typeface tf = Typeface.create(name, Typeface.NORMAL);
			typefaceMap.put(name, tf);
			return tf;
		}		
	}

}
