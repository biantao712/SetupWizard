package com.asus.cnsetupwizard;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.asus.cnsetupwizard.utils.Utils;

import java.util.Locale;

/**
 * Created by czy on 17-6-9.
 */

public class SetupWizardCompleteActivity extends BaseActivity {

    public final static String ACTION = "com.asus.cnsetupwizard.setupwizardcomplete";

    private ImageView welcomeImg;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.setupwizard_complete);

        welcomeImg = (ImageView) findViewById(R.id.welcome_title_icon);
        Button exitButton = (Button) findViewById(R.id.button_exit);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.finishSetupWizard(SetupWizardCompleteActivity.this);
                finish();
            }
        });

        Locale locale = getResources().getConfiguration().locale;
        if(!("zh_CN").equals(locale.toString())){
            welcomeImg.setImageResource(R.drawable.slogan_en);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode) {
            case KeyEvent.KEYCODE_BACK:
                //finish();
                break;

            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_MENU:
                break;
        }
        return false;
    }
}
