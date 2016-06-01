package com.bk.listerservice.utils;


import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.bk.listerservice.R;
import com.bk.listerservice.service.BgMusicControlService;
import com.bk.listerservice.ui.MainActivity;

public class AlarmManager {

	private Context mContext;

	private static AlarmManager mInstance;

	private AlarmManager(Context context) {
		this.mContext = context;
	}

	public static AlarmManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new AlarmManager(context);
		}
		return mInstance;
	}


	public boolean DeviceDisconnectAlarm(String address,String alarmInfo) {
		Intent intentDistance = new Intent(BgMusicControlService.CTL_ACTION);
		intentDistance.putExtra("control", 1);
		intentDistance.putExtra("address", address);
		mContext.sendBroadcast(intentDistance);
		return true;
	}

	/**
	 * 
	 * @description: 判断当前应用程序处于前台还是后台
	 */
	public boolean isApplicationBroughtToBackground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	private static final int NOTICE_ID = 1222;

	/**
	 * @description : 报警的notifycation
	 * @param context
	 * @param address
	 * @param string
	 */
	public void notifycationAlarm(Context context, String address, String string) {
		Intent intent = new Intent(context, MainActivity.class);
		final NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.ic_launcher,"AntiLost Alarming", System.currentTimeMillis());
		PendingIntent pendIntent = PendingIntent.getActivity(context, 0,intent, 0);
		notification.setLatestEventInfo(context, "Follow", string, pendIntent);
		manager.notify(NOTICE_ID, notification);
	}

}
