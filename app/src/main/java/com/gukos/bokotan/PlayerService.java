package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.BookQ;
import static com.gukos.bokotan.Dictionary.BookQ.q1;
import static com.gukos.bokotan.Dictionary.BookQ.qp1;
import static com.gukos.bokotan.Dictionary.BookQ.y08;
import static com.gukos.bokotan.Dictionary.BookQ.y1;
import static com.gukos.bokotan.Dictionary.BookQ.y2;
import static com.gukos.bokotan.Dictionary.BookQ.y3;
import static com.gukos.bokotan.Dictionary.DataLang.english;
import static com.gukos.bokotan.Dictionary.DataLang.japanese;
import static com.gukos.bokotan.Dictionary.Folder.all;
import static com.gukos.bokotan.Dictionary.Folder.passtan;
import static com.gukos.bokotan.Dictionary.Folder.tanjukugo;
import static com.gukos.bokotan.Dictionary.QuizData.huseikai;
import static com.gukos.bokotan.Dictionary.QuizData.seikai;
import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPathPs;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.fnAppSettings;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PipActivity.PIP_ACTION_UI;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_NAME;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_SINGLE_LINE;
import static com.gukos.bokotan.PipActivity.PIP_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_SINGLE_LINE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.isInitialized;

import android.app.AlertDialog;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

public class PlayerService extends Service {
	public static final String
		className = getClassName(),//場所によってこの関数の返す文字列が変わる
		PLAYERSERVICE_EXTRA_MODE = "ps_em",
		PLAYERSERVICE_EXTRA_BOOK = "ps_eb",
		PLAYERSERVICE_EXTRA_DATA_Q = "ps_edq",
		PLAYERSERVICE_EXTRA_SKIP_COND = "psesc",
		PLAYERSERVICE_EXTRA_SKIP_THRES_NUM = "psestn",
		PLAYERSERVICE_EXTRA_SKIP_THRES_COMP = "psestc",
		PLAYERSERVICE_EXTRA_NOW = "ps_en",
		PLAYERSERVICE_EXTRA_SHOW_APPEARED = "ps_esa",
		PLAYERSERVICE_ACTION = "playerservice_action",
		PLAYERSERVICE_MESSAGE_TYPE = "playerservice_message_type",
		PLAYERSERVICE_MESSAGE_STOP = "playerservice_message_stop",
		PLAYERSERVICE_MESSAGE_NOW = "ps_mn";
	
	public static float dPlaySpeedEng = 1.5f, dPlaySpeedJpn = 2f;
	static ArrayList<Dictionary.Entry> wordDataList = new ArrayList<>(), phraseDataList = new ArrayList<>();
	
