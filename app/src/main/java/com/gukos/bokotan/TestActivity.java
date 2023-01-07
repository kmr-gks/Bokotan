package com.gukos.bokotan;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Random;

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
		if (MainActivity.strQ=="1qTest"){
			MainActivity.lastnum=2400;
			wordE=WordData1q.e;
			wordJ=WordData1q.j;
		}
		if (MainActivity.strQ=="p1qTest"){
			MainActivity.lastnum=1850;
			wordE=WordDatap1q.e;
			wordJ=WordDatap1q.j;
		}
		nGenzaiNanMonme=getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).getInt("nGenzaiNanMonme",1);
		int unit;
		switch (Q_sentaku_activity.nRadioIdForRange){
			case R.id.radioButtonAV:unit=0;break;
			case R.id.radioButtonAN:unit=1;break;
			case R.id.radioButtonAA:unit=2;break;
			case R.id.radioButtonBV:unit=3;break;
			case R.id.radioButtonBN:unit=4;break;
			case R.id.radioButtonBA:unit=5;break;
			case R.id.radioButtonCV:unit=6;break;
			case R.id.radioButtonCN:unit=7;break;
			case R.id.radioButtonCA:unit=8;break;
			case R.id.radioButtonJukugo:unit=9;break;
			case R.id.radioButtonAM:unit=11;break;
			case R.id.radioButtonBM:unit=12;break;
			case R.id.radioButtonCM:unit=13;break;
			case R.id.radioButtonAll:
			default:unit=random.nextInt(10);
		}
		MainActivity.SetNumFromAndTo(MainActivity.lastnum,unit);
		((TextView)findViewById(R.id.textViewRange)).setText("No."+MainActivity.from+'-'+MainActivity.to);
		setMondaiBun();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).edit().putInt("nGenzaiNanMonme",nGenzaiNanMonme).commit();
	}

	public void setMondaiBun(){
		if (!bSkipMaruBatuButton) {
			bMarubatu.setVisibility(View.INVISIBLE);
			bSentaku1.setVisibility(View.VISIBLE);
			bSentaku2.setVisibility(View.VISIBLE);
			bSentaku3.setVisibility(View.VISIBLE);
			bSentaku4.setVisibility(View.VISIBLE);
			findViewById(R.id.buttonWakaranai).setVisibility(View.VISIBLE);
		}
		nMondaiTangoNum=random.nextInt(MainActivity.to-MainActivity.from+1)+MainActivity.from;
		nTangoNum[1]=random.nextInt(MainActivity.to-MainActivity.from+1)+MainActivity.from;
		nTangoNum[2]=random.nextInt(MainActivity.to-MainActivity.from+1)+MainActivity.from;
		nTangoNum[3]=random.nextInt(MainActivity.to-MainActivity.from+1)+MainActivity.from;
		nTangoNum[4]=random.nextInt(MainActivity.to-MainActivity.from+1)+MainActivity.from;
		nSeikaiSentakusi=random.nextInt(4)+1;//1-4
		nTangoNum[nSeikaiSentakusi]=nMondaiTangoNum;
		nWordSeikaisuu=getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).getInt("nWordSeikaisuu"+nMondaiTangoNum,0);
		nWordHuseikaisuu=getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).getInt("nWordHuseikaisuu"+nMondaiTangoNum,0);
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
		getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).edit().putInt("nWordSeikaisuu"+nMondaiTangoNum,nWordSeikaisuu).commit();
		getSharedPreferences("testActivity"+MainActivity.strQ,MODE_PRIVATE).edit().putInt("nWordHuseikaisuu"+nMondaiTangoNum,nWordHuseikaisuu).commit();
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