package com.asus.cnsetupwizard.wifi;

import java.util.Timer;
import java.util.TimerTask;

import com.asus.cnsetupwizard.R;

import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LoadingDialog extends Dialog {

	private static final String TAG = "LoadingDialog";
	
	private static final int DURATION = 2000;
	
	private ImageView frontIV;
	
	private Animation animation;
	
	private Timer animationTimer;
	
	private Context mContext;
	
	public LoadingDialog(Context context) {
		super(context,R.style.dialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_loading);
		frontIV = (ImageView)findViewById(R.id.img_front);
		
		animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_load_dialog);
		
		animationTimer = new Timer();
		animationTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				handler.sendEmptyMessage(1);
			}
		}, 0, DURATION * 2);
	}
	
	Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			frontIV.setAnimation(animation);
			animation.start();
		}
		
	};

	@Override
	protected void onStop() {
		super.onStop();
		animationTimer.cancel();
	}
	
	

}
