package com.asus.cnsetupwizard.fingerprint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.View;

import com.asus.cnsetupwizard.R;


public class AsusFindFingerprintSensorView extends View {

    private Context mContext;
    private Movie mMovie;
    private long mMovieStart;

    public static final String  SENSOR_FRONT = "front";
    public static final String  SENSOR_BACK = "back";

    public static final String  SENSOR_SHAPE_SQUARE = "square";

    private String mMode = SENSOR_FRONT;

    public AsusFindFingerprintSensorView(Context context) {
        super(context);
        init(context);
    }

    public AsusFindFingerprintSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AsusFindFingerprintSensorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        setFocusable(true);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        mMode = SystemProperties.get("ro.hardware.fp_position");
        String shape = SystemProperties.get("ro.hardware.fp_shape");
        if(mMode.contains(SENSOR_FRONT)){
            if(SENSOR_SHAPE_SQUARE.equals(shape)){
                setGifResource(context, R.drawable.fingerprint_tutorial_front_square);
            }else {
                setGifResource(context, R.drawable.fingerprint_tutorial_front);
            }
        } else {
            if(SENSOR_SHAPE_SQUARE.equals(shape)){
                setGifResource(context, R.drawable.fingerprint_tutorial_back_square);
            }else {
                setGifResource(context, R.drawable.fingerprint_tutorial_back);
            }
        }
    }

    public void setGifResource(Context context, int resId) {
        java.io.InputStream is;
        is = context.getResources().openRawResource(resId);
        mMovie = Movie.decodeStream(is);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(mMovie == null){
            return;
        }

        int width = mMovie.width();
        int height = mMovie.height();
        if(SENSOR_FRONT.equals(mMode)){
            width = mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.asus_fingerprint_find_sensor_content_front_w);
            height = mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.asus_fingerprint_find_sensor_content_front_h);
        }else{
            width = mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.asus_fingerprint_find_sensor_content_back_w);
            height = mContext.getResources()
                    .getDimensionPixelOffset(R.dimen.asus_fingerprint_find_sensor_content_back_h);
        }

        width = measureDimension(width, widthMeasureSpec);
        height = measureDimension(height, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    protected int measureDimension( int defaultSize, int measureSpec ) {

        int result = defaultSize;

        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }

        else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(defaultSize, specSize);
        }
        else {
            result = defaultSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        long now = android.os.SystemClock.uptimeMillis();
        if (0 == mMovieStart) { // first time
            mMovieStart = now;
        }

        if (null == mMovie) {
            super.onDraw(canvas);
        } else {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 5000;
            }
            canvas.setDrawFilter(
                    new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));

            int relTime = (int) ((now - mMovieStart) % dur);
            mMovie.setTime(relTime);

            float scaleWidth = (float)getWidth()/(float)mMovie.width() ;
            float scaleHeight = (float)getHeight()/(float)mMovie.height() ;
            float scale =  Math.min(scaleHeight, scaleWidth);

            int movieWidth = (int) (mMovie.width()*scale);
            int movieHeight = (int) (mMovie.height()*scale);

            int sx = getWidth()/2 - movieWidth/2 ;
            int sy = getHeight() - movieHeight;
            canvas.scale(scale, scale);
            mMovie.draw(canvas, sx, sy);

            invalidate();
        }

    }

    public String getPosition(){
        return mMode;
    }
}
