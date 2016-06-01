package com.bk.listerservice.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bk.listerservice.app.AppContext;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class SMSService extends Service{

	public final static String ACTION_RECEIVER = "com.gw.smstransmit.action.smsreceiver";
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private SmsObserver smsObserver ;

	public void onCreate(){
		smsObserver = new SmsObserver(this, smsHandler);  
        getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);
        IntentFilter filter = new IntentFilter(ACTION_RECEIVER);
        registerReceiver(smsReceiver, filter);
	}
	
	public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	public Handler smsHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			
		}
	};
	
	class SmsObserver extends ContentObserver {

		public SmsObserver(Context context, Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			//Toast.makeText(SMSService.this, "来短信啦啦啦啦啦啦啦啦啦", 2).show() ;
			
			if(	AppContext.mBluetoothLeService != null){
				AppContext.mBluetoothLeService.sendMsg("AAB00101000000000000");	
			}
			
            getSmsFromPhone();  
		}
	}
	
	BroadcastReceiver smsReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			
			switch (bundle.getInt("type")) {
			case 1:
				break;
			case 2:
				break;

			default:
				break;
			}
		}
	};
	
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	
	public void getSmsFromPhone() {
		ContentResolver cResolver = getContentResolver();
		String[] projection = new String[] { "_id", "address", "person", "date", "type", "body" };//
		String where = " address='10068' and read=?";
		String[] args = new String[]{"0"}; 
		Cursor cursor = cResolver.query(SMS_INBOX, projection, where, args, "date desc");
		if (null == cursor){
			return;
		}
		while (cursor.moveToNext()) {
			String number = cursor.getString(cursor.getColumnIndex("address"));//手机号
			String name = cursor.getString(cursor.getColumnIndex("person"));//联系人姓名列表
			String body = cursor.getString(cursor.getColumnIndex("body"));
			
			ContentValues values = new ContentValues();
			values.put("read", 1);
			cResolver.update(SMS_INBOX, values, "_id=?", new String[]{cursor.getString(0)});
			
			//获取自己短信服务号码中的验证码
			Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
			Matcher matcher = pattern.matcher(body);
			if (matcher.find()) {
				String res = matcher.group().substring(1, 11);
				Log.d("liujw", res);
			}
		}
		cursor.close();
	}
	
}
