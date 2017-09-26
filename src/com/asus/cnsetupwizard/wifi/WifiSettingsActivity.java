package com.asus.cnsetupwizard.wifi;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.asus.cnsetupwizard.BaseActivity;
import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.utils.Constants;

public class WifiSettingsActivity extends BaseActivity {
	private static final String TAG = "WifiSettingsActivity-1";
	public static final String ACTION = "com.asus.cnsetupwizard.WIFISETTING";

	static final int ACTION_PRE = 0;
	static final int ACTION_NEXT = 1;

	private boolean m_focus = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//LocalePicker.updateLocale(mlocale);
		setContentView(R.layout.wifisettings_layout);

		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		WifiSettingsFragment frag = new WifiSettingsFragment(this);

		ft.replace(R.id.fragment_container, frag);
		ft.commit();

		Constants.setATDProperty(false);
		//Log.i(TAG, "onCreate");
	}
	@Override
	protected void onResume() {
		super.onResume();
		//Log.i(TAG, "onResume"+"  m_focus="+m_focus+"  m_PageViewMgr.bHideForDlg="+m_PageViewMgr.bHideForDlg);
	}
	@Override 
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus); 
        //Log.i(TAG, "onWindowFocusChanged called. hasFocus="+hasFocus + "    m_PageViewMgr.bHideForDlg"+m_PageViewMgr.bHideForDlg); 
		m_focus = hasFocus;
    }
	@Override
	protected void onDestroy() {
		super.onDestroy();
		//Log.i(TAG, "onDestroy");
	}
	@Override
	protected void onPause(){
		super.onPause();
	}

	//////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		super.onKeyUp(keyCode, event);
		if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
			this.hideNavigationBar();
		}
		return false;
	}


	public void hideNavigationBar() {
		int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
				| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
				| View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

		if( android.os.Build.VERSION.SDK_INT >= 19 ){
			uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
		} else {
			uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
		}

		getWindow().getDecorView().setSystemUiVisibility(uiFlags);
	}


}
