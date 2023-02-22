package com.gukos.bokotan;

import static com.gukos.bokotan.GogenYomuFactory.getGogenString;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager;
import static com.gukos.bokotan.MyLibrary.PreferenceManager;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.PlaySound.strPhraseE;
import static com.gukos.bokotan.PlaySound.strPhraseJ;
import static com.gukos.bokotan.PlaySound.strQ;
import static com.gukos.bokotan.PlaySound.wordE;
import static com.gukos.bokotan.PlaySound.wordJ;
import static com.gukos.bokotan.PlayerFragment.lastnum;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEX;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;
import static com.gukos.bokotan.WordPhraseData.strQenum;
import static com.gukos.bokotan.WordPhraseData.toFindFromAndTo;

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.gukos.bokotan.databinding.ActivityTestBinding;

import java.util.Arrays;
import java.util.Random;

public class TestActivity extends AppCompatActivity {
	private static final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
	public static int[] nSeikaisuu = new int[3000], nHuseikaisuu = new int[3000];
	static boolean bSort = true;
	private int nGenzaiNanMonme, nMondaiTangoNum, nSeikaiSentakusi, testCount = 0, nQuiz = 0, nGokaku = 0, nSeitou = 0;
	final int[] nTangoNum = new int[10];
	final Random random = new Random();
	static final SoundPool sp = new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(2).build();
	static final int[] seikairitsu = new int[3000];
	final Seikairitsu[] numAndSeikairitu = new Seikairitsu[3000];
	
	private static ActivityTestBinding binding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			binding = DataBindingUtil.setContentView(this, R.layout.activity_test);
			
			//TODO ファイル名変更
			
