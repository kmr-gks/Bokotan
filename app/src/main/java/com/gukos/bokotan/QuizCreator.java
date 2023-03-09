package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.WordPhraseData.DataBook;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEx;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class QuizCreator {
	
	private final HandlerThread handlerThread;
	private final Handler handler;
	private final Context context;
	public static final String
		QTHREAD_ACTION_CLICKED = "qthread_action_clicked",
		QTHREAD_EXTRA_CHOICE = "qthread_extra_choice";
	private boolean onActive = true;
	private final SoundPool soundPool =
		new SoundPool.Builder()
			.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
			.setMaxStreams(2)
			.build();
	private final ArrayList<String> e = new ArrayList<>(), j = new ArrayList<>();
	private int nProblems = 0;
	int ansChoice, problemNum;
	private final Random random = new Random();
	
	static final class QuizWordData {
		public final String e, j;
		public final int no;
		public final DataBook dataBook;
		public final String dataQ;
		
		public QuizWordData(String e, String j, int no, DataBook dataBook, String dataQ) {
			this.e = e;
			this.j = j;
			this.no = no;
			this.dataBook = dataBook;
			this.dataQ = dataQ;
		}
		
		public String toString() {
			return "e=" + e + ",no=" + no + ",book=" + dataBook + ",q=" + dataQ;
		}
	}
	
	private final ArrayList<QuizWordData> list = new ArrayList<>();
	
	//コンストラクタ
	public QuizCreator(Context context, DataBook dataBook, WordPhraseData.DataQ dataQ) {
		this.context = context;
		handlerThread = new HandlerThread(getClassName());
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper()) {
			//looperを指定しているので、クイズスレッドで実行される
			//選択肢のボタンをクリックしたときの処理
			@Override
			public void handleMessage(Message message) {
				int choice = message.getData().getInt(QTHREAD_EXTRA_CHOICE, -1);
				if (ansChoice == choice) {
					if (QSentakuFragment.switchQuizOX.isChecked()) {
						soundPool.load(context, R.raw.seikai, 1);
					}
					sendBroadcastTextChange(TestFragment.ViewName.Ans, "正解" + list.get(problemNum).toString());
					sendBroadcastTextChange(TestFragment.ViewName.Marubatsu, "○");
					sendBroadcastColorChange(TestFragment.ViewName.Marubatsu, Color.RED);
				}
				else {
					if (QSentakuFragment.switchQuizOX.isChecked()) {
						soundPool.load(context, R.raw.huseikai, 1);
					}
					sendBroadcastTextChange(TestFragment.ViewName.Ans, "不正解 " + list.get(problemNum).toString());
					sendBroadcastTextChange(TestFragment.ViewName.Marubatsu, "×");
					sendBroadcastColorChange(TestFragment.ViewName.Marubatsu, Color.BLUE);
				}
				setMondai();
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
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(QTHREAD_ACTION_CLICKED));
			soundPool.setOnLoadCompleteListener((soundPool, id, status) -> soundPool.play(id, 1, 1, 1, 0, 1));
			
			//これを定期的に見る必要がある。
			if (!onActive) return;
			//単語データ読み取り
			String qString = dataQ.toString();
			switch (dataBook) {
				case passTan: {
					new WordPhraseData(PasstanWord + qString, context, list, passTan, qString);
					break;
				}
				case tanjukugo: {
					new WordPhraseData(TanjukugoWord + qString, context, list, tanjukugo, qString);
					new WordPhraseData(TanjukugoEXWord + qString, context, list, tanjukugoEx, qString);
					break;
				}
				case yumetan: {
					new WordPhraseData(YumeWord + qString, context, list, yumetan, qString);
					break;
				}
				default: {
					for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
						new WordPhraseData(PasstanWord + q, context, list, passTan, q);
					for (var q : new String[]{"1q", "p1q"})
						new WordPhraseData(TanjukugoWord + q, context, list, tanjukugo, q);
					for (var q : new String[]{"1q", "p1q"})
						new WordPhraseData(TanjukugoEXWord + q, context, list, tanjukugoEx, q);
					for (var q : new String[]{"00", "08", "1", "2", "3"})
						new WordPhraseData(YumeWord + q, context, list, yumetan, q);
					break;
				}
			}
			setMondai();
		});
	}
	
	//クイズスレッド
	private void setMondai() {
		nProblems++;
		//正解の選択肢を設定
		ansChoice = random.nextInt(4) + 1;
		//出題する単語を決定
		problemNum = random.nextInt(list.size());
		sendBroadcastTextChange(TestFragment.ViewName.No, nProblems + "問目 No." + problemNum + "list:" + list.get(problemNum).toString());
		if (QSentakuFragment.switchQuizHatsuon.isChecked()) {
			//単語を再生
			String mp3Path = null;
			String q = list.get(problemNum).dataQ;
			int no = list.get(problemNum).no;
			DataBook dataBook = list.get(problemNum).dataBook;
			//パス単1q,p1qのみ再生
			if (dataBook == passTan && q.endsWith("1q")) {
				mp3Path = MyLibrary.FileDirectoryManager.getPath(passTan, q, word, english, no);
			}
			if (dataBook == yumetan) {
				mp3Path = MyLibrary.FileDirectoryManager.getPath(yumetan, "y" + q, word, english, no);
			}
			if (dataBook == tanjukugo) {
				puts("getPath:" + tanjukugo + "," + q + "," + word + "," + english + "," + no);
				mp3Path = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugo" + q, word, english, no);
			}
			if (mp3Path != null) {
				soundPool.load(mp3Path, 1);
				sendBroadcastTextChange(TestFragment.ViewName.Debug, mp3Path);
			}
		}
		
		var choiceList = new int[4];
		//4つの選択肢はそれぞれ異なる
		do {
			for (int i = 0; i < 4; i++) {
				if (ansChoice - 1 == i) choiceList[i] = problemNum;
				else choiceList[i] = random.nextInt(list.size());
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
		//printCurrentState(",view=" + viewName + ",text=" + text);
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE)
				.putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.Text)
				.putExtra(TestFragment.QUIZ_VIEW_TEXT, text)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
	
	private void sendBroadcastColorChange(TestFragment.ViewName viewName, int color) {
		//printCurrentState(",view=" + viewName + ",text=" + text);
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE)
				.putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.TextColor)
				.putExtra(TestFragment.QUIZ_VIEW_COLOR, color)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}