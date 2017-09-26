package com.asus.cnsetupwizard.fingerprint;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.asus.cnsetupwizard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT;

/**
 * Launch this when you want the user to confirm their lock pattern.
 *
 * Sets an activity result of {@link Activity#RESULT_OK} when the user
 * successfully confirmed their pattern.
 */
public class ConfirmLockPattern extends ConfirmDeviceCredentialBaseActivity {

    public static class InternalActivity extends ConfirmLockPattern {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_layout);

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
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager
                .beginTransaction();
        fragmentTransaction.replace(R.id.fragment, new ConfirmLockPatternFragment());
        fragmentTransaction.commit();
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


    private enum Stage {
        NeedToUnlock,
        NeedToUnlockWrong,
        LockedOut
    }

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, ConfirmLockPatternFragment.class.getName());
        return modIntent;
    }


    public static class ConfirmLockPatternFragment extends ConfirmDeviceCredentialBaseFragment
            implements CredentialCheckResultTracker.Listener {

        // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;

        private static final String FRAGMENT_TAG_CHECK_LOCK_RESULT = "check_lock_result";

        private LockPatternView mLockPatternView;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private CredentialCheckResultTracker mCredentialCheckResultTracker;
        private boolean mDisappearing = false;
        private CountDownTimer mCountdownTimer;

        private TextView mHeaderTextView;
        private TextView mDetailsTextView;
        //private View mLeftSpacerLandscape;
        //private View mRightSpacerLandscape;

        // caller-supplied text for various prompts
        private CharSequence mHeaderText;
        private CharSequence mDetailsText;

        //private AppearAnimationUtils mAppearAnimationUtils;
        //private DisappearAnimationUtils mDisappearAnimationUtils;

        //++ Asus: for verizon "secure set-up feature" feature
        private boolean mFeedbackPwd = false;
        //++ Asus: for verizon "secure set-up feature" feature
        private Button next;
        private Button cancel;
        private Intent mIntent;

        // required constructor for fragments
        public ConfirmLockPatternFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.choose_lock_pattern, container, false);
            Resources resources = getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
            int height = resources.getDimensionPixelSize(resourceId);
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.area_title);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
            lp.setMargins(0, height, 0, 0);
            relativeLayout.setLayoutParams(lp);
            mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            mLockPatternView = (LockPatternView) view.findViewById(R.id.lockPattern);
            //mDetailsTextView = (TextView) view.findViewById(R.id.headerText);
            //mErrorTextView = (TextView) view.findViewById(R.id.headerText);
            //mLeftSpacerLandscape = view.findViewById(R.id.leftSpacer);
            //mRightSpacerLandscape = view.findViewById(R.id.rightSpacer);
            next = (Button) view.findViewById(R.id.footerRightButton);
            cancel = (Button) view.findViewById(R.id.footerLeftButton);
            cancel.setText(getResources().getString(R.string.wifi_cancel));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            next.setEnabled(false);
            next.setTextColor(getResources().getColor(R.color.button_disable_color));
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(mIntent);
                    getActivity().finish();
                }
            });

            // make it so unhandled touch events within the unlock screen go to the
            // lock pattern view.
            final LinearLayoutWithDefaultTouchRecepient topLayout
                    = (LinearLayoutWithDefaultTouchRecepient) view.findViewById(R.id.topLayout);
            topLayout.setDefaultTouchRecepient(mLockPatternView);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mHeaderText = intent.getCharSequenceExtra(
                        ConfirmDeviceCredentialBaseFragment.HEADER_TEXT);
                mDetailsText = intent.getCharSequenceExtra(
                        ConfirmDeviceCredentialBaseFragment.DETAILS_TEXT);
                mFeedbackPwd = intent.getBooleanExtra("needpwd",false);
            }

            mLockPatternView.setTactileFeedbackEnabled(
                    mLockPatternUtils.isTactileFeedbackEnabled());
            mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled(
                    mEffectiveUserId));
            mLockPatternView.setOnPatternListener(mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);

            if (savedInstanceState == null) {
                // on first launch, if no lock pattern is set, then finish with
                // success (don't want user to get stuck confirming something that
                // doesn't exist).
                if (!mLockPatternUtils.isLockPatternEnabled(mEffectiveUserId)) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }

            setAccessibilityTitle(mHeaderTextView.getText());

            mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_CHECK_LOCK_RESULT);
            if (mCredentialCheckResultTracker == null) {
                mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(mCredentialCheckResultTracker,
                        FRAGMENT_TAG_CHECK_LOCK_RESULT).commit();
            }

            ImageView backIcon = (ImageView) view.findViewById(R.id.button_back);
            backIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
            return view;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            // deliberately not calling super since we are managing this in full
        }

        @Override
        public void onPause() {
            super.onPause();

            if (mCountdownTimer != null) {
                mCountdownTimer.cancel();
            }
            mCredentialCheckResultTracker.setListener(null);
        }


        @Override
        public void onResume() {
            super.onResume();

            // if the user is currently locked out, enforce it.
            long deadline = mLockPatternUtils.getLockoutAttemptDeadline(mEffectiveUserId);
            if (deadline != 0) {
                mCredentialCheckResultTracker.clearResult();
                handleAttemptLockout(deadline);
            } else if (!mLockPatternView.isEnabled()) {
                // The deadline has passed, but the timer was cancelled. Or the pending lock
                // check was cancelled. Need to clean up.
                updateStage(Stage.NeedToUnlock);
            }
            mCredentialCheckResultTracker.setListener(this);
        }

        @Override
        protected void onShowError() {
        }



        private int getDefaultDetails() {
            return R.string.add_pattern;
        }

        private Object[][] getActiveViews() {
            ArrayList<ArrayList<Object>> result = new ArrayList<>();
            result.add(new ArrayList<Object>(Collections.singletonList(mHeaderTextView)));
            //result.add(new ArrayList<Object>(Collections.singletonList(mDetailsTextView)));
            //if (mCancelButton.getVisibility() == View.VISIBLE) {
            //    result.add(new ArrayList<Object>(Collections.singletonList(mCancelButton)));
            //}
            LockPatternView.CellState[][] cellStates = mLockPatternView.getCellStates();
            for (int i = 0; i < cellStates.length; i++) {
                ArrayList<Object> row = new ArrayList<>();
                for (int j = 0; j < cellStates[i].length; j++) {
                    row.add(cellStates[i][j]);
                }
                result.add(row);
            }
            //if (mFingerprintIcon.getVisibility() == View.VISIBLE) {
            //    result.add(new ArrayList<Object>(Collections.singletonList(mFingerprintIcon)));
            //}
            Object[][] resultArr = new Object[result.size()][cellStates[0].length];
            for (int i = 0; i < result.size(); i++) {
                ArrayList<Object> row = result.get(i);
                for (int j = 0; j < row.size(); j++) {
                    resultArr[i][j] = row.get(j);
                }
            }
            return resultArr;
        }

        @Override
        public void startEnterAnimation() {
            super.startEnterAnimation();
            //mLockPatternView.setAlpha(1f);

        }

        private void updateStage(Stage stage) {
            switch (stage) {
                case NeedToUnlock:
                    if (mHeaderText != null) {
                        mHeaderTextView.setText(mHeaderText);
                    } else {
                        mHeaderTextView.setText(R.string.add_pattern);
                    }
                    if (mDetailsText != null) {
                        //mDetailsTextView.setText(mDetailsText);
                    } else {
                       // mDetailsTextView.setText(getDefaultDetails());
                    }
                    //mHeaderTextView.setText("");
                    if (isProfileChallenge()) {
                        updateErrorMessage(mLockPatternUtils.getCurrentFailedPasswordAttempts(
                                mEffectiveUserId));
                    }

                    mLockPatternView.setEnabled(true);
                    mLockPatternView.enableInput();
                    mLockPatternView.clearPattern();
                    break;
                case NeedToUnlockWrong:
                    mHeaderTextView.setText(R.string.lockpattern_need_to_unlock_wrong);

                    mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    mLockPatternView.setEnabled(true);
                    mLockPatternView.enableInput();
                    break;
                case LockedOut:
                    mLockPatternView.clearPattern();
                    // enabled = false means: disable input, and have the
                    // appearance of being disabled.
                    mLockPatternView.setEnabled(false); // appearance of being disabled
                    break;
            }

            // Always announce the header for accessibility. This is a no-op
            // when accessibility is disabled.
            mHeaderTextView.announceForAccessibility(mHeaderTextView.getText());
        }

        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                mLockPatternView.clearPattern();
            }
        };

        // clear the wrong pattern unless they have started a new one
        // already
        private void postClearPatternRunnable() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            mLockPatternView.postDelayed(mClearPatternRunnable, WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }

        @Override
        protected void authenticationSucceeded() {
            mCredentialCheckResultTracker.setResult(true, new Intent(), 0, mEffectiveUserId);
        }

        private void startDisappearAnimation(final Intent intent) {
            if (mDisappearing) {
                return;
            }
            mDisappearing = true;
            //getActivity().setResult(RESULT_OK, intent);
            mIntent = intent;
            mIntent.setAction(FingerprintEnrollFindSensor.ACTION);
            //getActivity().finish();
            next.setEnabled(true);
            next.setTextColor(getResources().getColor(R.color.button_enable_color));
            mLockPatternView.setEnabled(false);
        }


        /**
         * The pattern listener that responds according to a user confirming
         * an existing lock pattern.
         */
        private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener
                = new LockPatternView.OnPatternListener()  {

            public void onPatternStart() {
                mHeaderTextView.setText(getResources().getString(R.string.add_pattern));
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCleared() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<Cell> pattern) {

            }

            public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                if (mPendingLockCheck != null || mDisappearing) {
                    return;
                }

                mLockPatternView.setEnabled(false);

                final boolean verifyChallenge = getActivity().getIntent().getBooleanExtra(
                        ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
                Intent intent = new Intent();
                //if (verifyChallenge) {
                //    if (isInternalActivity()) {
                //        Log.d("zwj","vetify");
                        startVerifyPattern(pattern, intent);
                        return;
                 //   }
               // } else {
               //     Log.d("zwj","check");
                    //startCheckPattern(pattern, intent);
                //    return;
               // }

                //mCredentialCheckResultTracker.setResult(false, intent, 0, mEffectiveUserId);
            }

            private boolean isInternalActivity() {
                return getActivity() instanceof ConfirmLockPattern.InternalActivity;
            }

            private void startVerifyPattern(final List<LockPatternView.Cell> pattern,
                                            final Intent intent) {
                final int localEffectiveUserId = mEffectiveUserId;
                final int localUserId = mUserId;
                long challenge = getActivity().getIntent().getLongExtra(
                        ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
                final LockPatternChecker.OnVerifyCallback onVerifyCallback =
                        new LockPatternChecker.OnVerifyCallback() {
                            @Override
                            public void onVerified(byte[] token, int timeoutMs) {
                                mPendingLockCheck = null;
                                boolean matched = false;
                                if (token != null) {
                                    matched = true;
                                    //if (mReturnCredentials) {
                                        intent.putExtra(
                                                ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN,
                                                token);
                                    //}
                                }
                                mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs,
                                        localEffectiveUserId);
                            }
                        };
                mPendingLockCheck = (localEffectiveUserId == localUserId)
                        ? LockPatternChecker.verifyPattern(
                        mLockPatternUtils, pattern, challenge, localUserId,
                        onVerifyCallback)
                        : LockPatternChecker.verifyTiedProfileChallenge(
                        mLockPatternUtils, LockPatternUtils.patternToString(pattern),
                        true, challenge, localUserId, onVerifyCallback);
            }

            private void startCheckPattern(final List<LockPatternView.Cell> pattern,
                                           final Intent intent) {
                if (pattern.size() < LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mCredentialCheckResultTracker.setResult(false, intent, 0, mEffectiveUserId);
                    return;
                }

                final int localEffectiveUserId = mEffectiveUserId;
                mPendingLockCheck = LockPatternChecker.checkPattern(
                        mLockPatternUtils,
                        pattern,
                        localEffectiveUserId,
                        new LockPatternChecker.OnCheckCallback() {
                            @Override
                            public void onChecked(boolean matched, int timeoutMs) {
                                mPendingLockCheck = null;
                                if (matched && isInternalActivity() && mReturnCredentials) {
                                    intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE,
                                            StorageManager.CRYPT_TYPE_PATTERN);
                                    intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD,
                                            LockPatternUtils.patternToString(pattern));
                                }
                                //++ Asus: for verizon "secure set-up feature" feature
                                if(matched){
                                    maybePutDataForSecureSetup(pattern, intent);
                                }
                                //-- Asus: for verizon "secure set-up feature" feature
                                mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs,
                                        localEffectiveUserId);
                            }
                        });
            }
        };

        private void maybePutDataForSecureSetup(List<LockPatternView.Cell> pattern, Intent intent){
            //++ Asus: for verizon "secure set-up feature" feature
            if(mFeedbackPwd){
                intent.putExtra("pwdtype",StorageManager.CRYPT_TYPE_PATTERN);
                intent.putExtra("pwd",mLockPatternUtils.patternToString(pattern));
            }
            //-- Asus: for verizon "secure set-up feature" feature
        }

        private void onPatternChecked(boolean matched, Intent intent, int timeoutMs,
                                      int effectiveUserId, boolean newResult) {
            mLockPatternView.setEnabled(true);
            if (matched) {
                if (newResult) {
                    reportSuccessfullAttempt();
                }
                startDisappearAnimation(intent);
                checkForPendingIntent();
            } else {
                if (timeoutMs > 0) {
                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline(
                            effectiveUserId, timeoutMs);
                    handleAttemptLockout(deadline);
                } else {
                    updateStage(Stage.NeedToUnlockWrong);
                    postClearPatternRunnable();
                }
                if (newResult) {
                    reportFailedAttempt();
                }
            }
        }

        @Override
        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs,
                                        int effectiveUserId, boolean newResult) {
            onPatternChecked(matched, intent, timeoutMs, effectiveUserId, newResult);
        }



        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            updateStage(Stage.LockedOut);
            long elapsedRealtime = SystemClock.elapsedRealtime();
            mCountdownTimer = new CountDownTimer(
                    elapsedRealtimeDeadline - elapsedRealtime,
                    LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

                @Override
                public void onTick(long millisUntilFinished) {
                    final int secondsCountdown = (int) (millisUntilFinished / 1000);
                    mHeaderTextView.setText(getString(
                            R.string.lockpattern_too_many_failed_confirmation_attempts,
                            secondsCountdown));
                }

                @Override
                public void onFinish() {
                    updateStage(Stage.NeedToUnlock);
                }
            }.start();
        }

    }
}