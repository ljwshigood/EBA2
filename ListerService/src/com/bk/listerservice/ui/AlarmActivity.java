package com.bk.listerservice.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bk.listerservice.R;
import com.bk.listerservice.service.BgMusicControlService;
import com.bk.listerservice.utils.Constant;

public class AlarmActivity extends BaseActivity {

	private Context mContext;

	private LinearLayout mLlOK ;
	
	private String mAlarmInfo ;
	
	private int mType ;
	
	private TextView mTvType ;
	
	private void initView(){
		mTvType = (TextView)findViewById(R.id.tv_key_info) ;
		mLlOK = (LinearLayout)findViewById(R.id.ll_ok);
		mLlOK.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
				intentDistance.putExtra("control", 2);
				mContext.sendBroadcast(intentDistance);
				finish();
			}
		});
	}
	
	private void getIntentData(){
		Intent intent = getIntent();
		mType = intent.getIntExtra("type", 0) ;
		if(mType == 0){
			mTvType.setText(mContext.getString(R.string.disconnect)) ;
		}else if(mType == 1){
			mTvType.setText(mContext.getString(R.string.short_key)) ;
		}else if(mType == 2){
			mTvType.setText(mContext.getString(R.string.long_key)) ;
		}
	}
	

	private IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.UPDATEVIEW) ;
		return intentFilter;
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
		initView();
		mContext = AlarmActivity.this;
		getIntentData();
		
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}
	

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			 if(action.equals(Constant.UPDATEVIEW)){
				finish() ;
			}

		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver) ;
	}
}
