package com.asus.cnsetupwizard;

import com.asus.cnsetupwizard.unbundle.Reflection;
import com.asus.cnsetupwizard.utils.CheckDeviceProperty;
import com.asus.cnsetupwizard.utils.Constants;
import com.asus.cnsetupwizard.utils.DataCollectionSettings;

import android.annotation.SuppressLint;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.app.ActivityManager;

public class SetupWizardExitActivity extends BaseActivity {
	private static final String TAG = "SetupWizardExit";
	public static final String ACTION = "com.asus.cnsetupwizard.EXIT";
	private static final boolean DEBUG = Constants.DEBUG;
	
	private final boolean isSystemApp = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.i(TAG, "onCreate");
		finishSetupWizard();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG)
			Log.i(TAG, "onDestroy");
		Constants.adjustStatusBarLocked(this,StatusBarManager.DISABLE_NONE);
	}
	
	@SuppressLint("NewApi") 
	public void finishSetupWizard() {
		if (DEBUG)
			Log.d(TAG, "finishSetupWizard");
		
		if ((ActivityManager.getCurrentUser() == 0) && (!isSnapViewUser())) {
			if(isTabletDevice()){//pad
				Log.d(TAG, "finishSetupWizard is pad");
				setZenMotionProperty();
			}
			else
			{
				Log.d(TAG, "finishSetupWizard is not pad");
				if(ProjectType.getProjectType() == ProjectType.PROJECT_NO_LAUNCHER3_BACKUP)
					setZenMotionProperty();
			}
		}

		//開關inspire asus
		//persist.asus.enduser.dialog
		if(CheckDeviceProperty.isSpecificSKU("CN")){
			SystemProperties.set("persist.asus.enduser.dialog",Integer.toString(0));
			Log.i(TAG, "SKU=CN,persist.asus.enduser.dialog default value=0");
		}else {
			boolean isUserParticipate  = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE)
					.getBoolean(Constants.SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS, true);
			if(isUserParticipate){
				Log.i(TAG, "set persist.asus.enduser.dialog = 1");
				SystemProperties.set("persist.asus.enduser.dialog",Integer.toString(1));
			}else{
				Log.i(TAG, "set persist.asus.enduser.dialog = 0");
				SystemProperties.set("persist.asus.enduser.dialog",Integer.toString(0));
			}
		}
		if(isSystemApp){
			PackageManager pm = getPackageManager();

			// Under eng-build, things that are related to data collection won't
			// be displayed.
			// Hence, do not need to set any data collection configurations if
			// under eng-build.
				DataCollectionSettings.setDataCollectionConfiguration(this);
			// Disable the GMS Setup wizard when system is first running
			// Under eng-build, they will not include GMS cnsetupwizard by
			// themselves.
			// No need to disable the GMS cnsetupwizard for them.
			try {
				/*pm.setComponentEnabledSetting(new ComponentName(
						"com.google.android.setupwizard",
						"com.google.android.setupwizard.SetupWizardActivity"),
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);*/

				pm.setComponentEnabledSetting(
						new ComponentName("com.google.android.setupwizard",
								"com.google.android.setupwizard.MobileConnectivityChangeReceiver"),
						PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
			} catch (IllegalArgumentException e) {
				Log.w(TAG,
						"Google Mobile Services has not installed on this device");
			}

		// Set setup wizard has run
		Settings.System.putInt(getContentResolver(),
				Settings.System.SETUP_WIZARD_HAS_RUN, 1);
		Settings.Secure.putString(getContentResolver(),
				Settings.Secure.LAST_SETUP_SHOWN, "honeycomb_epad_1");

		// New value to notify the system the user setup has completed after
		// JellyBean 4.2
		Settings.Secure.putInt(getContentResolver(),
				Settings.Secure.USER_SETUP_COMPLETE, 1);

		// Set a persistent setting to notify other apps the device has been
		// provisioned
		Settings.Global.putInt(getContentResolver(),
				Settings.Global.DEVICE_PROVISIONED, 1);

		// Send intent to tell KeyguardUpdateMonitor to update device provision
		// value
		Intent intent = new Intent("com.asus.cnsetupwizard.Finished");
		sendBroadcast(intent);

		// The flag "DONT_KILL_APP" is set for waiting that startLauncher()
		// could be called
		// If the flag is set to 0, the process will be killed immediately
		pm.setComponentEnabledSetting(new ComponentName(getPackageName(),
				"com.asus.cnsetupwizard.WelcomeActivity"),
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				0);
				//PackageManager.DONT_KILL_APP);
		}
		startLauncher();
		
		if (DEBUG)
			Log.d(TAG, "finish");
		mToken = null;
		finish();
	}
	
	private void startLauncher() {
		Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
		homeIntent.addCategory(Intent.CATEGORY_HOME);
		homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(homeIntent);
	}
	
	private void setZenMotionProperty() {

       //set persist.asus.dclick
       try {
           if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_ASUS_TOUCHGESTURE_DOUBLE_TAP)) {
			   
              SystemProperties.set("persist.asus.dclick", Integer.toString(1));
			
              Log.d(TAG, "Set persist.asus.dclick to 1!!!! ");
           } else {
              Log.d(TAG,"the hardware feature <asus.hardware.touchgesture.double_tap> is not exist!");
           }
       } catch (Exception exp) {
           Log.e(TAG, exp.getMessage().toString());
       }
       
       try {
           if (33 != Constants.swipe_prop) {
			   
               SystemProperties.set("persist.asus.swipeup", Integer.toString(1));
			
              Log.d(TAG, "Set persist.asus.swipeup to 1!   ");
           } else {
              Log.d(TAG,"the hardware feature <asus.hardware.touchgesture.swipe_up> is not exist!");
           }
       } catch (Exception exp) {
           Log.e(TAG, exp.getMessage().toString());
       }

       //set persist.asus.gesture.type
       try {
           if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_ASUS_TOUCHGESTURE_LAUNCH_APP)) {
              SystemProperties.set("persist.asus.gesture.type","1111111");
              Log.d(TAG, "Set persist.asus.gesture.type to 1111111 ");
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
	
	static public boolean isTabletDevice() {
        return "tablet".contentEquals(SystemProperties.get("ro.build.characteristics", "phone"));
    }
}
