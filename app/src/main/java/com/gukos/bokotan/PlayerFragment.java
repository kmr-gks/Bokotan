package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.getMethodName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_ACTION;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_ENG_SPEED;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_JPN_SPEED;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_NOW;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_STOP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_TYPE;
import static com.gukos.bokotan.WordPhraseData.sentakuQ;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.text.LineBreakConfig;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentPlayerBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class PlayerFragment extends UiManager.FragmentBingding<FragmentPlayerBinding> {
	public static Boolean isInitialized = false;
	public static final HashMap<String, String> hashMapKishutu = new HashMap<>();
	
	public static int lastnum, selectedIndex = 0;
	public static boolean playing = false, nowIsDecided = false;
	public static TextView tvWordEng, tvWordJpn, tvGenzai, tvsubE, tvsubJ, tvNumSeikaisuu, tvSeikaisu, tvGogen, textViewPath, textViewHatsuonKigou;
	private static ArrayAdapter<String> adapterUnit;
	AlertDialog adWord, adUnit;
	
	public static final String
		PLAYER_ACTION_UI_CHANGE = "player_action_ui_change",
		PLAYER_VIEW_TEXT = "player_view_text",
		PLAYER_VIEW_COLOR = "player_view_color",
		PLAYER_VIEW_PROPERTIES = "player_view_properties",
		PLAYER_VIEW_NAME = "player_view_name";
	
	public enum PlayerViewProperties {
		Text, TextColor
	}
	
	public enum PlayerViewName {
		genzai, hatsuon, subJ, subE, eng, jpn, path,
		Debug
	}
	
	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			PlayerViewName viewName = (PlayerViewName) bundle.getSerializable(PLAYER_VIEW_NAME);
			PlayerViewProperties viewProperties = (PlayerViewProperties) bundle.getSerializable(PLAYER_VIEW_PROPERTIES);
			final TextView textViewToHandle;
			switch (viewName) {
				case genzai: {
					textViewToHandle = binding.textViewGenzai;
					break;
				}
				case hatsuon: {
					textViewToHandle = binding.textViewHatsuonKigou;
					break;
				}
				case subJ: {
					textViewToHandle = binding.textViewSubtitleJpn;
					break;
				}
				case subE: {
					textViewToHandle = binding.textViewSubtitleEng;
					break;
				}
				case eng: {
					textViewToHandle = binding.textViewEng;
					break;
				}
				case jpn: {
					textViewToHandle = binding.textViewJpn;
					break;
				}
				case path: {
					textViewToHandle = binding.textViewPath;
					break;
				}
				case Debug: {
					//textViewToHandle=binding.
					//break;
				}
				default: {
					throw new IllegalStateException("view name is invalid");
				}
			}
			switch (viewProperties) {
				case Text: {
					textViewToHandle.setText(bundle.getString(PLAYER_VIEW_TEXT));
					break;
				}
				case TextColor: {
					textViewToHandle.setTextColor(bundle.getInt(PLAYER_VIEW_COLOR));
				}
			}
		}
	};
	
	public PlayerFragment() {
		super(FragmentPlayerBinding::inflate);
	}
	
	//ActivityのonCreateに相当
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			try {
				//UI設定
				context.registerReceiver(new DrawReceiver(drawHandler), new IntentFilter(PLAYER_ACTION_UI_CHANGE));
				tvWordEng = binding.textViewEng;
				tvWordJpn = binding.textViewJpn;
				tvGenzai = binding.textViewGenzai;
				tvsubE = binding.textViewSubtitleEng;
				tvsubJ = binding.textViewSubtitleJpn;
				tvGogen = binding.textViewGogen;
				tvNumSeikaisuu = binding.textViewNumSeikairitu;
				tvSeikaisu = binding.textViewSeikaisuu;
				textViewPath = binding.textViewPath;
				textViewHatsuonKigou = binding.textViewHatsuonKigou;
				
				//Android13以降 日本語の折り返しに対応
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					binding.textViewJpn.setLineBreakStyle(LineBreakConfig.LINE_BREAK_STYLE_NORMAL);
					binding.textViewJpn.setLineBreakWordStyle(LineBreakConfig.LINE_BREAK_WORD_STYLE_PHRASE);
				}
				
				binding.buttonNowChange.setOnClickListener(this::onSelectNowButtonClick);
				binding.buttonToBegin.setOnClickListener(this::onResetButtonClick);
				binding.buttonPip.setOnClickListener(this::onPIPButtonClicked);
				binding.seekBarEng.setOnSeekBarChangeListener((UiManager.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				binding.seekBarEng.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "english", 5));
				onSpeedSeekBar(binding.seekBarEng);
				binding.seekBarJpn.setOnSeekBarChangeListener((UiManager.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				binding.seekBarJpn.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "japanese", 10));
				onSpeedSeekBar(binding.seekBarJpn);
				binding.buttonStopService.setOnClickListener(this::onPlayerServiceStop);
				
				synchronized (isInitialized) {
					isInitialized = true;
				}
			} catch (Exception e) {
				showException(context, e);
			}
			
			puts(getMethodName() + " ended");
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public static void initialize(Context context) {
		try {
			//再生開始
			puts(getClassName() + getMethodName() + " start");
			playing = true;
			
			hashMapKishutu.clear();
			//バグ対策
			hashMapKishutu.put("smooth out 〜", "pass" + "p1q");    //1799
			hashMapKishutu.put("grow into 〜", "p1q");                //1675
			hashMapKishutu.put("accrue", "pass" + "1q");            //1568

			adapterUnit = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			if (sentakuQ.equals(WordPhraseData.q_num.test1q) || sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				ArrayList<String> strUnit = new ArrayList<>(Arrays.asList("でる度A動詞", "でる度A名詞", "でる度A形容詞", "でる度B動詞", "でる度B名詞", "でる度B形容詞", "でる度C動詞", "でる度C名詞", "でる度C形容詞", "熟語"));
				for (int i = 0; i < 10; i++) {
					WordPhraseData.SetNumFromAndTo(lastnum, i);
					//adapterUnit.add(strUnit.get(i) + String.format(" (%d-%d)", PlaySound.from,PlaySound.to));
				}
			}
			if (sentakuQ.equals(WordPhraseData.q_num.test1qEx)) {
				for (int i = 0; i <= 9; i++) {
					adapterUnit.add("Unit" + (i + 1) + " (" + WordPhraseData.toFindFromAndTo[12][i][0] + "-" + WordPhraseData.toFindFromAndTo[12][i][1] + ")");
				}
				adapterUnit.add("UnitEX" + " (" + WordPhraseData.toFindFromAndTo[12][10][0] + "-" + WordPhraseData.toFindFromAndTo[12][10][1] + ")");
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testp1qEx)) {
				for (int i = 0; i <= 9; i++) {
					adapterUnit.add("Unit" + (i + 1) + " (" + WordPhraseData.toFindFromAndTo[13][i][0] + "-" + WordPhraseData.toFindFromAndTo[13][i][1] + ")");
				}
				adapterUnit.add("UnitEX" + " (" + WordPhraseData.toFindFromAndTo[13][10][0] + "-" + WordPhraseData.toFindFromAndTo[13][10][1] + ")");
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testy08) || sentakuQ.equals(WordPhraseData.q_num.testy3)) {
				for (int i = 1; i <= 8; i++) {
					adapterUnit.add("Unit" + i + " (" + ((i - 1) * 100 + 1) + "-" + i * 100 + ")");
				}
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testy1) || sentakuQ.equals(WordPhraseData.q_num.testy2)) {
				for (int i = 1; i <= 10; i++) {
					adapterUnit.add("Unit" + i + " (" + ((i - 1) * 100 + 1) + "-" + i * 100 + ")");
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onSelectNowButtonClick(View v) {
		try {
			adapterUnit=null;
			AlertDialog.Builder builder =
				new AlertDialog.Builder(context).setTitle("選択してください").setSingleChoiceItems(adapterUnit, selectedIndex, (dialog, which) -> {
				selectedIndex = which;
				adUnit.dismiss();
				askTangoNumber(selectedIndex);
			});
			adUnit = builder.create();
			adUnit.show();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void askTangoNumber(int unit) {
		try {
			WordPhraseData.SetNumFromAndTo(lastnum, unit);
			
			ArrayAdapter<String> adapterWord = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			/*
			if (sentakuQ.equals(WordPhraseData.q_num.testy08)
				|| sentakuQ.equals(WordPhraseData.q_num.testy1)
				|| sentakuQ.equals(WordPhraseData.q_num.testy2)
				|| sentakuQ.equals(WordPhraseData.q_num.testy3)) {
				PlaySound.from = unit * 100 + 1;
				PlaySound.to = (unit + 1) * 100;
			}
			if (sentakuQ.equals(WordPhraseData.q_num.test1qEx)) {
				PlaySound.from = WordPhraseData.toFindFromAndTo[12][unit][0];
				PlaySound.to = WordPhraseData.toFindFromAndTo[12][unit][1];
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testp1qEx)) {
				PlaySound.from = WordPhraseData.toFindFromAndTo[13][unit][0];
				PlaySound.to = WordPhraseData.toFindFromAndTo[13][unit][1];
			}
			for (int i = PlaySound.from; i <= PlaySound.to; i++) {
				adapterWord.add(i + ":" + PlaySound.wordE[i] + " (" + PlaySound.wordJ[i] + ")");
			}
			*/
			adapterWord.add("aaa");
			adapterWord.add("bbb");
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("単語を選択してください");
			builder.setSingleChoiceItems(adapterWord, 0, this::onWordSelect);
			adWord = builder.create();
			adWord.show();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onWordSelect(DialogInterface dialog, int which) {
		try {
			//単語を選んだあと
			adWord.dismiss();
			context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE,PLAYERSERVICE_MESSAGE_NOW).putExtra(PLAYERSERVICE_MESSAGE_NOW,which));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onPlayerServiceStop(View view) {
		puts(getMethodName());
		Intent broadcastIntent = new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP);
		context.sendBroadcast(broadcastIntent);
	}
	
	public void onResetButtonClick(View v) {
		try {
			context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE,PLAYERSERVICE_MESSAGE_NOW).putExtra(PLAYERSERVICE_MESSAGE_NOW,1));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onPIPButtonClicked(View view) {
		try {
			if (PipActivity.startPIP) {
				//PIPを終了したい
			}
			else {
				startActivity(new Intent(context, PipActivity.class));
			}
			PipActivity.startPIP = !PipActivity.startPIP;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onSpeedSeekBar(View v) {
		try {
			SeekBar sb = (SeekBar) v;
			float speed = 1 + sb.getProgress() * 0.1f;
			if (sb.getId() == R.id.seekBarEng) {
				//英語
				binding.textViewSeekBarEng.setText(String.format("英語速度:%.1f", speed));
				//PlaySound.dPlaySpeedEng = speed;
				putIntData(context, "SeekBar", "english", sb.getProgress());
				context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_ENG_SPEED).putExtra(PLAYERSERVICE_MESSAGE_ENG_SPEED, speed));
			}
			else if (sb.getId() == R.id.seekBarJpn) {
				//日本語
				binding.textViewSeekBarJpn.setText(String.format("日本語速度:%.1f", speed));
				//PlaySound.dPlaySpeedJpn = speed;
				putIntData(context, "SeekBar", "japanese", sb.getProgress());
				context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_JPN_SPEED).putExtra(PLAYERSERVICE_MESSAGE_JPN_SPEED, speed));
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onSpeedSeekBar(SeekBar seekBar, int i, boolean b) {
		onSpeedSeekBar(seekBar);
	}
}