package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

import kotlin.jvm.functions.Function3;

public class QuizCreator {
	
	private HandlerThread handlerThread;
	private Handler handler;
	private Context context;
	public static final String
		QTHREAD_ACTION_CLICKED = "qthread_action_clicked",
		QTHREAD_EXTRA_CHOICE = "qthread_extra_choice";
	private boolean onActive = true;
	private ArrayList<String> e = new ArrayList<>(), j = new ArrayList<>();
	private final Random random = new Random();
	
	//メインスレッド
	public QuizCreator(Context context) {
		printCurrentState();
		this.context = context;
		
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
			//Testragmentの画面読み込みが終わるまで待機(スレッドが違うため、こちらの処理の方が早いことがある。)
			while (true) {
				synchronized (TestFragment.isInitialized) {
					if (TestFragment.isInitialized) break;
				}
				sleep(100);
			}
			sendBroadcast(TestFragment.ViewName.Mondaibun, "読み込み中");
			
			context.registerReceiver(new DrawReceiver(handler), new IntentFilter(QTHREAD_ACTION_CLICKED));
			
			//これを定期的に見る必要がある。
			if (!onActive) return;
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
		});
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
	}
	
	//TestFragment内のviewに表示する文字を変更させるためのintentを送信する。クイズスレッド
	private void sendBroadcast(TestFragment.ViewName viewName, String text) {
		printCurrentState(",view=" + viewName + ",text=" + text);
		Intent broadcastIntent =
			new Intent(TestFragment.QUIZ_ACTION_UI)
				.putExtra(TestFragment.QUIZ_UI_TEXT, text)
				.putExtra(TestFragment.QUIZ_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}