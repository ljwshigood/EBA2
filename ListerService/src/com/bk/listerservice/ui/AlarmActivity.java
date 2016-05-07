package com.bk.listerservice.ui;


import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bk.listerservice.R;
import com.bk.listerservice.app.AppContext;

public class AlarmActivity extends Activity {

	private Context mContext;

	private LinearLayout mLlOK ;
	
	private String mAlarmInfo ;
	
	private int mType ;
	
	private void initView(){
		mLlOK = (LinearLayout)findViewById(R.id.ll_ok);
		mLlOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	private void getIntentData(){
		Intent intent = getIntent();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		mContext = AlarmActivity.this;
		getIntentData();
		initView();
	}
	
	private class DestoryBroadcast  extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	
}