	Context context;
	Handler handler;
	private DrawReceiver drawReceiver;
	private final HashSet<String> appearedWords = new HashSet<>();
	private final HashMap<String, Integer> knownWordMap = new HashMap<>();
	Dictionary.Datatype selectMode, nowMode = Dictionary.Datatype.word;
	Dictionary.Folder dataBook = passtan;
	Dictionary.DataLang nowLang = english;
	BookQ dataQ;
	MediaPlayer mediaPlayer;
	String path;
	boolean isPlaying, isJoshiChecked = false;
	PlayerService.SkipContidion skipContidion;
	PlayerService.SkipThreshold skipThreshold;
	BiFunction<Integer, Integer, Boolean> skipChecker;
	int seikaisu = 0, huseikaisu = 0;
	ArrayList<String> fileNames;
	String fileName;
	ArrayList<Integer> sizeForBook = null;
	private int now = 1, count = 1;
	private double thresholdNum;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		selectMode = (Dictionary.Datatype) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_MODE);
		if (selectMode == Dictionary.Datatype.phrase) nowMode = Dictionary.Datatype.phrase;
		dataBook = (Dictionary.Folder) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_BOOK);
		dataQ = (BookQ) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_DATA_Q);
		now = intent.getIntExtra(PLAYERSERVICE_EXTRA_NOW, -1);
		skipContidion = (SkipContidion) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_SKIP_COND);
		thresholdNum = intent.getDoubleExtra(PLAYERSERVICE_EXTRA_SKIP_THRES_NUM, 0);
		skipThreshold = (SkipThreshold) intent.getSerializableExtra(PLAYERSERVICE_EXTRA_SKIP_THRES_COMP);
		switch (skipContidion) {
			case all: {
				skipChecker = (seikai, huseikai) -> true;
				break;
			}
			case seikaisu: {
				if (skipThreshold == SkipThreshold.eqormore)
					skipChecker = (seikai, huseikai) -> seikai >= thresholdNum;
				else
					skipChecker = (seikai, huseikai) -> seikai < thresholdNum;
				break;
			}
			case huseikai: {
				if (skipThreshold == SkipThreshold.eqormore)
					skipChecker = (seikai, huseikai) -> huseikai >= thresholdNum;
				else
					skipChecker = (seikai, huseikai) -> huseikai < thresholdNum;
				break;
			}
			case seikairate: {
				if (skipThreshold == PlayerService.SkipThreshold.eqormore)
					skipChecker = (seikai, huseikai) -> (seikai + huseikai == 0 ? -1 : (double) seikai / (seikai + huseikai)) >= thresholdNum;
				else
					skipChecker = (seikai, huseikai) -> (seikai + huseikai == 0 ? -1 : (double) seikai / (seikai + huseikai)) < thresholdNum;
				break;
			}
		}
		if (intent.getBooleanExtra(PLAYERSERVICE_EXTRA_SHOW_APPEARED, false)) {
			//既出の単語を飛ばす
			appearedWords.clear();
			if (dataBook != all) {
				if (dataBook == passtan || dataBook == tanjukugo) {
					for (var q : new BookQ[]{y08, y1, y2, y3}) {
						Dictionary.getList(Dictionary.BookName.yumetanWord, q).stream().map(entry -> entry.e).forEach(appearedWords::add);
					}
				}
				if ((dataBook == passtan && dataQ == q1) || dataBook == tanjukugo) {
					Dictionary.getList(Dictionary.BookName.PasstanWordData, qp1).stream().map(entry -> entry.e).forEach(appearedWords::add);
				}
				if (dataBook == tanjukugo) {
					Dictionary.getList(Dictionary.BookName.PasstanWordData, q1).stream().map(entry -> entry.e).forEach(appearedWords::add);
				}
				if (dataBook == tanjukugo && dataQ == q1) {
					Dictionary.getList(Dictionary.BookName.tanjukugoWord, qp1).stream().map(entry -> entry.e).forEach(appearedWords::add);
				}
			}
		}
		
		if (dataBook==tanjukugo){
			fileName = dnTestActivity + "tanjukugo" + dataQ.toString() + "Test";
		}else{
			fileName = dnTestActivity + dataQ.toString() + "Test";
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
				try {
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
							for (var playerViewName : PlayerViewName.values()) {
								sendBcTextChange(playerViewName, null);
							}
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
				} catch (Exception exception) {
					showException(context, exception);
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
			
			wordDataList = new ArrayList<>();
			phraseDataList = new ArrayList<>();
			switch (dataBook) {
				default:
				case passtan: {
					wordDataList = Dictionary.getList(Dictionary.BookName.PasstanWordData, dataQ);
					phraseDataList = Dictionary.getList(Dictionary.BookName.PasstanPhrase, dataQ);
					break;
				}
				case tanjukugo: {
					wordDataList = Dictionary.getList(Dictionary.BookName.tanjukugoWord, dataQ);
					phraseDataList = Dictionary.getList(Dictionary.BookName.tanjukugoPhrase, dataQ);
					break;
				}
				case yumetan: {
					wordDataList = Dictionary.getList(Dictionary.BookName.yumetanWord, dataQ);
					phraseDataList = Dictionary.getList(Dictionary.BookName.yumetanPhrase, dataQ);
					break;
				}
				case all: {
					for (var q : new BookQ[]{y1, y2, y3}) {
						wordDataList.addAll(Dictionary.getList(Dictionary.BookName.yumetanWord, q));
					}
					for (var q : new BookQ[]{qp1, q1}) {
						wordDataList.addAll(Dictionary.getList(Dictionary.BookName.PasstanWordData, q));
					}
					//todo 単熟語ex準1級のデータはunit8までしかない。
					for (var q : new BookQ[]{qp1, q1}) {
						wordDataList.addAll(Dictionary.getList(Dictionary.BookName.tanjukugoWord, q));
					}
					
					if (selectMode == Dictionary.Datatype.phrase || selectMode == Dictionary.Datatype.mix) {
						for (var q : new BookQ[]{y1, y2, y3}) {
							phraseDataList.addAll(Dictionary.getList(Dictionary.BookName.yumetanPhrase, q));
						}
						for (var q : new BookQ[]{qp1, q1}) {
							phraseDataList.addAll(Dictionary.getList(Dictionary.BookName.PasstanPhrase, q));
						}
						for (var q : new BookQ[]{qp1, q1}) {
							phraseDataList.addAll(Dictionary.getList(Dictionary.BookName.tanjukugoPhrase, q));
						}
					}
					
					if (dataBook == all) {
						sizeForBook = new ArrayList<>(Arrays.asList(1001, 1000, 800, 1850, 2400, 1680, 2364));
						fileNames = new ArrayList<>();
						fileNames.add(dnTestActivity + BookQ.y1 + "Test");
						fileNames.add(dnTestActivity + BookQ.y2 + "Test");
						fileNames.add(dnTestActivity + BookQ.y3 + "Test");
						fileNames.add(dnTestActivity + qp1 + "Test");
						fileNames.add(dnTestActivity + q1 + "Test");
						fileNames.add(dnTestActivity + "tanjukugo" + qp1 + "Test");
						fileNames.add(dnTestActivity + "tanjukugo" + q1 + "Test");
					}
					
					//単語データをmapに格納
					for (int i = 0; i < wordDataList.size(); i++) {
						if (!knownWordMap.containsKey(wordDataList.get(i).e)) {
							knownWordMap.put(wordDataList.get(i).e, i);
						}
					}
					break;
				}
			}
			onPlay();
		});
		return START_NOT_STICKY;
	}

	private void onPlay() {
		//リソースの開放
		releaseMediaPlayer(mediaPlayer);
		if (isPlaying) {
			ArrayList<Dictionary.Entry> list;
			if (nowMode == Dictionary.Datatype.phrase) list = phraseDataList;
			else list = wordDataList;
			
			if (wordDataList.size() == 0) {
				puts("データがありません。");
				new AlertDialog.Builder(context).setMessage("データがありません。").setPositiveButton("OK", (dialog, which) -> {
					stopSelf();
				}).create().show();
			}
			
			//助詞の確認
			if (dataBook == passtan && nowMode == Dictionary.Datatype.word && nowLang == japanese && !isJoshiChecked) {
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
				sendBcTextChange(PlayerViewName.tvcount, seikaisu+"/"+(seikaisu+huseikaisu)+" 再生回数:" + count + "回");
				sendBcTextChange(PlayerViewName.genzai, "No." + now);
				sendBcTextChange(PlayerViewName.jpn, list.get(now).j);
				
				if (nowMode == Dictionary.Datatype.word && QSentakuFragment.switchShouHatsuon.isChecked()) {
					sendBcTextChange(PlayerViewName.hatsuon, Dictionary.HatsuonKigou.getHatsuon(list.get(now).e));
				}
				else {
					sendBcTextChange(PlayerViewName.hatsuon, null);
				}
				
				sendBcTextChange(PipActivity.PipViewName.num, "No." + now);
				sendBcTextChange(PipActivity.PipViewName.jpn, list.get(now).j);
				//文を再生しているときは、単語も表示しておく。
				if (selectMode == Dictionary.Datatype.mix && nowMode == Dictionary.Datatype.phrase) {
					sendBcTextChange(PlayerViewName.subE, wordDataList.get(now).e);
					sendBcTextChange(PlayerViewName.subJ, wordDataList.get(now).j);
				}
				else {
					sendBcTextChange(PlayerViewName.subE, "");
					sendBcTextChange(PlayerViewName.subJ, "");
				}
				//英単語を表示するときは、英語の表示を一行にする
				if (nowMode == Dictionary.Datatype.word) {
					sendBcTextLinesChange(PlayerViewName.eng, list.get(now).e, true);
					sendBcTextLinesChange(PipActivity.PipViewName.eng, list.get(now).e, true);
				}
				else {
					sendBcTextLinesChange(PlayerViewName.eng, list.get(now).e, false);
					sendBcTextLinesChange(PipActivity.PipViewName.eng, list.get(now).e, false);
				}
			}
			
			path = getPathPs(wordDataList.get(now).folder, wordDataList.get(now).bookQ, nowMode, nowLang, wordDataList.get(now).numberInBook);
			try {
				mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
				if (mediaPlayer==null){
					//ファイルが存在しない
					//new AlertDialog.Builder(getApplicationContext()).setMessage
					// ("ファイルが存在しません。\n" + path).setPositiveButton("OK", null).show();
					sendBcTextChange(PlayerViewName.eng, "ファイルが存在しません。" + path);
					sendBcTextChange(PlayerViewName.jpn, "ファイルが存在しません。" + path);
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
					if (selectMode == Dictionary.Datatype.mix) {
						//単語->文
						if (nowMode == Dictionary.Datatype.word) {
							//現在単語だった
							nowMode = Dictionary.Datatype.phrase;
						}
						else {
							//文だった
							nowMode = Dictionary.Datatype.word;
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
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public enum SkipContidion {
		all, seikaisu, huseikai, seikairate
	}
	
	public enum SkipThreshold {
		eqormore, eqorless
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
		int loopCount=0;
		int i;
		if (now >= wordDataList.size() - 1) now = 0;
		//既出の単語を飛ばす
		if (dataBook == all) {
			int index = 0;
			do {
				loopCount++;
				now++;
				int sum = 0;
				for (i = 0; i < sizeForBook.size(); i++) {
					sum += sizeForBook.get(i);
					if (now < sum) {
						index = now - (sum - sizeForBook.get(i));
						break;
					}
				}
				try {
					seikaisu = (seikai.get(fileNames.get(i)))[index];
					huseikaisu = huseikai.get(fileNames.get(i))[index];
				} catch (NullPointerException exception) {
					showException(context, exception);
					seikaisu = 0;
					huseikaisu = 0;
				}
			} while ((now != knownWordMap.get(wordDataList.get(now).e) || !skipChecker.apply(seikaisu, huseikaisu)) && now < wordDataList.size() - 1 && loopCount < 1000);
			//printCurrentState("index="+index+"e"+wordDataList.get(now)+"正解"+"不正解"+"filename="+fileNames.get(i));
			//printCurrentState("正解"+seikaisu+"不正解"+huseikaisu);
		}
		else {
			do {
				loopCount++;
				now++;
				printCurrentState("fileName=" + fileName);
				seikaisu = seikai.get(fileName)[now];
				huseikaisu = huseikai.get(fileName)[now];
			} while ((appearedWords.contains(wordDataList.get(now).e) || !skipChecker.apply(seikaisu, huseikaisu)) && now < wordDataList.size() - 1 && loopCount < 1000);
			//printCurrentState("e"+wordDataList.get(now).e+"seikaisu="+seikaisu+"huseikaisu="+huseikaisu);
		}
		if (loopCount>=1000){
			//アクティビティのコンテキストが必要
			//new AlertDialog.Builder(context).setMessage("出題できる問題がありません。条件を変えてやり直してください。")
			// .setPositiveButton("OK",null).create().show();
			sleep(1000);
		}
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
				.putExtra(PLAYER_VIEW_NAME, viewName)
				.putExtra(PLAYER_VIEW_TEXT, text);
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
	 * fragment_playerのビューに表示する文字列と行数を指定する
	 *  以前は文字列の変更時に呼び出し、その後行数を指定する関数を呼び出していたが、そうするとテキストが変更されてから行数が変更されるまでに少しのラグがあり、長い文が一瞬一行で小さく表示されてしまう問題があった。
	 * <br> ※単語を表示するときは(どれだけ長くても)一行で表示し、英文を表示するときは複数行で表示するため、行数の指定が必要。
	 *
	 * @param viewName
	 * @param text
	 * @param isSingleLine
	 */
	private void sendBcTextLinesChange(PlayerViewName viewName,String text, boolean isSingleLine) {
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_NAME, viewName)
				.putExtra(PLAYER_VIEW_TEXT, text)
				.putExtra(PLAYER_VIEW_SINGLE_LINE, isSingleLine);
		context.sendBroadcast(broadcastIntent);
	}
	
	/**
	 * activity_pipのビューに表示する文字列と行数を指定する
	 *
	 * @param viewName
	 * @param text
	 * @param isSingleLine
	 */
	private void sendBcTextLinesChange(PipActivity.PipViewName viewName,String text, boolean isSingleLine) {
		Intent broadcastIntent =
			new Intent(PIP_ACTION_UI)
				.putExtra(PIP_VIEW_NAME, viewName)
				.putExtra(PIP_VIEW_TEXT, text)
				.putExtra(PIP_VIEW_SINGLE_LINE, isSingleLine);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void runOnUiThread(Runnable runnable) {
		new Handler(Looper.getMainLooper()).post(runnable);
	}
}