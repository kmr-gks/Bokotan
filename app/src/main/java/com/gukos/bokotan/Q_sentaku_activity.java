package com.gukos.bokotan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.tag;

import java.util.Calendar;

public class Q_sentaku_activity extends AppCompatActivity {
	static boolean skipwords;
	static boolean nowIsDecided = false;
	Switch swSkipKioku, swMaruBatu, swHyojiBeforeRead;
	RadioButton radioButtonEtoJ;
	EditText e;
	static q_num sentakuQ = q_num.testp1q;
	static q_num.mode WordPhraseOrTest = q_num.mode.word;
	static q_num.unit sentakuUnit = q_num.unit.all;
	static q_num.shurui sentakuShurui = q_num.shurui.matome;
	static q_num.strQ strQenum = q_num.strQ.strp1q;
	static boolean bSort=true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_q_sentaku);
		MainActivity.mp = null;

		//new AlertDialog.Builder(this).setTitle("title").setMessage("message").setPositiveButton("ok",null).create().show();
		swSkipKioku = findViewById(R.id.switchSkipOboe);
		swMaruBatu = findViewById(R.id.switchSkipMaruBatu);
		swHyojiBeforeRead = findViewById(R.id.switchHyojiYakuBeforeRead);
		e = findViewById(R.id.editTextNumber);
		radioButtonEtoJ = findViewById(R.id.radioButtonEtoJ);


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
		}
		switch (nWordPhraseOrTest) {
			default:
			case 1: {
				Log.d(tag, "nWorPoraT=1");
				startActivity(new Intent(this, MainActivity.class));
				break;
			}
			case 2: {
				Log.d(tag, "nWorPoraT=2");
				strQ = "ph" + strQ;
				startActivity(new Intent(this, MainActivity.class));
				break;
			}
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

	static int nRadioIdForRange = R.id.radioButtonAll, nUnit = 5, nShurui = 4, nWordPhraseOrTest = 1, nSelectedQ = 2;

	public void onRadioChecked(View v) {
		switch (v.getId()) {
			case R.id.radioButtonA: {
				nUnit = 1;
				sentakuUnit = q_num.unit.deruA;
				break;
			}
			case R.id.radioButtonB: {
				nUnit = 2;
				sentakuUnit = q_num.unit.deruB;
				break;
			}
			case R.id.radioButtonC: {
				nUnit = 3;
				sentakuUnit = q_num.unit.deruC;
				break;
			}
			case R.id.radioButtonJ: {
				nUnit = 4;
				sentakuUnit = q_num.unit.Jukugo;
				break;
			}
			case R.id.radioButtonAll: {
				nUnit = 5;
				sentakuUnit = q_num.unit.all;
				nShurui = 4;
				sentakuShurui = q_num.shurui.matome;
				break;
			}
			case R.id.radioButtonV: {
				nShurui = 1;
				sentakuShurui = q_num.shurui.verb;
				break;
			}
			case R.id.radioButtonN: {
				nShurui = 2;
				sentakuShurui = q_num.shurui.noum;
				break;
			}
			case R.id.radioButtonAj: {
				nShurui = 3;
				sentakuShurui = q_num.shurui.adjective;
				break;
			}
			case R.id.radioButtonM: {
				nShurui = 4;
				sentakuShurui = q_num.shurui.matome;
				break;
			}
			case R.id.radioButtonW: {
				nWordPhraseOrTest = 1;
				WordPhraseOrTest = q_num.mode.word;
				Log.d(tag, "nWorPorT=1");
				break;
			}
			case R.id.radioButtonP: {
				nWordPhraseOrTest = 2;
				WordPhraseOrTest = q_num.mode.phrase;
				Log.d(tag, "nWorPorT=2");
				break;
			}
			case R.id.radioButtonT: {
				nWordPhraseOrTest = 3;
				WordPhraseOrTest = q_num.mode.test;
				Log.d(tag, "nWorPorT=3");
				break;
			}
			case R.id.radioButtonExclusiveTest:{
				nWordPhraseOrTest=4;
				WordPhraseOrTest= q_num.mode.exTest;
				break;
			}
			case R.id.radioButtonSortTest:{
				nWordPhraseOrTest=5;
				WordPhraseOrTest= q_num.mode.sortTest;
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity.ResetMediaPlayer();
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
}