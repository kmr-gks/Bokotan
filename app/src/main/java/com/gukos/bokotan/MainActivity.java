package com.gukos.bokotan;

import static android.view.View.GONE;
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
	int nDebug = 0;
	PlaybackParams pp = null;
	Intent intent = null;
	protected ArrayAdapter<String> adapterUnit, adapterWord;
	protected int selectedIndex = 0;
	AlertDialog adWord, adUnit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			CommonVariables.tvWordEng = findViewById(R.id.TextViewEng);
			CommonVariables.tvWordJpn = findViewById(R.id.TextViewJpn);
			CommonVariables.tvGenzai = findViewById(R.id.tvGenzai);
			CommonVariables.tvsubE = findViewById(R.id.tvsubE);
			CommonVariables.tvsubJ = findViewById(R.id.tvsubJ);
			CommonVariables.tvGogen = findViewById(R.id.tvGogen);
			CommonVariables.tvNumSeikaisuu = findViewById(R.id.textViewNumSeikairitu);
			CommonVariables.tvSeikaisu = findViewById(R.id.textViewSeikaisuu);
			CommonVariables.textViewPath = findViewById(R.id.textViewPath);
			CommonVariables.textViewHatsuonKigou = findViewById(R.id.textViewHatsuonKigou);
			
			if (CommonVariables.strQ != null) CommonVariables.isPhraseMode = CommonVariables.strQ.charAt(1) == 'h';
			if (isWordAndPhraseMode || CommonVariables.isPhraseMode) {
				CommonVariables.tvsubE.setVisibility(View.VISIBLE);
				CommonVariables.tvsubJ.setVisibility(View.VISIBLE);
			} else {
				//単語の場合は右下の文字は非表示
				CommonVariables.tvsubE.setVisibility(GONE);
				CommonVariables.tvsubJ.setVisibility(GONE);
			}
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			
			if (CommonVariables.strQ == null) {
				CommonVariables.strQ = "p1q";
				sentakuQ = WordPhraseData.q_num.testp1q;
			}
			
			CommonVariables.hashMapKishutu.clear();
			switch (sentakuQ) {
				case test1q: {
					CommonVariables.lastnum = 2400;
					WordPhraseData w = new WordPhraseData(PasstanWord + "1q", this);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "1q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					if (swOnlyFirst.isChecked()) {
						//ユメタン単語
						for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
							WordPhraseData wpdy = new WordPhraseData(YumeWord + Q, this);
							for (int i = 1; i < Math.min(wpdy.e.length, wpdy.j.length); i++)
								if (wpdy.e[i] != null && wpdy.j[i] != null)
									CommonVariables.hashMapKishutu.put(wpdy.e[i], "yume" + Q);
						}
						//パス単準1級
						//パス単単語
						for (String Q : new String[]{"p1q"/*, "2q", "p2q","3q","4q","5q"*/}) {
							WordPhraseData wpdp = new WordPhraseData(PasstanWord + Q, this);
							for (int i = 1; i < Math.min(wpdp.e.length, wpdp.j.length); i++)
								if (wpdp.e[i] != null && wpdp.j[i] != null)
									CommonVariables.hashMapKishutu.put(wpdp.e[i], "pass" + Q);
						}
					}
					break;
				}
				case testp1q: {
					CommonVariables.lastnum = 1850;
					WordPhraseData w = new WordPhraseData(PasstanWord + "p1q", this);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p1q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					if (swOnlyFirst.isChecked()) {
						//ユメタン単語
						for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
							WordPhraseData wpd = new WordPhraseData(YumeWord + Q, this);
							for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
								if (wpd.e[i] != null && wpd.j[i] != null)
									CommonVariables.hashMapKishutu.put(wpd.e[i], "yume" + Q);
						}
					}
					break;
				}
				case test2q: {
					CommonVariables.lastnum = 1704;
					WordPhraseData w = new WordPhraseData(PasstanWord + "2q", this);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "2q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testp2q: {
					CommonVariables.lastnum = 1500;
					WordPhraseData w = new WordPhraseData(PasstanWord + "p2q", this);
					CommonVariables.wordE = w.e;
					CommonVariables.wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p2q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case test1qEx: {
					CommonVariables.lastnum = 2811;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "1q", this);
					WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + "1q", this);
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
					
					WordPhraseData p = new WordPhraseData(TanjukugoPhrase + "1q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testp1qEx: {
					CommonVariables.lastnum = 2400;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "p1q", this);
					WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + "p1q", this);
					ArrayList<String> arrayListE = new ArrayList(), arrayListJ = new ArrayList();
					arrayListE.add("index");
					arrayListJ.add("index");
					for (int i = 1; w.e[i] != null; i++) arrayListE.add(w.e[i]);
					for (int i = 1; wx.e[i] != null; i++) arrayListE.add(wx.e[i]);
					for (int i = 1; w.j[i] != null; i++) arrayListJ.add(w.j[i]);
					for (int i = 1; wx.j[i] != null; i++) arrayListJ.add(wx.j[i]);
					CommonVariables.wordE = arrayListE.toArray(new String[0]);
					CommonVariables.wordJ = arrayListJ.toArray(new String[0]);
					
					WordPhraseData p = new WordPhraseData(TanjukugoPhrase + "p1q", this);
					CommonVariables.strPhraseE = p.e;
					CommonVariables.strPhraseJ = p.j;
					break;
				}
				case testy00: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "00", this);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy08: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "08", this);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy1: {
					CommonVariables.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "1", this);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy2: {
					CommonVariables.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "2", this);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
				case testy3: {
					CommonVariables.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "3", this);
					CommonVariables.strPhraseE = CommonVariables.wordE = w.e;
					CommonVariables.strPhraseJ = CommonVariables.wordJ = w.j;
					break;
				}
			}
			//保存された単語の番号（級ごと）
			//if (Q_sentaku_activity.nowIsDecided|| Q_sentaku_activity.nUnit==5){]
			if (CommonVariables.nowIsDecided || sentakuUnit.equals(WordPhraseData.q_num.unit.all)) {
				//now=getSharedPreferences("MainActivity"+"now",MODE_PRIVATE).getInt(strQ+"now",1);
				CommonVariables.now = MyLibrary.PreferenceManager.getIntData(this, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", 1);
				CommonVariables.nFrom = 1;
				CommonVariables.nTo = CommonVariables.lastnum;
			} else if (CommonVariables.nUnit != 5) {
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
				onStartStopButtonClick(findViewById(R.id.buttonStartStop));
			}
			nDebug++;
			
			//1q
			//if (lastnum==2400){
			if (sentakuQ.equals(WordPhraseData.q_num.test1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.kioku_file[i] = MyLibrary.PreferenceManager.getBoolData(this, "settings-1q", "1q" + i, false);
					CommonVariables.kioku_chBox[i] = CommonVariables.kioku_file[i];
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(this, "testActivity" + "1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(this, "testActivity" + "1qTest", "nWordHuseikaisuu" + i, 0);
				}
				CommonVariables.kioku_chBox[1568] = CommonVariables.kioku_file[1568] = true;
			}
			//p1q
			//if (lastnum==1850){
			if (sentakuQ.equals(WordPhraseData.q_num.testp1q)) {
				for (int i = 0; i < CommonVariables.lastnum; i++) {
					CommonVariables.kioku_file[i] = MyLibrary.PreferenceManager.getBoolData(this, "settings-p1q", "p1q" + i, false);
					CommonVariables.kioku_chBox[i] = CommonVariables.kioku_file[i];
					CommonVariables.nSeikaisuu[i] = MyLibrary.PreferenceManager.getIntData(this, "testActivity" + "p1qTest", "nWordSeikaisuu" + i, 0);
					CommonVariables.nHuseikaisuu[i] = MyLibrary.PreferenceManager.getIntData(this, "testActivity" + "p1qTest", "nWordHuseikaisuu" + i, 0);
				}
				CommonVariables.kioku_chBox[1675] = CommonVariables.kioku_file[1675] = true;
				CommonVariables.kioku_chBox[1799] = CommonVariables.kioku_file[1799] = true;
			}
			adapterUnit = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
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
			
			SeekBar sbE = findViewById(R.id.seekBarEng);
			sbE.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
			});
			sbE.setProgress(MyLibrary.PreferenceManager.getIntData(this, "SeekBar", "english", 5));
			onSpeedSeekBar(sbE);
			SeekBar sbJ = findViewById(R.id.seekBarJpn);
			sbJ.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
			});
			sbJ.setProgress(MyLibrary.PreferenceManager.getIntData(this, "SeekBar", "japanese", 10));
			onSpeedSeekBar(sbJ);
			pp = new PlaybackParams();
			if (intent == null) {
				intent = new Intent(getApplication(), PlaySound.class);
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				startForegroundService(intent);
			}
			
			//https://oc-technote.com/android/service%E3%81%8B%E3%82%89activity%E3%81%AB%E5%80%A4%E3%82%92%E6%8A%95%E3%81%92%E3%81%9F%E3%82%8A%E7%94%BB%E9%9D%A2%E3%82%92%E6%9B%B4%E6%96%B0%E3%81%97%E3%81%9F%E3%82%8A%E3%81%99%E3%82%8B%E6%96%B9/
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onSelectNowButtonClick(View v) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					MainActivity.this);
			builder.setTitle("選択してください");
			//AlertDialogで選択された内容を保持
			builder.setSingleChoiceItems(adapterUnit, selectedIndex, (dialog, which) -> {selectedIndex = which;adUnit.dismiss();askTangoNumber(selectedIndex);});
			adUnit = builder.create();
			adUnit.show();
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	private void askTangoNumber(int unit) {
		try {
			CommonVariables.SetNumFromAndTo(CommonVariables.lastnum, unit);
			
			adapterWord = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("単語を選択してください");
			builder.setSingleChoiceItems(adapterWord, 0, this::onWordSelect);
			adWord = builder.create();
			adWord.show();
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onWordSelect(DialogInterface dialog, int which) {
		try {
			//単語を選んだあと
			adWord.dismiss();
			CommonVariables.now = CommonVariables.from + which - 1;
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onStartStopButtonClick(View v) {
		try {
			Button b = (Button) v;
			if (CommonVariables.playing) {
				saiseiStop();
				putIntData(this, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", CommonVariables.now);
				b.setText("start");
			} else {
				intent = new Intent(getApplication(), PlaySound.class);
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				startForegroundService(intent);
				
				b.setText("stop");
				CommonVariables.playing = true;
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onZenbunKensakuButtonClicked(View v) {
		try {
			//全文検索のボタン
			startActivity(new Intent(this, KensakuActivity.class));
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			if (intent != null) stopService(intent);
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	@Override
	public void onPause() {
		try {
			super.onPause();
			putIntData(this, "MainActivity" + "now", (CommonVariables.strQ.startsWith("ph") ? CommonVariables.strQ.substring(2) : CommonVariables.strQ) + "now", CommonVariables.now);
			nDebug++;
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void saiseiStop() {
		try {
			if (intent != null) {
				stopService(intent);
				intent = null;
			}
			CommonVariables.playing = false;
			
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onResetButtonClick(View v) {
		try {
			//saiseiStop();
			CommonVariables.now = 1;
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onSentakuButtonClicked(View v) {
		try {
			saiseiStop();
			startActivity(new Intent(this, SentakuActivity.class));
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onPIPButtonClicked(View view) {
		try {
			if (PipActivity.startPIP) {
				//PIPを終了したい
			} else {
				startActivity(new Intent(this, PipActivity.class));
			}
			PipActivity.startPIP = !PipActivity.startPIP;
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	public void onSpeedSeekBar(View v) {
		try {
			SeekBar sb = (SeekBar) v;
			if (sb.getId() == R.id.seekBarEng) {
				//英語
				((TextView) findViewById(R.id.tvSeekBarEng)).setText(String.format("英語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedEng = 1 + 0.1 * sb.getProgress();
				putIntData(this, "SeekBar", "english", sb.getProgress());
			} else if (sb.getId() == R.id.seekBarJpn) {
				//日本語
				((TextView) findViewById(R.id.tvSeekBarJpn)).setText(String.format("日本語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
				CommonVariables.dPlaySpeedJpn = 1 + 0.1 * sb.getProgress();
				putIntData(this, "SeekBar", "japanese", sb.getProgress());
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}
}