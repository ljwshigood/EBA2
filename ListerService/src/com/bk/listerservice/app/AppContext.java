package com.bk.listerservice.app;


import java.util.HashMap;

import android.app.Application;
import android.bluetooth.BluetoothGatt;

import com.bk.listerservice.blue.BluetoothLeService;

public class AppContext extends Application {

	public static String mDeviceAddress;

	int[] mRingResId = new int[10];
	
	String[] mRingString = new String[10];
	
	public static  boolean isAlarm = true ;
	
	
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
	
	@Override
	public void onCreate() {
		super.onCreate();
	}

}
