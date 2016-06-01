package com.bk.listerservice.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bk.listerservice.R;
import com.bk.listerservice.app.AppContext;
import com.bk.listerservice.bean.NotificationBean;
import com.bk.listerservice.blue.BluetoothLeService;
import com.bk.listerservice.service.BgMusicControlService;
import com.bk.listerservice.service.ListenerService;
import com.bk.listerservice.service.SMSService;
import com.bk.listerservice.utils.Constant;
import com.bk.listerservice.utils.SharePerfenceUtil;

public class MainActivity extends BaseActivity implements OnClickListener {

	private ImageView mIvSet;

	private Context mContext;
	
	private CheckBox mCbLed;

	private CheckBox mCbMotor;

	private ProgressBar mProgressBar;

	private TextView mTvDeviceName;

	private TextView mTvBattery;
	
	private int mProgressWidth ;
	
	private Button mTvOn ;
	
	private Button mTvOff ;
	
	private Button mBtnMotorOn ;
	
	private Button mBtnMotorOff ;

	private void initView() {
		//mBtnMotorOn = (Button)findViewById(R.id.btn_motor_on) ;
		//mBtnMotorOff = (Button)findViewById(R.id.btn_motor_off) ;
		//mTvOn = (Button)findViewById(R.id.tv_on) ;
		//mTvOff = (Button)findViewById(R.id.tv_off) ;
		mTvBattery = (TextView) findViewById(R.id.tv_battery);
		mTvDeviceName = (TextView) findViewById(R.id.tv_device_name);
		mProgressBar = (ProgressBar) findViewById(R.id.pb_battery);
		mCbLed = (CheckBox) findViewById(R.id.cb_led);
		mCbMotor = (CheckBox) findViewById(R.id.cb_motor);
		mIvSet = (ImageView) findViewById(R.id.iv_set);
		mIvSet.setOnClickListener(this);
		mCbLed.setOnClickListener(this);
		mCbMotor.setOnClickListener(this);
		//mTvOn.setOnClickListener(this) ;
		//mTvOff.setOnClickListener(this) ;
		//mBtnMotorOn.setOnClickListener(this) ;
		//mBtnMotorOff.setOnClickListener(this) ;
		mProgressWidth = dip2px(mContext, 280) ;
	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	@Override
	protected void onResume() {
		super.onResume();

		NotificationBean bean = AppContext.mNotificationBean;
		if (bean != null && bean.isShowNotificationDialog()) {
			bean.setShowNotificationDialog(false);
			Intent intent1 = new Intent(mContext, AlarmActivity.class);
			startActivity(intent1);
		}

		mNotificationMnager.cancel(bean.getNotificationID());
	}

	private IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(BluetoothLeService.ACTION_READ_DATA_AVAILABLE);
		intentFilter.addAction(Constant.UPDATEVIEW) ;
		return intentFilter;
	}

	private Handler mHandlerAudioBattery = new Handler();

