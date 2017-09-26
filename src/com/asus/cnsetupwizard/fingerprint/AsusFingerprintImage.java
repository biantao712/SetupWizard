package com.asus.cnsetupwizard.fingerprint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.asus.cnsetupwizard.R;


public class AsusFingerprintImage extends ImageView{

    private Context mContext;

    private int mFinishMask;
    private int mRatio;
    private int mLastProgress;
    private boolean mDrawFinishMask = false;

    private int[] mFingerprintStepId = {
            R.drawable.asus_fingerrint_enrollment_step_0,
            R.drawable.asus_fingerrint_enrollment_step_1,
            R.drawable.asus_fingerrint_enrollment_step_2,
            R.drawable.asus_fingerrint_enrollment_step_3,
            R.drawable.asus_fingerrint_enrollment_step_4,
            R.drawable.asus_fingerrint_enrollment_step_5,
            R.drawable.asus_fingerrint_enrollment_step_6,
            R.drawable.asus_fingerrint_enrollment_step_7,
            R.drawable.asus_fingerrint_enrollment_step_8,
            R.drawable.asus_fingerrint_enrollment_step_9,
            R.drawable.asus_fingerrint_enrollment_step_10,
            R.drawable.asus_fingerrint_enrollment_step_11,
            R.drawable.asus_fingerrint_enrollment_step_12,
            R.drawable.asus_fingerrint_enrollment_step_13,
            R.drawable.asus_fingerrint_enrollment_step_14,
            R.drawable.asus_fingerrint_enrollment_step_15,
            R.drawable.asus_fingerrint_enrollment_step_16,
            R.drawable.asus_fingerrint_enrollment_step_17,
            R.drawable.asus_fingerrint_enrollment_step_18,
            R.drawable.asus_fingerrint_enrollment_step_19,
            R.drawable.asus_fingerrint_enrollment_step_20,
            R.drawable.asus_fingerrint_enrollment_step_21,
            R.drawable.asus_fingerrint_enrollment_step_22,
            R.drawable.asus_fingerrint_enrollment_step_23,
            R.drawable.asus_fingerrint_enrollment_step_24,  // Continue image show on this index
            R.drawable.asus_fingerrint_enrollment_step_25,  // Continue image disappear on this index
            R.drawable.asus_fingerrint_enrollment_step_26,
            R.drawable.asus_fingerrint_enrollment_step_27,
            R.drawable.asus_fingerrint_enrollment_step_28,
            R.drawable.asus_fingerrint_enrollment_step_29,
            R.drawable.asus_fingerrint_enrollment_step_30,
            R.drawable.asus_fingerrint_enrollment_step_31,
            R.drawable.asus_fingerrint_enrollment_step_32,
            R.drawable.asus_fingerrint_enrollment_step_33,
            R.drawable.asus_fingerrint_enrollment_step_34,
            R.drawable.asus_fingerrint_enrollment_step_35};

    public static final double INTERRUPT_RATIO = 25f / 35f;

    public AsusFingerprintImage(Context context) {
        super(context);
        init(context);
    }

    public AsusFingerprintImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AsusFingerprintImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AsusFingerprintImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
        mContext = context;
        mFinishMask = getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        if (mRatio == 0) {
            super.onDraw(canvas);
            return;
        }

        int l = canvas.saveLayer(0, 0, w, h, null, Canvas.ALL_SAVE_FLAG);
        int step = (int) progressMapStep(mRatio);

        //Create fingerprint
        drawFingerprintProgress(step, 255 /*[0..255]*/, w, h, canvas);

        //draw Finish mask
        if(mDrawFinishMask) {
            Paint paint = new Paint();
            paint.setColor(mContext.getColor(R.color.category_bar_color));
            paint.setAlpha(200); //Range[0..255]
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawRect(0, mFinishMask, w, h, paint);
        }
        canvas.restoreToCount(l);
    }

    public double progressMapStep(double s){
        double a1 = 0;
        double a2 = FingerprintEnrollEnrolling.PROGRESS_BAR_MAX;
        double b1 = 0;
        double b2 = mFingerprintStepId.length -1;
        return b1 + ((s - a1)*(b2 - b1))/(a2 - a1);
    }

    private void drawFingerprintProgress(int step, int alpha, int w, int h, Canvas canvas){
        if(step >= mFingerprintStepId.length){
            step = mFingerprintStepId.length - 1;
        }
        canvas.saveLayer(0, 0, w, h, null, Canvas.ALL_SAVE_FLAG);
        Bitmap fpImg = drawble2Bitmap(w, h, mContext.getDrawable(mFingerprintStepId[step]));
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(fpImg, 0, 0, paint);
        canvas.restore();
    }

    private Bitmap drawble2Bitmap(int w, int h, Drawable drawable){
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public void setProgress(int progress){
        if(mRatio != progress) {
            mRatio = progress;
            invalidate();
        }
    }


    public void setFinishProgress(int progress){
        mDrawFinishMask = true;
        mFinishMask = progress;
        invalidate();
    }
}
