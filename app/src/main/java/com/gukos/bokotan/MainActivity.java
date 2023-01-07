package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DataType.word;
import static com.gukos.bokotan.MyLibrary.ExceptionHandler.showException;
import static com.gukos.bokotan.MyLibrary.getData;
import static com.gukos.bokotan.MyLibrary.putData;
import static com.gukos.bokotan.Q_sentaku_activity.isWordAndPhraseMode;
import static com.gukos.bokotan.Q_sentaku_activity.sentakuQ;
import static com.gukos.bokotan.Q_sentaku_activity.sentakuUnit;
import static com.gukos.bokotan.Q_sentaku_activity.swOnlyFirst;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
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

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
	static TextView tvWordEng,tvWordJpn,tvGenzai,tvsubE,tvsubJ,tvSeikaisuu,tvSeikaisu,tvGogen;
	static String strQ=null;//開始時には決まっている
	static boolean playing=false;
	static final String tag="E/";
	int nDebug =0;
	private final String[] strPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
	static String[] wordE;
	static String[] wordJ;
	static String[] strPhraseE;
	static String[] strPhraseJ;
	static int lastnum;
	public static int now;
	static boolean isPhraseMode;
	static double dPlaySpeedEng =0.1;
	static double dPlaySpeedJpn =0.1;
	PlaybackParams pp=null;
	static boolean bHyojiYakuBeforeRead=true,bEtoJ=true;
	static int[][][] toFindFromAndTo ={
			//1q
			{{1,233},{234,472},{473,700},	{701,919},{920,1177},{1178,1400},	{1401,1619},{1620,1861},{1862,2100},	{2101,2400},	{1,700},{701,1400},{1401,2100},{1,2400}},
			//p1q
			{{1,92},{93,362},{363,530},		{531,682},{683,883},{884,1050},		{1051,1262},{1263,1411},{1412,1550},	{1551,1850},	{1,530},{531,1050},{1051,1550},{1,1850}},
			//2q
			{{1,158},{159,316},{317,405},	{406,564},{565,719},{720,808},		{809,949},{950,1108},{1109,1179},		{1180,1704},	{1,405},{406,808},{809,1179},{1,1704}},
			//p2q
			{{1,125},{126,268},{269,373},	{374,484},{485,632},{633,735},		{736,839},{840,988},{989,1085},			{1086,1500},	{1,373},{374,735},{736,1085},{1,1500}},
			//3q
			{},
			//4q
			{},
			//5q
			{},
			//y00
			{{1,100},{101,200},{201,300},	{301,400},{401,500},{501,600},		{601,700},{701,800},{1,800},			{1,800},	{1,800},{1,800},{1,800},{1,800}},
			//y08
			{{1,100},{101,200},{201,300},	{301,400},{401,500},{501,600},		{601,700},{701,800},{801,900},			{1,1000},		{1,1000},{1,1000},{1,1000},{1,1000},},
			//y1
			{{1,100},{101,200},{201,300},	{301,400},{401,500},{501,600},		{601,700},{701,800},{801,900},			{1,1000},		{1,1000},{1,1000},{1,1000},{1,1000},},
			//y2
			{{1,100},{101,200},{201,300},	{301,400},{401,500},{501,600},		{601,700},{701,800},{801,900},			{1,1000},		{1,1000},{1,1000},{1,1000},{1,1000},},
			//y3
			{{1,100},{101,200},{201,300},	{301,400},{401,500},{501,600},		{601,700},{701,800},{1,800},			{1,800},	{1,800},{1,800},{1,800},{1,800}}};
	//1qのunit=8のfrom=0,8,0
	//p1qunit=5 to=1,5,1
	//SentakuActivity.javaから
	static boolean[] kioku_file =new boolean[2500];
	static boolean[] kioku_chBox =new boolean[2500];
	static int[] nSeikaisuu =new int[2500],nHuseikaisuu =new int[2500];
	static int nFrom,nTo;
	Intent intent=null;
	static HashMap<String,String> hashMapKishutu=new HashMap<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvWordEng = findViewById(R.id.TextViewEng);
		tvWordJpn = findViewById(R.id.TextViewJpn);
		tvGenzai=findViewById(R.id.tvGenzai);
		tvsubE=findViewById(R.id.tvsubE);
		tvsubJ=findViewById(R.id.tvsubJ);
		tvGogen=findViewById(R.id.tvGogen);
		tvSeikaisuu=findViewById(R.id.textViewNumSeikairitu);
		tvSeikaisu=findViewById(R.id.textViewSeikaisuu);

		if (strQ!=null) isPhraseMode=strQ.charAt(1)=='h';
		if (isWordAndPhraseMode||isPhraseMode){
			tvsubE.setVisibility(View.VISIBLE);
			tvsubJ.setVisibility(View.VISIBLE);
		}else{
			//単語の場合は右下の文字は非表示
			tvsubE.setVisibility(View.GONE);
			tvsubJ.setVisibility(View.GONE);
		}
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (strQ==null) {
			strQ = "p1q";
			sentakuQ = q_num.testp1q;
		}

		switch (sentakuQ){
			case test1q: {
				lastnum = 2400;
				WordPhraseData w = new WordPhraseData(PasstanWord+"1q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData(PasstanPhrase+"1q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				if (swOnlyFirst.isChecked()){
					//ユメタン単語
					for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
						WordPhraseData wpdy = new WordPhraseData(YumeWord + Q, this);
						for (int i = 1; i < Math.min(wpdy.e.length, wpdy.j.length); i++)
							if (wpdy.e[i] != null && wpdy.j[i] != null)
								hashMapKishutu.put(wpdy.e[i],"yume"+Q);
					}
					//パス単準1級
					//パス単単語
					for (String Q : new String[]{"p1q", "2q", "p2q","3q","4q","5q"}) {
						WordPhraseData wpdp = new WordPhraseData(PasstanWord + Q, this);
						for (int i = 1; i < Math.min(wpdp.e.length, wpdp.j.length); i++)
							if (wpdp.e[i] != null && wpdp.j[i] != null)
								hashMapKishutu.put(wpdp.e[i],"pass"+Q);
					}
				}
				break;
			}
			case testp1q:{
				lastnum=1850;
				WordPhraseData w = new WordPhraseData(PasstanWord+"p1q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData(PasstanPhrase+"p1q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				if (swOnlyFirst.isChecked()){
					//ユメタン単語
					for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
						WordPhraseData wpd = new WordPhraseData(YumeWord + Q, this);
						for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
							if (wpd.e[i] != null && wpd.j[i] != null)
								hashMapKishutu.put(wpd.e[i],"yume"+Q);
					}
				}
				break;
			}
			case test2q:{
				lastnum=1704;
				WordPhraseData w = new WordPhraseData(PasstanWord+"2q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData(PasstanPhrase+"2q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case testp2q:{
				lastnum=1500;
				WordPhraseData w = new WordPhraseData(PasstanWord+"p2q", this);
				wordE = w.e;
				wordJ = w.j;
				WordPhraseData p = new WordPhraseData(PasstanPhrase+"p2q", this);
				strPhraseE = p.e;
				strPhraseJ = p.j;
				break;
			}
			case testy00: {
				lastnum = 800;
				WordPhraseData w = new WordPhraseData(YumeWord+"00", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case testy08:{
				lastnum=800;
				WordPhraseData w = new WordPhraseData(YumeWord+"08", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case testy1:{
				lastnum=1000;
				WordPhraseData w = new WordPhraseData(YumeWord+"1", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case testy2:{
				lastnum=1000;
				WordPhraseData w = new WordPhraseData(YumeWord+"2", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
			case testy3:{
				lastnum=800;
				WordPhraseData w = new WordPhraseData(YumeWord+"3", this);
				strPhraseE = wordE = w.e;
				strPhraseJ = wordJ = w.j;
				break;
			}
		}
		//保存された単語の番号（級ごと）
		//if (Q_sentaku_activity.nowIsDecided|| Q_sentaku_activity.nUnit==5){]
		if (Q_sentaku_activity.nowIsDecided|| sentakuUnit.equals(q_num.unit.all)){
			now=getSharedPreferences("MainActivity"+"now",MODE_PRIVATE).getInt(strQ+"now",1);
			nFrom=1;
			nTo=lastnum;
		}
		else if (Q_sentaku_activity.nUnit!=5) {
			int unit=0;
			switch (Q_sentaku_activity.nUnit){
				case 1:{//A
					switch (Q_sentaku_activity.nShurui){
						case 1:{//V
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
			}

			SetNumFromAndTo(lastnum,unit);
			nFrom=from;
			nTo=to;
		}

		//パーミッション
		requestPermissions(strPermissions,1000);

		if (tvWordJpn.getText().equals("default")) {//初めてonCreateのとき
			onStartStopButtonClick((Button)findViewById(R.id.buttonStartStop));
		}
		nDebug++;

		//1q
		//if (lastnum==2400){
		if (sentakuQ.equals(q_num.test1q)){
			for (int i=0;i<lastnum;i++){
				kioku_file[i]=getSharedPreferences("settings-1q",MODE_PRIVATE).getBoolean("1q"+i,false);
				kioku_chBox[i]=kioku_file[i];
				nSeikaisuu[i]=getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + i, 0);
				nHuseikaisuu[i]=getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + i, 0);
			}
			kioku_chBox[1568]=kioku_file[1568]=true;
		}
		//p1q
		//if (lastnum==1850){
		if (sentakuQ.equals(q_num.testp1q)){
			for (int i=0;i<lastnum;i++){
				kioku_file[i]=getSharedPreferences("settings-p1q",MODE_PRIVATE).getBoolean("p1q"+i,false);
				kioku_chBox[i]=kioku_file[i];
				nSeikaisuu[i]=getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + i, 0);
				nHuseikaisuu[i]=getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + i, 0);
			}
			kioku_chBox[1675]=kioku_file[1675]=true;
			kioku_chBox[1799]=kioku_file[1799]=true;
		}
		adapterUnit = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice);
		String[] strUnit ={"でる度A動詞","でる度A名詞","でる度A形容詞","でる度B動詞","でる度B名詞","でる度B形容詞","でる度C動詞","でる度C名詞","でる度C形容詞","熟語"};
		for (int i=0;i<10;i++){
			SetNumFromAndTo(lastnum,i);
			adapterUnit.add(strUnit[i]+String.format("(%d-%d)",from,to));
		}

		SeekBar sbE=findViewById(R.id.seekBarEng);
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
		sbE.setProgress(getData( this,"SeekBar","english", 5));
		onSpeedSeekBar(sbE);
		SeekBar sbJ=findViewById(R.id.seekBarJpn);
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
		sbJ.setProgress(getData( this,"SeekBar","japanese", 10));
		onSpeedSeekBar(sbJ);
		pp=new PlaybackParams();
		if (intent==null) {
			intent = new Intent(getApplication(), PlaySound.class);
			intent.setAction( Intent.ACTION_OPEN_DOCUMENT  );
			startForegroundService(intent);
		}

	}

	protected ArrayAdapter<String> adapterUnit,adapterWord;
	protected int selectedIndex = 0;
	AlertDialog adWord,adUnit;
	static int from,to;

	public void onSelectNowButtonClick(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(
				MainActivity.this);
		builder.setTitle("選択してください");
		builder.setSingleChoiceItems(adapterUnit, selectedIndex,
				(dialog, which) -> {
					// AlertDialogで選択された内容を保持
					selectedIndex = which;
					adUnit.dismiss();
					askTangoNumber(selectedIndex);
				});
		adUnit = builder.create();
		adUnit.show();
	}

	private void askTangoNumber(int unit){
		SetNumFromAndTo(lastnum,unit);

		adapterWord=new ArrayAdapter<>(this,android.R.layout.simple_list_item_single_choice);
		for (int i=from;i<=to;i++){
			adapterWord.add(i+":"+wordE[i]+'('+wordJ[i]+')');
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("word選択してください");
		builder.setSingleChoiceItems(adapterWord, 0, this::onWordSelect);
		adWord = builder.create();
		adWord.show();
	}

	public static void SetNumFromAndTo(int lastnum,int unit){
		from=1;
		to=lastnum;
		if (unit>10) unit--;
		if (lastnum==2400){
			from=toFindFromAndTo[0][unit][0];
			to=toFindFromAndTo[0][unit][1];
		}
		if (lastnum==1850){
			from=toFindFromAndTo[1][unit][0];
			to=toFindFromAndTo[1][unit][1];
		}
		if (lastnum==1704){
			from=toFindFromAndTo[2][unit][0];
			to=toFindFromAndTo[2][unit][1];
		}
		if (lastnum==1500){
			from=toFindFromAndTo[3][unit][0];
			to=toFindFromAndTo[3][unit][1];
		}
	}

	public void onWordSelect(DialogInterface dialog, int which) {
		//単語を選んだあと
		adWord.dismiss();
		now=from+which-1;
	}

	public void onStartStopButtonClick(View v){
		try {
			Button b = (Button) v;
			if (playing) {
				saiseiStop();
				getSharedPreferences("MainActivity" + "now", MODE_PRIVATE).edit().putInt(strQ + "now", now).apply();
				b.setText("start");
			} else {
				intent = new Intent(getApplication(), PlaySound.class);
				intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
				startForegroundService(intent);

				b.setText("stop");
				playing = true;
			}
		}catch (Exception e){
			showException(e);
		}
	}

	public void onOboetaButtonTapped(View v){
		//暗記済み
		if (lastnum==2400) {
			kioku_file[now]=kioku_chBox[now]=true;
			getSharedPreferences("settings-1q", MODE_PRIVATE).edit().putBoolean("1q" + now, true).apply();
		}
		if (lastnum==1850) {
			kioku_file[now]=kioku_chBox[now]=true;
			getSharedPreferences("settings-p1q", MODE_PRIVATE).edit().putBoolean("p1q" + now, true).apply();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (intent!=null) stopService(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		getSharedPreferences("MainActivity"+"now",MODE_PRIVATE).edit().putInt(strQ + "now", now).apply();
		nDebug++;
	}

	public void saiseiStop(){
		if (intent!=null){
			stopService(intent);
			intent=null;
		}
		playing=false;
	}

	public void onResetButtonClick(View v){
		//saiseiStop();
		now=1;
	}

	public void onSentakuButtonClicked(View v){
		saiseiStop();
		startActivity(new Intent(this, SentakuActivity.class));
	}

	public void onPIPButtonClicked(View v){
		//enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
		PipActivity.startPIP=true;
		startActivity(new Intent(this,PipActivity.class));
	}

	public void onSpeedSeekBar(View v){
		SeekBar sb= (SeekBar) v;
		if (sb.getId()==R.id.seekBarEng) {
			//英語
			((TextView)findViewById(R.id.tvSeekBarEng)).setText(String.format("英語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
			dPlaySpeedEng =1+0.1*sb.getProgress();
			putData(this,"SeekBar","english",sb.getProgress());
		}else if(sb.getId()==R.id.seekBarJpn){
			//日本語
			((TextView)findViewById(R.id.tvSeekBarJpn)).setText(String.format("日本語速度:%.1f", 1.0 + sb.getProgress() / 10.0));
			dPlaySpeedJpn =1+0.1*sb.getProgress();
			putData(this,"SeekBar","japanese",sb.getProgress());
		}
	}
}
