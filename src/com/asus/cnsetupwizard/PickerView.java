package com.asus.cnsetupwizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Rect;
import java.util.Locale;

import com.android.internal.app.LocalePicker.LocaleInfo;

/**
 * 滚动选择器
 * 
 * @author chenjing
 * 
 */
public class PickerView extends View
{

	public static final String TAG = "PickerView";
	/**
	 * text之间间距和minTextSize之比
	 */
	public static final float MARGIN_ALPHA = 2.8f;
	private static final int MAXTEXTCOLOR =  0xffffff;
	private static final int MAXTEXTCOLOR2 =  0x03bed4;
	/**
	 * 自动回滚到中间的速度
	 */
	public static final float SPEED = 2;

//	private List<String> mDataList;
	private List<LocaleInfo> mList = new ArrayList<LocaleInfo>();
	private int mSelectedIndex = 0;
	private int mListSize = 0;
	/**
	 * 选中的位置，这个位置是mDataList的中心位置，一直不变
	 */
//	private int mCurrentSelected;
	private Paint mMaxPaint;
	private Paint mMinPaint;

	//ZC550KL  720P
	//private float mMaxTextSize = 44;
	//private float mMinTextSize = 36;
	private int m_ProjectType = -1;
	
	//飞马 1080P
	//22×400÷160
	private float mMaxTextSize = 58;
	//18×400÷160
	private float mMinTextSize = 48;

	private float mMaxTextAlpha = 255;
	private float mMinTextAlpha = 102;

	private int mMaxColorText = 0xffffff;
	private int mMinColorText = 0x000000;
	private Bitmap mLine = null;

	private int mViewHeight;
	private int mViewWidth;

	private float mLastDownY;
	private boolean mHasMoveAction = false;
	/**
	 * 滑动的距离
	 */
	private float mMoveLen = 0;
	private float mMoveLenMax = MARGIN_ALPHA * mMinTextSize / 2;

	private boolean isInit = false;
	private onSelectListener mSelectListener;
	private Timer timer;
	private MyTimerTask mTask;
	
	//for touch scroll left & right
    private boolean isMotionDown = false;
    private int downX;
    private int downY;
    
    Context mParentActivity;
    
    //West TBD
    private int offsetfordrawing = 150;
    private int device_type = -1;
    
    public void setParentActivity(Context context) {
        mParentActivity = context;
    }

