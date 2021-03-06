package com.asus.cnsetupwizard.fingerprint;

import android.annotation.Nullable;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.IntentSender;
import android.os.UserManager;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.asus.cnsetupwizard.utils.Utils;

public final class ChooseLockSettingsHelper {

    static final String EXTRA_KEY_TYPE = "type";
    static final String EXTRA_KEY_PASSWORD = "password";
    public static final String EXTRA_KEY_RETURN_CREDENTIALS = "return_credentials";
    public static final String EXTRA_KEY_HAS_CHALLENGE = "has_challenge";
    public static final String EXTRA_KEY_CHALLENGE = "challenge";
    public static final String EXTRA_KEY_CHALLENGE_TOKEN = "hw_auth_token";
    public static final String EXTRA_KEY_FOR_FINGERPRINT = "for_fingerprint";
    public static final String EXTRA_KEY_FOR_CHANGE_CRED_REQUIRED_FOR_BOOT = "for_cred_req_boot";

    public static final String PACKAGE = "com.android.settings";
    public static final String TITLE_TEXT = PACKAGE + ".ConfirmCredentials.title";
    public static final String HEADER_TEXT = PACKAGE + ".ConfirmCredentials.header";
    public static final String DETAILS_TEXT = PACKAGE + ".ConfirmCredentials.details";
    public static final String ALLOW_FP_AUTHENTICATION =
            PACKAGE + ".ConfirmCredentials.allowFpAuthentication";
    public static final String DARK_THEME = PACKAGE + ".ConfirmCredentials.darkTheme";
    public static final String SHOW_CANCEL_BUTTON =
            PACKAGE + ".ConfirmCredentials.showCancelButton";
    public static final String SHOW_WHEN_LOCKED =
            PACKAGE + ".ConfirmCredentials.showWhenLocked";


    @VisibleForTesting LockPatternUtils mLockPatternUtils;
    private Activity mActivity;
    private Fragment mFragment;

    public ChooseLockSettingsHelper(Activity activity) {
        mActivity = activity;
        mLockPatternUtils = new LockPatternUtils(activity);
    }

    public ChooseLockSettingsHelper(Activity activity, Fragment fragment) {
        this(activity);
        mFragment = fragment;
    }

    public LockPatternUtils utils() {
        return mLockPatternUtils;
    }


    //++ Asus: For verison request "secure set-up" featrue
    public boolean launchConfirmationActivityNeedPwd(int request, CharSequence title, boolean needPwd){
        return launchConfirmationActivity(request, title, null, null,
                false, false, false, 0, Utils.getCredentialOwnerUserId(mActivity), needPwd);
    }
    //-- Asus: For verison request "secure set-up" featrue

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    public boolean launchConfirmationActivity(int request, CharSequence title) {
        return launchConfirmationActivity(request, title, null, null, false, false);
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param returnCredentials if true, put credentials into intent. Note that if this is true,
     *                          this can only be called internally.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    boolean launchConfirmationActivity(int request, CharSequence title, boolean returnCredentials) {
        return launchConfirmationActivity(request, title, null, null, returnCredentials, false);
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param returnCredentials if true, put credentials into intent. Note that if this is true,
     *                          this can only be called internally.
     * @param userId The userId for whom the lock should be confirmed.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    public boolean launchConfirmationActivity(int request, CharSequence title,
                                              boolean returnCredentials, int userId) {
        return launchConfirmationActivity(request, title, null, null,
                returnCredentials, false, false, 0, Utils.enforceSameOwner(mActivity, userId));
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param header header of the confirmation screen; shown as large text
     * @param description description of the confirmation screen
     * @param returnCredentials if true, put credentials into intent. Note that if this is true,
     *                          this can only be called internally.
     * @param external specifies whether this activity is launched externally, meaning that it will
     *                 get a dark theme, allow fingerprint authentication and it will forward
     *                 activity result.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                       @Nullable CharSequence header, @Nullable CharSequence description,
                                       boolean returnCredentials, boolean external) {
        return launchConfirmationActivity(request, title, header, description,
                returnCredentials, external, false, 0, Utils.getCredentialOwnerUserId(mActivity));
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param header header of the confirmation screen; shown as large text
     * @param description description of the confirmation screen
     * @param returnCredentials if true, put credentials into intent. Note that if this is true,
     *                          this can only be called internally.
     * @param external specifies whether this activity is launched externally, meaning that it will
     *                 get a dark theme, allow fingerprint authentication and it will forward
     *                 activity result.
     * @param userId The userId for whom the lock should be confirmed.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                       @Nullable CharSequence header, @Nullable CharSequence description,
                                       boolean returnCredentials, boolean external, int userId) {
        return launchConfirmationActivity(request, title, header, description,
                returnCredentials, external, false, 0, Utils.enforceSameOwner(mActivity, userId));
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param header header of the confirmation screen; shown as large text
     * @param description description of the confirmation screen
     * @param challenge a challenge to be verified against the device credential.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    public boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                              @Nullable CharSequence header, @Nullable CharSequence description,
                                              long challenge) {
        return launchConfirmationActivity(request, title, header, description,
                true, false, true, challenge, Utils.getCredentialOwnerUserId(mActivity));
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param header header of the confirmation screen; shown as large text
     * @param description description of the confirmation screen
     * @param challenge a challenge to be verified against the device credential.
     * @param userId The userId for whom the lock should be confirmed.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    public boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                              @Nullable CharSequence header, @Nullable CharSequence description,
                                              long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description,
                true, false, true, challenge, Utils.enforceSameOwner(mActivity, userId));
    }

    /**
     * If a pattern, password or PIN exists, prompt the user before allowing them to change it.
     *
     * @param title title of the confirmation screen; shown in the action bar
     * @param header header of the confirmation screen; shown as large text
     * @param description description of the confirmation screen
     * @param external specifies whether this activity is launched externally, meaning that it will
     *                 get a dark theme, allow fingerprint authentication and it will forward
     *                 activity result.
     * @param challenge a challenge to be verified against the device credential.
     * @param userId The userId for whom the lock should be confirmed.
     * @return true if one exists and we launched an activity to confirm it
     * @see Activity#onActivityResult(int, int, android.content.Intent)
     */
    public boolean launchConfirmationActivityWithExternalAndChallenge(int request,
                                                                      @Nullable CharSequence title, @Nullable CharSequence header,
                                                                      @Nullable CharSequence description, boolean external, long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description, false,
                external, true, challenge, Utils.enforceSameOwner(mActivity, userId));
    }

