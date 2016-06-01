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
 */

package com.bk.listerservice.blue;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.bk.listerservice.impl.IDismissListener;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.wearme.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.wearme.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.wearme.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.wearme.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.wearme.bluetooth.le.EXTRA_DATA";

    
    public final static UUID RX_SERVICE_UUID =  UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb") ;
    
    public final static UUID RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb") ;
    
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(GattAttributes.HEART_RATE_MEASUREMENT);

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
                
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED,gatt.getDevice());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                System.out.println("onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_READ_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };
    
    public final static String ACTION_READ_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_READ_DATA_AVAILABLE";

	public IDismissListener mIDismissListener ;
	
	public IDismissListener getmIDismissListener() {
		return mIDismissListener;
	}

	public void setmIDismissListener(IDismissListener mIDismissListener) {
		this.mIDismissListener = mIDismissListener;
	}

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,BluetoothDevice device) {
        final Intent intent = new Intent(action);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device) ;
        sendBroadcast(intent);
    }
    
    private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }else if(BATTERY_CHAR_UUID.equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
        	String battery;
			try {
				battery = new String(data,"UTF-8");
				intent.putExtra(EXTRA_DATA, String.valueOf(data[0]));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
         
        }else {
        	
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        System.out.println("device.getBondState=="+device.getBondState());
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    
	public void disconnect() {
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		mBluetoothGatt.disconnect();
	}


    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    
    public static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    
    public static final UUID BATTERY_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    
    public void readBatteryCharacteristic(){
		if (mBluetoothAdapter == null || mBluetoothGatt == null) {
			Log.w(TAG, "BluetoothAdapter not initialized");
			return;
		}
		BluetoothGattService alarmService = null;
		
		BluetoothGattCharacteristic batteryCharacter = null;
		
		if(mBluetoothGatt != null){
			alarmService = mBluetoothGatt.getService(BATTERY_SERVICE_UUID);
		}
		
		if(alarmService != null){
			batteryCharacter = alarmService.getCharacteristic(BATTERY_CHAR_UUID);		
		}
		
		if(batteryCharacter != null){
			mBluetoothGatt.readCharacteristic(batteryCharacter);
		}
	}

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    
	public void writeRXCharacteristic(byte[] value) {
		if(mBluetoothGatt == null){
			return ;
		}
		
		BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
		if (RxService == null) {
			//broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
		if (RxChar == null) {
			//showMessage("Rx charateristic not found!");
			//broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			return;
		}
		
		RxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT); //WRITE_TYPE_NO_RESPONSE
		RxChar.setValue(value);
		
		boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
		
		if(!status){
			mHandler.postDelayed(runnable, 500);
		}
		
		Log.d(TAG, "write TXchar - status=" + status);
	}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			sendMsg(mMessage);
		}
	};
	
	private Handler mHandler =new Handler() ;
	


    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
    
 /*	public void sendMsg(String msg) {
 		byte[] value;
 		try {
 			String message = FormatUtils.toStringHex(msg);
 			value = message.getBytes("UTF-8");
 			writeRXCharacteristic(value);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 	*/
    
    private String mMessage = "" ;
    
 	public void sendMsg(String msg) {
 		mMessage = msg ;
 		byte[] value;
 		/*String message = EncriptyUtils.toStringHex(msg);
		value = message.getBytes("UTF-8");*/
 		
 		String str = msg.toLowerCase(Locale.getDefault());
		value = hexToBytes(str);
		
		Log.e("liujw","####################sendMsg : "+msg);
		
		String aa = "" ;
		for(int i = 0 ;i < value.length ;i++){
			aa += value[i] ;
		}
		
		Log.d("liujw","####################sendMsg : "+aa);
		
 		//[-86, -80, 1, 1, 0, 0, 0, 0, 0, 0]
 		
 		//value = hexStringToBytes(msg);
 		//value = parseHexStringToBytes("0x"+str);
		writeRXCharacteristic(value);
 	}
 	
 	 public byte[] parseHexStringToBytes(String paramString)
 	  {
 	    String str = paramString.substring(2).replaceAll("[^[0-9][a-f]]", "");
 	    byte[] arrayOfByte = new byte[str.length() / 2];
 	    for (int i = 0; ; i++)
 	    {
 	      if (i >= arrayOfByte.length)
 	        return arrayOfByte;
 	      arrayOfByte[i] = Long.decode("0x" + str.substring(i * 2, 2 + i * 2)).byteValue();
 	    }
 	  }
 	
 	public byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}
 	
 	private static byte charToByte(char c) {
 		
		return (byte) "0123456789ABCDEF".indexOf(c);
	}
	
    public final byte[] hexToBytes(String s){
    	//如果是奇数，就让它补一个零
    	if(s.length()%2!=0){
    		String s1=s.substring(0, s.length()-1);
    		String s2=s.substring(s.length()-1,s.length());
    		s=s1+"0"+s2;
    	}
    	byte[] bytes;
    	bytes=new byte[s.length()/2];
    	for(int i=0;i<bytes.length;i++){
    		bytes[i]=(byte)Integer.parseInt(s.substring(i*2, i*2+2),16);
    	}
    	return bytes;
    }

	public boolean isConnect(){
		boolean isRet = true ;
		if(mBluetoothGatt == null || !mBluetoothGatt.connect()){
			isRet = false ;
		}
		return isRet ;
	}

}
