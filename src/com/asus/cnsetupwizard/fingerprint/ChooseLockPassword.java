package com.asus.cnsetupwizard.fingerprint;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternUtils.RequestThrottledException;
import com.android.internal.widget.PasswordEntryKeyboardHelper;
import com.android.internal.widget.PasswordEntryKeyboardView;
import com.android.internal.widget.TextViewInputDisabler;
import com.asus.cnsetupwizard.BaseActivity;
import com.asus.cnsetupwizard.R;
import com.asus.cnsetupwizard.utils.Utils;

import static android.preference.PreferenceActivity.EXTRA_SHOW_FRAGMENT;
import static com.asus.cnsetupwizard.fingerprint.ChooseLockPattern.EXTRA_HIDE_DRAWER;

public class ChooseLockPassword extends BaseActivity {
    public static final String ACTION = "com.asus.cnsetupwizard.CHOOSELOCKPASSWORD";

    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";
    public static final String PASSWORD_MIN_LETTERS_KEY = "lockscreen.password_min_letters";
    public static final String PASSWORD_MIN_LOWERCASE_KEY = "lockscreen.password_min_lowercase";
    public static final String PASSWORD_MIN_UPPERCASE_KEY = "lockscreen.password_min_uppercase";
    public static final String PASSWORD_MIN_NUMERIC_KEY = "lockscreen.password_min_numeric";
    public static final String PASSWORD_MIN_SYMBOLS_KEY = "lockscreen.password_min_symbols";
    public static final String PASSWORD_MIN_NONLETTER_KEY = "lockscreen.password_min_nonletter";

    private static final String TAG = "ChooseLockPassword";

