package com.asus.cnsetupwizard.fingerprint;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

/**
 * An invisible retained fragment to track lock check result.
 */
public class CredentialCheckResultTracker extends Fragment {

    private Listener mListener;
    private boolean mHasResult = false;

    private boolean mResultMatched;
    private Intent mResultData;
    private int mResultTimeoutMs;
    private int mResultEffectiveUserId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setListener(Listener listener) {
        if (mListener == listener) {
            return;
        }

        mListener = listener;
        if (mListener != null && mHasResult) {
            mListener.onCredentialChecked(mResultMatched, mResultData, mResultTimeoutMs,
                    mResultEffectiveUserId, false /* newResult */);
        }
    }

    public void setResult(boolean matched, Intent intent, int timeoutMs, int effectiveUserId) {
        mResultMatched = matched;
        mResultData = intent;
        mResultTimeoutMs = timeoutMs;
        mResultEffectiveUserId = effectiveUserId;

        mHasResult = true;
        if (mListener != null) {
            mListener.onCredentialChecked(mResultMatched, mResultData, mResultTimeoutMs,
                    mResultEffectiveUserId, true /* newResult */);
            mHasResult = false;
        }
    }

    public void clearResult() {
        mHasResult = false;
        mResultMatched = false;
        mResultData = null;
        mResultTimeoutMs = 0;
        mResultEffectiveUserId = 0;
    }

    interface Listener {
        public void onCredentialChecked(boolean matched, Intent intent, int timeoutMs,
                                        int effectiveUserId, boolean newResult);
    }
}