	Handler updateHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			if (Math.abs(mMoveLen) < SPEED)
			{
				mMoveLen = 0;
				if (mTask != null)
				{
					mTask.cancel();
					mTask = null;
					performSelect();
				}
			} else
				// 这里mMoveLen / Math.abs(mMoveLen)是为了保有mMoveLen的正负号，以实现上滚或下滚
				mMoveLen = mMoveLen - mMoveLen / Math.abs(mMoveLen) * SPEED;
			invalidate();
		}

	};

	public PickerView(Context context)
	{
		super(context);
		init(context);
	}

	public PickerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public void setOnSelectListener(onSelectListener listener)
	{
		mSelectListener = listener;
	}

	private void performSelect()
	{
		if (mSelectListener != null)
			mSelectListener.onSelect(mList.get(mSelectedIndex));
	}

	public void setData(Locale l, List<LocaleInfo> locinfo)
	{
		mList.clear();
		int indexEn = 0;
		mSelectedIndex = 0;

		for (int i = 0; i < locinfo.size(); i++) {
			LocaleInfo info = locinfo.get(i);
			String language = info.getLocale().toString();
			//Log.d(TAG, "setData locale.toString()="+language);
			if(language.equals("zh_CN") || language.equals("zh_TW"))
			{
				//Log.d(TAG, "setData mList.add="+language);
				mList.add(info);
			}else if(language.equals("en_US"))
			{
				//Log.d(TAG, "setData mList.add="+language+"   indexEn="+i);
				indexEn = mList.size();
				mList.add(info);
			}
		}
		mListSize = mList.size();
		boolean bFind =false;
		for (int i = 0; i < mListSize; i++) {
			if (mList.get(i).getLocale().equals(l)) {
				mSelectedIndex = i;
				//Log.d(TAG, "setData mSelectedIndex="+mSelectedIndex);
				bFind = true;
			}
		}
		if(!bFind)
		{
			//Log.d(TAG, "setData indexEn="+indexEn);
			mSelectedIndex = indexEn;
		}
		invalidate();
	}

	public void setSelected(int selected)
	{
//		mCurrentSelected = selected;
	}

	private void moveHeadToTail()
	{
		mSelectedIndex = (mSelectedIndex + 1+ mListSize)%mListSize;
	}

	private void moveTailToHead()
	{
		mSelectedIndex = (mSelectedIndex - 1+ mListSize)%mListSize;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mViewHeight = getMeasuredHeight();
		mViewWidth = getMeasuredWidth();
		// 按照View的高度计算字体大小
		//mMaxTextSize = mViewHeight / 4.0f;
		//mMinTextSize = mMaxTextSize / 2f;
		isInit = true;
		invalidate();
	}

	private void init(Context context)
	{
		timer = new Timer();
		
		m_ProjectType = ProjectType.getProjectType();
		if(m_ProjectType == ProjectType.PROJECT_HAS_LAUNCHER3_BACKUP)
		{
			mMaxColorText = getResources().getColor(R.color.text_color);
			// = ((BitmapDrawable)getResources().getDrawable(R.drawable.dividingline2)).getBitmap();
		}else{
			mMaxColorText= getResources().getColor(R.color.text_color);
			//mLine = ((BitmapDrawable)getResources().getDrawable(R.drawable.dividingline)).getBitmap();
		}

		mMaxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaxPaint.setStyle(Style.FILL);
		mMaxPaint.setTextAlign(Align.CENTER);
		mMaxPaint.setColor(getResources().getColor(R.color.text_color));
		
		mMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMinPaint.setStyle(Style.FILL);
		mMinPaint.setTextAlign(Align.CENTER);
		mMinPaint.setColor(getResources().getColor(R.color.button_enable_color));
		
		mList.clear();
		
		float scale = context.getResources().getDisplayMetrics().density;

		mMaxTextSize = getResources().getDimensionPixelSize(R.dimen.welcome_content_size);
		mMinTextSize = getResources().getDimensionPixelSize(R.dimen.welcome_content_size);
		/*if(m_ProjectType == ProjectType.PROJECT_HAS_LAUNCHER3_BACKUP)
		{
			offsetfordrawing = 150;
		}else
		{
			offsetfordrawing = 300;
		}*/
		offsetfordrawing = 150;
		device_type = 4;//1080P

		Log.d(TAG, "init scale="+scale + "  device_type="+device_type);
		
		mMoveLenMax = MARGIN_ALPHA * mMinTextSize / 2;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		// 根据index绘制view
		if (isInit)
			drawData(canvas);
	}

	private void drawData(Canvas canvas)
	{
		// 先绘制选中的text再往上往下绘制其余的text
		float scale = parabola(mViewHeight / 4.0f, mMoveLen);
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mMaxPaint.setTextSize(size);
		if(Math.abs(mMoveLen) <  mMoveLenMax - 5)
		{
			mMaxPaint.setColor(mMaxColorText);
			mMaxPaint.setAlpha((int)mMaxTextAlpha);
		}
		else
		{
			mMaxPaint.setColor(mMinColorText);
			mMaxPaint.setAlpha((int)mMinTextAlpha);
		}
		// text居中绘制，注意baseline的计算才能达到居中，y值是text中心坐标
		float x = (float) (mViewWidth / 2.0);
		float y = (float) ((mViewHeight / 2.0 + mMoveLen) - offsetfordrawing);
		float y_line=  (float) ((mViewHeight / 2.0) - offsetfordrawing);
		FontMetricsInt fmi = mMaxPaint.getFontMetricsInt();
		float density = getResources().getDisplayMetrics().density;
		float baseline;
		if(density == 2.0){
			baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0))+125;
		}else {
			baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0))+100;
		}

		canvas.drawText(mList.get(mSelectedIndex).getLabel(), x, baseline, mMaxPaint);
		//Rect src = new Rect(0,0,(int)(mLine.getWidth()), (int)(mLine.getHeight()));

		if(device_type == 1)//pad Z300M
		{
			Rect dst1 = new Rect(500, (int)(y_line- 19),(int)mViewWidth-500, (int)(y_line-18));
			Rect dst2 = new Rect(500, (int)(y_line+ 20),(int)mViewWidth-500, (int)(y_line+21));
			//canvas.drawBitmap(mLine, src,  dst1, null);
			//canvas.drawBitmap(mLine, src, dst2, null);
		}
		else if(device_type == 2)//pad Z380KL
		{
			Rect dst1 = new Rect(150, (int)(y_line-30),(int)mViewWidth-150, (int)(y_line-27));
			Rect dst2 = new Rect(150, (int)(y_line+30),(int)mViewWidth-150, (int)(y_line+33));
			//canvas.drawBitmap(mLine, src,  dst1, null);
			//canvas.drawBitmap(mLine, src, dst2, null);
		}else
		{
			Rect dst1 = new Rect(0, (int)(y_line-60),(int)mViewWidth, (int)(y_line-58));
			Rect dst2 = new Rect(0, (int)(y_line+60),(int)mViewWidth, (int)(y_line+62));
			//canvas.drawBitmap(mLine, src,  dst1, null);
			//canvas.drawBitmap(mLine, src, dst2, null);
		}
		// 绘制上方data
		drawOtherText(canvas, 1, -1);
		// 绘制下方data
		drawOtherText(canvas, 1, 1);
	}

	/**
	 * @param canvas
	 * @param position
	 *            距离mCurrentSelected的差值
	 * @param type
	 *            1表示向下绘制，-1表示向上绘制
	 */
	private void drawOtherText(Canvas canvas, int position, int type)
	{
		int index = (mSelectedIndex + type+ mListSize)%mListSize;
		float d = (float) (MARGIN_ALPHA * mMinTextSize * position + type
				* mMoveLen);
		float scale = parabola(mViewHeight / 4.0f, d);
		float size = (mMaxTextSize - mMinTextSize) * scale + mMinTextSize;
		mMinPaint.setTextSize(size);
		mMinPaint.setAlpha((int)mMinTextAlpha);
		mMinPaint.setColor(getResources().getColor(R.color.button_enable_color));
		float y = (float) ((mViewHeight / 2.0 + type * d) - offsetfordrawing);
		FontMetricsInt fmi = mMinPaint.getFontMetricsInt();
		float density = getResources().getDisplayMetrics().density;
		float baseline;
		if(density == 2.0){
			baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0))+125;
		}else {
			baseline = (float) (y - (fmi.bottom / 2.0 + fmi.top / 2.0))+100;
		}
		canvas.drawText(mList.get(index).getLabel(),
				(float) (mViewWidth / 2.0), baseline, mMinPaint);
	}

	/**
	 * 抛物线
	 * 
	 * @param zero
	 *            零点坐标
	 * @param x
	 *            偏移量
	 * @return scale
	 */
	private float parabola(float zero, float x)
	{
		float f = (float) (1 - Math.pow(x / zero, 2));
		return f < 0 ? 0 : f;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		//Constants.setATDProperty(false);
		switch (event.getAction()& MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "MotionEvent.ACTION_DOWN");
                isMotionDown = true;
                downX = (int)event.getX() ;
                downY = (int)event.getY() ;
                doDown(event);

                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "MotionEvent.ACTION_MOVE");
                if(isMotionDown)
                {
                    int deltaX = (int)(event.getX() - downX);
                    int deltaY = (int)(event.getY() - downY);
                    Log.d(TAG, "MotionEvent.ACTION_UP  deltax =" + deltaX);
                    int x = Math.abs(deltaX);
                    int y = Math.abs(deltaY);
                    if(y > x)
                    {
						mHasMoveAction = true;
						doMove(event);
					}
				}
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "MotionEvent.ACTION_UP");
                if(isMotionDown)
                {
					if(mHasMoveAction)
					{
						mHasMoveAction = false;
						doUp(event);
					}else
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
                }
                isMotionDown = false;
                break;
        }
        
		return true;
	}

	private void doDown(MotionEvent event)
	{
		if (mTask != null)
		{
			mTask.cancel();
			mTask = null;
		}
		mLastDownY = event.getY();
	}

	private void doMove(MotionEvent event)
	{

		mMoveLen += (event.getY() - mLastDownY);
		Log.i(TAG, "doMove  mMoveLen:"+ mMoveLen);
		if (mMoveLen > MARGIN_ALPHA * mMinTextSize / 2)
		{
			// 往下滑超过离开距离
			while(mMoveLen > MARGIN_ALPHA * mMinTextSize / 2)
			{
				moveTailToHead();
				mMoveLen = mMoveLen - MARGIN_ALPHA * mMinTextSize;
				Log.i(TAG, "doMove  DOWN mMoveLen:"+ mMoveLen);
			}
		} else if (mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2)
		{
			// 往上滑超过离开距离
			while(mMoveLen < -MARGIN_ALPHA * mMinTextSize / 2)
			{
				moveHeadToTail();
				mMoveLen = mMoveLen + MARGIN_ALPHA * mMinTextSize;
				Log.i(TAG, "doMove  UP mMoveLen:"+ mMoveLen);
			}
		}

		mLastDownY = event.getY();
		invalidate();
	}

	private void doUp(MotionEvent event)
	{
		// 抬起手后mCurrentSelected的位置由当前位置move到中间选中位置
		if (Math.abs(mMoveLen) < 0.0001)
		{
			mMoveLen = 0;
			return;
		}
		if (mTask != null)
		{
			mTask.cancel();
			mTask = null;
		}
		mTask = new MyTimerTask(updateHandler);
		timer.schedule(mTask, 0, 10);
	}

	class MyTimerTask extends TimerTask
	{
		Handler handler;

		public MyTimerTask(Handler handler)
		{
			this.handler = handler;
		}

		@Override
		public void run()
		{
			handler.sendMessage(handler.obtainMessage());
		}

	}

	public interface onSelectListener
	{
		void onSelect(LocaleInfo data);
	}
}
