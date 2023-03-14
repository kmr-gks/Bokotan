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
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoPhrase;
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

import androidx.annotation.NonNull;

import com.gukos.bokotan.PlayerFragment.PlayerViewName;

import java.util.ArrayList;

public class PlayerService extends Service {
	public static final String
		className = getClassName(),//場所によってこの関数の返す文字列が変わる
		PLAYERSERVICE_EXTRA_MODE = "ps_em",
		PLAYERSERVICE_EXTRA_BOOK = "ps_eb",
		PLAYERSERVICE_EXTRA_DATA_Q = "ps_edq",
		PLAYERSERVICE_EXTRA_NOW="ps_en",
		PLAYERSERVICE_ACTION = "playerservice_action",
		PLAYERSERVICE_MESSAGE_TYPE = "playerservice_message_type",
		PLAYERSERVICE_MESSAGE_STOP = "playerservice_message_stop",
		PLAYERSERVICE_MESSAGE_JPN_SPEED = "playerservice_message_jpn_speed",
		PLAYERSERVICE_MESSAGE_ENG_SPEED = "playerservice_message_eng_speed",
		PLAYERSERVICE_MESSAGE_NOW="ps_mn";
	Context context;
	Handler handler;
	private DrawReceiver drawReceiver;
	q_num.mode selectMode, nowMode = q_num.mode.word;
	WordPhraseData.DataLang nowLang = english;
	ArrayList<QuizCreator.QuizWordData> wordDataList = new ArrayList<>(), phraseDataList = new ArrayList<>();
	WordPhraseData.DataBook dataBook = passTan;
	DataQ dataQ;
	float dPlaySpeedEng = 1.5f, dPlaySpeedJpn = 1.5f;
	int now = 1;
	MediaPlayer mediaPlayer;
	String path;
	boolean isPlaying, isJoshiChecked = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		selectMode = (q_num.mode) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_MODE);
		if (selectMode == q_num.mode.phrase) nowMode = q_num.mode.phrase;
		dataBook = (WordPhraseData.DataBook) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_BOOK);
		dataQ = (DataQ) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_DATA_Q);
		now=intent.getIntExtra(PLAYERSERVICE_EXTRA_NOW,-1);
		
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
		HandlerThread handlerThread = new HandlerThread(getClassName());
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			@Override
			public void handleMessage(@NonNull Message msg) {
				Bundle bundle = msg.getData();
				String messageType = bundle.getString(PLAYERSERVICE_MESSAGE_TYPE);
				switch (messageType) {
					case PLAYERSERVICE_MESSAGE_STOP: {
						//サービス停止
						puts("サービス停止");
						isPlaying = false;
						releaseMediaPlayer(mediaPlayer);
						//表示している文字列を削除
						sendBroadcastTextChange(PlayerViewName.path, "");
						sendBroadcastTextChange(PlayerViewName.eng, "");
						sendBroadcastTextChange(PlayerViewName.jpn, "");
						sendBroadcastTextChange(PlayerViewName.subE, "");
						sendBroadcastTextChange(PlayerViewName.subJ, "");
						sendBroadcastTextChange(PlayerViewName.genzai, "");
						runOnUiThread(() -> TabActivity.setTabPageNum(0));
						MyLibrary.PreferenceManager.putIntData(context, fnAppSettings, className + dataBook + dataQ + selectMode, now);
						context.unregisterReceiver(drawReceiver);
						thisService.stopSelf();
						break;
					}
					case PLAYERSERVICE_MESSAGE_JPN_SPEED: {
						dPlaySpeedJpn = bundle.getFloat(PLAYERSERVICE_MESSAGE_JPN_SPEED);
						break;
					}
					case PLAYERSERVICE_MESSAGE_ENG_SPEED: {
						dPlaySpeedEng = bundle.getFloat(PLAYERSERVICE_MESSAGE_ENG_SPEED);
						break;
					}
					case PLAYERSERVICE_MESSAGE_NOW:{
						now=bundle.getInt(PLAYERSERVICE_MESSAGE_NOW);
					}
				}
			}
		};
		//最初に実行される
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
			
			drawReceiver=new DrawReceiver(handler);
			context.registerReceiver(drawReceiver, new IntentFilter(PLAYERSERVICE_ACTION));
			
			if(now==-1){
				now = MyLibrary.PreferenceManager.getIntData(context, fnAppSettings, className + dataBook + dataQ + selectMode, 1);
			}
			
			String key=null;
			switch (dataBook) {
				case passTan: {
					wordDataList= WordPhraseData.getList(PasstanWord+dataQ);
					phraseDataList= WordPhraseData.getList(PasstanPhrase+dataQ);
					break;
				}
				case tanjukugo: {
					wordDataList= WordPhraseData.getList(TanjukugoWord+dataQ);
					wordDataList.addAll(WordPhraseData.getList(TanjukugoEXWord+dataQ));
					phraseDataList= WordPhraseData.getList(TanjukugoPhrase+dataQ);
					phraseDataList.addAll(WordPhraseData.getList(TanjukugoEXWord+dataQ));
					break;
				}
				case yumetan: {
					wordDataList= WordPhraseData.getList(YumeWord+dataQ.toString().substring(1));
					break;
				}
			}
			puts("key="+key);
			if (dataBook == yumetan) phraseDataList = wordDataList;
			
			onPlay();
		});
		return START_NOT_STICKY;
	}
	
	private void onPlay() {
		//リソースの開放
		releaseMediaPlayer(mediaPlayer);
		if (isPlaying) {
			ArrayList<QuizCreator.QuizWordData> list;
			if (nowMode == q_num.mode.phrase) list = phraseDataList;
			else list = wordDataList;
			
			//助詞の確認
			if (dataBook == passTan && nowMode == q_num.mode.word && nowLang == japanese && !isJoshiChecked) {
				isJoshiChecked = true;
				String word = list.get(now).j;
				if (word.charAt(0) == '～') word = word.substring(1);
				if (word.charAt(0) == '(') {
					int index = word.indexOf(')');
					word = word.substring(index + 1);
				}
				if (word.charAt(0) == '～') word = word.substring(1);
				char c = word.charAt(0);
				if (c == 'を' || c == 'に' || c == 'の' || c == 'で') {
					mediaPlayer = MediaPlayer.create(this, Uri.parse(MyLibrary.FileDirectoryManager.getJosiPath(c)));
					mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(dPlaySpeedJpn));
					mediaPlayer.start();
					mediaPlayer.setOnCompletionListener((mp) -> handler.post(this::onPlay));
					return;
				}
			}
			else {
				isJoshiChecked = false;
			}
			
			sendBroadcastTextChange(PlayerViewName.genzai, "No." + list.get(now).no);
			sendBroadcastTextChange(PlayerViewName.eng, list.get(now).e);
			sendBroadcastTextChange(PlayerViewName.jpn, list.get(now).j);
			sendBroadcastPipTextChange(PipActivity.PipViewName.num,"No."+now);
			sendBroadcastPipTextChange(PipActivity.PipViewName.eng,list.get(now).e);
			sendBroadcastPipTextChange(PipActivity.PipViewName.jpn,list.get(now).j);
			//文を再生しているときは、単語も表示しておく。
			if (selectMode == q_num.mode.wordPlusPhrase && nowMode == q_num.mode.phrase) {
				sendBroadcastTextChange(PlayerViewName.subE, wordDataList.get(now).e);
				sendBroadcastTextChange(PlayerViewName.subJ, wordDataList.get(now).j);
			}
			else {
				sendBroadcastTextChange(PlayerViewName.subE, "");
				sendBroadcastTextChange(PlayerViewName.subJ, "");
			}
			
			path = getPathPs(dataBook, dataQ, nowMode, nowLang, now);
			sendBroadcastTextChange(PlayerViewName.path, path);
			try {
				mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
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
							goNext();
						}
					}
					else {
						//ずっと単語またはずっと文
						nowMode = selectMode;
						goNext();
					}
					mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(dPlaySpeedJpn));
				}
				mediaPlayer.start();
			} catch (Exception exception) {
				showException(context, exception);
			}
		}
	}
	
	private void releaseMediaPlayer(MediaPlayer mediaPlayer){
		try {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		} catch (Exception ignored) {}
	}
	
	private void goNext(){
		if (now>=wordDataList.size()-1) now=1;
		else now++;
	}
	
	private void sendBroadcastTextChange(PlayerViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, Text)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void sendBroadcastPipTextChange(PipActivity.PipViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PipActivity.PIP_ACTION_UI)
				.putExtra(PipActivity.PIP_VIEW_TEXT, text)
				.putExtra(PipActivity.PIP_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void runOnUiThread(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}
}