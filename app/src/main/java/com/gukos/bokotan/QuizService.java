package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class QuizService extends Service {
	
	MeasureThread thread;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
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
	public void onDestroy() {
		thread.cancel();
	}
	
	private class MeasureThread extends Thread {
		boolean onActive = true;
		private Context context = getApplicationContext();
		
		public MeasureThread() {
		}
		
		public void run() {
			sendBroadcast(TestFragment.ViewName.Mondaibun, "読み込み中");
			Intent intent = new Intent(getApplication(), QuizService.class);
			intent.putExtra("REQUEST_CODE", 1);
			
			String[] e,j;
			
			synchronized (this) {
				if (!onActive) stopSelf();
				WordPhraseData w = new WordPhraseData(PasstanWord + "1q", context);
				e=w.e;
				j=w.j;
				MyLibrary.sleep(500);
				if (!onActive) stopSelf();
				sendBroadcast(TestFragment.ViewName.Mondaibun,e[1]);
				sendBroadcast(TestFragment.ViewName.Select1,j[1]);
				sendBroadcast(TestFragment.ViewName.Select2,j[2]);
			}
			//サービス終了
			stopSelf();
		}
		
		public void cancel() {
			onActive = false;
			stopSelf();
		}
		
		public void sendBroadcast(int viewName,String message) {
			Intent broadcastIntent = new Intent();
			broadcastIntent.putExtra("message", message);
			broadcastIntent.putExtra("viewName",viewName);
			broadcastIntent.setAction("UPDATE_ACTION");
			context.sendBroadcast(broadcastIntent);
			puts("PUT message"+broadcastIntent.getStringExtra("message")+"viewName"+broadcastIntent.getIntExtra("viewName",-1));
		}
	}
}