package com.gukos.bokotan;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class QuizService extends Service {
	
	MeasureThread thread;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int requestCode = intent.getIntExtra("REQUEST_CODE",0);
		Context context = getApplicationContext();
		String channelId = "default";
		String title = context.getString(R.string.app_name);
		
		PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Notification Channel 設定
		NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_MIN);
		
		if (notificationManager != null) {
			notificationManager.createNotificationChannel(channel);
			
			Notification notification = new Notification.Builder(context, channelId)
				.setContentTitle(title)
				// android標準アイコンから
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setContentText("MyApplication")
				.setAutoCancel(true)
				.setContentIntent(pendingIntent)
				.setWhen(System.currentTimeMillis())
				.build();
			
			startForeground(1, notification);
		}
		
		thread = new MeasureThread();
		thread.start();
		
		return START_NOT_STICKY;
	}
	@Override
	public void onDestroy(){
		thread.cancel();
	}
	
	private class MeasureThread extends Thread {
		boolean onActive = true;
		public MeasureThread() {
		}
		
		public void run() {
			Intent intent = new Intent(getApplication(), QuizService.class);
			intent.putExtra("REQUEST_CODE", 1);
			
			synchronized (this) {
				for (int i = 0; i < 10; i++) {
					Log.d("debug", " i = " + i);
					try {
						wait(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(!onActive) break;
				}
			}
			//サービス終了
			stopSelf();
		}
		public void cancel(){
			onActive = false;
			stopSelf();
		}
	}
}