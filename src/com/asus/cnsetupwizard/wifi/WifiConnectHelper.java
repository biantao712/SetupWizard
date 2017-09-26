package com.asus.cnsetupwizard.wifi;

import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WifiConnectHelper {
	
	private static final String TAG = "WifiConnectHelper";
	
	public static final int STATUS_OK_MIN = 200;
	
	public static final int STATUS_OK_MAX = 400;
	
	public static final int STATUS_NOT_FOUND = 404;
	
	public void checkConnect2Google(final Handler handler){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				URL url;
				int resCode = 0;
				try {
					url = new URL("http://www.google.com");
					HttpURLConnection httpURLConn = (HttpURLConnection)url.openConnection();
					httpURLConn.setConnectTimeout(2*1000);
					httpURLConn.setRequestMethod("GET");
					httpURLConn.setRequestProperty("Accept", "*/*");
					resCode = httpURLConn.getResponseCode();
					Log.i(TAG, "resCode = " + resCode); //获取响应码
				} catch (Exception e){
					Log.i(TAG, "Can not connect to Google");
					resCode = STATUS_NOT_FOUND;
				}finally{
					Message msg = new Message();
					msg.arg1 = resCode;
					handler.sendMessage(msg);
				}
			}
		}).start();
	}
	
}
