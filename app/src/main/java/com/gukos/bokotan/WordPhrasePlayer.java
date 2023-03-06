package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPath;
import static com.gukos.bokotan.MyLibrary.sleep;
import static com.gukos.bokotan.PlayerFragment.PLAYER_ACTION_UI_CHANGE;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_COLOR;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_NAME;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_PROPERTIES;
import static com.gukos.bokotan.PlayerFragment.PLAYER_VIEW_TEXT;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.Text;
import static com.gukos.bokotan.PlayerFragment.PlayerViewProperties.TextColor;
import static com.gukos.bokotan.PlayerFragment.isInitialized;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;

import java.util.ArrayList;

public class WordPhrasePlayer extends BackgroundThread{
	
	private final ArrayList<QuizCreator.QuizWordData> quizWordDataList = new ArrayList<>();
	private MediaPlayer mediaPlayer;
	private int now=1;
	private String path;
	
	@Override
	public void handleMessage(Message message) {}
	
	@Override
	public void initialize() {
		while (true) {
			synchronized (isInitialized) {
				if (isInitialized) break;
			}
			sleep(100);
		}
		sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng,"読み込み中");
		sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn, "読み込み中");
		
		for (var q : new String[]{"1q", "p1q"})
			new WordPhraseData(PasstanWord + q, context, quizWordDataList, passTan, q);
		onPlay();
		//onPlay(null);
	}
	
	public WordPhrasePlayer(Context context){
		super(context,getClassName());
	}
	
	private void onPlay() {
		//リソースの開放
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
		}
		sendBroadcastTextChange(PlayerFragment.PlayerViewName.genzai, "No."+quizWordDataList.get(now).no);
		sendBroadcastTextChange(PlayerFragment.PlayerViewName.eng,quizWordDataList.get(now).e);
		sendBroadcastTextChange(PlayerFragment.PlayerViewName.jpn,quizWordDataList.get(now).j);
		path = MyLibrary.FileDirectoryManager.getPath(passTan, "1q", word, english, now);
		mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener((mp) -> handler.post(this::onPlay));
		now++;
	}
	
	private void onPlay(MediaPlayer mediaPlayer) {
		if (mediaPlayer!=null){
			mediaPlayer.stop();
			mediaPlayer.reset();
			mediaPlayer.release();
		}
		path= MyLibrary.FileDirectoryManager.getPath(passTan, "1q", word, english, now);
		mediaPlayer=MediaPlayer.create(context, Uri.parse(path));
		mediaPlayer.start();
		mediaPlayer.setOnCompletionListener(this::onPlay);
		now++;
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
	
	private void sendBroadcastColorChange(PlayerFragment.PlayerViewName viewName, int color) {
		//printCurrentState(",view=" + viewName + ",text=" + text);
		Intent broadcastIntent =
			new Intent(PLAYER_ACTION_UI_CHANGE)
				.putExtra(PLAYER_VIEW_PROPERTIES,TextColor)
				.putExtra(PLAYER_VIEW_COLOR, color)
				.putExtra(PLAYER_VIEW_NAME, viewName);
		context.sendBroadcast(broadcastIntent);
	}
}