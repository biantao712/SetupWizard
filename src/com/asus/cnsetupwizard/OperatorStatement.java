package com.asus.cnsetupwizard;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by czy on 17-3-10.
 */
public class OperatorStatement extends Activity {

    public static final String TAG = "InspireAsusActivity";
    public static final String ACTION = "com.asus.cnsetupwizard.OPERATOR_STATEMENT";

    private ImageView backButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate (savedInstanceState);
        setContentView(R.layout.operator_statement_activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window win = getWindow();
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            //win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN );

            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(getResources().getColor(R.color.theme_color));// SDK21
        }

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.area_title);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
        lp.setMargins(0, height, 0, 0);
        relativeLayout.setLayoutParams(lp);

        backButton = (ImageView) findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume(){
        super.onResume();
        final View decorView = getWindow().getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
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
    public void onDestroy() {
        super.onDestroy();
    }
}
