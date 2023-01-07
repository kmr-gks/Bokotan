package com.gukos.bokotan;

import static com.gukos.bokotan.GogenYomuFactory.getGogenString;
import static com.gukos.bokotan.MainActivity.nHuseikaisuu;
import static com.gukos.bokotan.MainActivity.nSeikaisuu;
import static com.gukos.bokotan.MainActivity.strPhraseE;
import static com.gukos.bokotan.MainActivity.strPhraseJ;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.tag;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;
import static com.gukos.bokotan.MainActivity.toFindFromAndTo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Random;

class Seikairitsu{
	int num=0;
	int seitouritu=0;
	int toitakazu =0;
	Seikairitsu(int num,int seitouritu,int toitakazu){this.num=num;this.seitouritu=seitouritu;this.toitakazu =toitakazu;}
}

public class TestActivity extends AppCompatActivity {

	String logd="bokotan E/";
	TextView tvMondai;
	TextView tvSentaku1,tvSentaku2,tvSentaku3,tvSentaku4;
	TextView tvKaitou,tvKaisetsu,tvMaruBatu,tvRange;
	Button bSentaku1,bSentaku2,bSentaku3,bSentaku4;
	Button bMarubatu;
	int nGenzaiNanMonme;
	int nMondaiTangoNum,nSeikaiSentakusi,nTangoNum[]=new int[10];
	Random random=new Random();
	int nWordSeikaisuu,nWordHuseikaisuu;
	static boolean bSkipMaruBatuButton=false;
	static SoundPool sp=new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(1).build();
	static int nSound;
	static int seikairitsu[]=new int[2500];
	Seikairitsu numAndSeikairitu[]=new Seikairitsu[2500];
	int testCount=0;
	SharedPreferences.Editor spe;
	int nQuiz=0,nGokaku=0,nSeitou=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		tvMondai=findViewById(R.id.tvMondaibun);
		tvSentaku1=findViewById(R.id.bSentakusi1);
		tvSentaku2=findViewById(R.id.bSentakusi2);
		tvSentaku3=findViewById(R.id.bSentakusi3);
		tvSentaku4=findViewById(R.id.bSentakusi4);
		tvKaitou=findViewById(R.id.tvTorfKaitou);
		tvKaisetsu=findViewById(R.id.tvKaisetsu);
		tvMaruBatu=findViewById(R.id.tvMaruBatu);
		bSentaku1=findViewById(R.id.bSentakusi1);
		bSentaku2=findViewById(R.id.bSentakusi2);
		bSentaku3=findViewById(R.id.bSentakusi3);
		bSentaku4=findViewById(R.id.bSentakusi4);
		bMarubatu=findViewById(R.id.buttonMarubatu);
		tvRange=findViewById(R.id.textViewRange);
		Log.d("com.gukos.bokotan","strQ="+ strQ);
		switch (strQ) {
			case "1qTest":
			{
				WordPhraseData w = new WordPhraseData("WordData1q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData("Phrase1q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case "p1qTest":
			{
				MainActivity.lastnum = 1850;
				WordPhraseData w = new WordPhraseData("WordDatap1q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData("Phrasep1q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case "2qTest":
			{
				WordPhraseData w = new WordPhraseData("WordData2q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData("Phrase2q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case "p2qTest":
			{
				WordPhraseData w = new WordPhraseData("WordDatap2q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData("Phrasep2q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case "y08"+"Test": {
				MainActivity.lastnum = 800;
				WordPhraseData w = new WordPhraseData("WordDataYume08", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y1"+"Test": {
				MainActivity.lastnum = 1000;
				WordPhraseData w = new WordPhraseData("WordDataYume1", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y2"+"Test": {
				MainActivity.lastnum = 1000;
				WordPhraseData w = new WordPhraseData("WordDataYume2", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case "y3"+"Test": {
				MainActivity.lastnum = 800;
				WordPhraseData w = new WordPhraseData("WordDataYume3", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
		}
		nGenzaiNanMonme=getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).getInt("nGenzaiNanMonme",1);
		int unit=5;
		switch (Q_sentaku_activity.nUnit){
			case 1:{//A
				switch (Q_sentaku_activity.nShurui){
					case 1:{//V
						unit=0;
						break;
					}
					case 2:{//N
						unit=1;
						break;
					}
					case 3:{//Aj
						unit = 2;
						break;
					}
					case 4:{//M
						unit=11;
						break;
					}
				}
				break;
			}
			case 2:{//B
				switch (Q_sentaku_activity.nShurui){
					case 1:{//V
						unit=3;
						break;
					}
					case 2:{//N
						unit=4;
						break;
					}
					case 3:{//Aj
						unit=5;
						break;
					}
					case 4:{//M
						unit=12;
						break;
					}
				}
				break;
			}
			case 3:{//C
				switch (Q_sentaku_activity.nShurui){
					case 1:{//V
						unit=6;
						break;
					}
					case 2:{//N
						unit=7;
						break;
					}
					case 3:{//Aj
						unit=8;
						break;
					}
					case 4:{//M
						unit=13;
						break;
					}
				}
				break;
			}
			case 4: unit=9; break;
			case 5: unit=14; break;
		}
		Log.d("exception","strQ"+strQ+"to"+MainActivity.to+"from"+MainActivity.from+"lastnum"+MainActivity.lastnum);
		MainActivity.SetNumFromAndTo(MainActivity.lastnum,unit);


			for (int i=MainActivity.from;i<=MainActivity.to;i++) {
				nSeikaisuu[i] = getSharedPreferences("testActivity" + strQ, MODE_PRIVATE).getInt("nWordSeikaisuu" + i, 0);
				Log.d("com.gukos.bikotanjava", strQ);
				nHuseikaisuu[i] = getSharedPreferences("testActivity" + strQ, MODE_PRIVATE).getInt("nWordHuseikaisuu" + i, 0);
				if (nSeikaisuu[i] + nHuseikaisuu[i] > 0)
					seikairitsu[i] = nSeikaisuu[i] * 100 / (nSeikaisuu[i] + nHuseikaisuu[i]);
				else seikairitsu[i] = 0;
				numAndSeikairitu[i - MainActivity.from] = new Seikairitsu(i, seikairitsu[i],nSeikaisuu[i]+nHuseikaisuu[i]);
				nQuiz++;
				if (nSeikaisuu[i]>1) nGokaku++;
				if (nSeikaisuu[i]>0) nSeitou++;
			}
			Log.d("exception","strQ"+strQ+"to"+MainActivity.to+"from"+MainActivity.from+"lastnum"+MainActivity.lastnum);

		if (Q_sentaku_activity.bSort) Arrays.sort(numAndSeikairitu,0,MainActivity.to-MainActivity.from,(a,b)->{if (a.seitouritu==0&&b.seitouritu==0) return a.toitakazu-b.toitakazu;else return a.seitouritu-b.seitouritu;});
		else Arrays.sort(numAndSeikairitu,0,MainActivity.to-MainActivity.from,(a,b)->{if (a.seitouritu==0&&b.seitouritu==0) return b.toitakazu -a.toitakazu;else return b.seitouritu-a.seitouritu;});

		spe=getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit();
		setMondaiBun();
		sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int i, int i1) {
				soundPool.play(i,1f,1f,1,0,1f);
			}
		});
	}

	@Override
	protected void onPause(){
		super.onPause();
		Log.d(tag,"onpause");
		spe.apply();
	}

	@Override
	protected void onStop() {
		super.onStop();
		getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nGenzaiNanMonme",nGenzaiNanMonme).apply();
	}

	public void setMondaiBun(){
		//debug
		//出題範囲はMainActivity.to-MainActivity.from
		int nRangeForOptionsFrom=MainActivity.from,nRangeForOptionsTo=MainActivity.to;
		Log.d("com.gukos.bokotan","nGenzaiNanMonme"+nGenzaiNanMonme);
		Log.d("com.gukos.bokotan","nMondaiTangoNum"+nMondaiTangoNum);
		Log.d("com.gukos.bokotan","wordE[nMondaiTangoNum]"+wordE[nMondaiTangoNum]);
		Log.d("com.gukos.bokotan","nWordSeikaisuu"+nWordSeikaisuu);
		Log.d("com.gukos.bokotan","nWordHuseikaisuu"+nWordHuseikaisuu);
		//debug end
		if (!bSkipMaruBatuButton) {
			bMarubatu.setVisibility(View.INVISIBLE);
			bSentaku1.setVisibility(View.VISIBLE);
			bSentaku2.setVisibility(View.VISIBLE);
			bSentaku3.setVisibility(View.VISIBLE);
			bSentaku4.setVisibility(View.VISIBLE);
			findViewById(R.id.buttonWakaranai).setVisibility(View.VISIBLE);
		}
		if (Q_sentaku_activity.nShurui==4&&Q_sentaku_activity.nUnit!=4){
			//まとめの場合
			int unit;
			switch (Q_sentaku_activity.nUnit){
				case 1:{
					unit=random.nextInt(3);
					break;
				}
				case 2:{
					unit=random.nextInt(3)+3;
					break;
				}
				case 3:{
					unit=random.nextInt(3)+6;
					break;
				}
				case 5:
				default:{
					unit=random.nextInt(10);
					break;
				}
			}
			nRangeForOptionsFrom=MainActivity.toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][unit][0];
			nRangeForOptionsTo=MainActivity.toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][unit][1];
		}

		do {
			if (Q_sentaku_activity.WordPhraseOrTest.equals(q_num.mode.test))
				nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (Q_sentaku_activity.WordPhraseOrTest.equals(q_num.mode.sortTest)) {

				//if (sentakuQ.equals(q_num.test1q)||sentakuQ.equals(q_num.testp1q))nMondaiTangoNum = numAndSeikairitu[testCount++].num;
				//else nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				nMondaiTangoNum = numAndSeikairitu[testCount++].num;
				if (Q_sentaku_activity.strQenum.equals(q_num.strQ.str1q)||Q_sentaku_activity.strQenum.equals(q_num.strQ.strp1q)||Q_sentaku_activity.strQenum.equals(q_num.strQ.str2q)||Q_sentaku_activity.strQenum.equals(q_num.strQ.strp2q)){
					for (int i=0;i<=9;i++){
						if (toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][0]<=nMondaiTangoNum&&nMondaiTangoNum<=toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][1]){
							nRangeForOptionsFrom=toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][0];
							nRangeForOptionsTo=toFindFromAndTo[Q_sentaku_activity.sentakuQ.ordinal()][i][1];
						}
					}
				}
			}
			if (Q_sentaku_activity.WordPhraseOrTest.equals(q_num.mode.exTest)) {
				do {
					nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				} while (nSeikaisuu[nMondaiTangoNum] >= 2);
			}

			nTangoNum[1] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[2] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[3] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nTangoNum[4] = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
			nSeikaiSentakusi = random.nextInt(4) + 1;//1-4
			nTangoNum[nSeikaiSentakusi] = nMondaiTangoNum;
		}while (nTangoNum[1]==nTangoNum[2]||nTangoNum[1]==nTangoNum[3]||nTangoNum[1]==nTangoNum[4]||nTangoNum[2]==nTangoNum[3]||nTangoNum[2]==nTangoNum[4]||nTangoNum[3]==nTangoNum[4]);
		nWordSeikaisuu=getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).getInt("nWordSeikaisuu"+nMondaiTangoNum,0);
		nWordHuseikaisuu=getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).getInt("nWordHuseikaisuu"+nMondaiTangoNum,0);
		if (nWordSeikaisuu+nWordHuseikaisuu>0) {
			((TextView)findViewById(R.id.tvMondaiNum)).setText(nGenzaiNanMonme + "問目 No." + nMondaiTangoNum+'(' + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')');
		} else {
			((TextView)findViewById(R.id.tvMondaiNum)).setText(nGenzaiNanMonme + "問目 No." + nMondaiTangoNum +"(0% 0/0)");
		}
		tvMondai.setText(wordE[nMondaiTangoNum]);
		tvSentaku1.setText(wordJ[nTangoNum[1]]);
		tvSentaku2.setText(wordJ[nTangoNum[2]]);
		tvSentaku3.setText(wordJ[nTangoNum[3]]);
		tvSentaku4.setText(wordJ[nTangoNum[4]]);
		if (!bSkipMaruBatuButton){
			tvKaitou.setText("");
			tvKaisetsu.setText("");
			tvMaruBatu.setText("");
		}
		((TextView)findViewById(R.id.textViewRange)).setText("範囲 No."+MainActivity.from+'-'+MainActivity.to+"\n合格"+nGokaku+'/'+nSeitou+'/'+nQuiz);
		if (((CheckBox)findViewById(R.id.chHatsuon)).isChecked()) nSound=sp.load("/storage/emulated/0/Download/data/" + Q_sentaku_activity.strQenum.getQ + '/' + String.format("%04d", nMondaiTangoNum)  + "英.mp3",1);
	}

	public void checkKaitou(int sentaku){
		if (!bSkipMaruBatuButton) {
			bMarubatu.setVisibility(View.VISIBLE);
			bSentaku1.setVisibility(View.INVISIBLE);
			bSentaku2.setVisibility(View.INVISIBLE);
			bSentaku3.setVisibility(View.INVISIBLE);
			bSentaku4.setVisibility(View.INVISIBLE);
			findViewById(R.id.buttonWakaranai).setVisibility(View.INVISIBLE);
		}
		String strSeikaiHuseikai;
		if (sentaku==nSeikaiSentakusi) {
			strSeikaiHuseikai="正解";
			tvMaruBatu.setText("〇");
			tvMaruBatu.setTextColor(Color.RED);
			nWordSeikaisuu++;
			if (nWordSeikaisuu==1) nSeitou++;
			else if (nWordSeikaisuu==2) nGokaku++;
		} else {
			strSeikaiHuseikai="不正解";
			tvMaruBatu.setText("×");
			tvMaruBatu.setTextColor(Color.BLUE);
			nWordHuseikaisuu++;
		}
		//getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nWordSeikaisuu"+nMondaiTangoNum,nWordSeikaisuu).commit();
		//getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nWordHuseikaisuu"+nMondaiTangoNum,nWordHuseikaisuu).commit();
		spe.putInt("nWordSeikaisuu"+nMondaiTangoNum,nWordSeikaisuu);
		spe.putInt("nWordHuseikaisuu"+nMondaiTangoNum,nWordHuseikaisuu);
		tvKaitou.setText("解答:" + nSeikaiSentakusi +
				wordE[nMondaiTangoNum] + wordJ[nMondaiTangoNum]);
		tvKaisetsu.setText(
				"\n1 No."+nTangoNum[1]+'	'+wordE[nTangoNum[1]]+'	'+wordJ[nTangoNum[1]]+
				"\n2 No."+nTangoNum[2]+'	'+wordE[nTangoNum[2]]+'	'+wordJ[nTangoNum[2]]+
				"\n3 No."+nTangoNum[3]+'	'+wordE[nTangoNum[3]]+'	'+wordJ[nTangoNum[3]]+
				"\n4 No."+nTangoNum[4]+'	'+wordE[nTangoNum[4]]+'	'+wordJ[nTangoNum[4]]);
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
			default: sentaku=0;
		}
		Log.d(logd,"onSentakusiTapped"+sentaku);
		checkKaitou(sentaku);
	}

	public void onHintButtonTapped(View v){
		new AlertDialog.Builder(this).setTitle("ヒント:例文(No."+nMondaiTangoNum+")").setMessage(getGogenString(nMondaiTangoNum)+"\n\n"+strPhraseE[nMondaiTangoNum]+"\n\n"+strPhraseJ[nMondaiTangoNum]).setPositiveButton("OK",null).show();
	}
}