package com.asus.cnsetupwizard;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class ProjectType{
	
	public static final String TAG = "ProjectType";

	public static final int PROJECT_NO_LAUNCHER3_BACKUP= 0;//old
	public static final int PROJECT_HAS_LAUNCHER3_BACKUP = 1;//feima
	public static final int PROJECT_HAS_LAUNCHER3_NO_BACKUP = 2;//zfone3
	
	private static int s_ProType = -1;
	private static Context m_Context = null;
	private static PackageManager mPm = null;
	private static PackageInfo pi = null;
		
	public static void init(Context context)
	{
		m_Context = context;
		getProjectType();
	}
	
	private static boolean hasPackage(Context context, String packageName)
	{
		boolean result = true;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(packageName, 0);
		} catch (NameNotFoundException ex) {
			result = false;
			//Log.i(TAG, "hasPackage ==="+ex.toString());
		}
		//Log.i(TAG, "hasPackage packageName="+packageName+"   result="+result);
		return result;
	}
	
	private static boolean hasLauncher3(Context context)
	{
		return hasPackage(context, "com.asus.launcher3");
	}

	private static boolean hasBackup(Context context)
	{
		return hasPackage(context, "asus.com.backup");
	}
	
	public static int getProjectType(){
		//return PROJECT_HAS_LAUNCHER3;
		if(s_ProType != -1){
			//Log.i(TAG, "getProjectType 111==="+s_ProType);
			return s_ProType;
		}else
		{
			boolean has_launcher3 = hasLauncher3(m_Context);
			boolean has_backup = hasBackup(m_Context);
			if(has_launcher3 && has_backup)
			{
				s_ProType = PROJECT_HAS_LAUNCHER3_BACKUP;
			}else if(has_launcher3)
			{
				s_ProType = PROJECT_HAS_LAUNCHER3_NO_BACKUP;
			}else{
				s_ProType = PROJECT_NO_LAUNCHER3_BACKUP;
			}
			//Log.i(TAG, "getProjectType 222==="+s_ProType);
			return s_ProType;
		}
    }
}
