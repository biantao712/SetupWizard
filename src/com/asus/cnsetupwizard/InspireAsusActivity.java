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

package com.asus.cnsetupwizard;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.asus.cnsetupwizard.utils.Constants;

public class InspireAsusActivity extends Activity{
    public static final String TAG = "InspireAsusActivity";
    public static final String ACTION = "com.asus.cnsetupwizard.INSPIRE_ASUS";
    private static final boolean DEBUG = Constants.DEBUG;
    
    private ImageView backButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate (savedInstanceState);
        setContentView(R.layout.inspire_asus_fragment);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Window win = getWindow();
			//win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
			// 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
			//win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN );

			// 部分机型的statusbar会有半透明的黑色背景
			win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			win.setStatusBarColor(getResources().getColor(R.color.theme_color));// SDK21

		}
		Resources resources = getResources();
		int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
		int height = resources.getDimensionPixelSize(resourceId);
		RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.area_title);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
		lp.setMargins(0, height, 0, 0);
		relativeLayout.setLayoutParams(lp);
        
        backButton = (ImageView) findViewById(R.id.button_back);
        backButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			finish();
    		}
    	});
    }

    @Override
    public void onStop() {
        super.onStop();
    }

	@Override
	public void onResume(){
		super.onResume();
		final View decorView = getWindow().getDecorView();
		final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_IMMERSIVE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
		decorView.setSystemUiVisibility(uiOptions);
		decorView.getViewTreeObserver().addOnPreDrawListener(
				new ViewTreeObserver.OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						decorView.setSystemUiVisibility(uiOptions);
						return true;
					}

				});
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.i(TAG, "onDestroy");
	}
}
