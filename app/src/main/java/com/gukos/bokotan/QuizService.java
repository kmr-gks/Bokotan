package com.gukos.bokotan;


import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import kotlin.jvm.functions.Function3;

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
		private final Context context = getApplicationContext();
		
		@Override
		public void run() {
			sendBroadcast(TestFragment.ViewName.Mondaibun, "読み込み中");
			
			synchronized (this) {
				//これを定期的に見る必要がある。
				if (!onActive) stopSelf();
				ArrayList<String> e = new ArrayList<>(), j = new ArrayList<>();
				Random random=new Random();
				Function3<WordPhraseData, ArrayList<String>, ArrayList<String>, Void> addF = (wordPhraseData, stringE, stringsJ) -> {
					Arrays.stream(wordPhraseData.e).forEach(stringE::add);
					Arrays.stream(wordPhraseData.j).forEach(stringsJ::add);
					return null;
				};
				//単語データ読み取り
				for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
					addF.invoke(new WordPhraseData(PasstanWord + q, context), e, j);
				for (var q : new String[]{"1q", "p1q"})
					addF.invoke(new WordPhraseData(TanjukugoWord + q, context), e, j);
				for (var q : new String[]{"1q", "p1q"})
					addF.invoke(new WordPhraseData(TanjukugoEXWord + q, context), e, j);
				for (var q : new String[]{"00", "08", "1", "2", "3"})
					addF.invoke(new WordPhraseData(YumeWord + q, context), e, j);
				if (!onActive) stopSelf();
				sendBroadcast(TestFragment.ViewName.Mondaibun, e.get(1));
				sendBroadcast(TestFragment.ViewName.Select1, j.get(1));
				sendBroadcast(TestFragment.ViewName.Select2, j.get(2));
				sendBroadcast(TestFragment.ViewName.Select3, j.get(3));
				sendBroadcast(TestFragment.ViewName.Select4, j.get(4));
			}
			//サービス終了
			stopSelf();
		}
		
		public void cancel() {
			onActive = false;
			stopSelf();
		}
		
		public void sendBroadcast(TestFragment.ViewName viewName, String text) {
			Intent broadcastIntent =
				new Intent(TestFragment.QUIZ_ACTION_UI)
					.putExtra(TestFragment.QUIZ_UI_TEXT, text)
					.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
			context.sendBroadcast(broadcastIntent);
		}
	}
}