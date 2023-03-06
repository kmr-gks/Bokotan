package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPath;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_PROPERTIES;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.Text;
import static com.gukos.bokotan.PlayerFragment.isInitialized;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;
import static com.gukos.bokotan.WordPhraseData.q_num;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class PlayerService extends Service {
	
	public static final String
		PLAYERSERVICE_EXTRA_MODE = "PLAYERSERVICE_EXTRA_MODE",
		PLAYERSERVICE_EXTRA_BOOK="playerservice_extra_book",
		PLAYERSERVICE_EXTRA_Q="playerservice_extra_q";
	Context context;
	q_num.mode selectMode, nowMode = q_num.mode.word;
	
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("MyService", "onStartCommand()");
		Log.d("MyService", "Thread name = " + Thread.currentThread().getName());
		
		selectMode = (q_num.mode) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_MODE);
		if (selectMode == q_num.mode.phrase) nowMode = q_num.mode.phrase;
		dataBook= (WordPhraseData.DataBook) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_BOOK);
		stringQ=intent.getStringExtra(PLAYERSERVICE_EXTRA_Q);
		
		context = getApplicationContext();
		String channelId = "default";
		String title = context.getString(R.string.app_name);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intent, PendingIntent.FLAG_IMMUTABLE);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Notification Channel 設定
		NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
		
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
		
		Service thisService = this;
		HandlerThread handlerThread = new HandlerThread("test");
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			@Override
			public void handleMessage(@NonNull Message msg) {
				Bundle bundle=msg.getData();
				String messageType = bundle.getString(PLAYERSERVICE_MESSAGE_TYPE);
				switch (messageType) {
					case PLAYERSERVICE_MESSAGE_STOP: {
						//サービス停止
						puts("サービス停止");
						isPlaying = false;
						try {
							if (mediaPlayer != null) {
								mediaPlayer.stop();
								mediaPlayer.reset();
								mediaPlayer.release();
							}
							thisService.stopSelf();
						} catch (Exception exception) {
							;
						}
						break;
					}
					case PLAYERSERVICE_MESSAGE_JPN_SPEED:{
						dPlaySpeedJpn=bundle.getFloat(PLAYERSERVICE_MESSAGE_JPN_SPEED);
						break;
					}
					case PLAYERSERVICE_MESSAGE_ENG_SPEED:{
						dPlaySpeedEng=bundle.getFloat(PLAYERSERVICE_MESSAGE_ENG_SPEED);
						break;
					}
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
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng, "読み込み中");
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn, "読み込み中");
			isPlaying = true;
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(PLAYERSERVICE_ACTION));
			
			String strBookName;
			switch (dataBook){
				default:
				case passTan:{
					strBookName=PasstanWord;
					break;
				}
				case tanjukugo:{
					strBookName=TanjukugoWord;
					break;
				}
				case yumetan:{
					strBookName=YumeWord;
					break;
				}
			}
			puts("strBookName + stringQ="+strBookName + stringQ+",dataBook="+dataBook+",stringq="+stringQ);
			puts("PasstanWord + q="+PasstanWord + "p1q"+",passTan="+passTan+",q=p1q");
			if (dataBook==yumetan){
				//ユメタン:単語のみ
				new WordPhraseData(strBookName + stringQ.substring(1), context, wordDataList, dataBook, stringQ);
				phraseDataList=wordDataList;
			}else {
				if(selectMode== q_num.mode.word) {
					//単語データのみ読み込む
					new WordPhraseData(strBookName + stringQ, context, wordDataList, dataBook, stringQ);
				}else if (selectMode== q_num.mode.phrase){
					//文データのみ読み込む
					String dirpath=null;
					if (strBookName==PasstanWord){
						dirpath="Passtan/Phrase";
					}
					if (strBookName==TanjukugoWord){
						dirpath="TanjukugoEX/Phrase";
					}
					new WordPhraseData(dirpath + stringQ, context, phraseDataList, dataBook, stringQ);
				}else{
					//両方読み込む
					new WordPhraseData(strBookName + stringQ, context, wordDataList, dataBook, stringQ);
					String dirpath=null;
					if (strBookName==PasstanWord){
						dirpath="Passtan/Phrase";
					}
					if (strBookName==TanjukugoWord){
						dirpath="TanjukugoEX/Phrase";
					}
					new WordPhraseData(dirpath + stringQ, context, phraseDataList, dataBook, stringQ);
				}
			}
			onPlay();
		});
		return START_NOT_STICKY;
	}
	
	public static final String
		PLAYERSERVICE_ACTION = "playerservice_action",
		PLAYERSERVICE_MESSAGE_TYPE = "playerservice_message_type",
		PLAYERSERVICE_MESSAGE_STOP = "playerservice_message_stop",
		PLAYERSERVICE_MESSAGE_JPN_SPEED = "playerservice_message_jpn_speed",
		PLAYERSERVICE_MESSAGE_ENG_SPEED = "playerservice_message_eng_speed";
	
	Handler handler;
	MediaPlayer mediaPlayer;
	ArrayList<QuizCreator.QuizWordData> wordDataList = new ArrayList<>(),phraseDataList= new ArrayList<>();
	String path;
	boolean isPlaying;
	float dPlaySpeedEng = 1.5f, dPlaySpeedJpn = 1.5f;
	WordPhraseData.DataBook dataBook=passTan;
	String stringQ="p1q";
	
	private void onPlay() {
		//リソースの開放
		try {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		} catch (Exception exception) {
			;
		}
		if (isPlaying) {
			var list=wordDataList;
			if (nowMode== q_num.mode.phrase) list=phraseDataList;
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.genzai, "No." + list.get(now).no);
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng, list.get(now).e);
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn, list.get(now).j);
			puts("getPath:"+dataBook+","+stringQ+","+nowMode+","+nowLang+","+now);
			if (dataBook==tanjukugo) {
				path = getPath(dataBook, "tanjukugo"+stringQ, nowMode, nowLang, now);
			}else path = getPath(dataBook, stringQ, nowMode, nowLang, now);
			puts("path="+path);
			sendBroadcastTextChange(PlayerFragment.PlayerViewName.path, path);
			try {
				mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
				mediaPlayer.start();
				mediaPlayer.setOnCompletionListener((mp) -> handler.post(this::onPlay));
				
				if (nowLang == english) {
					//現在英語:日本語にする
					nowLang = japanese;
					mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(dPlaySpeedEng));
				}
				else {
					//現在日本語:英語にする
					nowLang = english;
					if (selectMode == q_num.mode.wordPlusPhrase) {
						//単語->文
						if (nowMode == q_num.mode.word) {
							//現在単語だった
							nowMode = q_num.mode.phrase;
						}
						else {
							//文だった
							nowMode = q_num.mode.word;
							now++;
						}
					}
					else {
						//ずっと単語またはずっと文
						nowMode = selectMode;
						now++;
					}
					mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(dPlaySpeedJpn));
				}
			} catch (Exception exception) {
				showException(context, exception);
			}
		}
	}
	
	WordPhraseData.DataLang nowLang = english;
	int now = 1;
	
	private void sendBroadcastTextChange(PlayerFragment.PlayerViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, Text)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}