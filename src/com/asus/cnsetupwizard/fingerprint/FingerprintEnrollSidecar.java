package com.asus.cnsetupwizard.fingerprint;

import android.annotation.Nullable;
import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.internal.logging.MetricsProto.MetricsEvent;

/**
 * Sidecar fragment to handle the state around fingerprint enrollment.
 */
public class FingerprintEnrollSidecar extends PreferenceFragment {

    private static String TAG = "FingerprintEnrollSidecar";

    private int mEnrollmentSteps = -1;
    private int mEnrollmentRemaining = 0;
    private Listener mListener;
    private boolean mEnrolling;
    private CancellationSignal mEnrollmentCancel;
    private Handler mHandler = new Handler();
    private byte[] mToken;
    private boolean mDone;
    private int mUserId;
    private FingerprintManager mFingerprintManager;

    //++ Asus fingerprint msg code
    private static final int ENROLL_ACQUIED_SIMILAR = 2000;
    //-- Asus fingerprint msg code

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mFingerprintManager = activity.getSystemService(FingerprintManager.class);
        mToken = activity.getIntent().getByteArrayExtra(
                ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);

        //mUserId = activity.getIntent().getIntExtra(Intent.EXTRA_USER_ID, UserHandle.USER_NULL);
        mUserId=UserHandle.myUserId();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mEnrolling) {
            startEnrollment();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!getActivity().isChangingConfigurations()) {
            cancelEnrollment();
        }
    }

    private void startEnrollment() {
        mHandler.removeCallbacks(mTimeoutRunnable);
        mEnrollmentSteps = -1;
        mEnrollmentCancel = new CancellationSignal();
        if (mUserId != UserHandle.USER_NULL) {
            mFingerprintManager.setActiveUser(mUserId);
        }
        mFingerprintManager.enroll(mToken, mEnrollmentCancel,
                0 /* flags */, mUserId, mEnrollmentCallback);
        mEnrolling = true;
    }

    public boolean cancelEnrollment() {
        boolean isCancel = false;
        mHandler.removeCallbacks(mTimeoutRunnable);
        if (mEnrolling) {
            mEnrollmentCancel.cancel();
            mEnrolling = false;
            mEnrollmentSteps = -1;
            isCancel = true;
            //return true;
        }
        return isCancel;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public int getEnrollmentSteps() {
        return mEnrollmentSteps;
    }

    public int getEnrollmentRemaining() {
        return mEnrollmentRemaining;
    }

    public boolean isDone() {
        return mDone;
    }

    private FingerprintManager.EnrollmentCallback mEnrollmentCallback
            = new FingerprintManager.EnrollmentCallback() {

        @Override
        public void onEnrollmentProgress(int remaining) {
            if (mEnrollmentSteps == -1) {
                mEnrollmentSteps = remaining;
            }
            mEnrollmentRemaining = remaining;
            mDone = remaining == 0;
            if (mListener != null) {
                mListener.onEnrollmentProgressChange(mEnrollmentSteps, remaining);
            }
        }

        @Override
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            //++ Asus feature
            if(helpMsgId == ENROLL_ACQUIED_SIMILAR){
                //helpString = getString(R.string.wifi);
            }
            //-- Asus feature
            if (mListener != null) {
                mListener.onEnrollmentHelp(helpString);
            }
        }

        @Override
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            if (mListener != null) {
                mListener.onEnrollmentError(errMsgId, errString);
            }
            mEnrolling = false;
        }
    };

    private final Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            cancelEnrollment();
        }
    };

    protected int getMetricsCategory() {
        return MetricsEvent.FINGERPRINT_ENROLL_SIDECAR;
    }

    public interface Listener {
        void onEnrollmentHelp(CharSequence helpString);
        void onEnrollmentError(int errMsgId, CharSequence errString);
        void onEnrollmentProgressChange(int steps, int remaining);
    }

    public boolean isEnrolling() {
        return mEnrolling;
    }
}
