package com.bk.listerservice.utils;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OnePhoneStateListener extends PhoneStateListener {

	@Override
	public void onCallStateChanged(int state, String incomingNumber) {

		switch (state) {
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i("liujw", "[Listener]等待接电话:" + incomingNumber);
			
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
