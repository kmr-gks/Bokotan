package com.gukos.bokotan;

import static com.gukos.bokotan.GogenYomuFactory.getGogenString;
import static com.gukos.bokotan.MainActivity.from;
import static com.gukos.bokotan.MainActivity.lastnum;
import static com.gukos.bokotan.MainActivity.nHuseikaisuu;
import static com.gukos.bokotan.MainActivity.nSeikaisuu;
import static com.gukos.bokotan.MainActivity.strPhraseE;
import static com.gukos.bokotan.MainActivity.strPhraseJ;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.to;
import static com.gukos.bokotan.MainActivity.toFindFromAndTo;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;
import static com.gukos.bokotan.MyLibrary.DataBook.passTan;
import static com.gukos.bokotan.MyLibrary.DataBook.tanjukugoEX;
import static com.gukos.bokotan.MyLibrary.DataBook.yumetan;
import static com.gukos.bokotan.MyLibrary.DataLang.english;
import static com.gukos.bokotan.MyLibrary.DataType.word;
import static com.gukos.bokotan.MyLibrary.*;
import static com.gukos.bokotan.MyLibrary.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.Q_sentaku_activity.strQenum;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.app.AlertDialog;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;

class Seikairitsu{
	final int num;
	final int seitouritu;
	final int toitakazu;
	Seikairitsu(int num,int seitouritu,int toitakazu){this.num=num;this.seitouritu=seitouritu;this.toitakazu =toitakazu;}
}

public class TestActivity extends AppCompatActivity {

	TextView tvMondai;
	TextView tvSentaku1,tvSentaku2,tvSentaku3,tvSentaku4;
	TextView tvKaitou,tvKaisetsu,tvMaruBatu,tvRange;
	Button bSentaku1,bSentaku2,bSentaku3,bSentaku4;
	Button bMarubatu;
	CheckBox checkBoxHatsuon,checkBoxKoukaon;
	int nGenzaiNanMonme;
	int nMondaiTangoNum;
	int nSeikaiSentakusi;
	final int[] nTangoNum =new int[10];
	final Random random=new Random();
	//int nWordSeikaisuu,nWordHuseikaisuu;
	static boolean bSkipMaruBatuButton=false;
	static final SoundPool sp=new SoundPool.Builder()
			.setAudioAttributes(
					new AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_GAME)
					.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
			.setMaxStreams(2)
			.build();
	static int nSound;
	static final int[] seikairitsu =new int[3000];
	final Seikairitsu[] numAndSeikairitu =new Seikairitsu[3000];
	int testCount=0;
	//問題数、合格数、正解数
	int nQuiz=0,nGokaku=0,nSeitou=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

		tvMondai = findViewById(R.id.tvMondaibun);
		tvSentaku1 = findViewById(R.id.bSentakusi1);
		tvSentaku2 = findViewById(R.id.bSentakusi2);
		tvSentaku3 = findViewById(R.id.bSentakusi3);
		tvSentaku4 = findViewById(R.id.bSentakusi4);
		tvKaitou = findViewById(R.id.tvTorfKaitou);
		tvKaisetsu = findViewById(R.id.tvKaisetsu);
		tvMaruBatu = findViewById(R.id.tvMaruBatu);
		bSentaku1 = findViewById(R.id.bSentakusi1);
		bSentaku2 = findViewById(R.id.bSentakusi2);
		bSentaku3 = findViewById(R.id.bSentakusi3);
		bSentaku4 = findViewById(R.id.bSentakusi4);
		bMarubatu = findViewById(R.id.buttonMarubatu);
		tvRange = findViewById(R.id.textViewRange);

		checkBoxHatsuon = findViewById(R.id.checkBoxHatsuon);
		checkBoxKoukaon = findViewById(R.id.checkBoxKoukaon);

