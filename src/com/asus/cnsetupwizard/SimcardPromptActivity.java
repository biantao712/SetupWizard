package com.asus.cnsetupwizard;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.asus.cnsetupwizard.wifi.WifiSettingsActivity;

/**
 * Created by czy on 17-3-29.
 */

public class SimcardPromptActivity extends BaseActivity {

    public static final String ACTION = "com.asus.cnsetupwizard.SIMCARD_PROMPT";
    SimStateReceive simStateReceive;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.simcard_prompt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(Color.TRANSPARENT);// SDK21

        }

        TextView previous = (TextView)findViewById(R.id.previous);
        TextView skip = (TextView)findViewById(R.id.skip);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(WelcomeActivity.ACTION);
                startActivity(intent);
                finish();
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(TermAndConditionActivity.ACTION);
                startActivity(intent);
                finish();
            }
        });

        simStateReceive = new SimStateReceive();
        IntentFilter filter = new IntentFilter("android.intent.action.SIM_STATE_CHANGED");
        registerReceiver(simStateReceive, filter);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        unregisterReceiver(simStateReceive);
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

    class SimStateReceive extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                TelephonyManager tm = (TelephonyManager)context.getSystemService(Service.TELEPHONY_SERVICE);
                int state = tm.getSimState();
                switch (state) {
                    case TelephonyManager.SIM_STATE_READY :
                        Intent intent1 = new Intent(SimcardPromptActivity.this,TermAndConditionActivity.class);
                        //intent.setAction(WifiSettingsActivity.ACTION);
                        startActivity(intent1);
                        finish();
                        break;
                    case TelephonyManager.SIM_STATE_UNKNOWN :
                    case TelephonyManager.SIM_STATE_ABSENT :
                    case TelephonyManager.SIM_STATE_PIN_REQUIRED :
                    case TelephonyManager.SIM_STATE_PUK_REQUIRED :
                    case TelephonyManager.SIM_STATE_NETWORK_LOCKED :
                    default:
                        //simState = 0;
                        break;
                }
            }
        }
    }
}
