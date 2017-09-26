package com.asus.cnsetupwizard;
import android.content.Context;
import android.util.Log;
import android.telephony.TelephonyManager;

public class TelephonyManagerMgr
{
	private static final String TAG = "TelephonyManagerMgr.java: ";

	private static TelephonyManagerMgr m_Instance;
	private Context mContext=null;
	private TelephonyManager mTelephonyManager;

	public static TelephonyManagerMgr getInstance(Context c) 
	{
		if(m_Instance == null) 
	    {
			m_Instance = new TelephonyManagerMgr(c);
	    }
	    return m_Instance;
	}
    /**
     * constructor
     */
	private TelephonyManagerMgr(Context context)
	{
		mContext = context;
		mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public TelephonyManager getTelephonyManager()
	{
		if(mTelephonyManager != null)
		{
			Log.d(TAG, "mTelephonyManager  != null");
			return mTelephonyManager;
		}else
		{
			mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
			Log.d(TAG, "mTelephonyManager  == null, retry...");
			if(mTelephonyManager == null)
			{
				Log.d(TAG, "mTelephonyManager  == null, retry fail");
			}
			return mTelephonyManager;
		}
	}
}
