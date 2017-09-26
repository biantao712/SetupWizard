package com.asus.cnsetupwizard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;
import android.view.MotionEvent;
import android.util.Log;

/**
 * TODO: document your custom view class.
 */
public class AdScrollView extends ScrollView {
	private static final String TAG = "AdScrollView";
	
	//for touch scroll left & right
    private boolean isMotionDown = false;
    private int downX;
    private int downY;
    
    Context mParentActivity;

    public AdScrollView(Context context) {
        super(context);
    }

    public AdScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setParentActivity(Context context) {
        mParentActivity = context;
    }

	@Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MotionEvent.ACTION_DOWN");
                isMotionDown = true;
                downX = (int)event.getX() ;
                downY = (int)event.getY() ;

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MotionEvent.ACTION_MOVE");
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MotionEvent.ACTION_UP");
                if(isMotionDown)
                {
                    int deltaX = (int)(event.getX() - downX);
                    int deltaY = (int)(event.getY() - downY);
                    Log.d(TAG, "MotionEvent.ACTION_UP  deltax =" + deltaX);
                    int x = Math.abs(deltaX);
                    int y = Math.abs(deltaY);
                    if(x > y)
                    {
                        if(deltaX > 50)
                        {
                        } else if(deltaX < -50) {
                        }
                    }
                }
                isMotionDown = false;
                break;
        }

        return super.onTouchEvent(event);
    }
}
