package com.asus.cnsetupwizard.fingerprint;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternUtils;
import com.asus.cnsetupwizard.BaseActivity;
import com.asus.cnsetupwizard.R;

/**
 * Created by czy on 17-4-5.
 */

public class SelectScreenLockActivity extends BaseActivity implements View.OnClickListener{

    public static final String ACTION = "com.asus.cnsetupwizard.SELECTSCREENLOCK";
    private boolean mHasPassword;
    private UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserManager = UserManager.get(this);
        final int passwordQuality = new ChooseLockSettingsHelper(this).utils()
                .getActivePasswordQuality(mUserManager.getCredentialOwnerProfile(0));
        mHasPassword = passwordQuality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        if(mHasPassword){
            Intent intent =null;
            LockPatternUtils mLockPatternUtils = new LockPatternUtils(this);
            if (!mLockPatternUtils.isLockPatternEnabled(UserHandle.myUserId())){
                intent = new Intent(this,ConfirmLockPassword.class);
            }else{
                intent = new Intent(this,ConfirmLockPattern.class);
            }
            long challenge = getSystemService(FingerprintManager.class).preEnroll();
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
            intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
            //intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, getToken());
            //intent.setAction(FingerprintEnrollFindSensor.ACTION);
            startActivity(intent);
            finish();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(getResources().getColor(R.color.asus_action_bottom_bar_color));// SDK21

        }

        setContentView(R.layout.select_screen_lock);
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.area_title);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
        lp.setMargins(0, height, 0, 0);
        relativeLayout.setLayoutParams(lp);
        View patten = findViewById(R.id.patten);
        View mixture = findViewById(R.id.mixture);
        View number = findViewById(R.id.number);
        ImageView backIcon = (ImageView)findViewById(R.id.button_back);
        TextView previous = (TextView)findViewById(R.id.previous);
        patten.setOnClickListener(this);
        previous.setOnClickListener(this);
        mixture.setOnClickListener(this);
        number.setOnClickListener(this);
        backIcon.setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        final int passwordQuality = new ChooseLockSettingsHelper(this).utils()
                .getActivePasswordQuality(mUserManager.getCredentialOwnerProfile(0));
        mHasPassword = passwordQuality != DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
        if(mHasPassword){
            finish();
        }
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

    }


    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        long challenge = getSystemService(FingerprintManager.class).preEnroll();
        switch (v.getId()){
            case R.id.patten:
                intent.setAction(ChooseLockPattern.ACTION);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
                startActivity(intent);
                finish();
                break;
            case R.id.previous:
                finish();
                break;
            case R.id.mixture:
                intent = new Intent();
                intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
                intent.setAction(ChooseLockPassword.ACTION);
                startActivity(intent);
                finish();
                break;
            case R.id.number:
                intent = new Intent();
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
                intent.setAction(ChooseLockPassword.ACTION);
                startActivity(intent);
                finish();
                break;
            case R.id.button_back:
                finish();
                break;
        }
    }
}
