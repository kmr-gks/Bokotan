package com.gukos.bokotan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.tag;

import java.util.Calendar;
import java.util.TreeMap;

public class Q_sentaku_activity extends AppCompatActivity {
	static boolean skipwords;
	static boolean nowIsDecided = false;
	static boolean isWordAndPhraseMode =false;
	Switch swSkipKioku, swMaruBatu, swHyojiBeforeRead;
	RadioButton radioButtonEtoJ;
	EditText e;
	static q_num sentakuQ = q_num.testp1q;
	static q_num.mode WordPhraseOrTest = q_num.mode.word;
	static q_num.unit sentakuUnit = q_num.unit.all;
	static q_num.shurui sentakuShurui = q_num.shurui.matome;
	static q_num.strQ strQenum = q_num.strQ.strp1q;
	static boolean bSort=true;
	static q_num.skipjouken skipjoken= q_num.skipjouken.kirokunomi;
	static final String debugTAG="E/ mydbgtg";
	static CheckBox cbDirTOugou;
	static int  nUnit = 5, nShurui = 4, nWordPhraseOrTest = 1;

	public static TreeMap<String,GogenYomu> trGogenYomu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_q_sentaku);

		//new AlertDialog.Builder(this).setTitle("title").setMessage("message").setPositiveButton("ok",null).create().show();
		swSkipKioku = findViewById(R.id.switchSkipOboe);
		swMaruBatu = findViewById(R.id.switchSkipMaruBatu);
		swHyojiBeforeRead = findViewById(R.id.switchHyojiYakuBeforeRead);
		e = findViewById(R.id.editTextNumber);
		radioButtonEtoJ = findViewById(R.id.radioButtonEtoJ);
		cbDirTOugou=findViewById(R.id.checkBoxDirTougou);

		Spinner spinnerHanni=findViewById(R.id.spinnerHanni);
		Spinner spinnerHinsi=findViewById(R.id.spinnerHinsi);
		Spinner spinnerMode=findViewById(R.id.spinnerMode);

		spinnerHanni.setSelection(4);
		spinnerHinsi.setSelection(3);
		spinnerMode.setSelection(2);

		spinnerHanni.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				Spinner spinner=(Spinner)adapterView;
				String strItem=(String) spinner.getSelectedItem();
				Log.d(debugTAG,"item"+strItem+"i"+i+"l"+l);
				switch (i){
					case 0:{
						nUnit=1;
						sentakuUnit=q_num.unit.deruA;
						break;
					}
					case 1:{
						nUnit=2;
						sentakuUnit=q_num.unit.deruB;
						break;
					}
					case 2:{
						nUnit=3;
						sentakuUnit=q_num.unit.deruC;
						break;
					}
					case 3:{
						nUnit=4;
						sentakuUnit=q_num.unit.Jukugo;
						break;
					}
					case 4:{
						nUnit=5;
						sentakuUnit=q_num.unit.all;
						nShurui=4;
						sentakuShurui=q_num.shurui.matome;
						break;
					}
				}
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) {}
		});
		spinnerHinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				Spinner spinner=(Spinner)adapterView;
				String strItem=(String) spinner.getSelectedItem();
				Log.d(debugTAG,"item"+strItem+"i"+i+"l"+l);
				switch (i){
					case 0:{
						nShurui=1;
						sentakuShurui= q_num.shurui.verb;
						break;
					}
					case 1:{
						nShurui=2;
						sentakuShurui= q_num.shurui.noum;
						break;
					}
					case 2:{
						nShurui=3;
						sentakuShurui= q_num.shurui.adjective;
						break;
					}
					case 3:{
						nShurui=4;
						sentakuShurui= q_num.shurui.matome;
						break;
					}
				}
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) {}
		});
		spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				Spinner spinner=(Spinner)adapterView;
				String strItem=(String) spinner.getSelectedItem();
				Log.d(debugTAG,"item"+strItem+"i"+i+"l"+l);
				switch (i){
					case 0:{
						nWordPhraseOrTest=1;
						WordPhraseOrTest= q_num.mode.word;
						break;
					}
					case 1:{
						nWordPhraseOrTest=2;
						WordPhraseOrTest= q_num.mode.phrase;
						break;
					}
					case 2:{
						nWordPhraseOrTest=6;
						WordPhraseOrTest= q_num.mode.wordPlusPhrase;
						break;
					}
					case 3:{
						nWordPhraseOrTest=3;
						WordPhraseOrTest= q_num.mode.test;
						break;
					}
					case 4:{
						nWordPhraseOrTest=4;
						WordPhraseOrTest= q_num.mode.exTest;
						break;
					}
					case 5:{
						nWordPhraseOrTest=5;
						WordPhraseOrTest= q_num.mode.sortTest;
						break;
					}
				}
			}
			@Override public void onNothingSelected(AdapterView<?> adapterView) {}
		});

		trGogenYomu=new GogenYomuFactory(this).getTrGogenYomu();

		((TextView)findViewById(R.id.textViewVersion)).setText("Version:"+getVersionName(this));
	}

	public void onSelectQ(View v) {
		if (e.length() != 0) {
			nowIsDecided = true;
			MainActivity.now = Integer.parseInt(e.getText().toString()) - 1;
		}
		if (swSkipKioku != null) {
			skipwords = swSkipKioku.isChecked();
		} else skipwords = false;
		MainActivity.bHyojiYakuBeforeRead = swHyojiBeforeRead.isChecked();
		MainActivity.bEtoJ = radioButtonEtoJ.isChecked();
		bSort=((Switch) findViewById(R.id.switchSort)).isChecked();
		switch (v.getId()) {
			case R.id.button1Q: {
				strQ = "1q";
				strQenum = q_num.strQ.str1q;
				sentakuQ = q_num.test1q;
				break;
			}
			default:
			case R.id.buttonP1Q: {
				strQ = "p1q";
				strQenum = q_num.strQ.strp1q;
				sentakuQ = q_num.testp1q;
				break;
			}
			case R.id.button2q:{
				strQ = "2q";
				strQenum = q_num.strQ.str2q;
				sentakuQ = q_num.test2q;
				break;
			}
			case R.id.buttonP2q:{
				strQ = "p2q";
				strQenum = q_num.strQ.strp2q;
				sentakuQ = q_num.testp2q;
				break;
			}
			case R.id.buttonYume0_0:
			case R.id.buttonYume0_8:{
				strQ="y08";
				strQenum=q_num.strQ.stry08;
				sentakuQ=q_num.testy08;
				break;
			}
			case R.id.buttonYume1:{
				strQ="y1";
				strQenum=q_num.strQ.stry1;
				sentakuQ=q_num.testy1;
				break;
			}
			case R.id.buttonYume2:{
				strQ="y2";
				strQenum=q_num.strQ.stry2;
				sentakuQ=q_num.testy2;
				break;
			}
			case R.id.buttonYume3:{
				strQ="y3";
				strQenum=q_num.strQ.stry3;
				sentakuQ=q_num.testy3;
				break;
			}
		}
		isWordAndPhraseMode=false;
		switch (nWordPhraseOrTest) {
			//単語
			default:
			case 1: {
				if (strQ.endsWith("1q")||strQ.startsWith("y")) {
					Log.d(tag, "nWorPoraT=1");
					startActivity(new Intent(this, MainActivity.class));
				}else{
					Toast.makeText(this,"単語は1級、準1級、ユメタンのみです。",Toast.LENGTH_SHORT).show();
				}
				break;
			}
			//文
			case 2: {
				if (!strQ.equals("1q")&&!strQ.equals("p1q")&&!strQ.startsWith("y")){
					Toast.makeText(this,"文は1級、準1級、ユメタンのみです。",Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(tag, "nWorPoraT=2");
				strQ = "ph" + strQ;

				startActivity(new Intent(this, MainActivity.class));
				break;
			}
			//単語+文
			case 6: {
				if (!strQ.equals("1q")&&!strQ.equals("p1q")&&!strQ.startsWith("y")){
					Toast.makeText(this,"単語+文は1級、準1級、ユメタンのみです。",Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d(tag, "nWorPoraT=6");
				isWordAndPhraseMode =true;
				startActivity(new Intent(this, MainActivity.class));
				break;
			}
			//テスト
			case 3:
			case 4:
			case 5:{
				Log.d(tag, "nWorPoraT=3");
				strQ = strQ + "Test";
				TestActivity.bSkipMaruBatuButton = swMaruBatu.isChecked();
				startActivity(new Intent(this, TestActivity.class));
				break;
			}
		}
		Log.d(tag, "strQ=" + strQ);
	}

	//static int nRadioIdForRange = R.id.radioButtonAll, nUnit = 5, nShurui = 4, nWordPhraseOrTest = 1, nSelectedQ = 2;



	public void onRadioChecked(View v) {
		switch (v.getId()) {
			case R.id.radioButtonOnlyKioku:{
				Log.d(tag,"radioButtonOnlyKioku");
				skipjoken= q_num.skipjouken.kirokunomi;
				break;
			}
			case R.id.radioButton1seikai:{
				Log.d(tag,"radioButton1seikai");
				skipjoken= q_num.skipjouken.seikai1;
				break;
			}
			case R.id.radioButton2huseikai:{
				Log.d(tag,"radioButton2seikai");
				skipjoken= q_num.skipjouken.huseikai2;
				break;
			}
		}
	}


	public void onAlarmset(View v) {
		int h, m, s;
		// 現在時刻を取得
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		// 時間選択ダイアログの生成
		TimePickerDialog timepick = new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						// 設定 ボタンクリック時の処理
						// 時間をセットする
						Calendar calendar = Calendar.getInstance();
						// Calendarを使って現在の時間をミリ秒で取得
						calendar.setTimeInMillis(System.currentTimeMillis());
						// 設定
						calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar.set(Calendar.MINUTE, minute);
						//明示的なBroadCast
						Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
						PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
						// アラームをセットする
						AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
						if (am != null) {
							am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
							Toast.makeText(getApplicationContext(), "Set Alarm ", Toast.LENGTH_SHORT).show();
						}
					}
				}, hour, minute, true);
		// 表示
		timepick.show();
	}

	//https://qiita.com/niwasawa/items/c8271f56f058965b318b
	public String getVersionName(Activity activity) {

		try {
			// Java パッケージ名を取得
			// android.content.Context#getPackageName
			String name = activity.getPackageName();

			// インストールされているアプリケーションパッケージの
			// 情報を取得するためのオブジェクトを取得
			// android.content.Context#getPackageManager
			PackageManager pm = activity.getPackageManager();

			// アプリケーションパッケージの情報を取得
			PackageInfo info = pm.getPackageInfo(name, PackageManager.GET_META_DATA);

			// バージョン番号の文字列を返す
			return info.versionName;

		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}