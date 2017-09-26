package com.asus.cnsetupwizard.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.cnsetupwizard.R;


/**
 * Small helper class to manage text/icon around fingerprint authentication UI.
 */
public class FingerprintUiHelper extends FingerprintManager.AuthenticationCallback {

    private static final long ERROR_TIMEOUT = 1300;

    private ImageView mIcon;
    private TextView mErrorTextView;
    private CancellationSignal mCancellationSignal;
    private int mUserId;

    private Callback mCallback;
    private FingerprintManager mFingerprintManager;

    public FingerprintUiHelper(TextView errorTextView, Callback callback,
                               int userId) {
        mFingerprintManager = errorTextView.getContext().getSystemService(FingerprintManager.class);
        //mIcon = icon;
        mErrorTextView = errorTextView;
        mCallback = callback;
        mUserId = userId;
    }

    public void startListening() {
        if (mFingerprintManager != null && mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.getEnrolledFingerprints(mUserId).size() > 0) {
            mCancellationSignal = new CancellationSignal();
            mFingerprintManager.setActiveUser(mUserId);
            mFingerprintManager.authenticate(
                    null, mCancellationSignal, 0 , this, null, mUserId);
            setFingerprintIconVisibility(true);
            //mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    private boolean isListening() {
        return mCancellationSignal != null && !mCancellationSignal.isCanceled();
    }

    private void setFingerprintIconVisibility(boolean visible) {
        //mIcon.setVisibility(visible ? View.VISIBLE : View.GONE);
        mCallback.onFingerprintIconVisibilityChanged(visible);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        showError(errString);
        setFingerprintIconVisibility(false);
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        showError(helpString);
    }

    /*@Override
    public void onAuthenticationFailed() {
        showError(getResources().getString(
                R.string.fingerprint_not_recognized));
    }*/

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
       // mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mCallback.onAuthenticated();
    }

    private void showError(CharSequence error) {
        if (!isListening()) {
            return;
        }

       // mIcon.setImageResource(R.drawable.ic_fingerprint_error);
        mErrorTextView.setText(error);
        mErrorTextView.removeCallbacks(mResetErrorTextRunnable);
        mErrorTextView.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT);
    }

    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mErrorTextView.setText("");
            //mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };

    public interface Callback {
        void onAuthenticated();
        void onFingerprintIconVisibilityChanged(boolean visible);
    }
}
