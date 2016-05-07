package com.bk.listerservice.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bk.listerservice.app.AppContext;
import com.bk.listerservice.blue.BluetoothLeService;

public class ListenerService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void createPhoneListener() {
		TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephony.listen(new OnePhoneStateListener(),PhoneStateListener.LISTEN_CALL_STATE);
	}
	
	private SmsReciver mSMSRecevier ;
	
	private BluetoothAdapter mBluetoothAdapter;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Log.e("liujw","####################ListenerService onCreate");
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		this.getApplicationContext().bindService(gattServiceIntent,mServiceConnection, BIND_AUTO_CREATE);
		
		createPhoneListener();
		
		mSMSRecevier = new SmsReciver() ;
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(999);
		registerReceiver(mSMSRecevier, intentFilter);
		
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		
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
		unregisterReceiver(mSMSRecevier) ;
		getApplicationContext().unbindService(mServiceConnection);	
	}
	

  public class OnePhoneStateListener extends PhoneStateListener {

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i("liujw", "[Listener]等待接电话:" + incomingNumber);
			AppContext.mBluetoothLeService.sendMsg("AAB00200000000000000");
			break;
		case TelephonyManager.CALL_STATE_IDLE:
			Log.i("liujw", "[Listener]电话挂断:" + incomingNumber);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.i("liujw", "[Listener]通话中:" + incomingNumber);
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
					
					AppContext.mBluetoothLeService.sendMsg("AAB00100000000000000");
					
				}
			}
		}

	}

}
