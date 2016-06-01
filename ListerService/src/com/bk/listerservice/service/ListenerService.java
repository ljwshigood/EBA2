package com.bk.listerservice.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.bk.listerservice.R;
import com.bk.listerservice.app.AppContext;
import com.bk.listerservice.blue.BluetoothLeService;
import com.bk.listerservice.ui.AlarmActivity;
import com.bk.listerservice.ui.MainActivity;
import com.bk.listerservice.utils.AlarmManager;
import com.bk.listerservice.utils.Constant;
import com.bk.listerservice.utils.SharePerfenceUtil;

public class ListenerService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void createPhoneListener() {
		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new OnePhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private BluetoothAdapter mBluetoothAdapter;
	
	private Context mContext ;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		this.getApplicationContext().bindService(gattServiceIntent,mServiceConnection, BIND_AUTO_CREATE);
		
		createPhoneListener();
		
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
		mContext = ListenerService.this;
		
		mAlarmManager = AlarmManager.getInstance(mContext) ;
		
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}
	
	private AlarmManager mAlarmManager;
	
	private void progressTopTaskDeviceDisconnect(Intent intent) {
		String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
		mAlarmManager.DeviceDisconnectAlarm(address,mContext.getString(R.string.ble_dis_connected));
	}

	private void progressDeviceDisconnect(Intent intent,String infomation) {
		String address = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
			boolean isAlarm = mAlarmManager.DeviceDisconnectAlarm(address, mContext.getString(R.string.ble_dis_connected));
			if (isAlarm) {
				notifycationAlarm(mContext, address,infomation,1);
			}
	}
	
	private static final int NOTICE_ID = 1222;
	
	private NotificationManager manager;
	
	public void notifycationAlarm(Context context, String address,String string, int type) {

		if (address == null) {
			Intent intent = new Intent(context, MainActivity.class);
			manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher,mContext.getString(R.string.ble_dis_connected),System.currentTimeMillis());
			PendingIntent pendIntent = PendingIntent.getActivity(context, 0,intent, 0);
			notification.setLatestEventInfo(context, mContext.getString(R.string.app_name), string,pendIntent);
			manager.notify(NOTICE_ID, notification);
			AppContext.mNotificationBean.setShowNotificationDialog(false);
			AppContext.mNotificationBean.setNotificationID(NOTICE_ID);
		} else {
			Intent intent = new Intent(context, MainActivity.class);
			manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification notification = new Notification(R.drawable.ic_launcher,mContext.getString(R.string.ble_dis_connected),System.currentTimeMillis());
			PendingIntent pendIntent = PendingIntent.getActivity(context, 0,intent, 0);
			notification.setLatestEventInfo(context, mContext.getString(R.string.app_name), string,pendIntent);
			manager.notify(NOTICE_ID, notification);
			AppContext.mNotificationBean.setAddress(address);
			AppContext.mNotificationBean.setShowNotificationDialog(true);
			AppContext.mNotificationBean.setAlarmInfo(string);
			AppContext.mNotificationBean.setNotificationID(NOTICE_ID);
			AppContext.mNotificationBean.setAlarmType(type);
		}
	}
	
	private Handler mDelayHander = new Handler(){
		public void handleMessage(Message msg) {
			
			if(mIntent == null){
				return ;
			}
			
			if (!mAlarmManager.isApplicationBroughtToBackground(mContext)) {
				progressTopTaskDeviceDisconnect(mIntent);
			}else if (mAlarmManager.isApplicationBroughtToBackground(mContext)) {
				progressDeviceDisconnect(mIntent,mContext.getString(R.string.ble_dis_connected));
			}
			
			Intent intent1 = new Intent(mContext,AlarmActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
			intent1.putExtra("type", 0);
			startActivity(intent1);
		};
	};
	
	Runnable mDisconnectRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			mDelayHander.sendEmptyMessage(0) ;
		}
	};

	private Intent mIntent ;
	
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			final String action = intent.getAction();
			BluetoothDevice blueAddress = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA) ;
			if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
				
				if(AppContext.isBlueDisconnect){
					AppContext.isBlueDisconnect = false ;
					return ;
				}
				
				mIntent = intent ;
				mDelayHander.postDelayed(mDisconnectRunnable, 3000);
				scanLeDevice(true);
				
			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				
				mDelayHander.removeCallbacks(mDisconnectRunnable) ;
				
				Message msg = new Message() ;
				msg.what = 1 ;
				
				mHandler.sendMessage(msg) ;
				
				SharePerfenceUtil.setParam(mContext, "device_address", blueAddress.getAddress()) ;
				
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),blueAddress.getAddress());
				}
				
				Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
				intentDistance.putExtra("control", 2);
				sendBroadcast(intentDistance);
				
				Intent intentUpdateView = new Intent(Constant.UPDATEVIEW);
				sendBroadcast(intentUpdateView);
				
			}
		}
	};
	

	private void displayGattServices(List<BluetoothGattService> gattServices,String address) {
		if (gattServices == null)
			return;
		for (BluetoothGattService gattService : gattServices) {
			if (gattService.getUuid().toString().startsWith("0000ffe0")) {
				List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
				for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
					if (gattCharacteristic.getUuid().toString().startsWith("0000ffe2")) {
						AppContext.mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
						//saveDatabaseAndStartActivity();
					}
				}
			}
		}
		
	}

	
	private boolean mScanning  ;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == 0){
				scanLeDevice(true);
			}else if(msg.what == 1){
				mHandler.removeCallbacks(mScannRunnable) ;
			}
		};
	};
	
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,final byte[] scanRecord) {
			new Thread() {
				public void run() {
					
					if (device == null)
						return ;
					
					String address = (String) SharePerfenceUtil.getParam(mContext, "device_address", "") ;
					if(!address.equals("") && address.equals(device.getAddress())){
						if (AppContext.mBluetoothLeService != null ) {
							AppContext.mBluetoothLeService.connect(device.getAddress());
						}	
					}
					
				};
			}.start();
		}
	};
	
	Runnable mScannRunnable = new Runnable() {
		
		@Override
		public void run() {
			
			Log.e("Listener","#######################mScannRunnable");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			Message msg = new Message() ;
			msg.what = 0 ;
			mHandler.sendMessageDelayed(msg, SCAN_PERIOD) ;
		}
	};
	
	private static final long SCAN_PERIOD = 5000;
	
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mHandler.postDelayed(mScannRunnable, SCAN_PERIOD);
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,IBinder service) {
			AppContext.mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
			if (!AppContext.mBluetoothLeService.initialize()) {
				stopSelf();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			AppContext.mBluetoothLeService = null;
		}
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		getApplicationContext().unbindService(mServiceConnection);	
		unregisterReceiver(mGattUpdateReceiver);
	}
	
	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		return intentFilter;
	}

  public class OnePhoneStateListener extends PhoneStateListener {

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i("liujw", "[Listener]等待接电话:" + incomingNumber);
			if(	AppContext.mBluetoothLeService != null){
				AppContext.mBluetoothLeService.sendMsg("AAB00201000000000000");	
			}
			
			break;
		case TelephonyManager.CALL_STATE_IDLE:
			Log.i("liujw", "[Listener]电话挂断:" + incomingNumber);
			
			if(	AppContext.mBluetoothLeService != null){
				AppContext.mBluetoothLeService.sendMsg("AAB00200000000000000");	
			}
			
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.i("liujw", "[Listener]通话中:" + incomingNumber);
			if(	AppContext.mBluetoothLeService != null){
				AppContext.mBluetoothLeService.sendMsg("AAB00200000000000000");	
			}
			break;
		}
		super.onCallStateChanged(state, incomingNumber);
	}
}
	
	
	public class SmsReciver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			SmsMessage msg = null;
			if (null != bundle) {
				Object[] smsObj = (Object[]) bundle.get("pdus");
				for (Object object : smsObj) {
					msg = SmsMessage.createFromPdu((byte[]) object);
		    		Date date = new Date(msg.getTimestampMillis());//时间
	                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	                String receiveTime = format.format(date);
					System.out.println("number:" + msg.getOriginatingAddress()
					+ "   body:" + msg.getDisplayMessageBody() + "  time:"
							+ msg.getTimestampMillis());
					
					/*if (msg.getOriginatingAddress().equals("10086")) {
						
					}*/
					
					Log.e("liujw","#####################msg.getOriginatingAddress() "+msg.getOriginatingAddress());
					
					if(	AppContext.mBluetoothLeService != null){
						AppContext.mBluetoothLeService.sendMsg("AAB00101000000000000");	
					}
					
				}
			}
		}

	}

}
