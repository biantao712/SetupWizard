package com.asus.cnsetupwizard.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.asus.cnsetupwizard.R;


/**
 * Activity explaining the fingerprint sensor location for fingerprint enrollment.
 */
public class FingerprintEnrollFindSensor extends Activity {

    private static String TAG = "FingerprintEnrollFindSensor";
    private static boolean DEBUG = true;
    public static final String ACTION = "com.asus.cnsetupwizard.FINGERPRINTFIND";

    private static final int CONFIRM_REQUEST = 1;
    private static final int ENROLLING = 2;
    public static final String EXTRA_KEY_LAUNCHED_CONFIRM = "launched_confirm_lock";
    public static final String EXTRA_KEY_SENEOR_POSITION = "fingerprint_sensor_position";

    //private FingerprintFindSensorAnimation mAnimation;
    private boolean mLaunchedConfirmLock;
    private FingerprintEnrollSidecar mSidecar;
    private boolean mNextClicked;

    private TextView mMessage;
    private AsusFindFingerprintSensorView mSensorPositionView;
    private boolean mPosition = true; //True == front, False == back
    protected byte[] mToken;
    protected int mUserId;

    protected static final int RESULT_FINISHED = RESULT_FIRST_USER;
    protected static final int RESULT_SKIP = RESULT_FIRST_USER + 1;
    protected static final int RESULT_TIMEOUT = RESULT_FIRST_USER + 2;
    public static final String TAG_SIDECAR = "sidecar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
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
        //setHeaderText(R.string.wifi);
        if (savedInstanceState != null) {
            mLaunchedConfirmLock = savedInstanceState.getBoolean(EXTRA_KEY_LAUNCHED_CONFIRM);
            mToken = savedInstanceState.getByteArray(
                    ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        }
        mToken = getIntent().getByteArrayExtra(
                ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        if (mToken == null && !mLaunchedConfirmLock) {
            //launchConfirmLock();
        } else if (mToken != null) {
            startLookingForFingerprint(); // already confirmed, so start looking for fingerprint
        }
        //startLookingForFingerprint();

        //++ Asus feature
        //mAnimation = (FingerprintFindSensorAnimation) findViewById(
        //        R.id.fingerprint_sensor_location_animation);
        mSensorPositionView = (AsusFindFingerprintSensorView)findViewById(R.id.find_sensor_view);
        mMessage = (TextView)findViewById(R.id.find_sensor_text);
        //mPosition = AsusFindFingerprintSensorView.SENSOR_FRONT.equals(mSensorPositionView.getPosition());
        mPosition = mSensorPositionView.getPosition().contains(AsusFindFingerprintSensorView.SENSOR_FRONT);
        if(mPosition){
            mMessage.setText(R.string.fingerprint_position_front);
        }else{
            mMessage.setText(R.string.fingerprint_position_back);
        }

        /*TextView hintText = (TextView) findViewById(R.id.hint_message);
        if(mPosition){
            hintText.setVisibility(View.VISIBLE);
        }*/
        //-- Asus feature

        mUserId = getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.myUserId());
        TextView cancel = (TextView) findViewById(R.id.cancel);
        TextView next = (TextView) findViewById(R.id.next);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
                finish();
            }
        });

        ImageView backIcon = (ImageView) findViewById(R.id.button_back);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    protected int getContentView() {
        return R.layout.fingerprint_enroll_find_sensor;
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
    protected void onStart() {
        super.onStart();
        //mAnimation.startAnimation();
    }

    private void startLookingForFingerprint() {
        mSidecar = (FingerprintEnrollSidecar) getFragmentManager().findFragmentByTag(
                TAG_SIDECAR);
        if (mSidecar == null) {
            mSidecar = new FingerprintEnrollSidecar();
            getFragmentManager().beginTransaction()
                    .add(mSidecar, TAG_SIDECAR).commit();
        }
        mSidecar.setListener(new FingerprintEnrollSidecar.Listener() {
            @Override
            public void onEnrollmentProgressChange(int steps, int remaining) {
                mNextClicked = true;
                if (mSidecar != null && !mSidecar.cancelEnrollment()) {
                    proceedToEnrolling();
                }
            }

            @Override
            public void onEnrollmentHelp(CharSequence helpString) {
            }

            @Override
            public void onEnrollmentError(int errMsgId, CharSequence errString) {
                if (mNextClicked && errMsgId == FingerprintManager.FINGERPRINT_ERROR_CANCELED) {
                    mNextClicked = false;
                    proceedToEnrolling();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mAnimation.pauseAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mAnimation.stopAnimation();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_KEY_LAUNCHED_CONFIRM, mLaunchedConfirmLock);
        outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
    }

    protected void onNextButtonClick() {
        mNextClicked = true;
        //if (mSidecar == null || (mSidecar != null && !mSidecar.cancelEnrollment())) {
            proceedToEnrolling();
        //}
    }

    private void proceedToEnrolling() {
        if(mSidecar != null){
            getFragmentManager().beginTransaction().remove(mSidecar).commit();
        }else{
        }
        mSidecar = null;
        startActivityForResult(getEnrollingIntent(), ENROLLING);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CONFIRM_REQUEST) {
            if (resultCode == RESULT_OK) {
                mToken = data.getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
                //overridePendingTransition(R.anim.suw_slide_next_in, R.anim.suw_slide_next_out);
                getIntent().putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
                startLookingForFingerprint();
            } else {
                finish();
            }
        } else if (requestCode == ENROLLING) {
            //++ Asus flow: return token to FingerprintSettings
            if(data == null){
                data = new Intent();
            }
            data.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
            //-- Asus flow: return token to FingerprintSettings
            if (resultCode == RESULT_FINISHED) {
                setResult(RESULT_FINISHED, data); // Asus flow: return token to FingerprintSettings
                finish();
            } else if (resultCode == RESULT_SKIP) {
                setResult(RESULT_SKIP, data);  // Asus flow: return token to FingerprintSettings
                finish();
            } else if (resultCode == RESULT_TIMEOUT) {
                setResult(RESULT_TIMEOUT);
                finish();
            } else {
                FingerprintManager fpm = getSystemService(FingerprintManager.class);
                int enrolled = fpm.getEnrolledFingerprints().size();
                int max = getResources().getInteger(
                        com.android.internal.R.integer.config_fingerprintMaxTemplatesPerUser);
                if (enrolled >= max) {
                    finish();
                } else {
                    // We came back from enrolling but it wasn't completed, start again.
                    startLookingForFingerprint();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT_FIND_SENSOR;
    }

    protected Intent getEnrollingIntent() {
        Intent intent = new Intent();
        //intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, mToken);
        if (mUserId != UserHandle.USER_NULL) {
            intent.putExtra(Intent.EXTRA_USER_ID, mUserId);
        }
        intent.setAction(FingerprintEnrollEnrolling.ACTION);
        return intent;
    }
}
