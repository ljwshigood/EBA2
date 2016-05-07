package com.bk.listerservice.ui;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bk.listerservice.R;
import com.bk.listerservice.app.AppContext;
import com.bk.listerservice.blue.BluetoothLeService;
import com.bk.listerservice.service.ListenerService;

public class MainActivity extends Activity implements OnClickListener{
	
	private ImageView mIvSet ;
	
	private Context mContext ;
	
	private CheckBox mCbLed ;
	
	private CheckBox mCbMotor ;
	
	private void initView(){
		mCbLed = (CheckBox)findViewById(R.id.cb_led);
		mCbMotor = (CheckBox)findViewById(R.id.cb_motor);
		mIvSet = (ImageView)findViewById(R.id.iv_set) ;
		mIvSet.setOnClickListener(this);
		mCbLed.setOnClickListener(this);
		mCbMotor.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}
	
	  private  IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
	        return intentFilter;
	    }
	
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String  data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA) ;
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            	
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            	
            	
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            	
            	
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            	if(data.startsWith("AAC101")){
            		Intent intent1 = new Intent(mContext,AlarmActivity.class);
            		startActivity(intent1);
            	}else if(data.startsWith("AAC102")){
            		Intent intent1 = new Intent(mContext,AlarmActivity.class);
            		startActivity(intent1);
            	}
            }
            
        }
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = MainActivity.this ;
		initView();
		
		Intent intent = new Intent(mContext,ListenerService.class);
		startService(intent);
		
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
		case R.id.iv_set:
			Intent intent = new Intent(mContext,DeviceScanActivity.class);
			startActivity(intent);
			break;
		case R.id.cb_led :
			if(mCbLed.isChecked()){
				AppContext.mBluetoothLeService.sendMsg("AAB10100000000000000") ;	
			}else{
				AppContext.mBluetoothLeService.sendMsg("AAB10200000000000000") ;
			}
			
			break ;
		case R.id.cb_motor :
			if(mCbMotor.isChecked()){
				AppContext.mBluetoothLeService.sendMsg("AAB10300000000000000") ;		
			}else{
				AppContext.mBluetoothLeService.sendMsg("AAB10400000000000000") ;
			}
		
			break ;
		default:
			break;
		}
	}


}
