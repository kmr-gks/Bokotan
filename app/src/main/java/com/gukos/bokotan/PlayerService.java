package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPathPs;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.fnAppSettings;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_PROPERTIES;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.Text;
import static com.gukos.bokotan.PlayerFragment.isInitialized;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.DataQ;
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
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gukos.bokotan.PlayerFragment.PlayerViewName;

import java.util.ArrayList;

public class PlayerService extends Service {
	
	public static final String
		PLAYERSERVICE_EXTRA_MODE = "ps_em",
		PLAYERSERVICE_EXTRA_BOOK="ps_eb",
		PLAYERSERVICE_EXTRA_DATA_Q="ps_edq";
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
		dataQ= (DataQ) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_DATA_Q);
		
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
						//表示している文字列を削除
						sendBroadcastTextChange(PlayerViewName.path, "");
						sendBroadcastTextChange(PlayerViewName.eng, "");
						sendBroadcastTextChange(PlayerViewName.jpn, "");
						sendBroadcastTextChange(PlayerViewName.subE, "");
						sendBroadcastTextChange(PlayerViewName.subJ, "");
						sendBroadcastTextChange(PlayerViewName.genzai, "");
						runOnUiThread(() -> TabActivity.setTabPageNum(0));
						MyLibrary.PreferenceManager.putIntData(context,fnAppSettings, getClassName()+ dataBook+dataQ+selectMode,now );
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
			sendBroadcastTextChange(PlayerViewName.eng, "読み込み中");
			sendBroadcastTextChange(PlayerViewName.jpn, "読み込み中");
			isPlaying = true;
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(PLAYERSERVICE_ACTION));
			
			now=MyLibrary.PreferenceManager.getIntData(context,fnAppSettings, getClassName()+ dataBook+dataQ+selectMode,1 );
			
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
			//extracted(strBookName);
			WordPhraseData.read(dataBook,strBookName,dataQ,context,wordDataList,phraseDataList, selectMode);
			if (dataBook==yumetan) phraseDataList=wordDataList;
			
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
	DataQ dataQ;
	
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
			sendBroadcastTextChange(PlayerViewName.genzai, "No." + list.get(now).no+",Q="+dataQ);
			sendBroadcastTextChange(PlayerViewName.eng, list.get(now).e);
			sendBroadcastTextChange(PlayerViewName.jpn, list.get(now).j);
			//文を再生しているときは、単語も表示しておく。
			if (selectMode== q_num.mode.wordPlusPhrase&&nowMode== q_num.mode.phrase){
				sendBroadcastTextChange(PlayerViewName.subE,wordDataList.get(now).e);
				sendBroadcastTextChange(PlayerViewName.subJ,wordDataList.get(now).j);
			}else{
				sendBroadcastTextChange(PlayerViewName.subE,"");
				sendBroadcastTextChange(PlayerViewName.subJ,"");
			}
			path=getPathPs(dataBook,dataQ,nowMode,nowLang,now);
			sendBroadcastTextChange(PlayerViewName.path, path);
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
	
	private void sendBroadcastTextChange(PlayerViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, Text)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void runOnUiThread(Runnable runnable){
		new Handler(Looper.getMainLooper()).post(runnable);
	}
}