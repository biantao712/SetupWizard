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

package com.asus.cnsetupwizard;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocalePicker.LocaleInfo;
import com.asus.cnsetupwizard.PickerView.onSelectListener;
import com.asus.cnsetupwizard.unbundle.Reflection;
import com.asus.cnsetupwizard.utils.Constants;
import com.asus.cnsetupwizard.wifi.WifiSettingsActivity;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;


//import android.app.ActionBar;

@SuppressLint("ClickableViewAccessibility")
public class WelcomeActivity extends BaseActivity {

    public static final String TAG = "WelcomeFragment";
    public static final String ACTION = "com.asus.cnsetupwizard.WELCOME";
    private static final boolean DEBUG = Constants.DEBUG;
    
    private TextView mWelcomeTextTitle;
    private TextView mWelcomeTextContent01;
    private TextView mWelcomeTextContent02;
    private TextView mWelcomeTextContent03;
	private TextView emergencyCall;
	private TextView next;

	PickerView mLocale_pv;
	private LinkedList<Locale> mRequests;
	private LocaleThread mLocaleThread;
	private ImageView welcomeImg;
	private TextView logoText;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		if (DEBUG)
			Log.i(TAG, "onCreate");
		if (ActivityManager.getCurrentUser() != UserHandle.USER_OWNER) {
			launchSetupWizardExit();
			finish();
			return;
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_fragment);

		welcomeImg = (ImageView)findViewById(R.id.welcome_title_icon);

		mLocale_pv = (PickerView) findViewById(R.id.locale_picker_pv);
		mLocale_pv.setParentActivity(this); 
		mRequests = new LinkedList<Locale>();

		logoText = (TextView) findViewById(R.id.logo_text);
		String logoTextContent = getResources().getString(R.string.cloud_launcher);
		logoText.setText(logoTextContent);

		/*String welcomeTitle = getResources().getString(R.string.welcome_title02);
		mWelcomeTextTitle = (TextView) findViewById(R.id.welcome_title);
		mWelcomeTextTitle.setText(welcomeTitle);
		
		String welcomeContent01 = getResources().getString(R.string.welcome_content01);
		mWelcomeTextContent01 = (TextView) findViewById(R.id.welcome_content01_tv);
		mWelcomeTextContent01.setText(welcomeContent01);
		Typeface typeFace =Typeface.createFromAsset(getAssets(),"fonts/Roboto-Light.ttf");
		mWelcomeTextContent01.setTypeface(typeFace);
		
		String welcomeContent02 = getResources().getString(R.string.welcome_content02);
		mWelcomeTextContent02 = (TextView) findViewById(R.id.welcome_content02_tv);
		mWelcomeTextContent02.setText(welcomeContent02);
		mWelcomeTextContent02.setTypeface(typeFace);
		
		String welcomeContent03 = getResources().getString(R.string.welcome_content03);
		mWelcomeTextContent03 = (TextView) findViewById(R.id.welcome_content03_tv);
		mWelcomeTextContent03.setText(welcomeContent03);
		mWelcomeTextContent03.setTypeface(typeFace);*/