			binding.checkBoxHatsuon.setChecked(PreferenceManager.getSetting(this, "checkBoxHatsuon", true));
			binding.checkBoxHatsuon.setOnClickListener(view -> PreferenceManager.putSetting(this, "checkBoxHatsuon", ((CheckBox) view).isChecked()));
			binding.checkBoxKoukaon.setChecked(PreferenceManager.getSetting(this, "binding.checkBoxKoukaon", true));
			binding.checkBoxKoukaon.setOnClickListener(view -> PreferenceManager.putSetting(this, "binding.checkBoxKoukaon", ((CheckBox) view).isChecked()));
			
			
			switch (strQ) {
				case "1qTest": {
					lastnum = 2400;
					WordPhraseData w = new WordPhraseData(PasstanWord + "1q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "1q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "p1qTest": {
					lastnum = 1850;
					WordPhraseData w = new WordPhraseData(PasstanWord + "p1q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p1q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "2qTest": {
					WordPhraseData w = new WordPhraseData(PasstanWord + "2q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "2q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "p2qTest": {
					WordPhraseData w = new WordPhraseData(PasstanWord + "p2q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(PasstanPhrase + "p2q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "tanjukugo1qTest": {
					lastnum = 2811;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "1q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(TanjukugoWord + "1q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "tanjukugop1qTest": {
					lastnum = 2400;
					WordPhraseData w = new WordPhraseData(TanjukugoWord + "p1q", this);
					wordE = w.e;
					wordJ = w.j;
					WordPhraseData p = new WordPhraseData(TanjukugoWord + "p1q", this);
					strPhraseE = p.e;
					strPhraseJ = p.j;
					break;
				}
				case "y08" + "Test": {
					PlayerFragment.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "08", this);
					strPhraseE = wordE = w.e;
					strPhraseJ = wordJ = w.j;
					break;
				}
				case "y1" + "Test": {
					PlayerFragment.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "1", this);
					strPhraseE = wordE = w.e;
					strPhraseJ = wordJ = w.j;
					break;
				}
				case "y2" + "Test": {
					PlayerFragment.lastnum = 1000;
					WordPhraseData w = new WordPhraseData(YumeWord + "2", this);
					strPhraseE = wordE = w.e;
					strPhraseJ = wordJ = w.j;
					break;
				}
				case "y3" + "Test": {
					PlayerFragment.lastnum = 800;
					WordPhraseData w = new WordPhraseData(YumeWord + "3", this);
					strPhraseE = wordE = w.e;
					strPhraseJ = wordJ = w.j;
					break;
				}
			}
			nGenzaiNanMonme = PreferenceManager.getIntData(this, dnTestActivity + strQ, PreferenceManager.DataName.現在何問目, 1);
			int unit = 5;
			switch (PlaySound.nUnit) {
				case 1: {//A
					switch (PlaySound.nShurui) {
						case 1: {//V
							unit = 0;
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
					switch (PlaySound.nShurui) {
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
					switch (PlaySound.nShurui) {
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
				case 5:
					unit = 14;
					break;
			}
			WordPhraseData.SetNumFromAndTo(PlayerFragment.lastnum, unit);
			
			//正解率読み込み
			nSeikaisuu = MyLibrary.PreferenceManager.stringToIntArray(MyLibrary.PreferenceManager.getStringData(this, dnTestActivity + strQ, keySeikai, ""));
			nHuseikaisuu = MyLibrary.PreferenceManager.stringToIntArray(MyLibrary.PreferenceManager.getStringData(this, dnTestActivity + strQ, keyHuseikai, ""));
			if (nSeikaisuu == null) nSeikaisuu = new int[3000];
			if (nHuseikaisuu == null) nHuseikaisuu = new int[3000];
			for (int i = PlaySound.from; i <= PlaySound.to; i++) {
				if (nSeikaisuu[i] + nHuseikaisuu[i] > 0)
					seikairitsu[i] = nSeikaisuu[i] * 100 / (nSeikaisuu[i] + nHuseikaisuu[i]);
				else seikairitsu[i] = 0;
				numAndSeikairitu[i - PlaySound.from] = new Seikairitsu(i, seikairitsu[i], nSeikaisuu[i] + nHuseikaisuu[i]);
				nQuiz++;
				//合格条件
				if (isGokaku(nSeikaisuu[i], nHuseikaisuu[i])) nGokaku++;
				if (nSeikaisuu[i] > 0) nSeitou++;
			}
			
			int toIndex = PlaySound.to - PlaySound.from;
			if (toIndex <= 0) toIndex = PlayerFragment.lastnum;
			
			//ソート
			if (bSort)
				Arrays.sort(numAndSeikairitu, 0, toIndex,
				            (a, b) -> {
					            //正答率0のときは解いた数で並び替え
					            if (a.seitouritu == 0 && b.seitouritu == 0)
						            return a.toitakazu - b.toitakazu;
						            //正答率準
					            else return a.seitouritu - b.seitouritu;
				            });
			else Arrays.sort(numAndSeikairitu, 0, toIndex, (a, b) -> {
				if (a.seitouritu == 0 && b.seitouritu == 0) return b.toitakazu - a.toitakazu;
				else return b.seitouritu - a.seitouritu;
			});
			
			setMondaiBun();
			sp.setOnLoadCompleteListener((soundPool, i, i1) -> {
				try {
					soundPool.play(i, 1f, 1f, 1, 0, 1f);
				} catch (Exception e) {
					ExceptionManager.showException(this, e);
				}
			});
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
		
	}
	
	static boolean isGokaku(int nSeikaisuu, int nHuseikaisuu) {
		int ans = nSeikaisuu - nHuseikaisuu * 3;
		return ans > 1;
	}
	
	@Override
	protected void onStop() {
		try {
			super.onStop();
			PreferenceManager.putIntData(this, dnTestActivity + strQ, "nGenzaiNanMonme", nGenzaiNanMonme);
			PreferenceManager.putStringData(this, dnTestActivity + strQ, keySeikai, PreferenceManager.intArrayToString(nSeikaisuu));
			PreferenceManager.putStringData(this, dnTestActivity + strQ, keyHuseikai, PreferenceManager.intArrayToString(nHuseikaisuu));
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	public void setMondaiBun() {
		try {
			//debug
			//出題範囲はMainActivity.to-MainActivity.from
			int nRangeForOptionsFrom = PlaySound.from, nRangeForOptionsTo = PlaySound.to;
			if (PlaySound.nShurui == 4 && PlaySound.nUnit != 4 && !strQ.contains("y")) {
				//まとめの場合
				int unit;
				switch (PlaySound.nUnit) {
					case 1: {
						unit = random.nextInt(3);
						break;
					}
					case 2: {
						unit = random.nextInt(3) + 3;
						break;
					}
					case 3: {
						unit = random.nextInt(3) + 6;
						break;
					}
					case 5:
					default: {
						unit = random.nextInt(10);
						break;
					}
				}
				nRangeForOptionsFrom = WordPhraseData.toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][unit][0];
				nRangeForOptionsTo = WordPhraseData.toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][unit][1];
			}
			
			do {
				//ランダムテストの場合
				if (WordPhraseData.WordPhraseOrTest.equals(WordPhraseData.q_num.mode.randomTest))
					nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				
				//正答率順テストの場合
				if (WordPhraseData.WordPhraseOrTest.equals(WordPhraseData.q_num.mode.seitouritsujunTest)) {
					nMondaiTangoNum = numAndSeikairitu[testCount].num;
					if (strQenum.equals(WordPhraseData.q_num.strQ.str1q)
							|| strQenum.equals(WordPhraseData.q_num.strQ.strp1q)
							|| strQenum.equals(WordPhraseData.q_num.strQ.str2q)
							|| strQenum.equals(WordPhraseData.q_num.strQ.strp2q)) {
						for (int i = 0; i <= 9; i++) {
							if (toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][i][0] <= nMondaiTangoNum && nMondaiTangoNum <= toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][i][1]) {
								nRangeForOptionsFrom = toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][i][0];
								nRangeForOptionsTo = toFindFromAndTo[WordPhraseData.sentakuQ.ordinal()][i][1];
							}
						}
					}
				}
				
				//不正解のみテスト
				if (WordPhraseData.WordPhraseOrTest.equals(WordPhraseData.q_num.mode.huseikainomiTest)) {
					do {
						nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
					} while (isGokaku(nSeikaisuu[nMondaiTangoNum], nHuseikaisuu[nMondaiTangoNum]));
				}
				
				nTangoNum[1] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				nTangoNum[2] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				nTangoNum[3] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				nTangoNum[4] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				nSeikaiSentakusi = random.nextInt(4) + 1;//1-4
				nTangoNum[nSeikaiSentakusi] = nMondaiTangoNum;
				
				//ユメタンのときそれぞれの品詞を調べる
				if (strQenum.equals(WordPhraseData.q_num.strQ.stry1) || strQenum.equals(WordPhraseData.q_num.strQ.stry2) || strQenum.equals(WordPhraseData.q_num.strQ.stry3)) {
					//1-35
					//36-70
					//71-100
				}
				
			} while (nTangoNum[1] == nTangoNum[2]
					|| nTangoNum[1] == nTangoNum[3]
					|| nTangoNum[1] == nTangoNum[4]
					|| nTangoNum[2] == nTangoNum[3]
					|| nTangoNum[2] == nTangoNum[4]
					|| nTangoNum[3] == nTangoNum[4]);
			testCount++;
			
			if (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum] > 0) {
				binding.textViewMondaiNum.setText(
						nGenzaiNanMonme + "問目 No." + nMondaiTangoNum
								+ '(' + (int) nSeikaisuu[nMondaiTangoNum] * 100
								/ (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum])
								+ "% " + nSeikaisuu[nMondaiTangoNum]
								+ '/' + (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum]) + ')');
			}
			else {
				binding.textViewMondaiNum.setText(nGenzaiNanMonme + "問目 No." + nMondaiTangoNum + "(0% 0/0)");
			}
			
			binding.textViewMondaibun.setText(wordE[nMondaiTangoNum]);
			binding.buttonChoice1.setText(wordJ[nTangoNum[1]]);
			binding.buttonChoice2.setText(wordJ[nTangoNum[2]]);
			binding.buttonChoice3.setText(wordJ[nTangoNum[3]]);
			binding.buttonChoice4.setText(wordJ[nTangoNum[4]]);
			
			binding.textViewHanni.setText("範囲 No." + PlaySound.from + '-' + PlaySound.to + "\n合格:" + nGokaku + '/' + nQuiz);
			
			if (binding.checkBoxHatsuon.isChecked()) {
				//発音にチェックされている
				String strLoadPath;
				if (strQ.startsWith("y")) {
					strLoadPath = FileDirectoryManager.getPath(yumetan, strQenum.getQ, word, english, nMondaiTangoNum);
				}
				else if (strQ.startsWith("tanjukugo")) {
					strLoadPath = FileDirectoryManager.getPath(tanjukugoEX, strQ.substring(0, strQ.length() - 4), word, english, nMondaiTangoNum);
				}
				else {
					strLoadPath = FileDirectoryManager.getPath(passTan, strQenum.getQ, word, english, nMondaiTangoNum);
				}
				sp.load(strLoadPath, 1);
			}
			
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	public void checkKaitou(int sentaku) {
		try {
			if (sentaku == nSeikaiSentakusi) {
				//正解
				if (binding.checkBoxKoukaon.isChecked()) sp.load(this, R.raw.seikai, 1);
				binding.textViewMaruBatu.setText("〇");
				binding.textViewMaruBatu.setTextColor(Color.RED);
				nSeikaisuu[nMondaiTangoNum]++;
				if (nSeikaisuu[nMondaiTangoNum] == 1) nSeitou++;
				//正解が増えることにより合格数が増えた場合更新
				//今回合格、正解する前は不合格のとき合格数を増やす
				if (isGokaku(nSeikaisuu[nMondaiTangoNum], nHuseikaisuu[nMondaiTangoNum])
						&& !isGokaku(nSeikaisuu[nMondaiTangoNum] - 1, nHuseikaisuu[nMondaiTangoNum]))
					nGokaku++;
				
			}
			else {
				//不正解
				if (binding.checkBoxKoukaon.isChecked()) sp.load(this, R.raw.huseikai, 1);
				binding.textViewMaruBatu.setText("×");
				binding.textViewMaruBatu.setTextColor(Color.BLUE);
				nHuseikaisuu[nMondaiTangoNum]++;
				//正解が増えることにより合格数が増えた場合更新
				//今回合格、正解する前は不合格のとき合格数を増やす
				if (!isGokaku(nSeikaisuu[nMondaiTangoNum], nHuseikaisuu[nMondaiTangoNum])
						&& isGokaku(nSeikaisuu[nMondaiTangoNum] - 1, nHuseikaisuu[nMondaiTangoNum]))
					nGokaku--;
			}
			puts("filename:" + dnTestActivity + strQ);
			
			binding.textViewKaitou.setText("解答:" + wordE[nMondaiTangoNum] + wordJ[nMondaiTangoNum] + "\n" + getGogenString(nMondaiTangoNum, false, false));
			binding.textViewKaisetsu.setText(
					"\n1 No." + nTangoNum[1] + '	' + wordE[nTangoNum[1]] + '	' + wordJ[nTangoNum[1]] +
							"\n2 No." + nTangoNum[2] + '	' + wordE[nTangoNum[2]] + '	' + wordJ[nTangoNum[2]] +
							"\n3 No." + nTangoNum[3] + '	' + wordE[nTangoNum[3]] + '	' + wordJ[nTangoNum[3]] +
							"\n4 No." + nTangoNum[4] + '	' + wordE[nTangoNum[4]] + '	' + wordJ[nTangoNum[4]]);
			nGenzaiNanMonme++;
			setMondaiBun();
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	public void onMarubatuTapped(View v) {
		try {
			setMondaiBun();
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	public void onSentakusiTapped(View v) {
		try {
			Button b = (Button) v;
			int sentaku = 0;
			switch (b.getId()) {
				case R.id.buttonChoice1: {
					sentaku = 1;
					break;
				}
				case R.id.buttonChoice2: {
					sentaku = 2;
					break;
				}
				case R.id.buttonChoice3: {
					sentaku = 3;
					break;
				}
				case R.id.buttonChoice4: {
					sentaku = 4;
					break;
				}
				default:
			}
			checkKaitou(sentaku);
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	public void onHintButtonTapped(View v) {
		try {
			new AlertDialog.Builder(this)
					.setTitle("ヒント:例文(No." + nMondaiTangoNum + ")")
					.setMessage(getGogenString(nMondaiTangoNum, true, true) + "\n\n" + strPhraseE[nMondaiTangoNum] + "\n\n\n\n\n" + strPhraseJ[nMondaiTangoNum])
					.setPositiveButton("OK", null)
					.show();
		} catch (Exception e) {
			ExceptionManager.showException(this, e);
		}
	}
	
	static class Seikairitsu {
		final int num, seitouritu, toitakazu;
		
		Seikairitsu(int num, int seitouritu, int toitakazu) {
			this.num = num;
			this.seitouritu = seitouritu;
			this.toitakazu = toitakazu;
		}
	}
}