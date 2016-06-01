package com.bk.listerservice.service;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.IBinder;
import android.os.Vibrator;

import com.bk.listerservice.R;
import com.bk.listerservice.bean.MediaPlayerBean;

public class BgMusicControlService extends Service {
	
	public static final String HELP_SOUND_FILE = "help_sound_setting";
	public static final String CTL_ACTION = "com.android.iwit.IWITARTIS.CTL_ACTION";
	MyReceiver serviceReceiver ;
	AudioManager mAudioManager ;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}


	public MediaPlayerBean iteratorMediaPlayer(
			HashMap<String, MediaPlayerBean> hashMapMediaPlayer, String address) {
		Iterator iter = hashMapMediaPlayer.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			if (key.toString().equals(address)) {
				return (MediaPlayerBean) val;
			}
		}
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		serviceReceiver = new MyReceiver();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		IntentFilter filter = new IntentFilter();
		filter.addAction(CTL_ACTION);
		registerReceiver(serviceReceiver, filter);
	}

	private MediaPlayerBean createMediaPlayer(int id, final String address) {
		
		MediaPlayer mediaPlayer = null;
		mediaPlayer = MediaPlayer.create(getBaseContext(), R.raw.linkloss);
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mediaPlayer.setVolume(5, 5);
		mediaPlayer.start();
		
		final MediaPlayerBean bean = new MediaPlayerBean();
		bean.setMediaPlayer(mediaPlayer);

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				/*if (bean != null) {
					bean.increase();
					if (playCount <= bean.getCount()) {
						if (mp != null) {
							mp.release();
							mp = null;
							if(vibrator != null){
								vibrator.cancel();
							}
						}
					} else {
						mp.seekTo(0);
						mp.start();
					}
				}*/
			}
		});
		return bean;
	}

	private Vibrator vibrator ;
	public static boolean isPause = true ;
	
	private static MediaPlayerBean mMediaPlayer = null  ;

	public static MediaPlayerBean getmMediaPlayer() {
		return mMediaPlayer;
	}

	public static void setmMediaPlayer(MediaPlayerBean mMediaPlayer) {
		BgMusicControlService.mMediaPlayer = mMediaPlayer;
	}

	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(final Context context, Intent intent) {
			int control = intent.getIntExtra("control", -1);
			String address = intent.getStringExtra("address");
			
			switch (control) {
			case 1:
				
				if(mMediaPlayer != null){
					releaseMusic(mMediaPlayer) ;
				}
				
				mMediaPlayer = createMediaPlayer(R.raw.linkloss,address) ;
				
				if (!mMediaPlayer.getMediaPlayer().isPlaying()) {
					mMediaPlayer.getMediaPlayer().start();
				}
				
				break;
			case 2:
				if(mMediaPlayer != null){
					releaseMusic(mMediaPlayer) ;
				}
				break;
			case 3:
				
				break;
			}
		}
	}

	private void releaseMusic(MediaPlayerBean mediaBean){
		if(mediaBean != null){
			MediaPlayer mediaPlayer = mediaBean.getMediaPlayer();
			if (mediaPlayer != null) {
				mediaPlayer.release();
				mediaPlayer = null;
			}	
		}
		mMediaPlayer = null ;
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(serviceReceiver);
	}

}
