package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.DebugManager.getCurrentState;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
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
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import kotlin.jvm.functions.Function3;

public class QuizService extends Service {
	
	QuizThread thread;
	
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
		
		puts(getCurrentState()+" thread name="+Thread.currentThread().getName());
		thread = new QuizThread();
		thread.start();
		
		return START_NOT_STICKY;
	}
	
	@Override
	public void onDestroy() {
		thread.cancel();
	}
	
	public class QuizThread extends Thread {
		public static final String
			QTHREAD_ACTION_CLICKED="qthread_action_clicked",
			QTHREAD_EXTRA_CHOICE="qthread_extra_choice";
		boolean onActive = true;
		private final Context context = getApplicationContext();
		ArrayList<String> e = new ArrayList<>(), j = new ArrayList<>();
		
		@Override
		public void run() {
			sendBroadcast(TestFragment.ViewName.Mondaibun, "読み込み中");
			
			synchronized (this) {
				context.registerReceiver(new DrawReceiver(clickHandler), new IntentFilter(QTHREAD_ACTION_CLICKED));
				
				puts(getCurrentState()+" thread name="+Thread.currentThread().getName());
				//これを定期的に見る必要がある。
				if (!onActive) stopSelf();
				Function3<WordPhraseData, ArrayList<String>, ArrayList<String>, Void> addF = (wordPhraseData, stringE, stringsJ) -> {
					Arrays.stream(wordPhraseData.e).filter(Objects::nonNull).forEach(stringE::add);
					Arrays.stream(wordPhraseData.j).filter(Objects::nonNull).forEach(stringsJ::add);
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
				setMondai();
			}
			//サービス終了
			stopSelf();
		}
		
		private void setMondai(){
			Random random=new Random();
			int ansChoice=random.nextInt(4)+1;
			int ansNum=random.nextInt(e.size());
			int choice1Num=random.nextInt(e.size());
			int choice2Num=random.nextInt(e.size());
			int choice3Num=random.nextInt(e.size());
			int choice4Num=random.nextInt(e.size());
			sendBroadcast(TestFragment.ViewName.Mondaibun, e.get(ansNum));
			sendBroadcast(TestFragment.ViewName.Select1, j.get(choice1Num));
			sendBroadcast(TestFragment.ViewName.Select2, j.get(choice2Num));
			sendBroadcast(TestFragment.ViewName.Select3, j.get(choice3Num));
			sendBroadcast(TestFragment.ViewName.Select4, j.get(choice4Num));
		}
		
		private Handler clickHandler = new Handler(Looper.getMainLooper()){
			//main thread
			@Override
			public void handleMessage(Message msg){
				puts(getCurrentState() + " thread name=" + Thread.currentThread().getName());
				int choice=msg.getData().getInt(QTHREAD_EXTRA_CHOICE,-1);
				puts("receive choice="+choice);
				setMondai();
			}
		};
		
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