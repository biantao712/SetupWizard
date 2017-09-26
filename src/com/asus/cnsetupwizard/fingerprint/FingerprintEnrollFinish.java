package com.asus.cnsetupwizard.fingerprint;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.TermAndConditionActivity;
import com.asus.cnsetupwizard.utils.Utils;
import com.asus.cnsetupwizard.wifi.WifiSettingsActivity;

/**
 * Created by czy on 17-4-13.
 */

public class FingerprintEnrollFinish extends Activity {

    public static final String ACTION = "com.asus.cnsetupwizard.FINGETPRINTFINISH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_enroll_finish);
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
        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.area_title);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
        lp.setMargins(0, height, 0, 0);
        relativeLayout.setLayoutParams(lp);

        TextView cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });

        TextView next = (TextView) findViewById(R.id.next_button);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setAction(TermAndConditionActivity.ACTION);
                startActivity(intent); */
                Utils.startCompleteActivity(FingerprintEnrollFinish.this);
                finish();
            }
        });

        ImageView backIcon = (ImageView) findViewById(R.id.button_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backActivity();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            backActivity();
        }
        return false;
    }

    public void backActivity(){
        try {
            Intent accountIntent = new Intent();
            ComponentName cn = new ComponentName("com.asus.launcher3", "com.asus.zenlife.activity.commonui.user.SetupWizardJumpActivity");
            accountIntent.setComponent(cn);
            //intent.setAction(FingerprintActivity.ACTION);

            accountIntent.setAction("com.asus.zenlife.action.ACCOUNT_WIZARD");
            accountIntent.putExtra("cnsetupwizard","fingerpringsetting");
            startActivity(accountIntent);
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
