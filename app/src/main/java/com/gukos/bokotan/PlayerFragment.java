package com.gukos.bokotan;

import static android.view.View.GONE;
import static com.gukos.bokotan.CommonVariables.hashMapKishutu;
import static com.gukos.bokotan.CommonVariables.now;
import static com.gukos.bokotan.CommonVariables.nowIsDecided;
import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.getMethodName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.CommonVariables.isWordAndPhraseMode;
import static com.gukos.bokotan.CommonVariables.sentakuQ;
import static com.gukos.bokotan.CommonVariables.sentakuUnit;
import static com.gukos.bokotan.CommonVariables.swOnlyFirst;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoPhrase;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerFragment extends Fragment {
	Context context;
	Activity activity;
	View viewFragment;
	private static ArrayAdapter<String> adapterUnit;
	protected int selectedIndex = 0;
	AlertDialog adWord, adUnit;
	
	public PlayerFragment() {
		super();
		puts(getClassName() + getMethodName());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_player, container, false);
	}
	
	private <T extends View> T findViewById(int id) {return viewFragment.findViewById(id);}
	
	//ActivityのonCreateに相当
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			puts(getClassName() + getMethodName());
			context = getContext();
			activity = getActivity();
			viewFragment = view;
			try {
				puts(getClassName() + getMethodName() + " start");
				
				//UI設定
				CommonVariables.tvWordEng = findViewById(R.id.textViewEng);
				CommonVariables.tvWordJpn = findViewById(R.id.textViewJpn);
				CommonVariables.tvGenzai = findViewById(R.id.textViewGenzai);
				CommonVariables.tvsubE = findViewById(R.id.textViewSubtitleEng);
				CommonVariables.tvsubJ = findViewById(R.id.textViewSubtitleJpn);
				CommonVariables.tvGogen = findViewById(R.id.textViewGogen);
				CommonVariables.tvNumSeikaisuu = findViewById(R.id.textViewNumSeikairitu);
				CommonVariables.tvSeikaisu = findViewById(R.id.textViewSeikaisuu);
				CommonVariables.textViewPath = findViewById(R.id.textViewPath);
				CommonVariables.textViewHatsuonKigou = findViewById(R.id.textViewHatsuonKigou);
				if (CommonVariables.strQ != null)
					CommonVariables.isPhraseMode = CommonVariables.strQ.charAt(1) == 'h';
				if (isWordAndPhraseMode || CommonVariables.isPhraseMode) {
					CommonVariables.tvsubE.setVisibility(View.VISIBLE);
					CommonVariables.tvsubJ.setVisibility(View.VISIBLE);
				}
				else {
					//単語の場合は右下の文字は非表示
					CommonVariables.tvsubE.setVisibility(GONE);
					CommonVariables.tvsubJ.setVisibility(GONE);
				}
				
				Button buttonStartStop = findViewById(R.id.buttonStartStop);
				buttonStartStop.setOnClickListener(this::onStartStopButtonClick);
				buttonStartStop.setText(CommonVariables.playing ? "stop" : "start");
				findViewById(R.id.buttonNowChange).setOnClickListener(this::onSelectNowButtonClick);
				findViewById(R.id.buttonToBegin).setOnClickListener(this::onResetButtonClick);
				findViewById(R.id.buttonPip).setOnClickListener(this::onPIPButtonClicked);
				
				activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
				
				if (CommonVariables.strQ == null) {
					CommonVariables.strQ = "p1q";
					sentakuQ = WordPhraseData.q_num.testp1q;
				}
				
				SeekBar sbE = findViewById(R.id.seekBarEng);
				sbE.setOnSeekBarChangeListener((MyLibrary.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				sbE.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "english", 5));
				onSpeedSeekBar(sbE);
				SeekBar sbJ = findViewById(R.id.seekBarJpn);
				sbJ.setOnSeekBarChangeListener((MyLibrary.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				sbJ.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "japanese", 10));
				onSpeedSeekBar(sbJ);
				puts(getClassName() + getMethodName() + " ended");
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
			CommonVariables.playing = true;
			
			hashMapKishutu.clear();
			//バグ対策
			hashMapKishutu.put("smooth out 〜", "pass" + "p1q");    //1799
			hashMapKishutu.put("grow into 〜", "p1q");                //1675
			hashMapKishutu.put("accrue", "pass" + "1q");            //1568
			
			switch (sentakuQ) {
				case test1q: {
					CommonVariables.lastnum = 2400;
					WordPhraseData w = new WordPhraseData(PasstanWord + "1q", context);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "1q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					if (swOnlyFirst.isChecked()) {
						//ユメタン単語
						for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
							WordPhraseData wpdy = new WordPhraseData(YumeWord + Q, context);
							for (int i = 1; i < Math.min(wpdy.e.length, wpdy.j.length); i++)
								if (wpdy.e[i] != null && wpdy.j[i] != null)
									hashMapKishutu.put(wpdy.e[i], "yume" + Q);
						}
						//パス単準1級
						//パス単単語
						for (String Q : new String[]{"p1q"/*, "2q", "p2q","3q","4q","5q"*/}) {
							WordPhraseData wpdp = new WordPhraseData(PasstanWord + Q, context);
							for (int i = 1; i < Math.min(wpdp.e.length, wpdp.j.length); i++)
								if (wpdp.e[i] != null && wpdp.j[i] != null)
									hashMapKishutu.put(wpdp.e[i], "pass" + Q);
						}
					}
					break;
				}
				case testp1q: {
					CommonVariables.lastnum = 1850;
					WordPhraseData w = new WordPhraseData(PasstanWord + "p1q", context);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p1q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					if (swOnlyFirst.isChecked()) {
						//ユメタン単語
						for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
							WordPhraseData wpd = new WordPhraseData(YumeWord + Q, context);
							for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
								if (wpd.e[i] != null && wpd.j[i] != null)
									hashMapKishutu.put(wpd.e[i], "yume" + Q);
						}
					}
					break;
				}
				case test2q: {
					CommonVariables.lastnum = 1704;
					WordPhraseData w = new WordPhraseData(PasstanWord + "2q", context);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "2q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testp2q: {
					CommonVariables.lastnum = 1500;
					WordPhraseData w = new WordPhraseData(PasstanWord + "p2q", context);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p2q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case test1qEx: {
					CommonVariables.lastnum = 2811;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "1q", context);
					WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + "1q", context);
					//配列を一旦Streamに変換して結合したあと、配列に戻す。
					ArrayList<String> arrayListE = new ArrayList<>(), arrayListJ = new ArrayList<>();
					arrayListE.add("index");
					arrayListJ.add("index");
					for (int i = 1; w.e[i] != null; i++) arrayListE.add(w.e[i]);
					for (int i = 1; wx.e[i] != null; i++) arrayListE.add(wx.e[i]);
					for (int i = 1; w.j[i] != null; i++) arrayListJ.add(w.j[i]);
					for (int i = 1; wx.j[i] != null; i++) arrayListJ.add(wx.j[i]);
					CommonVariables.wordE = arrayListE.toArray(new String[0]);
					CommonVariables.wordJ = arrayListJ.toArray(new String[0]);
					
					WordPhraseData p = new WordPhraseData(TanjukugoPhrase + "1q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testp1qEx: {
					CommonVariables.lastnum = 2400;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "p1q", context);
					WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + "p1q", context);
					ArrayList<String> arrayListE = new ArrayList<>(), arrayListJ = new ArrayList<>();
					arrayListE.add("index");
					arrayListJ.add("index");
					for (int i = 1; w.e[i] != null; i++) arrayListE.add(w.e[i]);
					for (int i = 1; wx.e[i] != null; i++) arrayListE.add(wx.e[i]);
					for (int i = 1; w.j[i] != null; i++) arrayListJ.add(w.j[i]);
					for (int i = 1; wx.j[i] != null; i++) arrayListJ.add(wx.j[i]);
					CommonVariables.wordE = arrayListE.toArray(new String[0]);
					CommonVariables.wordJ = arrayListJ.toArray(new String[0]);
					
					WordPhraseData p = new WordPhraseData(TanjukugoPhrase + "p1q", context);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testy00: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "00", context);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy08: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "08", context);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy1: {
					CommonVariables.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "1", context);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy2: {
					CommonVariables.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "2", context);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy3: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "3", context);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
			}
			
			if (!nowIsDecided && sentakuUnit.equals(WordPhraseData.q_num.unit.all)) {
				now = MyLibrary.PreferenceManager.getIntData(context, "MainActivity" +
					"now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", 1);
				CommonVariables.nFrom = 1;
				CommonVariables.nTo = CommonVariables.lastnum;
			}
			else if (CommonVariables.nUnit != 5) {
				int unit = 0;
				switch (CommonVariables.nUnit) {
					case 1: {//A
						switch (CommonVariables.nShurui) {
							case 1: {//V
								break;
							}
							case 2: {//N
								unit = 1;
								break;
							}
							case 3: {//Aj
								unit = 2;
								break;
							}
							case 4: {//M
								unit = 11;
								break;
							}
						}
						break;
					}
					case 2: {//B
						switch (CommonVariables.nShurui) {
							case 1: {//V
								unit = 3;
								break;
							}
							case 2: {//N
								unit = 4;
								break;
							}
							case 3: {//Aj
								unit = 5;
								break;
							}
							case 4: {//M
								unit = 12;
								break;
							}
						}
						break;
					}
					case 3: {//C
						switch (CommonVariables.nShurui) {
							case 1: {//V
								unit = 6;
								break;
							}
							case 2: {//N
								unit = 7;
								break;
							}
							case 3: {//Aj
								unit = 8;
								break;
							}
							case 4: {//M
								unit = 13;
								break;
							}
						}
						break;
					}
					case 4:
						unit = 9;
						break;
				}
				
				CommonVariables.SetNumFromAndTo(CommonVariables.lastnum, unit);
				CommonVariables.nFrom = CommonVariables.from;
				CommonVariables.nTo = CommonVariables.to;
			}
			
			//1q
			//if (lastnum==2400){
			if (sentakuQ.equals(WordPhraseData.q_num.test1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "1qTest", "nWordHuseikaisuu" + i, 0);
				}
			}
			//p1q
			//if (lastnum==1850){
			if (sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "p1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "p1qTest", "nWordHuseikaisuu" + i, 0);
				}
			}
			
			
			adapterUnit = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			if (sentakuQ.equals(WordPhraseData.q_num.test1q) || sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				ArrayList<String> strUnit = new ArrayList<>(Arrays.asList("でる度A動詞", "でる度A名詞", "でる度A形容詞",
				                                                          "でる度B動詞", "でる度B名詞", "でる度B形容詞", "でる度C動詞", "でる度C名詞", "でる度C形容詞", "熟語"));
				for (int i = 0; i < 10; i++) {
					CommonVariables.SetNumFromAndTo(CommonVariables.lastnum, i);
					adapterUnit.add(strUnit.get(i) + String.format(" (%d-%d)", CommonVariables.from, CommonVariables.to));
				}
			}
			if (sentakuQ.equals(WordPhraseData.q_num.test1qEx)) {
				for (int i = 0; i <= 9; i++) {
					adapterUnit.add("Unit" + (i + 1) + " (" + CommonVariables.toFindFromAndTo[12][i][0] + "-" + CommonVariables.toFindFromAndTo[12][i][1] + ")");
				}
				adapterUnit.add("UnitEX" + " (" + CommonVariables.toFindFromAndTo[12][10][0] + "-" + CommonVariables.toFindFromAndTo[12][10][1] + ")");
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testp1qEx)) {
				for (int i = 0; i <= 9; i++) {
					adapterUnit.add("Unit" + (i + 1) + " (" + CommonVariables.toFindFromAndTo[13][i][0] + "-" + CommonVariables.toFindFromAndTo[13][i][1] + ")");
				}
				adapterUnit.add("UnitEX" + " (" + CommonVariables.toFindFromAndTo[13][10][0] + "-" + CommonVariables.toFindFromAndTo[13][10][1] + ")");
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
			AlertDialog.Builder builder = new AlertDialog.Builder(
				context);
			builder.setTitle("選択してください");
			//AlertDialogで選択された内容を保持
			builder.setSingleChoiceItems(adapterUnit, selectedIndex, (dialog, which) -> {
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
			CommonVariables.SetNumFromAndTo(CommonVariables.lastnum, unit);
			
			ArrayAdapter<String> adapterWord = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			if (sentakuQ.equals(WordPhraseData.q_num.testy08)
				|| sentakuQ.equals(WordPhraseData.q_num.testy1)
				|| sentakuQ.equals(WordPhraseData.q_num.testy2)
				|| sentakuQ.equals(WordPhraseData.q_num.testy3)) {
				CommonVariables.from = unit * 100 + 1;
				CommonVariables.to = (unit + 1) * 100;
			}
			if (sentakuQ.equals(WordPhraseData.q_num.test1qEx)) {
				CommonVariables.from = CommonVariables.toFindFromAndTo[12][unit][0];
				CommonVariables.to = CommonVariables.toFindFromAndTo[12][unit][1];
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testp1qEx)) {
				CommonVariables.from = CommonVariables.toFindFromAndTo[13][unit][0];
				CommonVariables.to = CommonVariables.toFindFromAndTo[13][unit][1];
			}
			for (int i = CommonVariables.from; i <= CommonVariables.to; i++) {
				adapterWord.add(i + ":" + CommonVariables.wordE[i] + " (" + CommonVariables.wordJ[i] + ")");
			}
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
			now = CommonVariables.from + which - 1;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onStartStopButtonClick(View view) {
		try {
			Button button = (Button) view;
			if (button.getText().equals("stop")) {
				//再生中 停止する
				saiseiStop();
				saveNow();
				button.setText("start");
			}
			else {
				//再生する
				context.startForegroundService(new Intent(context, PlaySound.class));
				button.setText("stop");
				CommonVariables.playing = true;
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			context.stopService(new Intent(context, PlaySound.class));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	@Override
	public void onPause() {
		try {
			super.onPause();
			saveNow();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void saveNow() {
		try {
			if (CommonVariables.strQ != null)
				putIntData(context, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", now);
		} catch (Exception exception) {
			showException(context, exception);
		}
	}
	
	public void saiseiStop() {
		try {
			context.stopService(new Intent(context, PlaySound.class));
			CommonVariables.playing = false;
			
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onResetButtonClick(View v) {
		try {
			now = 1;
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
			if (sb.getId() == R.id.seekBarEng) {
				//英語
				((TextView) findViewById(R.id.textViewSeekBarEng)).setText(String.format("英語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedEng = 1 + 0.1 * sb.getProgress();
				putIntData(context, "SeekBar", "english", sb.getProgress());
			}
			else if (sb.getId() == R.id.seekBarJpn) {
				//日本語
				((TextView) findViewById(R.id.textViewSeekBarJpn)).setText(String.format("日本語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedJpn = 1 + 0.1 * sb.getProgress();
				putIntData(context, "SeekBar", "japanese", sb.getProgress());
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onSpeedSeekBar(SeekBar seekBar, int i, boolean b) {
		onSpeedSeekBar(seekBar);
	}
}