package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPathPs;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.fnAppSettings;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PipActivity.PIP_ACTION_UI;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_NAME;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_SINGLE_LINE;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_PROPERTIES;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_SINGLE_LINE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.Text;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.line;
import static com.gukos.bokotan.PlayerFragment.isInitialized;
import static com.gukos.bokotan.WordPhraseData.DataBook.all;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.DataQ;
import static com.gukos.bokotan.WordPhraseData.DataQ.q1;
import static com.gukos.bokotan.WordPhraseData.DataQ.qp1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y00;
import static com.gukos.bokotan.WordPhraseData.DataQ.y1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y2;
import static com.gukos.bokotan.WordPhraseData.DataQ.y3;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoPhrase;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;
import static com.gukos.bokotan.WordPhraseData.getList;

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
import java.util.HashMap;
import java.util.HashSet;

public class PlayerService extends Service {
	public static final String
		className = getClassName(),//場所によってこの関数の返す文字列が変わる
		PLAYERSERVICE_EXTRA_MODE = "ps_em",
		PLAYERSERVICE_EXTRA_BOOK = "ps_eb",
		PLAYERSERVICE_EXTRA_DATA_Q = "ps_edq",
		PLAYERSERVICE_EXTRA_NOW = "ps_en",
		PLAYERSERVICE_EXTRA_SHOW_APPEARED = "ps_esa",
		PLAYERSERVICE_ACTION = "playerservice_action",
		PLAYERSERVICE_MESSAGE_TYPE = "playerservice_message_type",
		PLAYERSERVICE_MESSAGE_STOP = "playerservice_message_stop",
		PLAYERSERVICE_MESSAGE_NOW = "ps_mn";
	Context context;
	Handler handler;
	private DrawReceiver drawReceiver;
	WordPhraseData.Mode selectMode, nowMode = WordPhraseData.Mode.word;
	WordPhraseData.DataLang nowLang = english;
	static ArrayList<WordPhraseData.WordInfo> wordDataList = new ArrayList<>();
	ArrayList<WordPhraseData.WordInfo> phraseDataList = new ArrayList<>();
	private final HashSet<String> appearedWords = new HashSet<>();
	private final HashMap<String, Integer> knownWordMap = new HashMap<>();
	WordPhraseData.DataBook dataBook = passTan;
	DataQ dataQ;
	public static float dPlaySpeedEng = 1.5f, dPlaySpeedJpn = 2f;
	private int now = 1, count = 1;
	MediaPlayer mediaPlayer;
	String path;
	boolean isPlaying, isJoshiChecked = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		selectMode = (WordPhraseData.Mode) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_MODE);
		if (selectMode == WordPhraseData.Mode.phrase) nowMode = WordPhraseData.Mode.phrase;
		dataBook = (WordPhraseData.DataBook) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_BOOK);
		dataQ = (DataQ) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_DATA_Q);
		now = intent.getIntExtra(PLAYERSERVICE_EXTRA_NOW, -1);
		if (intent.getBooleanExtra(PLAYERSERVICE_EXTRA_SHOW_APPEARED, false)) {
			//既出の単語を飛ばす
			appearedWords.clear();
			if (dataBook != all) {
				if (dataBook == passTan || dataBook == tanjukugo) {
					getList(YumeWord + y00.toString().substring(1)).stream().map(info -> info.e).forEach(appearedWords::add);
					getList(YumeWord + y1.toString().substring(1)).stream().map(info -> info.e).forEach(appearedWords::add);
					getList(YumeWord + y2.toString().substring(1)).stream().map(info -> info.e).forEach(appearedWords::add);
					getList(YumeWord + y3.toString().substring(1)).stream().map(info -> info.e).forEach(appearedWords::add);
				}
				if ((dataBook == passTan && dataQ == q1) || dataBook == tanjukugo) {
					getList(PasstanWord + qp1).stream().map(info -> info.e).forEach(appearedWords::add);
				}
				if (dataBook == tanjukugo) {
					getList(PasstanWord + q1).stream().map(info -> info.e).forEach(appearedWords::add);
				}
				if (dataBook == tanjukugo && dataQ == q1) {
					getList(TanjukugoWord + qp1).stream().map(info -> info.e).forEach(appearedWords::add);
				}
			}
			else {
				//knownWordMap.clear();
			}
		}
		
		context = getApplicationContext();
		String channelId = "default";
		String title = context.getString(R.string.app_name);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, new Intent(context, TabActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), PendingIntent.FLAG_IMMUTABLE);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// Notification Channel 設定
		NotificationChannel channel = new NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT);
		
		if (notificationManager != null) {
			notificationManager.createNotificationChannel(channel);
			Intent sendStopIntent =
				new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP);
			/*
			Intent sendStopIntent =new Intent(this, StopPlayBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			*/
			Intent sendPipIntent = new Intent(this, StartPipBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			PendingIntent sendStopPendingIntent = PendingIntent.getBroadcast(this, 0, sendStopIntent, PendingIntent.FLAG_IMMUTABLE);
			PendingIntent sendPipPendingIntent = PendingIntent.getBroadcast(this, 10, sendPipIntent, PendingIntent.FLAG_IMMUTABLE);
			
			Notification notification = new Notification.Builder(context, channelId)
				.setContentTitle(title)
				// android標準アイコンから
				.setSmallIcon(android.R.drawable.ic_media_play)
				.setContentText("MyApplication")
				.setAutoCancel(true)
				.addAction(R.drawable.ic_launcher_foreground, "停止", sendStopPendingIntent)
				.addAction(R.mipmap.launcher_new_icon, "小窓で表示", sendPipPendingIntent)
				.setContentIntent(pendingIntent)
				.setWhen(System.currentTimeMillis())
				.build();
			//Notification.FLAG_NO_CLEARだと消える(Android13)
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
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
						appearedWords.clear();
						//表示している文字列を削除
						sendBcTextChange(PlayerViewName.path, "");
						sendBcTextChange(PlayerViewName.eng, "");
						sendBcTextChange(PlayerViewName.jpn, "");
						sendBcTextChange(PlayerViewName.hatsuon, "");
						sendBcTextChange(PlayerViewName.subE, "");
						sendBcTextChange(PlayerViewName.subJ, "");
						sendBcTextChange(PlayerViewName.genzai, "");
						runOnUiThread(() -> TabActivity.setTabPageNum(0));
						MyLibrary.PreferenceManager.putIntData(context, fnAppSettings, className + dataBook + dataQ + selectMode, now);
						context.unregisterReceiver(drawReceiver);
						thisService.stopSelf();
						break;
					}
					case PLAYERSERVICE_MESSAGE_NOW: {
						now = bundle.getInt(PLAYERSERVICE_MESSAGE_NOW);
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
			sendBcTextChange(PlayerViewName.eng, "読み込み中");
			sendBcTextChange(PlayerViewName.jpn, "読み込み中");
			isPlaying = true;
			
			drawReceiver = new DrawReceiver(handler);
			context.registerReceiver(drawReceiver, new IntentFilter(PLAYERSERVICE_ACTION));
			
			if (now == -1) {
				now = MyLibrary.PreferenceManager.getIntData(context, fnAppSettings, className + dataBook + dataQ + selectMode, 1);
			}
			
			String key = null;
			wordDataList.clear();
			switch (dataBook) {
				default:
				case passTan: {
					wordDataList = WordPhraseData.getList(PasstanWord + dataQ);
					phraseDataList = WordPhraseData.getList(PasstanPhrase + dataQ);
					break;
				}
				case tanjukugo: {
					wordDataList = WordPhraseData.getList(TanjukugoWord + dataQ);
					wordDataList.addAll(WordPhraseData.getList(TanjukugoEXWord + dataQ));
					phraseDataList = WordPhraseData.getList(TanjukugoPhrase + dataQ);
					phraseDataList.addAll(WordPhraseData.getList(TanjukugoEXWord + dataQ));
					break;
				}
				case yumetan: {
					wordDataList = WordPhraseData.getList(YumeWord + dataQ.toString().substring(1));
					break;
				}
				case all: {
					wordDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y1.toString().substring(1)));
					wordDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y2.toString().substring(1)).subList(1, 1000 + 1));
					wordDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y3.toString().substring(1)).subList(1, 800 + 1));
					
					wordDataList.addAll(WordPhraseData.getList(PasstanWord + qp1).subList(1, 1850 + 1));
					wordDataList.addAll(WordPhraseData.getList(PasstanWord + q1).subList(1, 2400 + 1));
					
					wordDataList.addAll(WordPhraseData.getList(TanjukugoWord + qp1));
					wordDataList.addAll(WordPhraseData.getList(TanjukugoWord + q1));
					
					if (selectMode == WordPhraseData.Mode.phrase || selectMode == WordPhraseData.Mode.wordPlusPhrase) {
						phraseDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y1.toString().substring(1)));
						phraseDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y2.toString().substring(1)).subList(1, 1000 + 1));
						phraseDataList.addAll(WordPhraseData.getList(YumeWord + DataQ.y3.toString().substring(1)).subList(1, 800 + 1));
						
						phraseDataList.addAll(WordPhraseData.getList(PasstanPhrase + qp1).subList(1, 1850 + 1));
						phraseDataList.addAll(WordPhraseData.getList(PasstanPhrase + q1).subList(1, 2400 + 1));
						
						phraseDataList.addAll(WordPhraseData.getList(TanjukugoPhrase + qp1));
						phraseDataList.addAll(WordPhraseData.getList(TanjukugoPhrase + q1));
					}
					
					//単語データをmapに格納
					for (int i = 0; i < wordDataList.size(); i++) {
						if (!knownWordMap.containsKey(wordDataList.get(i).e)) {
							knownWordMap.put(wordDataList.get(i).e, i);
						}
					}
					//IntStream.range(0, wordDataList.size()).filter(i -> !knownWordMap.containsKey(wordDataList.get(i).e)).forEach(i -> knownWordMap.put(wordDataList.get(i).e, i));
					break;
				}
			}
			puts("key=" + key);
			if (dataBook == yumetan) phraseDataList = wordDataList;
			
			onPlay();
		});
		return START_NOT_STICKY;
	}
	
	private void onPlay() {
		//リソースの開放
		releaseMediaPlayer(mediaPlayer);
		if (isPlaying) {
			ArrayList<WordPhraseData.WordInfo> list;
			if (nowMode == WordPhraseData.Mode.phrase) list = phraseDataList;
			else list = wordDataList;
			
			//助詞の確認
			if (dataBook == passTan && nowMode == WordPhraseData.Mode.word && nowLang == japanese && !isJoshiChecked) {
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
			
			if (nowLang == english) {
				sendBcTextChange(PlayerViewName.tvcount, "再生回数:" + count + "回");
				sendBcTextChange(PlayerViewName.genzai, "No." + now);
				sendBcTextChange(PlayerViewName.eng, list.get(now).e);
				sendBcTextChange(PlayerViewName.jpn, list.get(now).j);
				
				if (nowMode == WordPhraseData.Mode.word && QSentakuFragment.switchShouHatsuon.isChecked()) {
					sendBcTextChange(PlayerViewName.hatsuon, WordPhraseData.HatsuonKigou.getHatsuon(list.get(now).e));
				}
				else {
					sendBcTextChange(PlayerViewName.hatsuon, null);
				}
				
				printCurrentState("now=" + now + ",map=" + knownWordMap.get(wordDataList.get(now).e));
				sendBcTextChange(PipActivity.PipViewName.num, "No." + now);
				sendBcTextChange(PipActivity.PipViewName.eng, list.get(now).e);
				sendBcTextChange(PipActivity.PipViewName.jpn, list.get(now).j);
				//文を再生しているときは、単語も表示しておく。
				if (selectMode == WordPhraseData.Mode.wordPlusPhrase && nowMode == WordPhraseData.Mode.phrase) {
					sendBcTextChange(PlayerViewName.subE, wordDataList.get(now).e);
					sendBcTextChange(PlayerViewName.subJ, wordDataList.get(now).j);
				}
				else {
					sendBcTextChange(PlayerViewName.subE, "");
					sendBcTextChange(PlayerViewName.subJ, "");
				}
				//英単語を表示するときは、英語の表示を一行にする
				if (nowMode == WordPhraseData.Mode.word) {
					sendBcLinesChange(PlayerViewName.eng, true);
					sendBcLinesChange(PipActivity.PipViewName.eng, true);
				}
				else {
					sendBcLinesChange(PlayerViewName.eng, false);
					sendBcLinesChange(PipActivity.PipViewName.eng, false);
				}
			}
			
			path = getPathPs(wordDataList.get(now).dataBook, wordDataList.get(now).dataQ, nowMode, nowLang, wordDataList.get(now).localNumber);
			try {
				mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
				if (mediaPlayer==null){
					//ファイルが存在しない
					//new AlertDialog.Builder(getApplicationContext()).setMessage
					// ("ファイルが存在しません。\n" + path).setPositiveButton("OK", null).show();
					return;
				}
				mediaPlayer.setOnCompletionListener((mp) -> handler.post(this::onPlay));
				if (nowLang == english) {
					//現在英語:日本語にする
					nowLang = japanese;
					mediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(dPlaySpeedEng));
				}
				else {
					//現在日本語:英語にする
					nowLang = english;
					if (selectMode == WordPhraseData.Mode.wordPlusPhrase) {
						//単語->文
						if (nowMode == WordPhraseData.Mode.word) {
							//現在単語だった
							nowMode = WordPhraseData.Mode.phrase;
						}
						else {
							//文だった
							nowMode = WordPhraseData.Mode.word;
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
	
	private void releaseMediaPlayer(MediaPlayer mediaPlayer) {
		try {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.reset();
				mediaPlayer.release();
			}
		} catch (Exception ignored) {}
	}
	
	private void goNext() {
		count++;
		if (now >= wordDataList.size() - 1) now = 0;
		//既出の単語を飛ばす
		if (dataBook == all) {
			do {
				now++;
			} while (now != knownWordMap.get(wordDataList.get(now).e) && now < wordDataList.size() - 1);
		}
		else {
			do {
				now++;
			} while (appearedWords.contains(wordDataList.get(now).e) && now < wordDataList.size() - 1);
		}
		printCurrentState("now=" + now+", max="+wordDataList.size() + ",map=" + knownWordMap.get(wordDataList.get(now).e));
	}
	
	/**
	 * fragment_playerのビューの文字を変更
	 *
	 * @param viewName
	 * @param text
	 */
	private void sendBcTextChange(PlayerViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, Text)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	/**
	 * activity_pipのビューの文字を変更
	 *
	 * @param viewName
	 * @param text
	 */
	private void sendBcTextChange(PipActivity.PipViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(PIP_ACTION_UI)
				.putExtra(PIP_VIEW_NAME, viewName)
				.putExtra(PIP_VIEW_TEXT, text);
		context.sendBroadcast(broadcastIntent);
	}
	
	/**
	 * fragment_playerのビューの文字表示を一行にするか複数行にするか設定
	 *
	 * @param viewName
	 * @param isSingleLine
	 */
	private void sendBcLinesChange(PlayerViewName viewName, boolean isSingleLine) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES, line)
				.putExtra(PLAYER_VIEW_NAME, viewName)
				.putExtra(PLAYER_VIEW_SINGLE_LINE, isSingleLine);
		context.sendBroadcast(broadcastIntent);
	}
	
	/**
	 * activity_pipのビューの文字表示を一行にするか複数行にするか設定
	 *
	 * @param viewName
	 * @param isSingleLine
	 */
	private void sendBcLinesChange(PipActivity.PipViewName viewName, boolean isSingleLine) {
		Intent broadcastIntent =
			new Intent(PIP_ACTION_UI)
				.putExtra(PIP_VIEW_NAME, viewName)
				.putExtra(PIP_VIEW_SINGLE_LINE, isSingleLine);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void runOnUiThread(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}
}