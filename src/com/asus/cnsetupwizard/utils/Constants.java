package com.asus.cnsetupwizard.utils;

import java.util.List;


import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.SystemProperties;
import android.util.Log;
import android.graphics.Movie;

public class Constants {

	// DEBUG is set by system property
	//public static final boolean DEBUG = (SystemProperties.getInt("ro.debuggable", 0) == 1);
	public static final boolean DEBUG = true;
	// PRIVATE_DEBUG is set by developer for locally debugging
	public static final boolean PRIVATE_DEBUG = false;

	public static final String SPCDOT = "•";
	public static final String RELOADPARTITION = "com.asus.SetupWizard.ReLoadPartition";
	public static final int RESULT_FAILED = 1;
	public static final int RESULT_ERROR = 3;
	public static final String AUTHERRORCONTENT = "autherrorcontent";

	public static int swipe_prop = 33;
	//SharedPreference key
	public static final String SP_NAME = "setupwizard_shared_preferences";
	public static final String SP_EULA_CHECKBOX = "eula_checkbox";
	public static final String SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS = SP_EULA_CHECKBOX;

	public static final String ASUS_INTELLIGNECE_MANAGER = "asus_intelligence_manager";
	public static final String SP_EULA_USER_ACCEPT_PRIVACY_POLICY = "eula_user_accept_privacy_policy";

	public static final String SP_COUNTRYCODE_LANGUAGE_NAME = "sp_countrycode_language_name";
	public static final String SP_COUNTRYCODE_LANGUAGE_VALUE = "sp_countrycode_language_value";

	public static final String SP_TALKBACK_NAME = "sp_talkback_name";
	public static final String SP_TALKBACK_VALUE = "sp_talkback__value";


	// A flag that records if user has visited EULA page
	public static final String SP_EULA_USER_VISITED = "eula_user_visited";
	public static final int ASUS_KEYBOARD_VERSIONCODE = 601501220;
	// A flag that records if a DataTransferDialog has been shown
	public static final String NEWASUSACCOUNT = "com.asus.accounts.productregister";
	public static boolean isShowDataTransferDialog = false;
	public static boolean hasModifiedDefaultLanguage = false;
	public static final int NEWASUSACCOUNT_REQUEST_CODE = 20002;
	public static final int SETUP_ACCOUNT_REQUEST_CODE = 70001;
	public final static int NEWASUSACCOUNT_SKIP = 11; // skip from 6-1 & 6-2, register product
	// failed +
	// ASUS account login failed
	public final static int NEWASUSACCOUNT_BACK = 12; // back from 6-1, register product failed+
	// ASUS account login failed
	public final static int NEWASUSACCOUNT_REGISTER_PRODUCT_SUCCESS = 13; // after register
	// product, register
	// product success +
	// ASUS account login success
	public final static int NEWASUSACCOUNT_ASUS_ACCOUNT_LOGIN_SUCCESS = 14; // after login
	// account,
	// register
	// product
	// failed +
	// ASUS account login success
	public final static int NEWASUSACCOUNT_BACK_WITH_LOGIN_SUCCESS = 15; // back from 6-1,
	// register product
	// failed +
	// ASUS account login success
	//actions
	public static final String ACTION_REDEEMSTATUS = "asus.intent.action.drive.redeemstatus";
	public static final String ACTION_NEXT = "com.asus.cnsetupwizard.nextpage";
	public static final String ACTION_GOOGLE_SUW = "com.asus.cnsetupwizard.google.suw";

	//account type
	public static final String ASUS_ACCOUNT_TYPE = "com.asus.account.asusservice";

	//public static boolean isWifiFirstRun = true;
	//public static boolean isMobleDataFirstRun = true;
	public static boolean isSetupWizardFirstRun = true;

	/**
	 * ZE500CL	ZE550ML	ZE551ML	ZE500KL	ZE550KL	ZE551KL	ZE500KG	ZE550KG	ZD550KL	ZE600KL	ZX550ML Z580C Z380C Z380KL
	 * Z300C	Z170C	Z170CG	Z370C	Z370CG	Z300C	Z300CG	Z300CL	Z170MG ZE601KL Z370KL ZC550KL
	 * G500TG	ZC452KG ZC551KL
	 */
	public static CharSequence[] GOOGLE_DRIVE_DEVICES = {"Z00D","Z008","Z00A","Z00E",
			"Z00L","Z00T","Z00RD","Z00W","Z00U","Z00M","Z00X","Z00SD","Z00VD","P01M","P022","P024",
			"P023","P01Z","P01Y","P01W","P01V","P021","P01T","P001","Z011","P002","Z010","Z00YD","Z014D","Z013D"};

	public static int RESTORE_TRY_COUNT = 0;

	/**
	 * judge SDK is 5.1
	 * @return
	 */
	public static boolean isLollipopMR1(){
		int sdkVersion;
		try {
			sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch (NumberFormatException e) {
			sdkVersion = 0;
		}
		return sdkVersion >= 22? true:false;
	}

	/**
	 * ODM have no com.asus.as.Analytics
	 * @return
	 */
	public static boolean isDeviceODM(Context context){
		PackageManager pm = context.getPackageManager();
		try {
			pm.getPackageInfo("com.asus.as", PackageManager.GET_ACTIVITIES);
		} catch (NameNotFoundException e) {
			Log.i("SetupWizard.Constants", "The device is ODM");
			return true;
		}
		Log.i("SetupWizard.Constants", "The device is Not ODM");
		return false;
	}

	private static StatusBarManager mStatusBarManager;

	public static void adjustStatusBarLocked(Context context,int what){
		if(mStatusBarManager == null){
			mStatusBarManager = (StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE);
		}
		mStatusBarManager.disable(what);
		Log.i("AsusSetupWizard.Constants", "adjustStatusBarLocked : " + what);
	}

	public static boolean isPackageExists(String packageName,Context context) {
		PackageManager manager = context.getPackageManager();
		Intent intent = new Intent().setPackage(packageName);
		List<ResolveInfo> infos = manager.queryIntentActivities(intent,
				PackageManager.GET_INTENT_FILTERS);
		if (infos == null || infos.size() < 1) {
			return false;
		}
		return true;
	}
	//用来给第四页的gif使用，提前加载避免卡顿
	public static Movie mMovie;
	public static Thread threadDecodeStream;

	public static void setATDProperty(boolean bActive) {
		try {
			String value = String.valueOf(bActive);
			Log.e("cnsetupwizard1", "I will setprop to "+value);
			SystemProperties.set("persist.sys.setupwizard.active", value);
		} catch (Exception exp) {
			Log.e("cnsetupwizard1", exp.getMessage().toString());
		}
	}
}
