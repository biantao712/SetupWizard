package com.asus.cnsetupwizard.fingerprint;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.storage.StorageManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.widget.LockPatternChecker;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.TextViewInputDisabler;
import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.utils.Utils;

import static android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT;

public class ConfirmLockPassword extends ConfirmDeviceCredentialBaseActivity {

    // The index of the array is isStrongAuth << 2 + isProfile << 1 + isAlpha.
    private static final int[] DETAIL_TEXTS = new int[] {
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
            R.string.enter_password_hint,
    };

    public static class InternalActivity extends ConfirmLockPassword {
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
        fragmentTransaction.replace(R.id.fragment, new ConfirmLockPasswordFragment());
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

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, ConfirmLockPasswordFragment.class.getName());
        return modIntent;
    }


    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setContentView(R.layout.choose_layout);
        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null && fragment instanceof ConfirmLockPasswordFragment) {
            ((ConfirmLockPasswordFragment)fragment).onWindowFocusChanged(hasFocus);
        }
    }*/

    public static class ConfirmLockPasswordFragment extends ConfirmDeviceCredentialBaseFragment
            implements View.OnClickListener, CredentialCheckResultTracker.Listener {
        private static final long ERROR_MESSAGE_TIMEOUT = 3000;
        private static final String FRAGMENT_TAG_CHECK_LOCK_RESULT = "check_lock_result";
        private TextView mPasswordEntry;
        private TextViewInputDisabler mPasswordEntryInputDisabler;
        private AsyncTask<?, ?, ?> mPendingLockCheck;
        private CredentialCheckResultTracker mCredentialCheckResultTracker;
        private boolean mDisappearing = false;
        private TextView mHeaderTextView;
        //private TextView mDetailsTextView;
        private CountDownTimer mCountdownTimer;
        private boolean mIsAlpha;
        private InputMethodManager mImm;
        private boolean mUsingFingerprint = false;
        //private AppearAnimationUtils mAppearAnimationUtils;
        //private DisappearAnimationUtils mDisappearAnimationUtils;
        private boolean mBlockImm;

        //++ Asus: for verizon "secure set-up feature" feature
        private boolean mFeedbackPwd = false;
        //-- Asus: for verizon "secure set-up feature" feature
        private Button next;

        // required constructor for fragments
        public ConfirmLockPasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            final int storedQuality = mLockPatternUtils.getKeyguardStoredPasswordQuality(
                    mEffectiveUserId);
            View view = inflater.inflate(R.layout.choose_lock_password, container,false);

            Resources resources = getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
            int height = resources.getDimensionPixelSize(resourceId);
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.area_title);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
            lp.setMargins(0, height, 0, 0);
            relativeLayout.setLayoutParams(lp);

            next = (Button) view.findViewById(R.id.next_button);
            Button cancel = (Button) view.findViewById(R.id.cancel_button);
            next.setOnClickListener(this);
            cancel.setOnClickListener(this);
            next.setEnabled(false);
            next.setTextColor(getResources().getColor(R.color.button_disable_color));

            mPasswordEntry = (TextView) view.findViewById(R.id.password_entry);
            mPasswordEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(s.length() >= 1){
                        next.setEnabled(true);
                        next.setTextColor(getResources().getColor(R.color.button_enable_color));
                    }else {
                        next.setEnabled(false);
                        next.setTextColor(getResources().getColor(R.color.button_disable_color));
                    }
                }
            });
           // mPasswordEntry.setOnEditorActionListener(this);
            mPasswordEntryInputDisabler = new TextViewInputDisabler(mPasswordEntry);

            mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
           // mDetailsTextView = (TextView) view.findViewById(R.id.detailsText);
            mErrorTextView = (TextView) view.findViewById(R.id.headerText);
            mIsAlpha = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == storedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == storedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_COMPLEX == storedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_MANAGED == storedQuality;

            mImm = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                CharSequence headerMessage = intent.getCharSequenceExtra(
                        ConfirmDeviceCredentialBaseFragment.HEADER_TEXT);
                CharSequence detailsMessage = intent.getCharSequenceExtra(
                        ConfirmDeviceCredentialBaseFragment.DETAILS_TEXT);
                if (TextUtils.isEmpty(headerMessage)) {
                    headerMessage = getString(getDefaultHeader());
                }
                if (TextUtils.isEmpty(detailsMessage)) {
                    detailsMessage = getString(getDefaultDetails());
                }
                mHeaderTextView.setText(headerMessage);
               // mDetailsTextView.setText(detailsMessage);
                mFeedbackPwd = intent.getBooleanExtra("needpwd",false);
            }
            int currentType = mPasswordEntry.getInputType();
            mPasswordEntry.setInputType(mIsAlpha ? currentType
                    : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));
            //mAppearAnimationUtils = new AppearAnimationUtils(getContext(),
            //        220, 2f /* translationScale */, 1f /* delayScale*/,
            //        AnimationUtils.loadInterpolator(getContext(),
            //                android.R.interpolator.linear_out_slow_in));
            //mDisappearAnimationUtils = new DisappearAnimationUtils(getContext(),
            //        110, 1f /* translationScale */,
            //        0.5f /* delayScale */, AnimationUtils.loadInterpolator(
            //        getContext(), android.R.interpolator.fast_out_linear_in));
            setAccessibilityTitle(mHeaderTextView.getText());

            mCredentialCheckResultTracker = (CredentialCheckResultTracker) getFragmentManager()
                    .findFragmentByTag(FRAGMENT_TAG_CHECK_LOCK_RESULT);
            if (mCredentialCheckResultTracker == null) {
                mCredentialCheckResultTracker = new CredentialCheckResultTracker();
                getFragmentManager().beginTransaction().add(mCredentialCheckResultTracker,
                        FRAGMENT_TAG_CHECK_LOCK_RESULT).commit();
            }
            TextView headTitle = (TextView) view.findViewById(R.id.lock_title);
            ImageView backIcon = (ImageView) view.findViewById(R.id.button_back);
            TextView title = (TextView) view.findViewById(R.id.title);
            backIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });

            if(mIsAlpha){
                title.setText(getResources().getString(R.string.enter_password_title));
                headTitle.setText(getResources().getString(R.string.enter_password_title));
            }else {
                headTitle.setText(getResources().getString(R.string.enter_pin_title));
            }


            return view;
        }

        private int getDefaultHeader() {
            return mIsAlpha ? R.string.lockpassword_confirm_your_password_header
                    : R.string.lockpassword_confirm_your_pin_header;
        }

        private int getDefaultDetails() {
            boolean isProfile = Utils.isManagedProfile(
                    UserManager.get(getActivity()), mEffectiveUserId);
            // Map boolean flags to an index by isStrongAuth << 2 + isProfile << 1 + isAlpha.
            int index = ((mIsStrongAuthRequired ? 1 : 0) << 2) + ((isProfile ? 1 : 0) << 1)
                    + (mIsAlpha ? 1 : 0);
            return DETAIL_TEXTS[index];
        }

        private int getErrorMessage() {
            return R.string.lockpassword_invalid_password;
        }

        /*@Override
        protected int getLastTryErrorMessage() {
            return mIsAlpha ? R.string.lock_profile_wipe_warning_content_password
                    : R.string.lock_profile_wipe_warning_content_pin;
        }*/

        @Override
        public void prepareEnterAnimation() {
            super.prepareEnterAnimation();
            mHeaderTextView.setAlpha(0f);
            //mDetailsTextView.setAlpha(0f);
            //mCancelButton.setAlpha(0f);
            mPasswordEntry.setAlpha(0f);
            //mFingerprintIcon.setAlpha(0f);
            mBlockImm = true;
        }

        /*private View[] getActiveViews() {
            ArrayList<View> result = new ArrayList<>();
            result.add(mHeaderTextView);
            result.add(mDetailsTextView);
            if (mCancelButton.getVisibility() == View.VISIBLE) {
                result.add(mCancelButton);
            }
            result.add(mPasswordEntry);
            if (mFingerprintIcon.getVisibility() == View.VISIBLE) {
                result.add(mFingerprintIcon);
            }
            return result.toArray(new View[] {});
        }*/

        /*@Override
        public void startEnterAnimation() {
            super.startEnterAnimation();
            mAppearAnimationUtils.startAnimation(getActiveViews(), new Runnable() {
                @Override
                public void run() {
                    mBlockImm = false;
                    resetState();
                }
            });
        }*/

        @Override
        public void onPause() {
            super.onPause();
            if (mCountdownTimer != null) {
                mCountdownTimer.cancel();
                mCountdownTimer = null;
            }
            mCredentialCheckResultTracker.setListener(null);
        }

        /*@Override
        protected int getMetricsCategory() {
            return MetricsEvent.CONFIRM_LOCK_PASSWORD;
        }*/

        @Override
        public void onResume() {
            super.onResume();
            long deadline = mLockPatternUtils.getLockoutAttemptDeadline(mEffectiveUserId);
            if (deadline != 0) {
                mCredentialCheckResultTracker.clearResult();
                //handleAttemptLockout(deadline);
            } else {
                resetState();
                mErrorTextView.setText("");
                if (isProfileChallenge()) {
                    updateErrorMessage(mLockPatternUtils.getCurrentFailedPasswordAttempts(
                            mEffectiveUserId));
                }
            }
            mCredentialCheckResultTracker.setListener(this);
        }

        @Override
        protected void authenticationSucceeded() {
            mCredentialCheckResultTracker.setResult(true, new Intent(), 0, mEffectiveUserId);
        }

        @Override
        public void onFingerprintIconVisibilityChanged(boolean visible) {
            mUsingFingerprint = visible;
        }

        private void resetState() {
            if (mBlockImm) return;
            mPasswordEntry.setEnabled(true);
            mPasswordEntryInputDisabler.setInputEnabled(true);
            if (shouldAutoShowSoftKeyboard()) {
                mImm.showSoftInput(mPasswordEntry, InputMethodManager.SHOW_IMPLICIT);
            }
        }

        private boolean shouldAutoShowSoftKeyboard() {
            return mPasswordEntry.isEnabled() && !mUsingFingerprint;
        }

        public void onWindowFocusChanged(boolean hasFocus) {
            if (!hasFocus || mBlockImm) {
                return;
            }
            // Post to let window focus logic to finish to allow soft input show/hide properly.
            mPasswordEntry.post(new Runnable() {
                @Override
                public void run() {
                    if (shouldAutoShowSoftKeyboard()) {
                        resetState();
                        return;
                    }

                    mImm.hideSoftInputFromWindow(mPasswordEntry.getWindowToken(),
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            });
        }

        private void handleNext() {
            if (mPendingLockCheck != null || mDisappearing) {
                return;
            }
            mPasswordEntryInputDisabler.setInputEnabled(false);

            final String pin = mPasswordEntry.getText().toString();
            final boolean verifyChallenge = getActivity().getIntent().getBooleanExtra(
                    ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
            Intent intent = new Intent();
            //if (verifyChallenge)  {
             //   Log.d("zwj","handleNext3");
                //if (isInternalActivity()) {
                //    Log.d("zwj","handleNext4");
                    startVerifyPassword(pin, intent);
                    return;
               // }
            //} else {
            //    Log.d("zwj","handleNext5");
           //     startCheckPassword(pin, intent);
           //     return;
           // }

            //mCredentialCheckResultTracker.setResult(false, intent, 0, mEffectiveUserId);
        }

        private boolean isInternalActivity() {
            return getActivity() instanceof ConfirmLockPassword.InternalActivity;
        }

        private void startVerifyPassword(final String pin, final Intent intent) {
            long challenge = getActivity().getIntent().getLongExtra(
                    ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
            final int localEffectiveUserId = mEffectiveUserId;
            final int localUserId = mUserId;
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
                               // }
                            }
                            mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs,
                                    localUserId);
                        }
                    };
            mPendingLockCheck = (localEffectiveUserId == localUserId)
                    ? LockPatternChecker.verifyPassword(
                    mLockPatternUtils, pin, challenge, localUserId, onVerifyCallback)
                    : LockPatternChecker.verifyTiedProfileChallenge(
                    mLockPatternUtils, pin, false, challenge, localUserId,
                    onVerifyCallback);
        }

        private void startCheckPassword(final String pin, final Intent intent) {
            final int localEffectiveUserId = mEffectiveUserId;
            mPendingLockCheck = LockPatternChecker.checkPassword(
                    mLockPatternUtils,
                    pin,
                    localEffectiveUserId,
                    new LockPatternChecker.OnCheckCallback() {
                        @Override
                        public void onChecked(boolean matched, int timeoutMs) {
                            mPendingLockCheck = null;
                            if (matched && isInternalActivity() && mReturnCredentials) {
                                intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_TYPE,
                                        mIsAlpha ? StorageManager.CRYPT_TYPE_PASSWORD
                                                : StorageManager.CRYPT_TYPE_PIN);
                                intent.putExtra(
                                        ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, pin);
                            }
                            //++ Asus: for verizon "secure set-up feature" feature
                            if(matched){
                                maybePutDataForSecureSetup(pin, intent);
                            }
                            //-- Asus: for verizon "secure set-up feature" feature
                            mCredentialCheckResultTracker.setResult(matched, intent, timeoutMs,
                                    localEffectiveUserId);
                        }
                    });
        }

        private void maybePutDataForSecureSetup(String pin, Intent intent){
            //++ Asus: for verizon "secure set-up feature" feature
            if(mFeedbackPwd){
                int computedQuality = LockPatternUtils.computePasswordQuality(pin);
                boolean numeric = computedQuality
                        == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
                boolean numericComplex = computedQuality
                        == DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX;
                int type = numeric || numericComplex ? StorageManager.CRYPT_TYPE_PIN
                        : StorageManager.CRYPT_TYPE_PASSWORD;
                intent.putExtra("pwdtype",type);
                intent.putExtra("pwd",pin);
            }
            //-- Asus: for verizon "secure set-up feature" feature
        }

        private void startDisappearAnimation(final Intent intent) {
            if (mDisappearing) {
                return;
            }
            mDisappearing = true;

            /*if (getActivity().getThemeResId() == R.style.Theme_ConfirmDeviceCredentialsDark) {
                mDisappearAnimationUtils.startAnimation(getActiveViews(), new Runnable() {
                    @Override
                    public void run() {
                        // Bail if there is no active activity.
                        if (getActivity() == null || getActivity().isFinishing()) {
                            return;
                        }

                        getActivity().setResult(RESULT_OK, intent);
                        getActivity().finish();
                        getActivity().overridePendingTransition(
                                R.anim.confirm_credential_close_enter,
                                R.anim.confirm_credential_close_exit);
                    }
                });
            } else {

            }*/
            intent.setAction(FingerprintEnrollFindSensor.ACTION);
            startActivity(intent);
            //getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }

        private void onPasswordChecked(boolean matched, Intent intent, int timeoutMs,
                                       int effectiveUserId, boolean newResult) {
            mPasswordEntryInputDisabler.setInputEnabled(true);
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
                    showError(getErrorMessage(), ERROR_MESSAGE_TIMEOUT);
                }
                if (newResult) {
                    reportFailedAttempt();
                }
            }
        }

        @Override
        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs,
                                        int effectiveUserId, boolean newResult) {
            onPasswordChecked(matched, intent, timeoutMs, effectiveUserId, newResult);
        }

        @Override
        protected void onShowError() {
            mPasswordEntry.setText(null);
        }

        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            mPasswordEntry.setEnabled(false);
            mCountdownTimer = new CountDownTimer(
                    elapsedRealtimeDeadline - elapsedRealtime,
                    LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

                @Override
                public void onTick(long millisUntilFinished) {
                    final int secondsCountdown = (int) (millisUntilFinished / 1000);
                    showError(getString(
                            R.string.lockpattern_too_many_failed_confirmation_attempts,
                            secondsCountdown), 0);
                }

                @Override
                public void onFinish() {
                    resetState();
                    mErrorTextView.setText("");
                    if (isProfileChallenge()) {
                        updateErrorMessage(mLockPatternUtils.getCurrentFailedPasswordAttempts(
                                mEffectiveUserId));
                    }
                }
            }.start();
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.next_button:
                    handleNext();
                    break;

                case R.id.cancel_button:
                    //getActivity().setResult(RESULT_CANCELED);
                    getActivity().finish();
                    break;
            }
        }
    }
}