		checkBoxHatsuon.setChecked(getBoolData(this, "checkBoxHatsuon", "checked", true));
		checkBoxHatsuon.setOnClickListener(view -> putBoolData(this, "checkBoxHatsuon", "checked", ((CheckBox) view).isChecked()));
		checkBoxKoukaon.setChecked(getBoolData(this, "checkBoxKoukaon", "checked", true));
		checkBoxKoukaon.setOnClickListener(view -> putBoolData(this, "checkBoxKoukaon", "checked", ((CheckBox) view).isChecked()));

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
				MainActivity.lastnum = 800;
				WordPhraseData w = new WordPhraseData(YumeWord + "08", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y1" + "Test": {
				MainActivity.lastnum = 1000;
				WordPhraseData w = new WordPhraseData(YumeWord + "1", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y2" + "Test": {
				MainActivity.lastnum = 1000;
				WordPhraseData w = new WordPhraseData(YumeWord + "2", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y3" + "Test": {
				MainActivity.lastnum = 800;
				WordPhraseData w = new WordPhraseData(YumeWord + "3", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
		}
		nGenzaiNanMonme = getIntData(this, DataName.testActivity + strQ, DataName.nGenzaiNanMonme, 1);
		int unit = 5;
		switch (Q_sentaku_activity.nUnit) {
			case 1: {//A
				switch (Q_sentaku_activity.nShurui) {
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
				switch (Q_sentaku_activity.nShurui) {
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
				switch (Q_sentaku_activity.nShurui) {
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
		MainActivity.SetNumFromAndTo(MainActivity.lastnum, unit);

		puts("from" + from + "to" + to);

		//正解率読み込み
		for (int i = MainActivity.from; i <= MainActivity.to; i++) {
			nSeikaisuu[i] = getIntData(this, DataName.testActivity + strQ, DataName.単語正解数 + i, 0);
			nHuseikaisuu[i] = getIntData(this, DataName.testActivity + strQ, DataName.単語不正解数 + i, 0);
			if (nSeikaisuu[i] + nHuseikaisuu[i] > 0)
				seikairitsu[i] = nSeikaisuu[i] * 100 / (nSeikaisuu[i] + nHuseikaisuu[i]);
			else seikairitsu[i] = 0;
			numAndSeikairitu[i - MainActivity.from] = new Seikairitsu(i, seikairitsu[i], nSeikaisuu[i] + nHuseikaisuu[i]);
			nQuiz++;
			puts("読み込み:"+i+"単語"+wordE[i]+"	正解"+nSeikaisuu[i]+"	不正解"+nHuseikaisuu[i]);
			//合格条件
			if (isGokaku(nSeikaisuu[i],nHuseikaisuu[i])) nGokaku++;
			if (nSeikaisuu[i] > 0) nSeitou++;
		}

		int toIndex = MainActivity.to - MainActivity.from;
		if (toIndex <= 0) toIndex = MainActivity.lastnum;

		//ソート
		if (Q_sentaku_activity.bSort)
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

		for (int i=0;i<toIndex;i++){
			Seikairitsu seikairitsu=numAndSeikairitu[i];
			puts("並び替え:"+i+"単語"+wordE[seikairitsu.num]+"	正解率"+seikairitsu.seitouritu+"	解いた数"+seikairitsu.toitakazu);
		}

		setMondaiBun();
		sp.setOnLoadCompleteListener((soundPool, i, i1) -> {
			try {
				soundPool.play(i, 1f, 1f, 1, 0, 1f);
			} catch (Exception e) {
				showException(this, e);
			}
		});

	}

	static boolean isGokaku(int nSeikaisuu,int nHuseikaisuu){
		int ans=nSeikaisuu-nHuseikaisuu*3;
		return ans>1;
	}

	static boolean isGokaku_(int nSeikaisuu,int nHuseikaisuu){
		return nSeikaisuu>0;
	}

	@Override
	protected void onStop() {
		super.onStop();
		putIntData(this,DataName.testActivity+ strQ,"nGenzaiNanMonme",nGenzaiNanMonme);
	}

	public void setMondaiBun() {
		//debug
		//出題範囲はMainActivity.to-MainActivity.from
		int nRangeForOptionsFrom = MainActivity.from, nRangeForOptionsTo = MainActivity.to;
		//debug end
		if (!bSkipMaruBatuButton) {
			bMarubatu.setVisibility(View.INVISIBLE);
			bSentaku1.setVisibility(View.VISIBLE);
			bSentaku2.setVisibility(View.VISIBLE);
			bSentaku3.setVisibility(View.VISIBLE);
			bSentaku4.setVisibility(View.VISIBLE);
			findViewById(R.id.buttonWakaranai).setVisibility(View.VISIBLE);
		}
		if (Q_sentaku_activity.nShurui == 4 && Q_sentaku_activity.nUnit != 4 && !strQ.contains("y")) {
			//まとめの場合
			int unit;
			switch (Q_sentaku_activity.nUnit) {
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
			nRangeForOptionsFrom = MainActivity.toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][unit][0];
			nRangeForOptionsTo = MainActivity.toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][unit][1];
		}

		do {
			//ランダムテストの場合
			if (Q_sentaku_activity.WordPhraseOrTest.equals(MyLibrary.q_num.mode.randomTest))
				nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;

			//正答率順テストの場合
			if (Q_sentaku_activity.WordPhraseOrTest.equals(MyLibrary.q_num.mode.seitouritsujunTest)) {
				nMondaiTangoNum = numAndSeikairitu[testCount].num;
				if (strQenum.equals(MyLibrary.q_num.strQ.str1q)
						|| strQenum.equals(MyLibrary.q_num.strQ.strp1q)
						|| strQenum.equals(MyLibrary.q_num.strQ.str2q)
						|| strQenum.equals(MyLibrary.q_num.strQ.strp2q)) {
					for (int i = 0; i <= 9; i++) {
						if (toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][0] <= nMondaiTangoNum && nMondaiTangoNum <= toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][1]) {
							nRangeForOptionsFrom = toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][0];
							nRangeForOptionsTo = toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][1];
						}
					}
				}
			}

			//不正解のみテスト
			if (Q_sentaku_activity.WordPhraseOrTest.equals(MyLibrary.q_num.mode.huseikainomiTest)) {
				do {
					nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				} while (isGokaku(nSeikaisuu[nMondaiTangoNum],nHuseikaisuu[nMondaiTangoNum]));
			}

			nTangoNum[1] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[2] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[3] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[4] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nSeikaiSentakusi = random.nextInt(4) + 1;//1-4
			nTangoNum[nSeikaiSentakusi] = nMondaiTangoNum;
		} while (nTangoNum[1] == nTangoNum[2]
				|| nTangoNum[1] == nTangoNum[3]
				|| nTangoNum[1] == nTangoNum[4]
				|| nTangoNum[2] == nTangoNum[3]
				|| nTangoNum[2] == nTangoNum[4]
				|| nTangoNum[3] == nTangoNum[4]);
		testCount++;

		nSeikaisuu[nMondaiTangoNum] = getIntData(this,DataName.testActivity + strQ,DataName.単語正解数 + nMondaiTangoNum, 0);
		nHuseikaisuu[nMondaiTangoNum] = getIntData(this,DataName.testActivity + strQ, DataName.単語不正解数 + nMondaiTangoNum, 0);

		TextView textViewMondaiNumber=findViewById(R.id.tvMondaiNum);
		if (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum] > 0) {
			textViewMondaiNumber.setText(
					nGenzaiNanMonme + "問目 No." + nMondaiTangoNum
							+ '(' + (int) nSeikaisuu[nMondaiTangoNum] * 100
							/ (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum])
							+ "% " + nSeikaisuu[nMondaiTangoNum]
							+ '/' + (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum]) + ')');
		} else {
			textViewMondaiNumber.setText(nGenzaiNanMonme + "問目 No." + nMondaiTangoNum + "(0% 0/0)");
		}

