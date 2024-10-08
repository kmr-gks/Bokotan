package com.gukos.bokotan;


import static com.gukos.bokotan.Dictionary.BookName.PasstanWordData;
import static com.gukos.bokotan.Dictionary.BookName.tanjukugoWord;
import static com.gukos.bokotan.Dictionary.BookName.yumetanWord;
import static com.gukos.bokotan.Dictionary.BookQ;
import static com.gukos.bokotan.Dictionary.BookQ.all;
import static com.gukos.bokotan.Dictionary.BookQ.q1;
import static com.gukos.bokotan.Dictionary.BookQ.qp1;
import static com.gukos.bokotan.Dictionary.DataLang.english;
import static com.gukos.bokotan.Dictionary.Folder;
import static com.gukos.bokotan.Dictionary.Folder.tanjukugo;
import static com.gukos.bokotan.Dictionary.QuizData.huseikai;
import static com.gukos.bokotan.Dictionary.QuizData.monme;
import static com.gukos.bokotan.Dictionary.QuizData.seikai;
import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.sleep;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiFunction;

public class QuizCreator {
	private static QuizCreator instance = null;
	
	private final HandlerThread handlerThread;
	private final Handler handler;
	private final Context context;
	private BroadcastReceiver broadcastReceiver;
	public static final String
		QTHREAD_ACTION_CLICKED = "qthread_action_clicked",
		QTHREAD_EXTRA_CHOICE = "qthread_extra_choice",
		QTHREAD_EXTRA_STOP = "qes";
	private boolean onActive = true;
	private final SoundPool soundPool =
		new SoundPool.Builder()
			.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
			.setMaxStreams(2)
			.build();
	BookQ dataQ;
	private int nProblems = 0;
	int ansChoice, problemNum;
	private final int[] choiceList = new int[4];
	private String fileName;
	private final Random random = new Random();
	private ArrayList<Dictionary.Entry> list = new ArrayList<>();
	
	//全範囲から問題を出す時に使用する。全体の通し番号から、本の名前と、その本の中の通し番号を返す。
	private ArrayList<String> keyForBook = null;
	private ArrayList<Integer> sizeForBook = null;
	private ArrayList<String> fileNameForBook = null;
	private BiFunction<Integer, Integer, Boolean> skipChecker;
	
