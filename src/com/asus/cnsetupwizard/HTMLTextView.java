package com.asus.cnsetupwizard;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

/**
 * TODO: document your custom view class.
 */
public class HTMLTextView extends TextView {
	private static final String TAG = "HTMLTextView.java";
	private static final String ZH_TW = "html/zh_tw/inspire_asus.html";
	private static final String ZH_CN = "html/zh_cn/inspire_asus.html";
	private static final String EN = "html/en/inspire_asus.html";

    public HTMLTextView(Context context, AttributeSet set) {
        super(context, set);
        Log.d(TAG, "HTMLTextView");
        init(context);
    }

    private void init(Context context){

        final AssetManager am = context.getAssets();
        InputStream in = null;
        String htmlString = "";
        String URL = EN;
        
        try {
			Locale locale = Locale.getDefault();
			String language = locale.getLanguage().toLowerCase();
			String country = locale.getCountry().toLowerCase();
			if(language.equals("zh") && country.equals("tw"))
			{
				URL = ZH_TW;
			}else if(language.equals("zh") && country.equals("cn"))
			{
				URL = ZH_CN;
			}else
			{
				URL = EN;
			}
		} catch (Exception e) {
			URL = EN;
			Log.d(TAG, "Exception =" + e);
        }
        

		try {
			in = am.open(URL);
			Log.d(TAG, "in =" + in);
			StringBuilder sb=new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			Log.d(TAG, "br =" + br);
			String read;

			while((read=br.readLine()) != null) {
				Log.d(TAG, "read =" + read);
				sb.append(read);   
			}

			br.close();
			htmlString = sb.toString();
			Log.d(TAG, "htmlString =" + htmlString);
        } catch (Exception e) {
			Log.d(TAG, "Exception =" + e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {}
            }
        }
       setText(Html.fromHtml(htmlString));
    }
}
