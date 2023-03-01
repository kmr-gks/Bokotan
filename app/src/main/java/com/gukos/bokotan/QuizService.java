package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
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
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import kotlin.jvm.functions.Function3;

public class QuizService extends Service {
	
	private HandlerThread handlerThread;
	private Handler handler;
	private Context context;
	public static final String
		QTHREAD_ACTION_CLICKED = "qthread_action_clicked",
		QTHREAD_EXTRA_CHOICE = "qthread_extra_choice";
	private boolean onActive = true;
	private ArrayList<String> e = new ArrayList<>(), j = new ArrayList<>();
	private final Random random=new Random();
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	//メインスレッド
	public int onStartCommand(Intent intent, int flags, int startId) {
		printCurrentState();
		int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
		context = getApplicationContext();
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
		
		handlerThread = new HandlerThread("QuizHandlerThread");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			//looperを指定しているので、クイズスレッド
			@Override
			public void handleMessage(Message message) {
				printCurrentState();
				int choice = message.getData().getInt(QTHREAD_EXTRA_CHOICE, -1);
				puts("receive choice=" + choice);
				setMondai();
			}
		};
		//handler.post()はクイズスレッド
		handler.post(() -> {
			//クイズを開始する処理
			printCurrentState();
			sendBroadcast(TestFragment.ViewName.Mondaibun, "読み込み中");
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(QTHREAD_ACTION_CLICKED));
			
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
			
			//サービス終了
			stopSelf();
		});
		
		return START_NOT_STICKY;
	}
	
	//クイズスレッド
	private void setMondai() {
		printCurrentState();
		int ansChoice = random.nextInt(4) + 1;
		int ansNum = random.nextInt(e.size());
		int choice1Num = random.nextInt(e.size());
		int choice2Num = random.nextInt(e.size());
		int choice3Num = random.nextInt(e.size());
		int choice4Num = random.nextInt(e.size());
		sendBroadcast(TestFragment.ViewName.Mondaibun, e.get(ansNum));
		sendBroadcast(TestFragment.ViewName.Select1, j.get(choice1Num));
		sendBroadcast(TestFragment.ViewName.Select2, j.get(choice2Num));
		sendBroadcast(TestFragment.ViewName.Select3, j.get(choice3Num));
		sendBroadcast(TestFragment.ViewName.Select4, j.get(choice4Num));
	}
	
	public void cancel() {
		onActive = false;
		stopSelf();
	}
	
	//TestFragment内のviewに表示する文字を変更させるためのintentを送信する。クイズスレッド
	private void sendBroadcast(TestFragment.ViewName viewName, String text) {
		printCurrentState();
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI)
				.putExtra(TestFragment.QUIZ_UI_TEXT, text)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	@Override
	public void onDestroy() {
		cancel();
	}
}