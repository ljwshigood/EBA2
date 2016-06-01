/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.bk.listerservice.ui;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bk.listerservice.R;
import com.bk.listerservice.app.AppContext;
import com.bk.listerservice.blue.BluetoothLeService;
import com.bk.listerservice.impl.IDismissListener;
import com.bk.listerservice.utils.SharePerfenceUtil;
import com.bk.listerservice.view.BKProgressDialog;

public class DeviceScanActivity extends BaseActivity implements OnClickListener ,IDismissListener{

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean mScanning;
	private Handler mHandler;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 10000;
	
	private ListView mLvBlueDevice;

	private Context mContext;

	private BluetoothDevice mDevice;

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if (mDialogProgress != null) {
					mDialogProgress.dismiss();
				}
				
				SharePerfenceUtil.setParam(mContext, "device_address", device.getAddress()) ;
				
				if (AppContext.mBluetoothLeService != null) {
					displayGattServices(AppContext.mBluetoothLeService.getSupportedGattServices(),device.getAddress());
				}
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
		
		finish() ; 
	}

	private ImageView mIvBack;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		super.onCreate(savedInstanceState);
		
		mContext = DeviceScanActivity.this ;
		mHandler = new Handler();

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported,Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		setContentView(R.layout.activity_device_scan);
		mLvBlueDevice = (ListView) findViewById(R.id.lv_scan_device);
		mContext = this;
		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		mLvBlueDevice.setAdapter(mLeDeviceListAdapter);
		mLvBlueDevice.setOnItemClickListener(mLeDeviceListAdapter);

		initView();

		
		if(AppContext.mBluetoothLeService != null){
			AppContext.mBluetoothLeService.setmIDismissListener(this);
		}
		
		setTitle(mContext.getString(R.string.scanner));
		
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private TextView mTvTitleInfo;

	private void setTitle(String info) {
		mTvTitleInfo = (TextView) findViewById(R.id.tv_main_info);
		mTvTitleInfo.setText(info);
	}

	private TextView mTvScan ;
	
	private void initView() {
		mTvScan = (TextView)findViewById(R.id.tv_scan) ;
		mIvBack = (ImageView) findViewById(R.id.iv_back);
		mIvBack.setOnClickListener(this);
		mTvScan.setOnClickListener(this) ;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!mBluetoothAdapter.isEnabled()) {
			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}
		scanLeDevice(true);
	}
	


	@Override
	protected void onDestroy() {
		scanLeDevice(false);
		if (mDialogProgress != null) {
			mDialogProgress.dismiss();
			mDialogProgress = null;
		}
		unregisterReceiver(mGattUpdateReceiver);
		
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
	}
	
	private ProgressDialog dialog;
	
	private Handler mDialogHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			if(dialog != null){
				dialog.dismiss() ;
				dialog = null ;	
			}
		
		};
	};
	
	/**
	 * @param enable
	 */
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			
			
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
		
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	private ArrayList<BluetoothDevice> mLeDevices;

	private class LeDeviceListAdapter extends BaseAdapter implements OnItemClickListener {

		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.list_item_device, null);
				viewHolder = new ViewHolder();
				viewHolder.ivDevice = (ImageView) view.findViewById(R.id.iv_device);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.tv_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);

			return view;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
			
			final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
			mDevice = device;
			if (device == null)
				return;
			if (AppContext.mBluetoothLeService != null) {
				AppContext.mBluetoothLeService.connect(device.getAddress());
			}
			showProgressBarDialog();
		}
	}

	public BKProgressDialog mDialogProgress = null;

	private void showProgressBarDialog() {
		String info = mContext.getString(R.string.device_connected_title);
		mDialogProgress = new BKProgressDialog(mContext, R.style.MyDialog,info);
		mDialogProgress.show();
	}

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					//if(device.getName().equalsIgnoreCase("CSR ANCS")){
						mLeDeviceListAdapter.addDevice(device);
						mLeDeviceListAdapter.notifyDataSetChanged();	
					//}
					
				}
			});
		}
	};
	
	

	public boolean iteraGattHashMap(Map map, String address) {
		Iterator iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (key.toString().equals(address)) {
				return true;
			}
		}
		return false;
	}

	static class ViewHolder {
		TextView deviceName;
		ImageView ivDevice;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_back:
			finish();
			break;
		case R.id.tv_scan:
			scanLeDevice(true);
			break ;
		default:
			break;
		}
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		return intentFilter;
	}

	@Override
	public void dismiss() {
		if (mDialogProgress != null) {
			mDialogProgress.dismiss();
		}
	}

}