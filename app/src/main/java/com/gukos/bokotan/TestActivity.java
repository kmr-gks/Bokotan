package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.kioku_chBox;
import static com.gukos.bokotan.MainActivity.kioku_file;
import static com.gukos.bokotan.MainActivity.nHuseikaisuu;
import static com.gukos.bokotan.MainActivity.nSeikaisuu;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.toFindFromAndTo;
import static com.gukos.bokotan.Q_sentaku_activity.sentakuQ;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

class Seikairitsu{
	int num=0;
	int seitouritu=0;
	Seikairitsu(int a,int b){this.num=a;this.seitouritu=b;}
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
	static String[] wordE;
	static String[] wordJ;
	int nWordSeikaisuu,nWordHuseikaisuu;
	static boolean bSkipMaruBatuButton=false;
	static SoundPool sp=new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(1).build();
	static int nSound;
	static int seikairitsu[]=new int[2500];
	Seikairitsu numAndSeikairitu[]=new Seikairitsu[2500];
	int testCount=0;

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
		if (strQ.equals("1qTest")){
			MainActivity.lastnum=2400;
			wordE=WordData1q.e;
			wordJ=WordData1q.j;
		} else if (strQ.equals("p1qTest")){
			MainActivity.lastnum=1850;
			wordE=WordDatap1q.e;
			wordJ=WordDatap1q.j;
		} else if (strQ.equals("2qTest")){
			MainActivity.lastnum=1704;
			wordE=WordData2q.e;
			wordJ=WordData2q.j;
		}else if (strQ.equals("p2qTest")){
			MainActivity.lastnum=1500;
			wordE=WordData2q.e;
			wordJ=WordData2q.j;
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
		MainActivity.SetNumFromAndTo(MainActivity.lastnum,unit);
		((TextView)findViewById(R.id.textViewRange)).setText("範囲 No."+MainActivity.from+'-'+MainActivity.to);

		//from MainActivity
		//if (sentakuQ.equals(q_num.test1q)||sentakuQ.equals(q_num.testp1q)){
			for (int i=MainActivity.from;i<MainActivity.to;i++){
				nSeikaisuu[i]=getSharedPreferences("testActivity" + strQ, MODE_PRIVATE).getInt("nWordSeikaisuu" + i, 0);
				Log.d("com.gukos.bikotanjava",strQ);
				nHuseikaisuu[i]=getSharedPreferences("testActivity" + strQ, MODE_PRIVATE).getInt("nWordHuseikaisuu" + i, 0);
				if (nSeikaisuu[i]+nHuseikaisuu[i]>0) seikairitsu[i]=nSeikaisuu[i]*100/(nSeikaisuu[i]+nHuseikaisuu[i]);
				else seikairitsu[i]=0;
				numAndSeikairitu[i-MainActivity.from]=new Seikairitsu(i,seikairitsu[i]);
			}
		//}
		if (Q_sentaku_activity.bSort) Arrays.sort(numAndSeikairitu,0,MainActivity.to-MainActivity.from,(a,b)->a.seitouritu-b.seitouritu);
		else Arrays.sort(numAndSeikairitu,0,MainActivity.to-MainActivity.from,(a,b)->b.seitouritu-a.seitouritu);


		setMondaiBun();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nGenzaiNanMonme",nGenzaiNanMonme).commit();
	}

	public void setMondaiBun(){
		//debug
		//出題範囲はMainActivity.to-MainActivity.from
		int nRangeForOptionsFrom=MainActivity.from,nRangeForOptionsTo=MainActivity.to;
		((TextView)findViewById(R.id.textViewDebugTest)).setText("nUnit="+Q_sentaku_activity.nUnit+"nShurui"+Q_sentaku_activity.nShurui);
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
			((TextView)findViewById(R.id.textViewDebugTest)).append("unit"+unit);
		}

