package com.asus.cnsetupwizard;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import com.asus.cnsetupwizard.utils.Constants;

import java.lang.reflect.Method;

//import android.app.ActionBar;

public class BaseActivity extends Activity {
	private static final String TAG = "BaseActivity";
	private static final boolean DEBUG = Constants.DEBUG;


    //for touch scroll left & right
    private boolean isMotionDown = false;
    private int downX;
    private int downY;

	protected static WifiManager mWifiManager;
	protected static TelephonyManager mTelephonyManager;

	private int indicator_count = 4;
	private int indicator_width = 8;
	private int indicator_interval = 12;
//	private boolean isPad = false;
	private int m_ProjectType = -1;
	public static byte[] mToken;
	public static boolean isOpenAsusManager = true;

	protected static boolean isInspireAsusChecked = true;
	/*private  int mask =
			StatusBarManager.DISABLE_EXPAND |
					StatusBarManager.DISABLE_RECENT |
					StatusBarManager.DISABLE_HOME |
					StatusBarManager.DISABLE_BACK |
					StatusBarManager.DISABLE_SEARCH;*/


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.i(TAG, "onCreate");
		
		ProjectType.init(this);
		m_ProjectType = ProjectType.getProjectType();

        //m_PageViewMgr = PageViewMgr.getPageViewMgr(this, indicator_count, indicator_width, indicator_interval);
		
		//setTranslucentStatus(true);
		int mask;
		if(checkDeviceHasNavigationBar(this)){
			mask = StatusBarManager.DISABLE_EXPAND |
							StatusBarManager.DISABLE_RECENT |
							StatusBarManager.DISABLE_HOME |
							StatusBarManager.DISABLE_BACK |
							StatusBarManager.DISABLE_SEARCH;
		}else {
			mask = StatusBarManager.DISABLE_EXPAND |
					StatusBarManager.DISABLE_RECENT |
					StatusBarManager.DISABLE_HOME |
					StatusBarManager.DISABLE_SEARCH;
		}
		
		Constants.adjustStatusBarLocked(this, mask);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Window win = getWindow();
			win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
			// 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
			win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN );

			// 部分机型的statusbar会有半透明的黑色背景
			win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			win.setStatusBarColor(Color.TRANSPARENT);// SDK21

		}
		//Log.i(TAG, "StatusBarManager.DISABLE_MASK");
		
		// Set device orientation
		/*int screenSize = this.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		if (screenSize <= Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}*/
		//open the wifi by deault by lei_guo
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (Constants.isSetupWizardFirstRun) {
			//默认打开wifi
			mWifiManager.setWifiEnabled(true);
			//默认关闭数据流量
			//mTelephonyManager = TelephonyManagerMgr.getInstance(this).getTelephonyManager();
			//mTelephonyManager.setDataEnabled(false);
			//首次运行设置prop为true
			//Constants.setATDProperty(true);
			Constants.isSetupWizardFirstRun = false;
		}
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG)
			Log.i(TAG, "onStart");
		//when reset device and set the USER_SETUP_COMPLETE=1 someway,to prevent the SUW crash.
		boolean completed = Settings.Secure.getInt(getContentResolver(),Settings.Secure.USER_SETUP_COMPLETE , 0) != 0;
		if (completed) {
			Log.i(TAG, "USER_SETUP completed");
			launchSetupWizardExit();
			//mWizardManagerInstance.launchActivity(this, SetupWizardExitActivity.ACTION);
			finish();
		}
		Log.i(TAG, "USER_SETUP not completed");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG)
			Log.i(TAG, "onResume");

		hideNavigationBar();

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.i(TAG, "onDestroy");
		//int iIndex = mWizardManagerInstance.getCurrentActivityIndex();
		//if(iIndex == 4){
			//Log.i(TAG, "setVisible false, getCurrentActivityIndex = " + iIndex);
			//m_PageViewMgr.setVisible(false);
		//}
