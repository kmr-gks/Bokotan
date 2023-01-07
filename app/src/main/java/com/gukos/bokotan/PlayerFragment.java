package com.gukos.bokotan;

import static android.view.View.GONE;
import static com.gukos.bokotan.CommonVariables.hashMapKishutu;
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
import android.media.PlaybackParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

public class PlayerFragment extends Fragment {
	Context context;
	Activity activity;
	View viewFragment;
	
	int nDebug = 0;
	PlaybackParams pp = null;
	Intent intent = null;
	protected ArrayAdapter<String> adapterUnit, adapterWord;
	protected int selectedIndex = 0;
	AlertDialog adWord, adUnit;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_player, container, false);
	}
	
	private <T extends View> T findViewById(int id){return viewFragment.findViewById(id);}
	
	//ActivityのonCreateに相当
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			context = getContext();
			activity = getActivity();
			viewFragment = view;
			new Thread(() -> activity.runOnUiThread(() -> initialize())).start();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public void initialize() {
		try {
			//UI設定
			CommonVariables.tvWordEng = findViewById(R.id.fpTextViewEng);
			CommonVariables.tvWordJpn = findViewById(R.id.fpTextViewJpn);
			CommonVariables.tvGenzai = findViewById(R.id.fptvGenzai);
			CommonVariables.tvsubE = findViewById(R.id.fptvsubE);
			CommonVariables.tvsubJ = findViewById(R.id.fptvsubJ);
			CommonVariables.tvGogen = findViewById(R.id.fptvGogen);
			CommonVariables.tvNumSeikaisuu = findViewById(R.id.fptextViewNumSeikairitu);
			CommonVariables.tvSeikaisu = findViewById(R.id.fptextViewSeikaisuu);
			CommonVariables.textViewPath = findViewById(R.id.fptextViewPath);
			CommonVariables.textViewHatsuonKigou = findViewById(R.id.fptextViewHatsuonKigou);
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
			
			findViewById(R.id.fpbuttonStartStop).setOnClickListener(this::onStartStopButtonClick);
			findViewById(R.id.fpbuttonKensakuFromMainActivity).setOnClickListener(this::onZenbunKensakuButtonClicked);
			findViewById(R.id.fpbuttonSaisho).setOnClickListener(this::onResetButtonClick);
			findViewById(R.id.fpbuttonPIP).setOnClickListener(this::onPIPButtonClicked);
			findViewById(R.id.fpbuttonItiran).setOnClickListener(this::onSentakuButtonClicked);
			
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			
			if (CommonVariables.strQ == null) {
				CommonVariables.strQ = "p1q";
				sentakuQ = WordPhraseData.q_num.testp1q;
			}
			hashMapKishutu.clear();
			
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
									CommonVariables.hashMapKishutu.put(wpdy.e[i], "yume" + Q);
						}
						//パス単準1級
						//パス単単語
						for (String Q : new String[]{"p1q"/*, "2q", "p2q","3q","4q","5q"*/}) {
							WordPhraseData wpdp = new WordPhraseData(PasstanWord + Q, context);
							for (int i = 1; i < Math.min(wpdp.e.length, wpdp.j.length); i++)
								if (wpdp.e[i] != null && wpdp.j[i] != null)
									CommonVariables.hashMapKishutu.put(wpdp.e[i], "pass" + Q);
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
									CommonVariables.hashMapKishutu.put(wpd.e[i], "yume" + Q);
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
					ArrayList<String> arrayListE = new ArrayList(), arrayListJ = new ArrayList();
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
					ArrayList<String> arrayListE = new ArrayList(), arrayListJ = new ArrayList();
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
			
			if (CommonVariables.nowIsDecided || sentakuUnit.equals(WordPhraseData.q_num.unit.all)) {
				//now=getSharedPreferences("MainActivity"+"now",MODE_PRIVATE).getInt(strQ+"now",1);
				CommonVariables.now = MyLibrary.PreferenceManager.getIntData(context, "MainActivity" +
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
			
			if (CommonVariables.tvWordJpn.getText().equals("default")) {//初めてonCreateのとき
				onStartStopButtonClick(findViewById(R.id.fpbuttonStartStop));
			}
			
			//1q
			//if (lastnum==2400){
			if (sentakuQ.equals(WordPhraseData.q_num.test1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.kioku_file[i] = MyLibrary.PreferenceManager.getBoolData(context, "settings-1q", "1q" + i, false);
					CommonVariables.kioku_chBox[i] = CommonVariables.kioku_file[i];
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "1qTest", "nWordHuseikaisuu" + i, 0);
				}
				CommonVariables.kioku_chBox[1568] = CommonVariables.kioku_file[1568] = true;
			}
			//p1q
			//if (lastnum==1850){
			if (sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.kioku_file[i] = MyLibrary.PreferenceManager.getBoolData(context, "settings-p1q", "p1q" + i, false);
					CommonVariables.kioku_chBox[i] = CommonVariables.kioku_file[i];
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "p1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(context, "testActivity" + "p1qTest", "nWordHuseikaisuu" + i, 0);
				}
				CommonVariables.kioku_chBox[1675] = CommonVariables.kioku_file[1675] = true;
				CommonVariables.kioku_chBox[1799] = CommonVariables.kioku_file[1799] = true;
			}
			adapterUnit = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			if (sentakuQ.equals(WordPhraseData.q_num.test1q) || sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				ArrayList strUnit = new ArrayList(Arrays.asList("でる度A動詞", "でる度A名詞", "でる度A形容詞", "でる度B動詞", "でる度B名詞", "でる度B形容詞", "でる度C動詞", "でる度C名詞", "でる度C形容詞", "熟語"));
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
				ArrayList strUnit = new ArrayList<>();
				for (int i = 1; i <= 8; i++) {
					strUnit.add("Unit" + i);
					adapterUnit.add("Unit" + i + " (" + ((i - 1) * 100 + 1) + "-" + i * 100 + ")");
				}
			}
			if (sentakuQ.equals(WordPhraseData.q_num.testy1) || sentakuQ.equals(WordPhraseData.q_num.testy2)) {
				ArrayList strUnit = new ArrayList<>();
				for (int i = 1; i <= 10; i++) {
					strUnit.add("Unit" + i);
					adapterUnit.add("Unit" + i + " (" + ((i - 1) * 100 + 1) + "-" + i * 100 + ")");
				}
			}
			
			SeekBar sbE = findViewById(R.id.fpseekBarEng);
			sbE.setOnSeekBarChangeListener(new OnSeekBarChangeListenerEng());
			sbE.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "english", 5));
			onSpeedSeekBar(sbE);
			SeekBar sbJ = findViewById(R.id.fpseekBarJpn);
			sbJ.setOnSeekBarChangeListener(new OnSeekBarChangeListenerJpn());
			sbJ.setProgress(MyLibrary.PreferenceManager.getIntData(context, "SeekBar", "japanese", 10));
			onSpeedSeekBar(sbJ);
			pp = new PlaybackParams();
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
			builder.setSingleChoiceItems(adapterUnit, selectedIndex, (dialog, which) -> {selectedIndex = which;adUnit.dismiss();askTangoNumber(selectedIndex);});
			adUnit = builder.create();
			adUnit.show();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void askTangoNumber(int unit) {
		try {
			CommonVariables.SetNumFromAndTo(CommonVariables.lastnum, unit);
			
			adapterWord = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
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
			CommonVariables.now = CommonVariables.from + which - 1;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onStartStopButtonClick(View v) {
		try {
			Button b = (Button) v;
			if (CommonVariables.playing) {
				saiseiStop();
				putIntData(context, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ?
						CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", CommonVariables.now);
				b.setText("start");
			} else {
				intent = new Intent(context, PlaySound.class);
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				context.startForegroundService(intent);
				
				b.setText("stop");
				CommonVariables.playing = true;
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onZenbunKensakuButtonClicked(View v) {
		try {
			//全文検索のボタン
			startActivity(new Intent(context, KensakuActivity.class));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			if (intent != null) context.stopService(intent);
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	@Override
	public void onPause() {
		try {
			super.onPause();
			if(CommonVariables.strQ!=null) putIntData(context, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", CommonVariables.now);
			nDebug++;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void saiseiStop() {
		try {
			if (intent != null) {
				context.stopService(intent);
				intent = null;
			}
			CommonVariables.playing = false;
			
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onResetButtonClick(View v) {
		try {
			//saiseiStop();
			CommonVariables.now = 1;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onSentakuButtonClicked(View v) {
		try {
			saiseiStop();
			startActivity(new Intent(context, SentakuActivity.class));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onPIPButtonClicked(View view) {
		try {
			if (PipActivity.startPIP) {
				//PIPを終了したい
			} else {
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
			if (sb.getId() == R.id.fpseekBarEng) {
				//英語
				((TextView) findViewById(R.id.fptvSeekBarEng)).setText(String.format("英語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedEng = 1 + 0.1 * sb.getProgress();
				putIntData(context, "SeekBar", "english", sb.getProgress());
			} else if (sb.getId() == R.id.fpseekBarJpn) {
				//日本語
				((TextView) findViewById(R.id.fptvSeekBarJpn)).setText(String.format("日本語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedJpn = 1 + 0.1 * sb.getProgress();
				putIntData(context, "SeekBar", "japanese", sb.getProgress());
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private class OnSeekBarChangeListenerEng implements SeekBar.OnSeekBarChangeListener {
		@Override//ツマミがドラッグされると呼ばれる
		public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			onSpeedSeekBar(seekBar);
		}
		
		@Override//ツマミがタッチされた時に呼ばれる
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override//ツマミがリリースされた時に呼ばれる
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}
	
	private class OnSeekBarChangeListenerJpn implements SeekBar.OnSeekBarChangeListener {
		@Override//ツマミがドラッグされると呼ばれる
		public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
			onSpeedSeekBar(seekBar);
		}
		
		@Override//ツマミがタッチされた時に呼ばれる
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override//ツマミがリリースされた時に呼ばれる
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	}
}