    private boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                               @Nullable CharSequence header, @Nullable CharSequence description,
                                               boolean returnCredentials, boolean external, boolean hasChallenge,
                                               long challenge, int userId) {
        return launchConfirmationActivity(request, title, header, description, returnCredentials,
                external, hasChallenge, challenge, userId, false);
    }

    private boolean launchConfirmationActivity(int request, @Nullable CharSequence title,
                                               @Nullable CharSequence header, @Nullable CharSequence description,
                                               boolean returnCredentials, boolean external, boolean hasChallenge,
                                               long challenge, int userId, boolean needpwd) {
        final int effectiveUserId = UserManager.get(mActivity).getCredentialOwnerProfile(userId);
        boolean launched = false;

        switch (mLockPatternUtils.getKeyguardStoredPasswordQuality(effectiveUserId)) {
            /*case DevicePolicyManager.PASSWORD_QUALITY_SOMETHING:
                launched = launchConfirmationActivity(request, title, header, description,
                        returnCredentials || hasChallenge
                                ? ConfirmLockPattern.InternalActivity.class
                                : ConfirmLockPattern.class, returnCredentials, external,
                        hasChallenge, challenge, userId, needpwd);
                break;*/
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC:
            case DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC:
            case DevicePolicyManager.PASSWORD_QUALITY_COMPLEX:
            case DevicePolicyManager.PASSWORD_QUALITY_MANAGED:
                /*launched = launchConfirmationActivity(request, title, header, description,
                        returnCredentials || hasChallenge
                                ? ConfirmLockPassword.InternalActivity.class
                                : ConfirmLockPassword.class, returnCredentials, external,
                        hasChallenge, challenge, userId, needpwd);*/
                break;
        }
        return launched;
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header,
                                               CharSequence message, Class<?> activityClass, boolean returnCredentials,
                                               boolean external, boolean hasChallenge, long challenge,
                                               int userId) {
        return launchConfirmationActivity(request, title, header, message, activityClass,
                returnCredentials, external, hasChallenge, challenge, userId, false);
    }

    private boolean launchConfirmationActivity(int request, CharSequence title, CharSequence header,
                                               CharSequence message, Class<?> activityClass, boolean returnCredentials,
                                               boolean external, boolean hasChallenge, long challenge,
                                               int userId, boolean needpwd) {
        final Intent intent = new Intent();
        intent.putExtra(TITLE_TEXT, title);
        intent.putExtra(HEADER_TEXT, header);
        intent.putExtra(DETAILS_TEXT, message);
        intent.putExtra(ALLOW_FP_AUTHENTICATION, external);
        intent.putExtra(DARK_THEME, external);
        intent.putExtra(SHOW_CANCEL_BUTTON, external);
        intent.putExtra(SHOW_WHEN_LOCKED, external);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_RETURN_CREDENTIALS, returnCredentials);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, hasChallenge);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        // we should never have a drawer when confirming device credentials.
        //intent.putExtra(SettingsActivity.EXTRA_HIDE_DRAWER, true);
        intent.putExtra(Intent.EXTRA_USER_ID, userId);
        //++ Asus: For verison request "secure set-up" featrue
        intent.putExtra("needpwd", needpwd);
        //-- Asus: For verison request "secure set-up" featrue
        intent.setClassName(PACKAGE, activityClass.getName());
        if (external) {
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            if (mFragment != null) {
                copyOptionalExtras(mFragment.getActivity().getIntent(), intent);
                mFragment.startActivity(intent);
            } else {
                copyOptionalExtras(mActivity.getIntent(), intent);
                mActivity.startActivity(intent);
            }
        } else {
            if (mFragment != null) {
                mFragment.startActivityForResult(intent, request);
            } else {
                mActivity.startActivityForResult(intent, request);
            }
        }
        return true;
    }

    private void copyOptionalExtras(Intent inIntent, Intent outIntent) {
        IntentSender intentSender = inIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        if (intentSender != null) {
            outIntent.putExtra(Intent.EXTRA_INTENT, intentSender);
        }
        int taskId = inIntent.getIntExtra(Intent.EXTRA_TASK_ID, -1);
        if (taskId != -1) {
            outIntent.putExtra(Intent.EXTRA_TASK_ID, taskId);
        }
        // If we will launch another activity once credentials are confirmed, exclude from recents.
        // This is a workaround to a framework bug where affinity is incorrect for activities
        // that are started from a no display activity, as is ConfirmDeviceCredentialActivity.
        // TODO: Remove once that bug is fixed.
        if (intentSender != null || taskId != -1) {
            outIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            outIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        }
    }
}