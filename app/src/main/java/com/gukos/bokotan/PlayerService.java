package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPath;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_PROPERTIES;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.Text;
import static com.gukos.bokotan.PlayerFragment.isInitialized;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PlayerService extends Service {
	MeasureThread thread;
	Context context;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("MyService", "onStartCommand()");
		Log.d("MyService", "Thread name = " + Thread.currentThread().getName());
		
		int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
		context = getApplicationContext();
		String channelId = "default";
		String title = context.getString(R.string.app_name);
		
		PendingIntent pendingIntent =
			PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
		
		NotificationManager notificationManager =
			(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		// Notification Channel 設定
		NotificationChannel channel = new NotificationChannel(
			channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
		
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
			
			// startForeground
			startForeground(1, notification);
			
		}
		
		thread = new MeasureThread(this);
		thread.start();
		
		Service thisService= this;
		HandlerThread handlerThread=new HandlerThread("test");
		handlerThread.start();
		handler=new Handler(handlerThread.getLooper()){
			@Override
			public void handleMessage(@NonNull Message msg) {
				String messageType=msg.getData().getString(PLAYERSERVICE_MESSAGE_TYPE);
				if (messageType.equals(PLAYERSERVICE_MESSAGE_STOP)){
					//サービス停止
					puts("サービス停止");
					isPlaying=false;
					if (mediaPlayer != null) {
						if(mediaPlayer.isPlaying()) mediaPlayer.stop();
						mediaPlayer.reset();
						mediaPlayer.release();
					}
					thisService.stopSelf();
				}
			}
		};
		handler.post(() -> {
			while (true) {
				synchronized (isInitialized) {
					if (isInitialized) break;
				}
				sleep(100);
			}
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng,"読み込み中");
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn, "読み込み中");
			isPlaying=true;
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(PLAYERSERVICE_ACTION));
			
			for (var q : new String[]{"1q", "p1q"})
				new WordPhraseData(PasstanWord + q, context, quizWordDataList, passTan, q);
			onPlay();
		});
		return START_NOT_STICKY;
	}
	
	public static final String
		PLAYERSERVICE_ACTION ="playerservice_action_stop",
		PLAYERSERVICE_MESSAGE_TYPE="playerservice_message_type",
		PLAYERSERVICE_MESSAGE_STOP="playerservice_message_stop";
	
	Handler handler;
	MediaPlayer mediaPlayer;
	ArrayList<QuizCreator.QuizWordData> quizWordDataList = new ArrayList<>();
	int now=1;
	String path;
	boolean isPlaying;
	
	private void onPlay() {
		//リソースの開放
		if (mediaPlayer != null) {
			if(mediaPlayer.isPlaying()) mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
		}
		if (isPlaying) {
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.genzai, "No." + quizWordDataList.get(now).no);
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng, quizWordDataList.get(now).e);
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn, quizWordDataList.get(now).j);
			path = getPath(passTan, "1q", word, english, now);
			mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener((mp) -> handler.post(this::onPlay));
			now++;
		}
	}
	
	private void sendBroadcastTextChange(PlayerFragment.PlayerViewName viewName, String text) {
		//printCurrentState(",view=" + viewName + ",text=" + text);
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, Text)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}


class MeasureThread extends Thread{
	Service service;
	MeasureThread(Service service){
		this.service=service;
	}
	public void run() {
		Log.d("MeasureThread run", "Thread name = "
			+ Thread.currentThread().getName());
		
			for (int i = 0; i < 10; i++) {
				puts(" i = " + i);
				MyLibrary.sleep(1000);
			}
		service.stopSelf();
	}
}