package com.gukos.bokotan;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import static com.gukos.bokotan.Q_sentaku_activity.sentakuQ;
import static com.gukos.bokotan.Q_sentaku_activity.sentakuUnit;
import static com.gukos.bokotan.Q_sentaku_activity.skipwords;



public class MainActivity extends AppCompatActivity {
	static TextView tvWordEng,tvWordJpn,tvDebug,tvExcept,tvGenzai,tvsubE,tvsubJ,tvSeikaisuu;
	//static Button bStart,bStop,bReset,bAdd,bSubtract;
	String path;
	static String strQ=null;//開始時には決まっている
	static boolean playing=false;
	static final String tag="DEBUG_TAG";
	int nDebug =0;
	private final String[] strPermissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
	static String[] wordE;
	static String[] wordJ;
	static String[] strPhraseE;
	static String[] strPhraseJ;
	static int lastnum;
	static MediaPlayer mp=null;
	private char langage;
	public static int now;
	static boolean isPhraseMode;
	String chID;
	Notification notification;
	NotificationManager notificationManager;
	double dPlaySpeed=2.0;
	PlaybackParams pp=null;
	static boolean bHyojiYakuBeforeRead=true,bEtoJ=true;
	static int toFindFromAndTo[][][]={
			{{1,233},{234,472},{473,700},	{701,919},{920,1177},{1178,1400},	{1401,1619},{1620,1861},{1862,2100},	{2101,2400},	{1,700},{701,1400},{1401,2100},{1,2400}},
			{{1,92},{93,362},{363,530},		{531,682},{683,883},{884,1050},		{1051,1262},{1263,1411},{1412,1550},	{1551,1850},	{1,530},{531,1050},{1051,1550},{1,1850}},
			{{1,158},{159,316},{317,405},	{406,564},{565,719},{720,808},		{809,949},{950,1108},{1109,1179},		{1180,1704},	{1,405},{406,808},{809,1179},{1,1704}},
			{{1,125},{126,268},{269,373},	{374,484},{485,632},{633,735},		{736,839},{840,988},{989,1085},			{1086,1500},	{1,373},{374,735},{736,1085},{1,1500}}};
	//1qのunit=8のfrom=0,8,0
	//p1qunit=5 to=1,5,1
	//SentakuActivity.javaから
	static boolean[] kioku_file =new boolean[2500];
	static boolean[] kioku_chBox =new boolean[2500];
	static int nSeikaisuu[]=new int[2500],nHuseikaisuu[]=new int[2500];
	int nFrom,nTo;
	Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tvWordEng = findViewById(R.id.TextViewEng);
		tvWordJpn = findViewById(R.id.TextViewJpn);
		tvDebug = findViewById(R.id.textViewDebug);
		tvExcept = findViewById(R.id.tvExcept);
		tvGenzai=findViewById(R.id.tvGenzai);
		tvsubE=findViewById(R.id.tvsubE);tvsubE.setText(null);
		tvsubJ=findViewById(R.id.tvsubJ);tvsubJ.setText(null);
		tvSeikaisuu=findViewById(R.id.textViewNumSeikairitu);

		if (strQ!=null) isPhraseMode=strQ.charAt(1)=='h';
		mp=new MediaPlayer();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		if (strQ==null) {
			strQ = "p1q";
			sentakuQ = q_num.testp1q;
		}

		switch (sentakuQ){
			case test1q:{
				lastnum=2400;
				wordE =WordData1q.e;
				wordJ=WordData1q.j;
				strPhraseE =Phrase1q.e;
				strPhraseJ =Phrase1q.j;
				break;
			}
			case testp1q:{
				lastnum=1850;
				wordE =WordDatap1q.e;
				wordJ=WordDatap1q.j;
				strPhraseE =Phrasep1q.e;
				strPhraseJ =Phrasep1q.j;
				break;
			}
			case test2q:{
				lastnum=1704;
				wordE =WordData2q.e;
				wordJ=WordData2q.j;
				strPhraseE =Phrase2q.e;
				strPhraseJ =Phrase2q.j;
				break;
			}
			case testp2q:{
				lastnum=1500;
				wordE =WordDatap2q.e;
				wordJ=WordDatap2q.j;
				strPhraseE =Phrasep2q.e;
				strPhraseJ =Phrasep2q.j;
				break;
			}
		}/*
		switch (strQ){
			case "1q":
			case "ph1q":{
				lastnum=2400;
				wordE =WordData1q.e;
				wordJ=WordData1q.j;
				strPhraseE =Phrase1q.e;
				strPhraseJ =Phrase1q.j;
				break;
			}
			case "p1q":
			case "php1q":{
				lastnum=1850;
				wordE =WordDatap1q.e;
				wordJ=WordDatap1q.j;
				strPhraseE =Phrasep1q.e;
				strPhraseJ =Phrasep1q.j;
				break;
			}
		}*/