		do {
			if (Q_sentaku_activity.WordPhraseOrTest.equals(q_num.mode.test))
				nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (Q_sentaku_activity.WordPhraseOrTest.equals(q_num.mode.sortTest)) {
				if (sentakuQ.equals(q_num.test1q)||sentakuQ.equals(q_num.testp1q))nMondaiTangoNum = numAndSeikairitu[testCount++].num;
				else nMondaiTangoNum = random.nextInt(nRangeForOptionsTo - nRangeForOptionsFrom + 1) + nRangeForOptionsFrom;
				/*
				if (Q_sentaku_activity.strQenum.equals(q_num.strQ.str1q)) {
					if (1 <= nMondaiTangoNum && nMondaiTangoNum <= 233) {
						nRangeForOptionsFrom = 1;
						nRangeForOptionsTo = 233;
					} else if (234 <= nMondaiTangoNum && nMondaiTangoNum <= 472) {
						nRangeForOptionsFrom = 234;
						nRangeForOptionsTo = 472;
					} else if (473 <= nMondaiTangoNum && nMondaiTangoNum <= 700) {
						nRangeForOptionsFrom = 473;
						nRangeForOptionsTo = 700;
					} else if (701 <= nMondaiTangoNum && nMondaiTangoNum <= 919) {
						nRangeForOptionsFrom = 701;
						nRangeForOptionsTo = 919;
					} else if (920 <= nMondaiTangoNum && nMondaiTangoNum <= 1177) {
						nRangeForOptionsFrom = 920;
						nRangeForOptionsTo = 1177;
					} else if (1178 <= nMondaiTangoNum && nMondaiTangoNum <= 1400) {
						nRangeForOptionsFrom = 1178;
						nRangeForOptionsTo = 1400;
					} else if (1401 <= nMondaiTangoNum && nMondaiTangoNum <= 1619) {
						nRangeForOptionsFrom = 1401;
						nRangeForOptionsTo = 1619;
					} else if (1620 <= nMondaiTangoNum && nMondaiTangoNum <= 1861) {
						nRangeForOptionsFrom = 1620;
						nRangeForOptionsTo = 1861;
					} else if (1862 <= nMondaiTangoNum && nMondaiTangoNum <= 2100) {
						nRangeForOptionsFrom = 1862;
						nRangeForOptionsTo = 2100;
					} else if (2101 <= nMondaiTangoNum && nMondaiTangoNum <= 2400) {
						nRangeForOptionsFrom = 2101;
						nRangeForOptionsTo = 2400;
					}
				}
				if (Q_sentaku_activity.strQenum.equals(q_num.strQ.strp1q)) {
					if (1 <= nMondaiTangoNum && nMondaiTangoNum <= 92) {
						nRangeForOptionsFrom = 1;
						nRangeForOptionsTo = 92;
					} else if (93 <= nMondaiTangoNum && nMondaiTangoNum <= 362) {
						nRangeForOptionsFrom = 93;
						nRangeForOptionsTo = 362;
					} else if (363 <= nMondaiTangoNum && nMondaiTangoNum <= 530) {
						nRangeForOptionsFrom = 363;
						nRangeForOptionsTo = 530;
					} else if (531 <= nMondaiTangoNum && nMondaiTangoNum <= 682) {
						nRangeForOptionsFrom = 531;
						nRangeForOptionsTo = 682;
					} else if (683 <= nMondaiTangoNum && nMondaiTangoNum <= 883) {
						nRangeForOptionsFrom = 683;
						nRangeForOptionsTo = 883;
					} else if (884 <= nMondaiTangoNum && nMondaiTangoNum <= 1050) {
						nRangeForOptionsFrom = 884;
						nRangeForOptionsTo = 1050;
					} else if (1051 <= nMondaiTangoNum && nMondaiTangoNum <= 1262) {
						nRangeForOptionsFrom = 1051;
						nRangeForOptionsTo = 1262;
					} else if (1263 <= nMondaiTangoNum && nMondaiTangoNum <= 1411) {
						nRangeForOptionsFrom = 1263;
						nRangeForOptionsTo = 1411;
					} else if (1412 <= nMondaiTangoNum && nMondaiTangoNum <= 1550) {
						nRangeForOptionsFrom = 1412;
						nRangeForOptionsTo = 1550;
					} else if (1551 <= nMondaiTangoNum && nMondaiTangoNum <= 1850) {
						nRangeForOptionsFrom = 1551;
						nRangeForOptionsTo = 1850;
					}
				}
				*/
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
				} while (nSeikaisuu[nMondaiTangoNum] > 3);
			}
			((TextView) findViewById(R.id.textViewDebugTest)).append("from" + nRangeForOptionsFrom + "to" + nRangeForOptionsTo + "%%" + seikairitsu[nMondaiTangoNum] + ' ' + nSeikaisuu[nMondaiTangoNum] + '/' + (nSeikaisuu[nMondaiTangoNum] + nHuseikaisuu[nMondaiTangoNum]));


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
			tvMondai.setText("現在" + nGenzaiNanMonme + "問目 No." + nMondaiTangoNum + '\n' + wordE[nMondaiTangoNum] + '(' + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')');
		} else {
			tvMondai.setText("現在" + nGenzaiNanMonme + "問目 No." + nMondaiTangoNum + '\n' + wordE[nMondaiTangoNum] + "(0% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')');
		}
		tvSentaku1.setText(wordJ[nTangoNum[1]]);
		tvSentaku2.setText(wordJ[nTangoNum[2]]);
		tvSentaku3.setText(wordJ[nTangoNum[3]]);
		tvSentaku4.setText(wordJ[nTangoNum[4]]);
		if (!bSkipMaruBatuButton){
			tvKaitou.setText("");
			tvKaisetsu.setText("");
			tvMaruBatu.setText("");
		}

		//Soundpool
		sp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int i, int i1) {
				soundPool.play(i,1f,1f,1,0,1f);
			}
		});
		nSound=sp.load("/storage/emulated/0/Download/data/" + Q_sentaku_activity.strQenum.getQ + '/' + String.format("%04d", nMondaiTangoNum)  + "英.mp3",1);
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
		} else {
			strSeikaiHuseikai="不正解";
			tvMaruBatu.setText("×");
			tvMaruBatu.setTextColor(Color.BLUE);
			nWordHuseikaisuu++;
		}
		getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nWordSeikaisuu"+nMondaiTangoNum,nWordSeikaisuu).commit();
		getSharedPreferences("testActivity"+ strQ,MODE_PRIVATE).edit().putInt("nWordHuseikaisuu"+nMondaiTangoNum,nWordHuseikaisuu).commit();
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
}