		tvMondai.setText(wordE[nMondaiTangoNum]);
		tvSentaku1.setText(wordJ[nTangoNum[1]]);
		tvSentaku2.setText(wordJ[nTangoNum[2]]);
		tvSentaku3.setText(wordJ[nTangoNum[3]]);
		tvSentaku4.setText(wordJ[nTangoNum[4]]);
		if (!bSkipMaruBatuButton) {
			tvKaitou.setText("");
			tvKaisetsu.setText("");
			tvMaruBatu.setText("");
		}

		tvRange.setText("範囲 No." + MainActivity.from + '-' + MainActivity.to + "\n合格:" + nGokaku + '/' + nQuiz);

		try {
			if (checkBoxHatsuon.isChecked()) {
				//発音にチェックされている
				String strLoadPath;
				if (strQ.startsWith("y")) {
					strLoadPath=getPath(yumetan, strQenum.getQ, word, english, nMondaiTangoNum);
				}else if (strQ.startsWith("tanjukugo")) {
					strLoadPath=getPath(tanjukugoEX, strQ.substring(0,strQ.length()-4), word, english, nMondaiTangoNum);
				} else {
					strLoadPath=getPath(passTan, strQenum.getQ, word, english, nMondaiTangoNum);
				}
				nSound=sp.load(strLoadPath,1);
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}

	public void checkKaitou(int sentaku) {
		if (!bSkipMaruBatuButton) {
			bMarubatu.setVisibility(View.VISIBLE);
			bSentaku1.setVisibility(View.INVISIBLE);
			bSentaku2.setVisibility(View.INVISIBLE);
			bSentaku3.setVisibility(View.INVISIBLE);
			bSentaku4.setVisibility(View.INVISIBLE);
			findViewById(R.id.buttonWakaranai).setVisibility(View.INVISIBLE);
		}
		if (sentaku == nSeikaiSentakusi) {
			//正解
			if(checkBoxKoukaon.isChecked()) sp.load(this,R.raw.seikai,1);
			tvMaruBatu.setText("〇");
			tvMaruBatu.setTextColor(Color.RED);
			nSeikaisuu[nMondaiTangoNum]++;
			if (nSeikaisuu[nMondaiTangoNum] == 1) nSeitou++;
			//正解が増えることにより合格数が増えた場合更新
			//今回合格、正解する前は不合格のとき合格数を増やす
			if (isGokaku(nSeikaisuu[nMondaiTangoNum],nHuseikaisuu[nMondaiTangoNum])
					&&!isGokaku(nSeikaisuu[nMondaiTangoNum]-1,nHuseikaisuu[nMondaiTangoNum])) nGokaku++;
			putIntData(this,DataName.testActivity+strQ,DataName.単語正解数 + nMondaiTangoNum, nSeikaisuu[nMondaiTangoNum]);

		} else {
			//不正解
			if(checkBoxKoukaon.isChecked()) sp.load(this,R.raw.huseikai,1);
			tvMaruBatu.setText("×");
			tvMaruBatu.setTextColor(Color.BLUE);
			nHuseikaisuu[nMondaiTangoNum]++;
			//正解が増えることにより合格数が増えた場合更新
			//今回合格、正解する前は不合格のとき合格数を増やす
			if (!isGokaku(nSeikaisuu[nMondaiTangoNum],nHuseikaisuu[nMondaiTangoNum])
					&&isGokaku(nSeikaisuu[nMondaiTangoNum]-1,nHuseikaisuu[nMondaiTangoNum])) nGokaku--;
			putIntData(this,DataName.testActivity+strQ,DataName.単語不正解数 + nMondaiTangoNum, nHuseikaisuu[nMondaiTangoNum]);
		}

		tvKaitou.setText("解答:" + wordE[nMondaiTangoNum] + wordJ[nMondaiTangoNum] + "\n" + getGogenString(nMondaiTangoNum, false, false));
		tvKaisetsu.setText(
				"\n1 No." + nTangoNum[1] + '	' + wordE[nTangoNum[1]] + '	' + wordJ[nTangoNum[1]] +
						"\n2 No." + nTangoNum[2] + '	' + wordE[nTangoNum[2]] + '	' + wordJ[nTangoNum[2]] +
						"\n3 No." + nTangoNum[3] + '	' + wordE[nTangoNum[3]] + '	' + wordJ[nTangoNum[3]] +
						"\n4 No." + nTangoNum[4] + '	' + wordE[nTangoNum[4]] + '	' + wordJ[nTangoNum[4]]);
		nGenzaiNanMonme++;
		if (bSkipMaruBatuButton) setMondaiBun();
	}

	public void onMarubatuTapped(View v){
		setMondaiBun();
	}

	public void onSentakusiTapped(View v){
		Button b=(Button) v;
		int sentaku=0;
		switch (b.getId()){
			case R.id.bSentakusi1:{
				sentaku=1;
				break;
			}
			case R.id.bSentakusi2:{
				sentaku=2;
				break;
			}
			case R.id.bSentakusi3:{
				sentaku=3;
				break;
			}
			case R.id.bSentakusi4:{
				sentaku=4;
				break;
			}
			default:
		}
		checkKaitou(sentaku);
	}

	public void onHintButtonTapped(View v){
		new AlertDialog.Builder(this)
				.setTitle("ヒント:例文(No."+nMondaiTangoNum+")")
				.setMessage(getGogenString(nMondaiTangoNum, true, true)+"\n\n"+strPhraseE[nMondaiTangoNum]+"\n\n\n\n\n"+strPhraseJ[nMondaiTangoNum])
				.setPositiveButton("OK",null)
				.show();
	}
}