		//保存された単語の番号（級ごと）
		//if (Q_sentaku_activity.nowIsDecided|| Q_sentaku_activity.nUnit==5){]
		if (Q_sentaku_activity.nowIsDecided|| sentakuUnit.equals(q_num.unit.all)){
			now=this.getPreferences(MODE_PRIVATE).getInt(strQ+"now",1);
			nFrom=1;
			nTo=lastnum;
		}
		else if (Q_sentaku_activity.nUnit!=5) {
			int unit=0;
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
			}

			SetNumFromAndTo(lastnum,unit);
			nFrom=from;
			nTo=to;
		}
		Log.d(tag,"nowisDecided"+Q_sentaku_activity.nowIsDecided+"nUnit"+Q_sentaku_activity.nUnit+"nShurui"+Q_sentaku_activity.nShurui);


		//パーミッション
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			requestPermissions(strPermissions,1000);
		}

		if (tvWordJpn.getText().equals("default")) {//初めてonCreateのとき
			onStartButtonClick((Button)findViewById(R.id.buttonStartStop));
			nDebug++;Log.d(tag+nDebug,"onCreate,true");
		} else {
			nDebug++;Log.d(tag+nDebug,"onCreate,false:");
		}

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
		String strUnit[]={"でる度A動詞","でる度A名詞","でる度A形容詞","でる度B動詞","でる度B名詞","でる度B形容詞","でる度C動詞","でる度C名詞","でる度C形容詞","熟語"};
		for (int i=0;i<10;i++){
			SetNumFromAndTo(lastnum,i);
			adapterUnit.add(strUnit[i]+String.format("(%d-%d)",from,to));
		}

		SeekBar sb=findViewById(R.id.seekBar);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			pp=new PlaybackParams();
		}

		Log.d(tag,tvWordJpn.getText().toString());
		if (tvWordJpn.getText().toString().equals("default")){
			onStartButtonClick(findViewById(R.id.buttonStartStop));
		}
		//intent=new Intent(getApplication(),PlaySound.class);
		//startForegroundService(intent);

	}

	protected ArrayAdapter<String> adapterUnit,adapterWord;
	protected int selectedIndex = 0;
	AlertDialog adWord,adUnit;
	static int from,to;

	public void onSelectNowButtonClick(View v){
		//EditText et=findViewById(R.id.editTextNumberSigned);
		//now=Integer.parseInt(et.getText().toString());
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
		Log.d(tag,"MainActivity.SetNumFromAndTo"+"lastnum"+lastnum+"unit"+unit);
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
	public static void SetNumFromAndTo_(int lastnum,int unit)
	{
		from=0;to=0;
		if (lastnum == 2400) {
			switch (unit){
				case 0:{
					from=1;
					to=233;
					break;
				}
				case 1:{
					from=234;
					to=472;
					break;
				}
				case 2:{
					from=473;
					to=700;
					break;
				}
				case 3:{
					from=701;
					to=919;
					break;
				}
				case 4:{
					from=920;
					to=1177;
					break;
				}
				case 5:{
					from=1178;
					to=1400;
					break;
				}
				case 6:{
					from=1401;
					to=1619;
					break;
				}
				case 7:{
					from=1620;
					to=1861;
					break;
				}
				case 8:{
					from=1862;
					to=2100;
					break;
				}
				case 9:{
					from=2101;
					to=2400;
					break;
				}
				case 11:{
					from=1;
					to=700;
					break;
				}
				case 12:{
					from=701;
					to=1400;
					break;
				}
				case 13:{
					from=1401;
					to=2100;
					break;
				}
				case 14:{
					from=1;
					to=2400;
					break;
				}
				default: return;
			}
		}
		if (lastnum==1850) {
			switch (unit) {
				case 0: {//A-V
					from = 1;
					to = 92;
					break;
				}
				case 1: {//A-N
					from = 93;
					to = 362;
					break;
				}
				case 2: {//A-A
					from = 363;
					to = 530;
					break;
				}
				case 3: {
					from = 531;
					to = 682;
					break;
				}
				case 4: {
					from = 683;
					to = 883;
					break;
				}
				case 5: {
					from = 884;
					to = 1050;
					break;
				}
				case 6: {
					from = 1051;
					to = 1262;
					break;
				}
				case 7: {
					from = 1263;
					to = 1411;
					break;
				}
				case 8: {
					from = 1412;
					to = 1550;
					break;
				}
				case 9: {
					from = 1551;
					to = 1850;
					break;
				}
				case 11:{
					from=1;
					to=530;
					break;
				}
				case 12:{
					from=531;
					to=1050;
					break;
				}
				case 13:{
					from=1051;
					to=1550;
					break;
				}
				case 14:{
					from=1;
					to=1850;
					break;
				}
				default:
					return;
			}
		}
	}
	public void onWordSelect(DialogInterface dialog, int which) {
		//単語を選んだあと
		//TextView tv=findViewById(R.id.textView);
		//tv.setText(",word="+which+"num="+(from+which));
		adWord.dismiss();
		now=from+which-1;
	}

	void bokotanPlayEnglish(){

		if (now%20==0) this.getPreferences(MODE_PRIVATE).edit().putInt(strQ + "now", now).commit();

		if (bEtoJ) {
			now++;
			if (skipwords) {
				while (kioku_chBox[now]) {
					now++;
				}
			}
			if (now <= nFrom) now = nFrom;
			if (now >= nTo) now = nFrom;




		/*
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			notification = new Notification.Builder(this, chID)
					.setContentTitle("now"+now+wordE[now]+' '+wordJ[now])  //通知タイトル
					.setColor(Color.RED)
					//.setContentText(strPhraseJ[now]+strPhraseE[now])        //通知内容
					.setSmallIcon(android.R.drawable.star_big_on)                  //通知用アイコン
					.setStyle(new Notification.BigTextStyle()
							.setBigContentTitle(String.format("%d%s:%s",now,wordE[now],wordJ[now]))
							.bigText(strPhraseJ[now]+'\n'+strPhraseE[now])
					)
					.build();                                       //通知のビルド
			notification.flags=Notification.FLAG_NO_CLEAR;

			//システムから通知マネージャー取得
			notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(1, notification);
		}
		*/

			tvGenzai.setText("No." + now);
			int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
			if (lastnum == 2400) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			} else if (lastnum == 1850) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			}
			tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);

			if (isPhraseMode) {
				if (bHyojiYakuBeforeRead) {
					tvWordJpn.setText(strPhraseJ[now]);
				} else {
					tvWordJpn.setText("");
				}
				tvWordEng.setText(strPhraseE[now]);
				tvsubE.setText(wordE[now]);
				tvsubJ.setText(wordJ[now]);
			} else {
				if (bHyojiYakuBeforeRead) {
					tvWordJpn.setText(wordJ[now]);
				} else {
					tvWordJpn.setText("");
				}
				tvWordEng.setText(wordE[now]);
			}
		}

		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&isInPictureInPictureMode()){
			PipActivity.ChangeText(wordE[now],wordJ[now],now);
		//}

		if (isPhraseMode)//フレーズならば
		{
			path="/storage/emulated/0/Download/data/"+strQ+'/'+String.format("%04d",now)+"例.mp3";
		} else {
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now)  + "英.mp3";
		}
		try {
			tvDebug.setText("succeed");
			mp=new MediaPlayer();
			mp.setDataSource(path);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (pp!=null) mp.setPlaybackParams(pp.setSpeed((float) dPlaySpeed));
			}
			mp.prepare();
			mp.start();
			mp.setOnCompletionListener(mp -> {
				if (mp!=null&&mp.isPlaying()) mp.stop();
				mp.reset();
				mp.release();
				mp=null;
				if (isPhraseMode)
				{
					bokotanPlayJapanese();
				} else {
					JosiCheck(0);
				}
			});
		} catch (IOException e) {
			Log.d(tag,"Mediaplayer:erroe"+e.getMessage()+"@bokotanPlayEnglish");
			tvDebug.setText("Error:eng"+e.getMessage());
			tvExcept.setText(path);
			e.printStackTrace();
		}
		if (!bEtoJ&&!bHyojiYakuBeforeRead) {
			if (isPhraseMode) {
				tvWordEng.setText(strPhraseE[now]);
			} else {
				tvWordEng.setText(wordE[now]);
			}
		}


	}

	void bokotanPlayJapanese(){

		if (!bEtoJ) {
			now++;
			if (skipwords){
				while (kioku_chBox[now]){
					now++;
				}
			}
			if (now<=nFrom) now=nFrom;
			if (now>=nTo) now=nFrom;

			tvGenzai.setText("No." + now);
			int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
			if (lastnum == 2400) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			} else if (lastnum == 1850) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			}
			tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);

			if (isPhraseMode) {
				if (bHyojiYakuBeforeRead) {
					tvWordEng.setText(strPhraseE[now]);
				} else {
					tvWordEng.setText("");
				}
				tvWordJpn.setText(strPhraseJ[now]);
				tvsubE.setText(wordE[now]);
				tvsubJ.setText(wordJ[now]);
			} else {
				if (bHyojiYakuBeforeRead) {
					tvWordEng.setText(wordE[now]);
				} else {
					tvWordEng.setText("");
				}
				tvWordJpn.setText(wordJ[now]);
			}
		}

		if (isPhraseMode){
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now)  + "日.mp3";
		} else {
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now) + "訳.mp3";
		}
		try {
			tvDebug.setText("succeed");
			mp=new MediaPlayer();
			mp.setDataSource(path);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (pp!=null) mp.setPlaybackParams(pp.setSpeed((float) dPlaySpeed));
		}
		mp.prepare();
		mp.start();
		mp.setOnCompletionListener(mp -> {
			if (mp!=null&&mp.isPlaying()) mp.stop();
			mp.reset();
			mp.release();
			mp=null;
			bokotanPlayEnglish();
		});
	} catch (IOException e) {
			Log.d(tag,"Mediaplayer:erroe"+e.getMessage()+"@bokotanPlayJapanese");
			tvExcept.setText(path);
			tvDebug.setText("Error:jpn"+e.getMessage());
		e.printStackTrace();
	}
		if (bEtoJ&&!bHyojiYakuBeforeRead) {
			if (isPhraseMode) {
				tvWordJpn.setText(strPhraseJ[now]);
			} else {
				tvWordJpn.setText(wordJ[now]);
			}
		}
	}


	public void JosiCheck(int index)//最初は0を指定
	{
		if (langage=='英') return;
		MediaPlayer mpJosi = new MediaPlayer();
		String strJosi;
		int nowForJosiCheck=now;
		if (!bEtoJ) nowForJosiCheck++;
		char c= wordJ[nowForJosiCheck].charAt(index);
		tvDebug.setText("now:"+nowForJosiCheck+"最初の文字:"+c);
		switch (c)
		{
			case 'を':
			{
				strJosi="/storage/emulated/0/Download/data/postpositional/wo.mp3";
				break;
			}
			case 'に':
			{
				strJosi="/storage/emulated/0/Download/data/postpositional/ni.mp3";
				break;
			}
			case 'の':
			{
				strJosi="/storage/emulated/0/Download/data/postpositional/no.mp3";
				break;
			}
			case 'で':
			{
				strJosi="/storage/emulated/0/Download/data/postpositional/de.mp3";
				break;
			}
			case '（':
			{
				for (int i = 0; i< wordJ[nowForJosiCheck].length(); i++)
				{
					if (wordJ[nowForJosiCheck].charAt(i)=='）')
					{
						JosiCheck(i+1);
						return;
					}
				}
			}
			case '～':
			{
				JosiCheck(1);
				return;
			}
			default://助詞がない場合
			{
				bokotanPlayJapanese();
				return;
			}
		}
		//助詞がある場合
		try {
			tvDebug.setText("succeed");
			mpJosi =new MediaPlayer();
			mpJosi.setDataSource(strJosi);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				pp=mpJosi.getPlaybackParams().setSpeed((float) dPlaySpeed);
				mpJosi.setPlaybackParams(pp);
			}
			mpJosi.prepare();
			mpJosi.start();
			mpJosi.setOnCompletionListener(mpJosi1 -> {
				if (mpJosi1!=null&&mpJosi1.isPlaying())  mpJosi1.stop();
				mpJosi1.reset();
				mpJosi1.release();
				mpJosi1 =null;
				bokotanPlayJapanese();
			});
		} catch (IOException e) {
			Log.d(tag,"Mediaplayer:erroe"+e.getMessage()+"@JosiCheck");
			tvDebug.setText("Error:josi"+e.getMessage());
			e.printStackTrace();
		}
	}

	public void onStartButtonClick(View v){
		Button b=(Button)v;
		if (playing){
			onStopButtonClick(null);
			this.getPreferences(MODE_PRIVATE).edit().putInt(strQ + "now", now).commit();
			b.setText("start");
		} else {
			bokotanPlayEnglish();
			b.setText("stop");
			playing=true;
		}
	}

	public void onResetButtonClick(View v){
		new AlertDialog.Builder(this).setTitle( "リセットしますか" ).setMessage( "１から再生し直します" )
				.setPositiveButton( "yes", (dialog, which) -> {
					// クリックしたときの処理
					now=nFrom;
					ResetMediaPlayer();
					onStartButtonClick((Button)findViewById(R.id.buttonStartStop));
				})
				.setNegativeButton("no", (dialog, which) -> {
					// クリックしたときの処理
				})
				.setNeutralButton( "cancel", (dialog, which) -> {
					// クリックしたときの処理
				})
				.show();
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
	public void onResume() {
		super.onResume();
		nDebug++;Log.d(tag+nDebug,"onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		//stopService(intent);
		this.getPreferences(MODE_PRIVATE).edit().putInt(strQ + "now", now).commit();
		nDebug++;Log.d(tag+nDebug,"onPause");
	}

	public void onStopButtonClick(View v){
		playing=false;
		ResetMediaPlayer();
	}

	static void ResetMediaPlayer(){
		if (mp!=null){
			if (mp.isPlaying()) mp.stop();
			mp.reset();
			mp.release();
			mp=null;
		}
	}

	@Override
	public void onUserLeaveHint(){
		super.onUserLeaveHint();
		Log.d(tag,"onUserLeave");
	}
	@Override
	public void onStop() {
		super.onStop();

		Log.d(tag, "onStop");
	}

	/*
	@Override
	public void onUserLeaveHint() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			boolean result=enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
			Log.d(tag,"enter_PIP");
			if (!result) Log.d(tag,"pip:failed");
		}
		nDebug++;Log.d(tag+nDebug,"onUserLeaveHint");
	}
	*/

	@Override
	public void onConfigurationChanged(@NonNull Configuration c){
		super.onConfigurationChanged(c);
		nDebug++;Log.d(tag+nDebug,"onConfigurationChanged");
	}

	@Override
	public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
		super.onPictureInPictureModeChanged(isInPictureInPictureMode);

		/*
		if (isInPictureInPictureMode) {
			//今からPIP
			nDebug++;Log.d(tag+nDebug,"onPictureInPictureModeChanged"+isInPictureInPictureMode);
			startActivity(new Intent(this,PipActivity.class));
		} else {
			//PIP終わり
			nDebug++;Log.d(tag+nDebug,"onPictureInPictureModeChanged"+isInPictureInPictureMode);
			onStopButtonClick((Button)findViewById(R.id.stop));
			this.getPreferences(MODE_PRIVATE).edit().putInt(strQ + "now", now).commit();
		}
		*/
	}

	public void onSentakuButtonClicked(View v){
		onStopButtonClick(null);
		startActivity(new Intent(this,SentakuActivity.class));
	}

	public void onPIPButtonClicked(View v){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			//enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
			PipActivity.startPIP=true;
			startActivity(new Intent(this,PipActivity.class));
			Log.d(tag,"pip");
		}
	}

	public void onSpeedSeekBar(View v){
		SeekBar sb= (SeekBar) v;
		TextView tv=findViewById(R.id.tvSeekBar);
		tv.setText(String.format("Speed:%.1f",1.0+sb.getProgress()/10.0));
		dPlaySpeed=1+0.1*sb.getProgress();
	}
}