	private QuizCreator(Context context, Folder dataBook, BookQ dataQ, PlayerService.SkipContidion skipContidion, double thresholdNum, PlayerService.SkipThreshold skipThreshold) {
		this.context = context;
		switch (skipContidion) {
			case all: {
				skipChecker = (seikai, huseikai) -> true;
				break;
			}
			case seikaisu: {
				if (skipThreshold == PlayerService.SkipThreshold.eqormore)
					skipChecker = (seikai, huseikai) -> seikai >= thresholdNum;
				else
					skipChecker = (seikai, huseikai) -> seikai < thresholdNum;
				break;
			}
			case huseikai: {
				if (skipThreshold == PlayerService.SkipThreshold.eqormore)
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
		handlerThread = new HandlerThread(getClassName());
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			//looperを指定しているので、クイズスレッドで実行される
			//選択肢のボタンをクリックしたときの処理
			@Override
			public void handleMessage(Message message) {
				var bundle = message.getData();
				if (bundle.containsKey(QTHREAD_EXTRA_CHOICE)) {
					//選択肢を押した時
					int choice = bundle.getInt(QTHREAD_EXTRA_CHOICE, -1);
					if (ansChoice == choice) {
						if (QSentakuFragment.switchQuizOX.isChecked()) {
							soundPool.load(context, R.raw.seikai, 1);
						}
						sendBroadcastTextChange(TestFragment.ViewName.Marubatsu, "○");
						sendBroadcastColorChange(TestFragment.ViewName.Marubatsu, Color.RED);
						seikai.get(fileName)[problemNum]++;
					}
					else {
						if (QSentakuFragment.switchQuizOX.isChecked()) {
							soundPool.load(context, R.raw.huseikai, 1);
						}
						sendBroadcastTextChange(TestFragment.ViewName.Marubatsu, "×");
						sendBroadcastColorChange(TestFragment.ViewName.Marubatsu, Color.BLUE);
						huseikai.get(fileName)[problemNum]++;
					}
					var editorial = new StringBuilder();
					for (var i = 0; i < 4; i++) {
						var info = list.get(choiceList[i]);
						if (i == ansChoice - 1) {
							editorial.append("<font color=\"red\">").append(info.e).append(" ").append(info.j).append("</font>").append("<br>");
						}
						else {
							editorial.append(info.e).append(" ").append(info.j).append("<br>");
						}
					}
					sendBroadcastTextChange(TestFragment.ViewName.Editorial, Html.fromHtml(editorial.toString(), Html.FROM_HTML_MODE_COMPACT));
					setMondai();
				}
				else if (bundle.containsKey(QTHREAD_EXTRA_STOP)) {
					//クイズを終了する処理 表示を消す
					for (var viewName : TestFragment.ViewName.values()) {
						sendBroadcastTextChange(viewName, null);
					}
				}
			}
		};
		//handler.post()はクイズスレッドで、最初に実行される
		handler.post(() -> {
			//クイズを開始する処理
			//Testragmentの画面読み込みが終わるまで待機(スレッドが違うため、こちらの処理の方が早いことがある。)
			while (true) {
				synchronized (TestFragment.isInitialized) {
					if (TestFragment.isInitialized) break;
				}
				sleep(100);
			}
			sendBroadcastTextChange(TestFragment.ViewName.Mondaibun, "読み込み中");
			sendBroadcastTextChange(TestFragment.ViewName.Idontknow, "わかりません");
			
			broadcastReceiver = new DrawReceiver(handler);
			context.registerReceiver(broadcastReceiver, new IntentFilter(QTHREAD_ACTION_CLICKED));
			soundPool.setOnLoadCompleteListener((soundPool, id, status) -> soundPool.play(id, 1, 1, 1, 0, 1));
			
			//これを定期的に見る必要がある。
			if (!onActive) return;
			String qString = dataQ.toString();
			this.dataQ = dataQ;
			if (dataBook == tanjukugo) {
				fileName = dnTestActivity + "tanjukugo" + qString + "Test";
			}
			else {
				fileName = dnTestActivity + qString + "Test";
			}
			switch (dataBook) {
				case passtan: {
					list = Dictionary.getList(PasstanWordData, dataQ);
					break;
				}
				case tanjukugo: {
					list = Dictionary.getList(tanjukugoWord, dataQ);
					/*
					if (dataQ == qp1) {
						list = new ArrayList<>(list.subList(0, 1680 + 1));
					}
					*/
					break;
				}
				case yumetan: {
					list = Dictionary.getList(yumetanWord, dataQ);
					break;
				}
				default: {
					//全範囲から出題
					keyForBook = new ArrayList<>(Arrays.asList(
						PasstanWordData.toString() + q1,
						PasstanWordData.toString() + qp1,
						tanjukugoWord.toString() + q1,
						tanjukugoWord.toString() + qp1,
						yumetanWord.toString() + Dictionary.BookQ.y1,
						yumetanWord.toString() + Dictionary.BookQ.y2,
						yumetanWord.toString() + Dictionary.BookQ.y3
					));
					fileNameForBook = new ArrayList<>(Arrays.asList(
						dnTestActivity + "1q" + "Test",
						dnTestActivity + "p1q" + "Test",
						dnTestActivity + "tanjukugo1q" + "Test",
						dnTestActivity + "tanjukugop1q" + "Test",
						dnTestActivity + "y1" + "Test",
						dnTestActivity + "y2" + "Test",
						dnTestActivity + "y3" + "Test"
					));
					sizeForBook = new ArrayList<>();
					for (var key : keyForBook) {
						var addList = Dictionary.getList(key);
						/*
						if (key.equals(TanjukugoWord + Dictionary.BookQ.qp1)) {
							addList = new ArrayList<>(list.subList(0, 1680 + 1));
						}
						 */
						sizeForBook.add(addList.size());
					}
					list.addAll(Dictionary.getList(PasstanWordData, q1));
					list.addAll(Dictionary.getList(PasstanWordData, qp1));
					list.addAll(Dictionary.getList(tanjukugoWord, q1));
					list.addAll(Dictionary.getList(tanjukugoWord, qp1));
					list.addAll(Dictionary.getList(yumetanWord, BookQ.y1));
					list.addAll(Dictionary.getList(yumetanWord, BookQ.y2));
					list.addAll(Dictionary.getList(yumetanWord, BookQ.y3));
					break;
				}
			}
			setMondai();
		});
	}
	
	//コンストラクタ
	public static QuizCreator build(Context context, Folder dataBook, BookQ dataQ, PlayerService.SkipContidion skipContidion, double skipThresholdNum, PlayerService.SkipThreshold skipThreshold) {
		synchronized (QuizCreator.class) {
			if (instance != null) {
				instance.stop();
			}
			instance = new QuizCreator(context, dataBook, dataQ, skipContidion, skipThresholdNum, skipThreshold);
			return instance;
		}
	}
	
	private void stop() {
		try {
			context.unregisterReceiver(broadcastReceiver);
		} catch (Exception exception) {
			showException(context, exception);
		}
	}
	
	//クイズスレッド
	private void setMondai() {
		nProblems++;
		int seikaisu, huseikaisu;
		int loopCount = 0;
		//正解の選択肢を設定
		do {
			loopCount++;
			ansChoice = random.nextInt(4) + 1;
			//出題する単語を決定
			problemNum = random.nextInt(list.size());
			if (dataQ == all) {
				//全範囲から出題
				int sum = 0;
				for (int i = 0; i < sizeForBook.size(); i++) {
					sum += sizeForBook.get(i);
					if (problemNum < sum) {
						//i番目の本から出題
						list = Dictionary.getList(keyForBook.get(i));
						problemNum -= sum - sizeForBook.get(i);
						fileName = fileNameForBook.get(i);
						break;
					}
				}
			}
			seikaisu = seikai.get(fileName)[problemNum];
			huseikaisu = huseikai.get(fileName)[problemNum];
		} while (!skipChecker.apply(seikaisu, huseikaisu) && loopCount < 1000);
		if (loopCount >= 1000) {
			new AlertDialog.Builder(context).setMessage("出題できる問題がありません。条件を変えてやり直してください。").setPositiveButton("OK", null).create().show();
			return;
		}
		
		sendBroadcastTextChange(TestFragment.ViewName.No, nProblems + "問目 No." + problemNum);
		sendBroadcastTextChange(TestFragment.ViewName.monme, monme.get(fileName) + "問目" + " 正解率" + seikaisu + "/" + (seikaisu + huseikaisu));
		//問目++
		monme.put(fileName, monme.get(fileName) + 1);
		
		if (QSentakuFragment.switchQuizHatsuon.isChecked()) {
			//単語を再生
			var path = list.get(problemNum).toPath(english);
			try {
				soundPool.load(path, 1);
			} catch (Exception exception) {
				showException(context, exception);
			}
		}
		
		//4つの選択肢はそれぞれ異なる
		do {
			for (int i = 0; i < 4; i++) {
				if (ansChoice - 1 == i) choiceList[i] = problemNum;
				else {
					choiceList[i] = random.nextInt(list.size());
				}
			}
		} while (Arrays.stream(choiceList).distinct().count() != 4);
		
		sendBroadcastTextChange(TestFragment.ViewName.Mondaibun, list.get(problemNum).e);
		sendBroadcastTextChange(TestFragment.ViewName.Select1, list.get(choiceList[0]).j);
		sendBroadcastTextChange(TestFragment.ViewName.Select2, list.get(choiceList[1]).j);
		sendBroadcastTextChange(TestFragment.ViewName.Select3, list.get(choiceList[2]).j);
		sendBroadcastTextChange(TestFragment.ViewName.Select4, list.get(choiceList[3]).j);
	}
	
	public void cancel() {
		onActive = false;
	}
	
	//TestFragment内のviewに表示する文字を変更させるためのintentを送信する。クイズスレッド
	private void sendBroadcastTextChange(TestFragment.ViewName viewName, String text) {
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE)
				.putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.Text)
				.putExtra(TestFragment.QUIZ_VIEW_TEXT_STRING, text)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void sendBroadcastTextChange(TestFragment.ViewName viewName, CharSequence text) {
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE)
				.putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.Text)
				.putExtra(TestFragment.QUIZ_VIEW_TEXT_CHARSEQ, text)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void sendBroadcastColorChange(TestFragment.ViewName viewName, int color) {
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE)
				.putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.TextColor)
				.putExtra(TestFragment.QUIZ_VIEW_COLOR, color)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}