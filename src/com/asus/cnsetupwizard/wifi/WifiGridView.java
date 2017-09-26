package com.asus.cnsetupwizard.wifi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.GridView;

public class WifiGridView extends GridView {
	public static final String TAG = "WifiGridView";
	
	private boolean isMotionDown = false;
	private int downX;
	private int downY;
	
	private Context mParentContext;
	
	static final int ACTION_PRE = 0;
   	static final int ACTION_NEXT = 1;
	
	public void setParentActivity(Context ct) {
		mParentContext = ct;
	}
	
	public WifiGridView(Context context) {
	super(context);
	// TODO Auto-generated constructor stub
	}
	public WifiGridView(Context context,AttributeSet attrs) {
	super(context,attrs);
	// TODO Auto-generated constructor stub
	}
	public WifiGridView(Context context,AttributeSet attrs,int defStyle) {
	super(context,attrs,defStyle);
	// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub		
		 switch (event.getAction()& MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN:
				isMotionDown = true;
				downX = (int)event.getX() ;
				downY = (int)event.getY() ;
				Log.d(TAG, "MotionEvent.ACTION_DOWN");
				Log.d(TAG, "MotionEvent.ACTION_DOWN   downX=" + downX);
             break;
         case MotionEvent.ACTION_MOVE:
             break;
         case MotionEvent.ACTION_UP:
			Log.d(TAG, "MotionEvent.ACTION_UP");
			if(isMotionDown)
			{
				int deltaX = (int)event.getX() - downX;
				int deltaY = (int)event.getY() - downY;
				Log.d(TAG, "MotionEvent.ACTION_UP downX=" + deltaX);
				int x = Math.abs(deltaX);
				int y = Math.abs(deltaY);
				
				Log.d(TAG, "deltxX =" + deltaX + "delataY = " + deltaY);
				
				if(x > y)
				{
					if(deltaX > 100)
					{
						//finish();
						//applyBackwardTransition();
						//Message msg = new Message();
						//msg.setTarget(m_WifiSettingMessageHandler);
						//msg.what = ACTION_PRE;
						//msg.sendToTarget();
					} else if(deltaX < -100) {
						//launchDataCollection();
						//Message msg = new Message();
						//msg.setTarget(m_WifiSettingMessageHandler);
						//msg.what = ACTION_NEXT;
						//msg.sendToTarget();
					}
				}
			}
			isMotionDown = false;
			break;
        }           
		return super.onTouchEvent(event);
	}
}