	Runnable autoReadBatteryRunable = new Runnable() {

		@Override
		public void run() {
			if (AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()) {
				AppContext.mBluetoothLeService.readBatteryCharacteristic();
			}
			mHandlerAudioBattery.postDelayed(autoReadBatteryRunable, 3000);
		}
	};
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			mCbLed.setChecked(true) ;
			mCbMotor.setChecked(true);
		};
	};
	
	private Handler mDelayHander = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			if(AppContext.mBluetoothLeService != null){
				AppContext.mBluetoothLeService.disconnect() ;
				AppContext.mBluetoothLeService.close() ;
			}
			
			mIvSet.setBackgroundResource(R.drawable.setting_button_grey);
			mTvDeviceName.setText("");
			mTvBattery.setText("");
			mProgressBar.setProgress(0);
		};
	};
	

	private void dialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage(mContext.getString(R.string.already_connect));
		builder.setTitle(mContext.getString(R.string.tip));
		builder.setPositiveButton(mContext.getString(R.string.ok),new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(AppContext.mBluetoothLeService != null){
					AppContext.mBluetoothLeService.disconnect() ;
					AppContext.mBluetoothLeService.close() ;
					AppContext.isBlueDisconnect = true ;
				}
				dialog.dismiss();
			}
		}) ;
		
		builder.create().show();
	}
	
	Runnable mDisconnectRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			mDelayHander.sendEmptyMessage(0) ;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);

			if (BluetoothLeService.ACTION_READ_DATA_AVAILABLE.equals(action)) {
				mProgressBar.setProgress(Integer.valueOf(data));
				float percent = Float.valueOf(Integer.valueOf(data)) / Float.valueOf(100) ;
				LayoutParams lp = mTvBattery.getLayoutParams();
				lp.width = (int)(mProgressWidth * percent) ;
				mTvBattery.setLayoutParams(lp);
				mTvBattery.setText(Integer.valueOf(data) + "%") ;
			} else if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				
				mDelayHander.postDelayed(mDisconnectRunnable, 3000);

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				
				mHandler.removeCallbacks(mDisconnectRunnable) ;
				
				if((boolean) SharePerfenceUtil.getParam(mContext, "led", true)){
					mCbLed.setChecked(true) ;
				}else{
					mCbLed.setChecked(false) ;
				}
				
				if((boolean) SharePerfenceUtil.getParam(mContext, "motor", true)){
					mCbMotor.setChecked(true) ;
				}else{
					mCbMotor.setChecked(false) ;
				}
				
				mIvSet.setBackgroundResource(R.drawable.setting_button_white);
				mTvDeviceName.setText(device.getName());
				
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
				if (data.startsWith("01")) {
					Intent intent1 = new Intent(mContext, AlarmActivity.class);
					intent1.putExtra("type", 1);
					startActivity(intent1);
				} else if (data.startsWith("02")) {
					Intent intent1 = new Intent(mContext, AlarmActivity.class);
					intent1.putExtra("type", 2);
					startActivity(intent1);
				}
			}

		}
	};

	protected NotificationManager mNotificationMnager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = MainActivity.this;
		initView();

		Intent intentMusic = new Intent(mContext, BgMusicControlService.class);
		startService(intentMusic);

		Intent intentSMS = new Intent(mContext, SMSService.class);
		startService(intentSMS);

		Intent intent = new Intent(mContext, ListenerService.class);
		startService(intent);

		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

		mHandlerAudioBattery.postDelayed(autoReadBatteryRunable, 1000);

		mNotificationMnager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		SharePerfenceUtil.setParam(mContext, "led", true);
		SharePerfenceUtil.setParam(mContext, "motor", true);
	
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		/*case R.id.tv_on:
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.sendMsg("AAB00201000000000000");
			}
			break ;
		case R.id.tv_off:
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.sendMsg("AAB10200000000000000");
			}
			break ;
		case R.id.btn_motor_on:
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.sendMsg("AAB10300000000000000");
			}
			break ;
		case R.id.btn_motor_off:
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.sendMsg("AAB10400000000000000");
			}
			break ;*/
		case R.id.iv_set:
			if(AppContext.mBluetoothLeService != null && AppContext.mBluetoothLeService.isConnect()){
				dialog() ;
			}else if(AppContext.mBluetoothLeService != null && !AppContext.mBluetoothLeService.isConnect()){
				Intent intent = new Intent(mContext, DeviceScanActivity.class);
				startActivity(intent);	
			}
			
			break;
		case R.id.cb_led:
			if (mCbLed.isChecked()) {
				if (AppContext.mBluetoothLeService != null) {
					AppContext.mBluetoothLeService.sendMsg("AAB10100000000000000");
				}

			} else {
				if (AppContext.mBluetoothLeService != null) {
					AppContext.mBluetoothLeService.sendMsg("AAB10200000000000000");
				}
			}
			SharePerfenceUtil.setParam(mContext, "led", mCbLed.isChecked()) ;
			break ;
		case R.id.cb_motor:
			if (mCbMotor.isChecked()) {
				if (AppContext.mBluetoothLeService != null) {
					AppContext.mBluetoothLeService.sendMsg("AAB10300000000000000");
				}

			} else {
				if (AppContext.mBluetoothLeService != null) {
					AppContext.mBluetoothLeService.sendMsg("AAB10400000000000000");
				}

			}
			
			SharePerfenceUtil.setParam(mContext, "motor", mCbMotor.isChecked()) ;
			break;
		default:
			break;
		}
	}

}
