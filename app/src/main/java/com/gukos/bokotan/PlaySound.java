package com.gukos.bokotan;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class PlaySound extends Service {
	static SoundPool sp=new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(1).build();
	static int nSound;
	static boolean bKeepPlaying=true;
	String strLocalQ="p1q";
	String tag="com.gukos.PlaySound";
	int nLocalMondaiTangoNum=0;
	public PlaySound() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		startForeground(1,new Notification());
		Log.d(tag,"thread");
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d(tag,"run");
				//Soundpool
				sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
					@Override
					public void onLoadComplete(SoundPool soundPool, int i, int i1) {
						Log.d(tag,"before play");
						soundPool.play(i,1f,1f,1,0,1f);
						try {
							//Thread.sleep(pausetime[nLocalMondaiTangoNum%20]);
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							//((TextView)findViewById(R.id.textView)).setText("NOW"+nLocalMondaiTangoNum+e.getMessage());
						}
						setMondaiBun();
					}
				});
			}
		}).start();
		setMondaiBun();
		return START_NOT_STICKY;
	}
	public void setMondaiBun(){
		Log.d(tag,"setMondaibun"+nLocalMondaiTangoNum);
		nLocalMondaiTangoNum++;
		//((TextView)findViewById(R.id.textView)).setText("NOW"+nLocalMondaiTangoNum);
		sp.unload(nSound);
		nSound=sp.load("/storage/emulated/0/Download/data/" + strLocalQ + '/' + String.format("%04d", nLocalMondaiTangoNum)  + "英.mp3",1);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}