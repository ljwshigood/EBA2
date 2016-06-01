package com.bk.listerservice.app;


import java.util.HashMap;

import android.app.Application;
import android.bluetooth.BluetoothGatt;

import com.baidu.batsdk.BatSDK;
import com.bk.listerservice.bean.NotificationBean;
import com.bk.listerservice.blue.BluetoothLeService;

public class AppContext extends Application {

	public static String mDeviceAddress;

	int[] mRingResId = new int[10];
	
	String[] mRingString = new String[10];
	
	public static  boolean isAlarm = true ;
	
	public static NotificationBean mNotificationBean = new NotificationBean();
	
	public static boolean isShow = true ;
	
	public static  HashMap<String, BluetoothGatt> mHashMapConnectGatt = new HashMap<String, BluetoothGatt>();
	
	public static int mCurrentTab = 0 ;
	
	public static BluetoothLeService mBluetoothLeService;
	
	public static boolean isEndMusic ;
	
	public int mBatteryValues = 90 ;
	
	public static int[] mDeviceStatus = {0,0};
	
	public boolean isExistDeviceConnected = false;
	
	public static double mLongitude = 0.0 ;
	
	public static double mLatitude = 0.0;
	
	public static boolean isStart = true ;
	
	public static boolean isFlash = true;
	
	public static boolean isBlueDisconnect = false ;
	
	@Override
	public void onCreate() {
		
		BatSDK.init(this, "cc280be87a7bebd8");
		BatSDK.setCollectScreenshot(true);
		BatSDK.setSendPrivacyInformation(true) ;
		
		super.onCreate();
	}

}