//		ActionBar actionBar = getActionBar();
//		if(actionBar != null)
//			actionBar.show();
	}
	
	private void hideNavigationBar(){
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

/*	private void setActionBarTitle(ActionBar actionBar) {
		if(actionBar == null)
			return;
		String currentActivity = mWizardManagerInstance.getCurrentActivity();
		switch (currentActivity) {
		case WifiSettingsActivity.ACTION:
			actionBar.setTitle(getResources().getString(R.string.select_wifi_title));
			break;
		case DataCollectionActivity.ACTION:
			actionBar.setTitle(getResources().getString(R.string.data_collection_title));
			break;
		default:
			break;
		}
	}*/
	
	public void setTranslucentStatus(boolean on) {
		Window win = getWindow();
		WindowManager.LayoutParams winParams = win.getAttributes();
		final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
		if (on) {
			winParams.flags |= bits;
		} else {
			winParams.flags &= ~bits;
		}
		win.setAttributes(winParams);
	}

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
			
		case KeyEvent.KEYCODE_HOME:
		case KeyEvent.KEYCODE_MENU:
			break;
		}
		return false;
	}
	
	private static final String ACTION_NEXT = "com.android.wizard.NEXT";
	private static final String EXTRA_SCRIPT_URI = "scriptUri";
    private static final String EXTRA_ACTION_ID = "actionId";
    private static final String EXTRA_RESULT_CODE = "com.android.cnsetupwizard.ResultCode";
    public static final String EXTRA_THEME = "theme";

	
	/*@Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MotionEvent.ACTION_DOWN");
                isMotionDown = true;
                downX = (int)event.getX() ;
                downY = (int)event.getY() ;

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MotionEvent.ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MotionEvent.ACTION_UP");
                if(isMotionDown)
                {
                    int deltaX = (int)(event.getX() - downX);
                    int deltaY = (int)(event.getY() - downY);
                    Log.d(TAG, "MotionEvent.ACTION_UP  deltax =" + deltaX);
                    int x = Math.abs(deltaX);
                    int y = Math.abs(deltaY);
                    if(x > y)
                    {
                        if(deltaX > 50)
                        {
                            mWizardManagerInstance.onPrevious(this);
                        } else if(deltaX < -50) {
							if(m_ProjectType == ProjectType.PROJECT_NO_LAUNCHER3_BACKUP)
							{
								int iIndex = mWizardManagerInstance.getCurrentActivityIndex();
								if(3 == iIndex)
								{
									Log.d(TAG, "SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS   =" + isInspireAsusChecked);
									if(isInspireAsusChecked)
									{
										finish();
										mWizardManagerInstance.launchActivity(this, SetupWizardExitActivity.ACTION);
									}
								}else
								{
									mWizardManagerInstance.onNext(this);
								}
							}else
							{
								int iIndex = mWizardManagerInstance.getCurrentActivityIndex();
								if(3 == iIndex)
								{
									Log.d(TAG, "SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS   =" + isInspireAsusChecked);
									if(isInspireAsusChecked)
									{
										mWizardManagerInstance.onNext(this);
									}
								}else
								{
									mWizardManagerInstance.onNext(this);
								}
							}
                        }
                    }
                }
                isMotionDown = false;
                break;
        }

        return super.onTouchEvent(event);
    }*/
	/**
	 * Activity finish use the animation
	 */
	public void applyBackwardTransition(){
		overridePendingTransition(R.anim.slide_back_in, R.anim.slide_back_out);
	}
	
	/**
	 * Start Activity use the animation
	 */
	public void applyForwardTransition(){
		overridePendingTransition(R.anim.slide_next_in, R.anim.slide_next_out);
	}
	
	public void launchActivity(String action){
		Intent intent = new Intent();
		intent.setAction(action);
		startActivity(intent);
		applyForwardTransition();
	}
		public void launchSetupWizardExit(){		
		launchActivity(SetupWizardExitActivity.ACTION);
	}



	public static boolean checkDeviceHasNavigationBar(Context context) {
		boolean hasNavigationBar = false;
		Resources rs = context.getResources();
		int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
		try {
			Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {

		}
		return hasNavigationBar;

	}
}