    public static final String CONFIRM_CREDENTIALS = "confirm_credentials";
    protected static final String EXTRA_PASSWORD_QUALITY = "extra_password_quality";
    protected static final String EXTRA_UNLOCK_METHOD_INTENT = "extra_unlock_method_intent";
    public static final String EXTRA_REQUIRE_PASSWORD = "extra_require_password";

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, getFragmentClass().getName());
        return modIntent;
    }

    public static Intent createIntent(Context context, int quality,
                                      int minLength, final int maxLength, boolean requirePasswordToDecrypt,
                                      boolean confirmCredentials) {
        Intent intent = new Intent().setClass(context, ChooseLockPassword.class);
        intent.putExtra(LockPatternUtils.PASSWORD_TYPE_KEY, quality);
        intent.putExtra(PASSWORD_MIN_KEY, minLength);
        intent.putExtra(PASSWORD_MAX_KEY, maxLength);
        intent.putExtra(CONFIRM_CREDENTIALS, confirmCredentials);
        intent.putExtra("extra_require_password", requirePasswordToDecrypt);
        return intent;
    }

    public static Intent createIntent(Context context, int quality,
                                      int minLength, final int maxLength, boolean requirePasswordToDecrypt,
                                      boolean confirmCredentials, int userId) {
        Intent intent = createIntent(context, quality, minLength, maxLength,
                requirePasswordToDecrypt, confirmCredentials);
        intent.putExtra(Intent.EXTRA_USER_ID, userId);
        return intent;
    }

    public static Intent createIntent(Context context, int quality,
                                      int minLength, final int maxLength, boolean requirePasswordToDecrypt, String password) {
        Intent intent = createIntent(context, quality, minLength, maxLength,
                requirePasswordToDecrypt, false);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD, password);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength,
                                      int maxLength, boolean requirePasswordToDecrypt, String password, int userId) {
        Intent intent = createIntent(context, quality, minLength, maxLength,
                requirePasswordToDecrypt, password);
        intent.putExtra(Intent.EXTRA_USER_ID, userId);
        return intent;
    }

    public static Intent createIntent(Context context, int quality,
                                      int minLength, final int maxLength, boolean requirePasswordToDecrypt, long challenge) {
        Intent intent = createIntent(context, quality, minLength, maxLength,
                requirePasswordToDecrypt, false);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        return intent;
    }

    public static Intent createIntent(Context context, int quality, int minLength,
                                      int maxLength, boolean requirePasswordToDecrypt, long challenge, int userId) {
        Intent intent = createIntent(context, quality, minLength, maxLength,
                requirePasswordToDecrypt, challenge);
        intent.putExtra(Intent.EXTRA_USER_ID, userId);
        return intent;
    }


    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockPasswordFragment.class.getName().equals(fragmentName)) return true;
        return false;
    }

    /* package */ Class<? extends Fragment> getFragmentClass() {
        return ChooseLockPasswordFragment.class;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Fix on phones
        // Disable IME on our window since we provide our own keyboard
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
        //WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
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
        fragmentTransaction.replace(R.id.fragment, new ChooseLockPasswordFragment());
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

    public static class ChooseLockPasswordFragment extends PreferenceFragment
            implements OnClickListener, OnEditorActionListener,  TextWatcher,
            SaveAndFinishWorker.Listener {
        private static final String KEY_FIRST_PIN = "first_pin";
        private static final String KEY_UI_STAGE = "ui_stage";
        private static final String KEY_CURRENT_PASSWORD = "current_password";
        private static final String FRAGMENT_TAG_SAVE_AND_FINISH = "save_and_finish_worker";

        private String mCurrentPassword;
        private String mChosenPassword;
        private boolean mHasChallenge;
        private long mChallenge;
        private TextView mPasswordEntry;
        private TextViewInputDisabler mPasswordEntryInputDisabler;
        private int mPasswordMinLength = LockPatternUtils.MIN_LOCK_PASSWORD_SIZE;
        private int mPasswordMaxLength = 16;
        private int mPasswordMinLetters = 0;
        private int mPasswordMinUpperCase = 0;
        private int mPasswordMinLowerCase = 0;
        private int mPasswordMinSymbols = 0;
        private int mPasswordMinNumeric = 0;
        private int mPasswordMinNonLetter = 0;
        private LockPatternUtils mLockPatternUtils;
        private SaveAndFinishWorker mSaveAndFinishWorker;
        private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private Stage mUiStage = Stage.Introduction;

        private TextView mHeaderText;
        private String mFirstPin;
        private KeyboardView mKeyboardView;
        private PasswordEntryKeyboardHelper mKeyboardHelper;
        private boolean mIsAlphaMode;
        private Button mCancelButton;
        private Button mNextButton;
        private static final int CONFIRM_EXISTING_REQUEST = 58;
        static final int RESULT_FINISHED = RESULT_FIRST_USER;
        private static final long ERROR_MESSAGE_TIMEOUT = 3000;
        private static final int MSG_SHOW_ERROR = 1;

        private int mUserId;
        private boolean mHideDrawer = false;

        // +++ AMAX AT&T issue
        private boolean mSimplePasswordEnabled = true;

        private TextView headTitle;
        // ---

        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_ERROR) {
                    updateStage((Stage) msg.obj);
                }
            }
        };

        /**
         * Keep track internally of where the user is in choosing a pattern.
         */
        protected enum Stage {

            Introduction(R.string.to_next,
                    R.string.to_next,
                    R.string.to_next),

            NeedToConfirm(R.string.lockpassword_confirm_your_password_header,
                    R.string.lockpassword_confirm_your_pin_header,
                    R.string.lockpassword_ok_label),

            ConfirmWrong(R.string.lockpassword_confirm_passwords_dont_match,
                    R.string.lockpassword_confirm_passwords_dont_match,
                    R.string.lockpassword_continue_label);

            Stage(int hintInAlpha, int hintInNumeric, int nextButtonText) {
                this.alphaHint = hintInAlpha;
                this.numericHint = hintInNumeric;
                this.buttonText = nextButtonText;
            }

            public final int alphaHint;
            public final int numericHint;
            public final int buttonText;
        }

        // required constructor for fragments
        public ChooseLockPasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPatternUtils = new LockPatternUtils(getActivity());
            Intent intent = getActivity().getIntent();
            if (!(getActivity() instanceof ChooseLockPassword)) {
                throw new SecurityException("Fragment contained in wrong activity");
            }

            // +++ AMAX AT&T issue
            //mSimplePasswordEnabled = mLockPatternUtils.getSimplePasswordEnabled();
            // ---

            // Only take this argument into account if it belongs to the current profile.
            mUserId = Utils.getUserIdFromBundle(getActivity(), intent.getExtras());
            mRequestedQuality = Math.max(intent.getIntExtra(LockPatternUtils.PASSWORD_TYPE_KEY,
                    mRequestedQuality), mLockPatternUtils.getRequestedPasswordQuality(
                    mUserId));
            mPasswordMinLength = Math.max(Math.max(
                    LockPatternUtils.MIN_LOCK_PASSWORD_SIZE,
                    intent.getIntExtra(PASSWORD_MIN_KEY, mPasswordMinLength)),
                    mLockPatternUtils.getRequestedMinimumPasswordLength(mUserId));
            mPasswordMaxLength = intent.getIntExtra(PASSWORD_MAX_KEY, mPasswordMaxLength);
            mPasswordMinLetters = Math.max(intent.getIntExtra(PASSWORD_MIN_LETTERS_KEY,
                    mPasswordMinLetters), mLockPatternUtils.getRequestedPasswordMinimumLetters(
                    mUserId));
            mPasswordMinUpperCase = Math.max(intent.getIntExtra(PASSWORD_MIN_UPPERCASE_KEY,
                    mPasswordMinUpperCase), mLockPatternUtils.getRequestedPasswordMinimumUpperCase(
                    mUserId));
            mPasswordMinLowerCase = Math.max(intent.getIntExtra(PASSWORD_MIN_LOWERCASE_KEY,
                    mPasswordMinLowerCase), mLockPatternUtils.getRequestedPasswordMinimumLowerCase(
                    mUserId));
            mPasswordMinNumeric = Math.max(intent.getIntExtra(PASSWORD_MIN_NUMERIC_KEY,
                    mPasswordMinNumeric), mLockPatternUtils.getRequestedPasswordMinimumNumeric(
                    mUserId));
            mPasswordMinSymbols = Math.max(intent.getIntExtra(PASSWORD_MIN_SYMBOLS_KEY,
                    mPasswordMinSymbols), mLockPatternUtils.getRequestedPasswordMinimumSymbols(
                    mUserId));
            mPasswordMinNonLetter = Math.max(intent.getIntExtra(PASSWORD_MIN_NONLETTER_KEY,
                    mPasswordMinNonLetter), mLockPatternUtils.getRequestedPasswordMinimumNonLetter(
                    mUserId));

            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
            mHideDrawer = getActivity().getIntent().getBooleanExtra(EXTRA_HIDE_DRAWER, false);

            if (intent.getBooleanExtra(
                    ChooseLockSettingsHelper.EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT, false)) {
                SaveAndFinishWorker w = new SaveAndFinishWorker();
                final boolean required = getActivity().getIntent().getBooleanExtra(
                        EXTRA_REQUIRE_PASSWORD, false);
                String current = intent.getStringExtra(
                        ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                w.setBlocking(true);
                w.setListener(this);
                w.start(mChooseLockSettingsHelper.utils(), required,
                        false, 0, current, current, mRequestedQuality, mUserId);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.choose_lock_password, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Resources resources = getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
            int height = resources.getDimensionPixelSize(resourceId);
            RelativeLayout relativeLayout = (RelativeLayout) view.findViewById(R.id.area_title);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.lock_button_height));
            lp.setMargins(0, height, 0, 0);
            relativeLayout.setLayoutParams(lp);

            mCancelButton = (Button) view.findViewById(R.id.cancel_button);
            mCancelButton.setOnClickListener(this);
            mNextButton = (Button) view.findViewById(R.id.next_button);
            mNextButton.setOnClickListener(this);

            mIsAlphaMode = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == mRequestedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == mRequestedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_COMPLEX == mRequestedQuality;
            mKeyboardView = (PasswordEntryKeyboardView) view.findViewById(R.id.keyboard);
            mPasswordEntry = (TextView) view.findViewById(R.id.password_entry);
            mPasswordEntry.setOnEditorActionListener(this);
            mPasswordEntry.addTextChangedListener(this);
            mPasswordEntryInputDisabler = new TextViewInputDisabler(mPasswordEntry);

            final Activity activity = getActivity();
            mKeyboardHelper = new PasswordEntryKeyboardHelper(activity,
                    mKeyboardView, mPasswordEntry);
            mKeyboardHelper.setKeyboardMode(mIsAlphaMode ?
                    PasswordEntryKeyboardHelper.KEYBOARD_MODE_ALPHA
                    : PasswordEntryKeyboardHelper.KEYBOARD_MODE_NUMERIC);

            mHeaderText = (TextView) view.findViewById(R.id.headerText);
            mKeyboardView.requestFocus();

            int currentType = mPasswordEntry.getInputType();
            mPasswordEntry.setInputType(mIsAlphaMode ? currentType
                    : (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD));

            Intent intent = getActivity().getIntent();
            final boolean confirmCredentials = intent.getBooleanExtra(
                    CONFIRM_CREDENTIALS, true);
            mCurrentPassword = intent.getStringExtra(ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
            mHasChallenge = intent.getBooleanExtra(
                    ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, false);
            mChallenge = intent.getLongExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, 0);
            if (savedInstanceState == null) {
                updateStage(Stage.Introduction);
                if (confirmCredentials) {
                    mChooseLockSettingsHelper.launchConfirmationActivity(CONFIRM_EXISTING_REQUEST,
                            getString(R.string.finish_button_label), true,
                            mUserId);
                }
            } else {
                // restore from previous state
                mFirstPin = savedInstanceState.getString(KEY_FIRST_PIN);
                final String state = savedInstanceState.getString(KEY_UI_STAGE);
                if (state != null) {
                    mUiStage = Stage.valueOf(state);
                    updateStage(mUiStage);
                }

                if (mCurrentPassword == null) {
                    mCurrentPassword = savedInstanceState.getString(KEY_CURRENT_PASSWORD);
                }

                // Re-attach to the exiting worker if there is one.
                mSaveAndFinishWorker = (SaveAndFinishWorker) getFragmentManager().findFragmentByTag(
                        FRAGMENT_TAG_SAVE_AND_FINISH);
            }

            TextView title = (TextView) view.findViewById(R.id.title);
            ImageView backIcon = (ImageView) view.findViewById(R.id.button_back);
            headTitle = (TextView) view.findViewById(R.id.lock_title);


            if(mIsAlphaMode){
                title.setText(getResources().getString(R.string.enter_password_title));
            }
            backIcon.setOnClickListener(this);
        }


        protected int getMetricsCategory() {
            return MetricsEvent.CHOOSE_LOCK_PASSWORD;
        }

        @Override
        public void onResume() {
            super.onResume();
            updateStage(mUiStage);
            if (mSaveAndFinishWorker != null) {
                mSaveAndFinishWorker.setListener(this);
            } else {
                mKeyboardView.requestFocus();
            }
        }

        @Override
        public void onPause() {
            mHandler.removeMessages(MSG_SHOW_ERROR);
            if (mSaveAndFinishWorker != null) {
                mSaveAndFinishWorker.setListener(null);
            }

            super.onPause();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString(KEY_UI_STAGE, mUiStage.name());
            outState.putString(KEY_FIRST_PIN, mFirstPin);
            outState.putString(KEY_CURRENT_PASSWORD, mCurrentPassword);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
                case CONFIRM_EXISTING_REQUEST:
                    if (resultCode != Activity.RESULT_OK) {
                        getActivity().setResult(RESULT_FINISHED);
                        getActivity().finish();
                    } else {
                        mCurrentPassword = data.getStringExtra(
                                ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD);
                    }
                    break;
            }
        }

        /*protected Intent getRedactionInterstitialIntent(Context context) {
            return RedactionInterstitial.createStartIntent(context, mUserId);
        }*/

        protected void updateStage(Stage stage) {
            final Stage previousStage = mUiStage;
            mUiStage = stage;
            updateUi();

            // If the stage changed, announce the header for accessibility. This
            // is a no-op when accessibility is disabled.
            if (previousStage != stage) {
                mHeaderText.announceForAccessibility(mHeaderText.getText());
            }
        }

        /**
         * Validates PIN and returns a message to display if PIN fails test.
         * @param password the raw password the user typed in
         * @return error message to show to user or null if password is OK
         */
        private String validatePassword(String password) {
            if (password.length() < mPasswordMinLength) {
                return getString(mIsAlphaMode ?
                        R.string.lockpassword_password_too_short
                        : R.string.lockpassword_pin_too_short);
            }
            if (password.length() > mPasswordMaxLength) {
                return getString(mIsAlphaMode ?
                        R.string.lockpassword_password_too_long
                        : R.string.lockpassword_password_too_long, mPasswordMaxLength + 1);
            }
            int letters = 0;
            int numbers = 0;
            int lowercase = 0;
            int symbols = 0;
            int uppercase = 0;
            int nonletter = 0;
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                // allow non control Latin-1 characters only
                if (c < 32 || c > 127) {
                    return getString(R.string.lockpassword_illegal_character);
                }
                if (c >= '0' && c <= '9') {
                    numbers++;
                    nonletter++;
                } else if (c >= 'A' && c <= 'Z') {
                    letters++;
                    uppercase++;
                } else if (c >= 'a' && c <= 'z') {
                    letters++;
                    lowercase++;
                } else {
                    symbols++;
                    nonletter++;
                }
            }
            if (DevicePolicyManager.PASSWORD_QUALITY_NUMERIC == mRequestedQuality
                    || DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX == mRequestedQuality) {
                if (letters > 0 || symbols > 0) {
                    // This shouldn't be possible unless user finds some way to bring up
                    // soft keyboard
                    return getString(R.string.lockpassword_pin_contains_non_digits);
                }
                // Check for repeated characters or sequences (e.g. '1234', '0000', '2468')
                final int sequence = LockPatternUtils.maxLengthSequence(password);
                if (DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX == mRequestedQuality
                        && sequence > LockPatternUtils.MAX_ALLOWED_SEQUENCE) {
                    return getString(R.string.lockpassword_pin_no_sequential_digits);
                }
                // AMAX AT&T issue, Complex PIN/Password requirement, pin/password should not be repeated and sequential
                /*if (isSimplePassword(password)) {
                    return getString(R.string.lockpassword_pin_no_sequential_digits);
                }*/
                // AMAX AT&T issue, Complex PIN/Password requirement, pin/password should not be repeated and sequential
            } else if (DevicePolicyManager.PASSWORD_QUALITY_COMPLEX == mRequestedQuality) {
                // TODO mPasswordMinNonLetter is mapped to MinPasswordComplexCharacter
                // This original code for non-letter is ambiguous: it sometimes represents
                // how many non-letter characters but it sometimes how many complex character
                // groups. So this must be corrected; otherwise, it is hard to maintain.
                // ---------------------------------------------------------------------------------
                // Valid values for MinDevicePasswordComplexCharacters are 1 to 4.
                // The value specifies the number of character groups that are required to be present
                // in the password. The character groups are defined as:
                //      Lower case alphabetical characters
                //      Upper case alphabetical characters
                //      Numbers
                //      Non-alphanumeric characters
                if (!isComplexCharacterSetsSufficient(lowercase, uppercase, numbers, symbols,
                        mPasswordMinNonLetter)) {
                    if (letters < mPasswordMinLetters) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_letters, mPasswordMinLetters),
                                mPasswordMinLetters);
                    } else if (numbers < mPasswordMinNumeric) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_numeric, mPasswordMinNumeric),
                                mPasswordMinNumeric);
                    } else if (lowercase < mPasswordMinLowerCase) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_lowercase, mPasswordMinLowerCase),
                                mPasswordMinLowerCase);
                    } else if (uppercase < mPasswordMinUpperCase) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_uppercase, mPasswordMinUpperCase),
                                mPasswordMinUpperCase);
                    } else if (symbols < mPasswordMinSymbols) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_symbols, mPasswordMinSymbols),
                                mPasswordMinSymbols);
                    } else if (nonletter < mPasswordMinNonLetter) {
                        return String.format(getResources().getQuantityString(
                                R.plurals.lockpassword_password_requires_nonletter, mPasswordMinNonLetter),
                                mPasswordMinNonLetter);
                    }
                }
            } else {
                if (Utils.isATT() || Utils.isVerizonSKU()) {
                    if (DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC == mRequestedQuality) {
                        if (letters < 1) {
                            return String.format(getResources().getQuantityString(
                                    R.plurals.lockpassword_password_requires_letters, 1), 1);
                        }
                    } else if (DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC == mRequestedQuality) {
                        if (!isAlphanumericSufficient(letters, numbers)) {
                            if (letters < 1) {
                                return String.format(getResources().getQuantityString(
                                        R.plurals.lockpassword_password_requires_letters, 1), 1);
                            } else if (numbers < 1) {
                                return String.format(getResources().getQuantityString(
                                        R.plurals.lockpassword_password_requires_numeric, 1), 1);
                            }
                        }
                        // For alphanumeric, non-letter means complex character groups
                        /*if (!isComplexCharacterSetsSufficient(lowercase, uppercase, numbers, symbols,
                                mPasswordMinNonLetter)) {
                            return String.format(getString(R.string.insufficient_character_sets),
                                    mPasswordMinNonLetter);
                        }*/
                        /*if (isSimplePassword(password)) {
                            // AMAX AT&T issue, Complex PIN/Password requirement, pin/password should not be repeated and sequential
                            return getString(R.string.lockpassword_pin_no_sequential_digits);
                        }*/
                    }
                } else {
                    final boolean alphabetic = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC
                            == mRequestedQuality;
                    final boolean alphanumeric = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC
                            == mRequestedQuality;
                    if ((alphabetic || alphanumeric) && letters == 0) {
                        return getString(R.string.lockpassword_password_requires_alpha);
                    }
                    if (alphanumeric && numbers == 0) {
                        return getString(R.string.lockpassword_password_requires_digit);
                    }
                }
            }
            if(mLockPatternUtils.checkPasswordHistory(password, mUserId)) {
                return getString(mIsAlphaMode ? R.string.lockpassword_password_recently_used
                        : R.string.lockpassword_pin_recently_used);
            }

            return null;
        }

        private boolean isComplexCharacterSetsSufficient(int lowercase, int uppercase,
                                                         int numbers, int symbols, int limit) {
            return getComplexCharacterSets(lowercase, uppercase, numbers, symbols) >= limit;
        }

        private int getComplexCharacterSets(int lowercase, int uppercase, int numbers, int symbols) {
            int complexCharacterGroups = 0;
            if (lowercase != 0) {
                complexCharacterGroups++;
            }
            if (uppercase != 0) {
                complexCharacterGroups++;
            }
            if (numbers != 0) {
                complexCharacterGroups++;
            }
            if (symbols != 0) {
                complexCharacterGroups++;
            }
            return complexCharacterGroups;
        }

        private boolean isAlphanumericSufficient(int letters, int numbers) {
            return letters != 0 && numbers != 0;
        }

        public void handleNext() {
            if (mSaveAndFinishWorker != null) return;
            mChosenPassword = mPasswordEntry.getText().toString();
            if (TextUtils.isEmpty(mChosenPassword)) {
                return;
            }
            String errorMsg = null;
            if (mUiStage == Stage.Introduction) {
                errorMsg = validatePassword(mChosenPassword);
                if (errorMsg == null) {
                    mFirstPin = mChosenPassword;
                    mPasswordEntry.setText("");
                    updateStage(Stage.NeedToConfirm);
                    headTitle.setText(getResources().getString(R.string.confirm_password));
                }
            } else if (mUiStage == Stage.NeedToConfirm) {
                if (mFirstPin.equals(mChosenPassword)) {
                    startSaveAndFinish();
                } else {
                    CharSequence tmp = mPasswordEntry.getText();
                    if (tmp != null) {
                        Selection.setSelection((Spannable) tmp, 0, tmp.length());
                    }
                    updateStage(Stage.ConfirmWrong);
                }
            }
            if (errorMsg != null) {
                showError(errorMsg, mUiStage);
            }
        }

        protected void setNextEnabled(boolean enabled) {
            mNextButton.setEnabled(enabled);
            if(enabled){
                mNextButton.setTextColor(getResources().getColor(R.color.button_enable_color));
            }else {
                mNextButton.setTextColor(getResources().getColor(R.color.button_disable_color));
            }
        }

        protected void setNextText(int text) {
            mNextButton.setText(text);
        }

        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction(SelectScreenLockActivity.ACTION);
            switch (v.getId()) {
                case R.id.next_button:
                    handleNext();
                    break;
                case R.id.cancel_button:
                    startActivity(intent);
                    getActivity().finish();
                    break;
                case R.id.button_back:
                    startActivity(intent);
                    getActivity().finish();
                    break;
            }
        }

        private void showError(String msg, final Stage next) {
            mHeaderText.setText(msg);
            mHeaderText.announceForAccessibility(mHeaderText.getText());
            Message mesg = mHandler.obtainMessage(MSG_SHOW_ERROR, next);
            mHandler.removeMessages(MSG_SHOW_ERROR);
            mHandler.sendMessageDelayed(mesg, ERROR_MESSAGE_TIMEOUT);
        }

        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            // Check if this was the result of hitting the enter or "done" key
            if (actionId == EditorInfo.IME_NULL
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT) {
                handleNext();
                return true;
            }
            return false;
        }

        /**
         * Update the hint based on current Stage and length of password entry
         */
        private void updateUi() {
            final boolean canInput = mSaveAndFinishWorker == null;
            String password = mPasswordEntry.getText().toString();
            final int length = password.length();
            if (mUiStage == Stage.Introduction) {
                if (length < mPasswordMinLength) {
                    String msg = getString(mIsAlphaMode ? R.string.lockpassword_password_too_short
                            : R.string.lockpassword_pin_too_short, mPasswordMinLength);
                    mHeaderText.setText(msg);
                    setNextEnabled(false);
                } else {
                    String error = validatePassword(password);
                    if (error != null) {
                        mHeaderText.setText(error);
                        setNextEnabled(false);
                    } else {
                        mHeaderText.setText(null);
                        setNextEnabled(true);
                    }
                }
            } else {
                mHeaderText.setText(mIsAlphaMode ? mUiStage.alphaHint : mUiStage.numericHint);
                setNextEnabled(canInput && length > 0);
            }
            setNextText(mUiStage.buttonText);
            mPasswordEntryInputDisabler.setInputEnabled(canInput);
        }

        public void afterTextChanged(Editable s) {
            // Changing the text while error displayed resets to NeedToConfirm state
            if (mUiStage == Stage.ConfirmWrong) {
                mUiStage = Stage.NeedToConfirm;
            }
            updateUi();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        // +++ ASUS AT&T issue
        private static boolean isTooMuchRepeatingChar(String password){
            final int maxRepeat = password.length() / 2;
            int repeatingCount = 0;
            for (int i = 0; i < password.length(); i++) {
                // If the amount of continuous repeating characters are over
                // half of the password, it's not valid.
                if (i > 0) {
                    int num = password.charAt(i - 1);
                    int numNext = password.charAt(i);
                    if (num == numNext) {
                        if (repeatingCount == 0) {
                            repeatingCount = 2;
                        } else {
                            repeatingCount++;
                        }
                        if (repeatingCount >= maxRepeat) {
                            return true;
                        }
                    } else {
                        repeatingCount = 0;
                    }
                }
            }
            return false;
        }

        public static boolean isSimplePassword(String password) {
            boolean allCharTheSame = true;
            int numbers = 0;
            int lowercase = 0;
            int uppercase = 0;
            int symbols = 0;
            char c = password.charAt(0);
            for (int i = 0; i < password.length(); i++) {
                if (c != password.charAt(i)) {
                    allCharTheSame = false;
                }
                if (c >= '0' && c <= '9') {
                    numbers++;
                } else if (c >= 'A' && c <= 'Z') {
                    uppercase++;
                } else if (c >= 'a' && c <= 'z') {
                    lowercase++;
                } else {
                    symbols++;
                }
            }
            // Check if the string is constructed by single character
            // Indicates the char type set
            if (allCharTheSame == true) {
                // Password is simple like 1111,aaaa
                return true;
            }

            if(isTooMuchRepeatingChar(password)){
                return true;
            }

            int complexity = 0;
            if (numbers >= 1) {
                complexity++;
            }
            if (uppercase >= 1) {
                complexity++;
            }
            if (lowercase >= 1) {
                complexity++;
            }
            if (symbols >= 1) {
                complexity++;
            }
            // If password contains more than 2 character sets, it's not simple
            if (complexity >= 2) {
                return false;
            }

            boolean monotonic = true;
            if (symbols == 0) {
                // check monotonic increasing case ex:123456789
                for (int i = 0; i < password.length(); i++) {
                    if (i > 0) {
                        int num = password.charAt(i - 1);
                        int numNext = password.charAt(i);
                        if ((numNext - num) != 1) {
                            monotonic = false;
                            break;
                        }
                    }
                }
                if (monotonic == true) {
                    return true;
                } else {
                    monotonic = true;
                    // check monotonic decreasing case ex:987654321
                    for (int i = 0; i < password.length(); i++) {
                        if (i > 0) {
                            int num = password.charAt(i - 1);
                            int numNext = password.charAt(i);
                            if ((num - numNext) != 1) {
                                monotonic = false;
                                break;
                            }
                        }
                    }
                    if (monotonic == true) {
                        return true;
                    } else {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        // ---

        private void startSaveAndFinish() {
            if (mSaveAndFinishWorker != null) {
                Log.w(TAG, "startSaveAndFinish with an existing SaveAndFinishWorker.");
                return;
            }

            mPasswordEntryInputDisabler.setInputEnabled(false);
            setNextEnabled(false);

            mSaveAndFinishWorker = new SaveAndFinishWorker();
            mSaveAndFinishWorker.setListener(this);

            getFragmentManager().beginTransaction().add(mSaveAndFinishWorker,
                    FRAGMENT_TAG_SAVE_AND_FINISH).commit();
            getFragmentManager().executePendingTransactions();

            final boolean required = getActivity().getIntent().getBooleanExtra(
                    EXTRA_REQUIRE_PASSWORD, false);
            mSaveAndFinishWorker.start(mLockPatternUtils, required, mHasChallenge, mChallenge,
                    mChosenPassword, mCurrentPassword, mRequestedQuality, mUserId);
        }

        @Override
        public void onChosenLockSaveFinished(boolean wasSecureBefore, Intent resultData) {
            getActivity().setResult(RESULT_FINISHED, resultData);

            /*if (!wasSecureBefore) {
                Intent intent = getRedactionInterstitialIntent(getActivity());
                if (intent != null) {
                    intent.putExtra(EXTRA_HIDE_DRAWER, mHideDrawer);
                    startActivity(intent);
                }
            }*/
            startActivity(resultData);
            getActivity().finish();
        }
    }

    private static class SaveAndFinishWorker extends SaveChosenLockWorkerBase {

        private String mChosenPassword;
        private String mCurrentPassword;
        private int mRequestedQuality;

        public void start(LockPatternUtils utils, boolean required,
                          boolean hasChallenge, long challenge,
                          String chosenPassword, String currentPassword, int requestedQuality, int userId) {
            prepare(utils, required, hasChallenge, challenge, userId);

            mChosenPassword = chosenPassword;
            mCurrentPassword = currentPassword;
            mRequestedQuality = requestedQuality;
            mUserId = userId;

            start();
        }

        @Override
        protected Intent saveAndVerifyInBackground() {
            Intent result = null;
            mUtils.saveLockPassword(mChosenPassword, mCurrentPassword, mRequestedQuality,
                    mUserId);

            if (mHasChallenge) {
                byte[] token;
                try {
                    token = mUtils.verifyPassword(mChosenPassword, mChallenge, mUserId);
                } catch (RequestThrottledException e) {
                    token = null;
                }

                if (token == null) {
                    Log.e(TAG, "critical: no token returned for known good password.");
                }

                result = new Intent();
                result.setAction(FingerprintEnrollFindSensor.ACTION);
                //setToken(token);
                result.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, token);
            }

            return result;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.setAction(SelectScreenLockActivity.ACTION);
            startActivity(intent);
            finish();
        }
        return false;
    }
}