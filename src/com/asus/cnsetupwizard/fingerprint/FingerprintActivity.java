package com.asus.cnsetupwizard.fingerprint;

import android.content.ComponentName;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.asus.cnsetupwizard.BaseActivity;
import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.TermAndConditionActivity;
import com.asus.cnsetupwizard.utils.Utils;
import com.asus.cnsetupwizard.wifi.WifiSettingsActivity;

/**
 * Created by czy on 17-4-5.
 */

public class FingerprintActivity extends BaseActivity {

    public static final String ACTION = "com.asus.cnsetupwizard.FINGERPRINT";
    FingerprintManager mFingerprintManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LocalePicker.updateLocale(mlocale);
        mFingerprintManager = (FingerprintManager) getSystemService(
                FINGERPRINT_SERVICE);
        if(mFingerprintManager.hasEnrolledFingerprints()){
            //Intent intent = new Intent();
            //intent.setAction(TermAndConditionActivity.ACTION);
            //startActivity(intent);
            Utils.startCompleteActivity(this);
            finish();
        }
        setContentView(R.layout.fingetprint);
        TextView skip = (TextView)findViewById(R.id.skip);
        TextView addFingerprint = (TextView)findViewById(R.id.fingetprint_add);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent();
                intent.setAction(TermAndConditionActivity.ACTION);
                startActivity(intent);*/
                Utils.startCompleteActivity(FingerprintActivity.this);
            }
        });

        addFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(SelectScreenLockActivity.ACTION);

                //intent.setClassName("com.android.settings","com.asus.suw.lockscreen.AsusSetupFingerprintEnrollEnrolling");
                startActivity(intent);
            }
        });

        TextView previous = (TextView) findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Log.d("FingerprintActivity","not found account activity");
                    intent.setAction(WifiSettingsActivity.ACTION);
                    startActivity(intent);
                    finish();
                }

            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mFingerprintManager.hasEnrolledFingerprints()){
            /*Intent intent = new Intent();
            intent.setAction(TermAndConditionActivity.ACTION);
            startActivity(intent);*/
            Utils.startCompleteActivity(this);
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
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
                Log.d("FingerprintActivity","not found account activity");
                intent.setAction(WifiSettingsActivity.ACTION);
                startActivity(intent);
                finish();
            }
        }
        return false;
    }
}