		emergencyCall = (TextView) findViewById(R.id.emergency_call);
		emergencyCall.setText(getResources().getString(R.string.emergency_call));
		emergencyCall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent INTENT_EMERGENCY_DIAL = new Intent()
						.setAction("com.android.phone.EmergencyDialer.DIAL")
						.setPackage("com.android.phone");
				startActivity(INTENT_EMERGENCY_DIAL);
				int mask = StatusBarManager.DISABLE_EXPAND |
						StatusBarManager.DISABLE_RECENT |
						StatusBarManager.DISABLE_HOME |
						StatusBarManager.DISABLE_SEARCH;
				Constants.adjustStatusBarLocked(WelcomeActivity.this, mask);
			}
		});

		next = (TextView) findViewById(R.id.next);
		next.setText(getResources().getString(R.string.next));
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TelephonyManager mTelephonyManager = TelephonyManagerMgr.getInstance(getApplicationContext()).getTelephonyManager();
				Intent intent = new Intent();
				if(TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState() || TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState(2)
						|| TelephonyManager.SIM_STATE_READY == mTelephonyManager.getSimState(1)){
					intent.setAction(TermAndConditionActivity.ACTION);
				} else {
					intent.setAction(SimcardPromptActivity.ACTION);
				}
				startActivity(intent);
				finish();
			}
		});

		List<LocaleInfo> mLocaleInfos = LocalePicker.getAllAssetLocales(this,false);
		mLocale_pv.setData(Locale.getDefault(), mLocaleInfos);
		LocalePicker.updateLocale(Locale.getDefault());
		mLocale_pv.setOnSelectListener(new onSelectListener()
		{

			@Override
			public void onSelect(LocaleInfo info)
			{
				if (DEBUG)
					Log.i(TAG, "onSelect "+info.toString());
				String welcomeTextTitle;
				String welcomeContent01;
				String welcomeContent02;
				String welcomeContent03;
				String emergencyCallText;
				String nextText;
				String mLogoText;
				
				Locale locale = info.getLocale();

				switch(locale.toString())
				{
					case "zh_CN":
						/*welcomeTextTitle = getResources().getString(R.string.welcome_title02_zh_CN);
						welcomeContent01 = getResources().getString(R.string.welcome_content01_zh_CN);
						welcomeContent02 = getResources().getString(R.string.welcome_content02_zh_CN);
						welcomeContent03 = getResources().getString(R.string.welcome_content03_zh_CN);*/
						mLogoText = getResources().getString(R.string.cloud_launcher_zh_CN);
						emergencyCallText = getResources().getString(R.string.emergency_call_zh_CN);
						nextText = getResources().getString(R.string.next_zh_CN);
						//welcomeImg.setImageResource(R.drawable.slogan);
						break;
					case "zh_TW":
						/*welcomeTextTitle = getResources().getString(R.string.welcome_title02_zh_TW);
						welcomeContent01 = getResources().getString(R.string.welcome_content01_zh_TW);
						welcomeContent02 = getResources().getString(R.string.welcome_content02_zh_TW);
						welcomeContent03 = getResources().getString(R.string.welcome_content03_zh_TW);*/
						mLogoText = getResources().getString(R.string.cloud_launcher_zh_TW);
						emergencyCallText = getResources().getString(R.string.emergency_call_zh_TW);
						nextText = getResources().getString(R.string.next_zh_TW);
						//welcomeImg.setImageResource(R.drawable.slogan_en);
						break;
					default:
						/*welcomeTextTitle = getResources().getString(R.string.welcome_title02_en);
						welcomeContent01 = getResources().getString(R.string.welcome_content01_en);
						welcomeContent02 = getResources().getString(R.string.welcome_content02_en);
						welcomeContent03 = getResources().getString(R.string.welcome_content03_en);*/
						mLogoText = getResources().getString(R.string.cloud_launcher_en);
						emergencyCallText = getResources().getString(R.string.emergency_call_EN);
						nextText = getResources().getString(R.string.en_next);
						//welcomeImg.setImageResource(R.drawable.slogan_en);
						break;
				}
				//mWelcomeTextContent01.setTypeface(typeFace);
				//mWelcomeTextContent02.setTypeface(typeFace);
				//mWelcomeTextContent03.setTypeface(typeFace);
				/*mWelcomeTextTitle.setText(welcomeTextTitle);
				mWelcomeTextContent01.setText(welcomeContent01);
				mWelcomeTextContent02.setText(welcomeContent02);
				mWelcomeTextContent03.setText(welcomeContent03);*/
				logoText.setText(mLogoText);
				emergencyCall.setText(emergencyCallText);
				next.setText(nextText);
				enqueueRequest(locale);
				//LocalePicker.updateLocale(locale);
			}
		});

		if (ActivityManager.getCurrentUser() != UserHandle.USER_OWNER) {
			launchSetupWizardExit();
			finish();
			return;
		}

		/*if ((ActivityManager.getCurrentUser() == 0) && (!isSnapViewUser())) {
			defaultZenMotionProperty();
		}*/
    }

	@Override
	protected void onStart() {
		super.onStart();
		//start new flow
		//开启线程做decodestream动作
		Constants.threadDecodeStream=new Thread(new Runnable(){
			@Override
			public void run(){
				Constants.mMovie = Movie.decodeStream(getResources().openRawResource(R.drawable.banner3));
			}
		});
		if(!Constants.threadDecodeStream.isAlive()){
			Constants.threadDecodeStream.start();
		}
		//thread.run();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.i(TAG, "onDestroy");
		stopRequestProcessing();
	}

    protected void enqueueRequest(Locale locale) {
        Locale request = (Locale)locale.clone();
        synchronized (mRequests) {
				//Log.i(TAG, "mRequests.add   "+request.getDisplayCountry());
				mRequests.clear();
                mRequests.add(request);
                mRequests.notifyAll();
        }

        startRequestProcessing();
    }

    /**
     * Starts a background thread to process contact-lookup requests, unless one
     * has already been started.
     */
    private synchronized void startRequestProcessing() {
        // If a thread is already started, don't start another.
        if (mLocaleThread != null) {
            return;
        }

        mLocaleThread = new LocaleThread();
        mLocaleThread.setPriority(Thread.MIN_PRIORITY);
        mLocaleThread.start();
    }

    /**
     * Stops the background thread that processes updates and cancels any
     * pending requests to start it.
     */
    private synchronized void stopRequestProcessing() {

        if (mLocaleThread != null) {
            // Stop the thread; we are finished with it.
            mLocaleThread.stopProcessing();
            mLocaleThread.interrupt();
            mLocaleThread = null;
        }
    }

    private class LocaleThread extends Thread {
        private volatile boolean mDone = false;

        public LocaleThread() {
        }

        public void stopProcessing() {
            mDone = true;
        }

        @Override
        public void run() {
            while (true) {
                // Check if thread is finished, and if so return immediately.
                if (mDone) return;

                // Obtain next request, if any is available.
                // Keep synchronized section small.
                Locale req = null;
                synchronized (mRequests) {
                    if (!mRequests.isEmpty()) {
                        req = mRequests.removeFirst();
                    }
                }

                if (req != null) {
                    // Process the request. If the lookup succeeds
                    //Log.i("WelcomeFragment", "LocaleThread    Process the request     "+req.getDisplayCountry());
                    LocalePicker.updateLocale(req);
                } else {
                    // Wait until another request is available, or until this
                    // thread is no longer needed (as indicated by being
                    // interrupted).
                    try {
                        synchronized (mRequests) {
							//Log.i("WelcomeFragment", "LocaleThread    mRequests.wait");
                            mRequests.wait(1000);
                        }
                    } catch (InterruptedException ie) {
                        // Ignore, and attempt to continue processing requests.
                    }
                }
            }
        }
    }

	private void defaultZenMotionProperty() {
		//default persist.asus.dclick
		try {
			if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_ASUS_TOUCHGESTURE_DOUBLE_TAP)) {

				SystemProperties.set("persist.asus.dclick", Integer.toString(0));
				Log.d(TAG, "Set persist.asus.dclick to 0!  ");
			} else {
				Log.d(TAG,"the hardware feature <asus.hardware.touchgesture.double_tap> is not exist!");
			}
		} catch (Exception exp) {
			Log.e(TAG, exp.getMessage().toString());
		}

		try {
			Constants.swipe_prop = SystemProperties.getInt("persist.asus.swipeup",33);
			if (33 != Constants.swipe_prop) {
				SystemProperties.set("persist.asus.swipeup", Integer.toString(0));
				Log.d(TAG, "Set persist.asus.swipeup to 0!   ");
			} else {
				Log.d(TAG,"the hardware feature <asus.hardware.touchgesture.double_tap> is not exist!");
			}
		} catch (Exception exp) {
			Log.e(TAG, exp.getMessage().toString());
		}

		//default persist.asus.gesture.type
		try {
			if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_ASUS_TOUCHGESTURE_LAUNCH_APP)) {

				SystemProperties.set("persist.asus.gesture.type","0000000");
				Log.d(TAG, "Set persist.asus.gesture.type to 0000000 ");
			} else {
				Log.d(TAG,"the hardware feature <asus.hardware.touchgesture.launch_app> is not exist!");
			}
		} catch (Exception exp) {
			Log.e(TAG, exp.getMessage().toString());
		}
	}

	private boolean isSnapViewUser() {
		try {
			UserManager mCoverUserManager = UserManager.get(this);
			if (mCoverUserManager != null) {
				Log.d(TAG, "mCoverUserManager is NULL");
				android.content.pm.UserInfo user = mCoverUserManager
						.getUserInfo(UserHandle.myUserId());
				// android.content.pm.UserInfo user =
				// ActivityManagerNative.getDefault().getCurrentUser();

				if (user != null) {
					boolean result = Reflection.isSnapView(user);
					Log.d(TAG,
							"user.isSnapView() is :" + String.valueOf(result));
					return result;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
			case KeyEvent.KEYCODE_BACK:
				//finish();
				break;

			case KeyEvent.KEYCODE_HOME:
			case KeyEvent.KEYCODE_MENU:
				break;
		}
		return false;
	}
}
