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

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.asus.cnsetupwizard.utils.Constants;
import com.asus.cnsetupwizard.utils.DataCollectionSettings;
import com.asus.cnsetupwizard.wifi.WifiSettingsActivity;

//import android.app.ActionBar;

public class TermAndConditionActivity extends BaseActivity implements OnClickListener {
    public static final String TAG = "AdActivity";
    public static final String ACTION = "com.asus.cnsetupwizard.TermAndCondition";

    private static final boolean DEBUG = Constants.DEBUG;
    private boolean mInWebLink = false;

    private CheckBox mCheckBox;
    private TextView mCheckBoxTextView;

    private CheckBox mAsusManager;
    private TextView mAsusManagerText;
    //private boolean isOpenAsusManager = true;
    private TextView next;




    //private Button mExitBtn;



    private static final String CID_KEY = "ro.config.CID";
    FingerprintManager mFingerprintManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG)
            Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        setContentView(R.layout.term_and_condition);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(getResources().getColor(R.color.asus_content_bg_color));// SDK21

        }

        initUI();

        // Set a flag to record that this user has read EULA page
        if(savedInstanceState == null) {
            setSharedPreference(Constants.SP_EULA_USER_VISITED, true);
        }

        /*ScrollView scrollView = (ScrollView)findViewById(R.id.data_collection_eula_scrollview);
        ScrollView asusManagerScrollView = (ScrollView) findViewById(R.id.asus_intelligence_manager_scrollview) ;
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if(language == "en"){
            LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) scrollView.getLayoutParams(); //取控件textView当前的布局参数
            //linearParams.height = 20;// 控件的高强制设成20

            linearParams.height = getResources().getDimensionPixelOffset(R.dimen.fingerprint_title_margin_top);// 控件的宽强制设成30

            scrollView.setLayoutParams(linearParams);

            LinearLayout.LayoutParams linearParams1 =(LinearLayout.LayoutParams) asusManagerScrollView.getLayoutParams(); //取控件textView当前的布局参数
            //linearParams.height = 20;// 控件的高强制设成20

            linearParams1.height = getResources().getDimensionPixelOffset(R.dimen.lock_title_margin_top);// 控件的宽强制设成30

            asusManagerScrollView.setLayoutParams(linearParams1);
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG)
            Log.i(TAG, "onResume");
        final View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {

                    @Override
                    public boolean onPreDraw() {
                        decorView.setSystemUiVisibility(uiOptions);
                        return true;
                    }

                });

        if(mInWebLink)
        {
            mInWebLink = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (DEBUG)
            Log.i(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG)
            Log.i(TAG, "onDestroy");
    }

    private void initUI(){
        String CID = null;
        /*mExitBtn = (Button) findViewById(R.id.button_exit);
        mExitBtn.setOnClickListener(this);*/
        next = (TextView) findViewById(R.id.next);
        next.setOnClickListener(this);

        CID = SystemProperties.get(CID_KEY);


        //gifView = (GifView) findViewById(R.id.gifView);
        //gifView.setMovieResource(R.drawable.banner3_01);
        //此处的movie已经在SetupWizardActivity.java里面初始化过了。
        //gifView.setMovie(Constants.mMovie);

        mCheckBox = (CheckBox) findViewById(R.id.data_collection_agreement_checkbox);
        mCheckBoxTextView = (TextView) findViewById(R.id.data_collection_agreement_tv);
        //isInspireAsusChecked = getSharedPreference(Constants.SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS, true);
        mCheckBox.setChecked(isInspireAsusChecked);

        mAsusManager = (CheckBox) findViewById(R.id.asus_intelligence_manager_checkbox);
        mAsusManagerText = (TextView) findViewById(R.id.asus_intelligence_manager_tv);
       // isOpenAsusManager = getSharedPreference(Constants.ASUS_INTELLIGNECE_MANAGER, true);
        mAsusManager.setChecked(isOpenAsusManager);
        if(isInspireAsusChecked && isOpenAsusManager)
        {
            next.setEnabled(true);
            next.setAlpha(1.0f);
            /*mExitBtn.setEnabled(true);
            mExitBtn.setTextColor(getResources().getColor(R.color.text_color));
            mExitBtn.setBackground(getDrawable(R.drawable.start_button_enable_bg));*/
        }else
        {
            next.setEnabled(false);
            next.setAlpha(0.3f);
            /*mExitBtn.setEnabled(false);
            mExitBtn.setTextColor(getResources().getColor(R.color.asus_content_summary_color));
            mExitBtn.setBackground(getDrawable(R.drawable.start_button_disable_bg));*/
        }

        mCheckBoxTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean state = mCheckBox.isChecked();
                mCheckBox.setChecked(state = !state);
            }
        });

        mAsusManagerText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAsusManager.setChecked(!mAsusManager.isChecked());
            }
        });

        TextView textView = (TextView) findViewById(R.id.data_collection_eula_textview);

        SpannableString spString = new SpannableString(getResources().getString(R.string.data_collection_policy_title));
        spString.setSpan(new ClickableSpan(){
            @Override
            public void updateDrawState(TextPaint ds){
                super.updateDrawState(ds);
                ds.setTextSize(getResources().getDimensionPixelSize(R.dimen.ad_content_size));//设置字体大小
                //ds.setFakeBoldText(true);//设置粗体
                ds.setColor(getResources().getColor(R.color.text_color));//设置字体颜色
                //ds.setUnderlineText(false);//设置取消下划线
            }
            @Override
            public void onClick(View widget){
                Intent intent = new Intent();
                intent.setAction(InspireAsusActivity.ACTION);
                mInWebLink = true;
                startActivity(intent);
                //添加点击事件
            }
        }, 0, getResources().getString(R.string.data_collection_policy_title).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.append(spString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        String privacyClauseString = getString(R.string.operator_statement);
        SpannableString spannableString = new SpannableString(privacyClauseString);
        spannableString.setSpan(new ClickableSpan(){
            @Override
            public void updateDrawState(TextPaint ds){
                super.updateDrawState(ds);
                ds.setTextSize(getResources().getDimensionPixelSize(R.dimen.ad_content_size));//设置字体大小
                //ds.setFakeBoldText(true);//设置粗体
                ds.setColor(getResources().getColor(R.color.text_color));//设置字体颜色
                //ds.setUnderlineText(false);//设置取消下划线
            }
            @Override
            public void onClick(View widget){
                Intent intent = new Intent();
                intent.setAction(OperatorStatement.ACTION);
                startActivity(intent);
                //添加点击事件
            }
        }, 0, privacyClauseString.length(), 0);
        setListeners();

        SpannableString stringAnd = new SpannableString(getResources().getString(R.string.and));
        stringAnd.setSpan(new ClickableSpan(){
            @Override
            public void onClick(View widget) {

            }

            @Override
            public void updateDrawState(TextPaint ds){
                super.updateDrawState(ds);
                ds.setTextSize(getResources().getDimensionPixelSize(R.dimen.ad_content_size));//设置字体大小
                //ds.setFakeBoldText(true);//设置粗体
                ds.setColor(getResources().getColor(R.color.text_color));//设置字体颜色
                ds.setUnderlineText(false);//设置取消下划线
            }
        }, 0, getResources().getString(R.string.and).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView privacyClause = (TextView) findViewById(R.id.operator_statement);
        if(CID.equals("CMCC")) {
            textView.append(stringAnd);
            //textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.append(spannableString);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            privacyClause.setVisibility(View.VISIBLE);
        }


        TextView previous = (TextView) findViewById(R.id.previous);
        mFingerprintManager = (FingerprintManager) getSystemService(
                FINGERPRINT_SERVICE);
        previous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(WelcomeActivity.ACTION);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setListeners() {

        mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (DEBUG)
                    Log.i(TAG, "OnCheckedChangeListener, check box is checked? " + isChecked);
                isInspireAsusChecked = isChecked;
                setSharedPreference(Constants.SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS, isChecked);
                if(isInspireAsusChecked && isOpenAsusManager)
                {
                    next.setEnabled(true);
                    next.setAlpha(1.0f);
                    /*mExitBtn.setEnabled(true);
                    mExitBtn.setTextColor(getResources().getColor(R.color.text_color));
                    mExitBtn.setBackground(getDrawable(R.drawable.start_button_enable_bg));*/
                }else
                {
                    next.setEnabled(false);
                    next.setAlpha(0.3f);
                    /*mExitBtn.setEnabled(false);
                    mExitBtn.setTextColor(getResources().getColor(R.color.asus_content_summary_color));
                    mExitBtn.setBackground(getDrawable(R.drawable.start_button_disable_bg));*/
                }
            }
        });

        mAsusManager.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setSharedPreference(Constants.ASUS_INTELLIGNECE_MANAGER, isChecked);
                isOpenAsusManager = isChecked;
                if(isInspireAsusChecked && isOpenAsusManager)
                {
                    next.setEnabled(true);
                    next.setAlpha(1.0f);
                    /*mExitBtn.setEnabled(true);
                    mExitBtn.setTextColor(getResources().getColor(R.color.text_color));
                    mExitBtn.setBackground(getDrawable(R.drawable.start_button_enable_bg));*/
                }else
                {
                    next.setEnabled(false);
                    next.setAlpha(0.3f);
                    /*mExitBtn.setEnabled(false);
                    mExitBtn.setTextColor(getResources().getColor(R.color.asus_content_summary_color));
                    mExitBtn.setBackground(getDrawable(R.drawable.start_button_disable_bg));*/
                }
            }
        });


    }

    @Override
    public void onStop() {
        super.onStop();
        isInspireAsusChecked = mCheckBox.isChecked();
        setSharedPreference(Constants.SP_EULA_USER_PARTICIPATE_INSPIRE_ASUS, mCheckBox.isChecked());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent();
                intent.setAction(WelcomeActivity.ACTION);
                startActivity(intent);
                finish();
                break;

            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_MENU:
                break;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                //finishWizard();
                Intent intent = new Intent(WifiSettingsActivity.ACTION);
                startActivity(intent);
                //finish();
                break;
            default:
                break;
        }
    }

    private void setSharedPreference(String key, boolean value) {
        SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        sp.edit().putBoolean(key, value).commit();
    }

    private boolean getSharedPreference(String key, boolean defValue) {
        SharedPreferences sp = getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);
        return sp.getBoolean(key, defValue);
    }


    public void backActivity(){
        try {
            if(mFingerprintManager.hasEnrolledFingerprints()){
                Intent accountIntent = new Intent();
                ComponentName cn = new ComponentName("com.asus.launcher3", "com.asus.zenlife.activity.commonui.user.SetupWizardJumpActivity");
                accountIntent.setComponent(cn);
                //intent.setAction(FingerprintActivity.ACTION);

                accountIntent.setAction("com.asus.zenlife.action.ACCOUNT_WIZARD");
                accountIntent.putExtra("cnsetupwizard","fingerpringsetting");
                startActivity(accountIntent);
            }
            finish();

        }catch (Exception e){
            Intent intent = new Intent();
            Log.d("FingerprintEnrollFinish","not found account activity");
            intent.setAction(WifiSettingsActivity.ACTION);
            startActivity(intent);
            finish();
        }
    }
}
