package com.gukos.bokotan;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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

import java.util.Calendar;
import java.util.Date;

public class Q_sentaku_activity extends AppCompatActivity {
	static boolean skipwords;
	static boolean nowIsDecided =false;
	Switch swSkipKioku,swMaruBatu,swHyojiBeforeRead;
	RadioButton radioButtonEtoJ;
	EditText e;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_q_sentaku);
		MainActivity.mp=null;

		//new AlertDialog.Builder(this).setTitle("title").setMessage("message").setPositiveButton("ok",null).create().show();
		swSkipKioku =findViewById(R.id.switchSkipOboe);
		swMaruBatu=findViewById(R.id.switchSkipMaruBatu);
		swHyojiBeforeRead=findViewById(R.id.switchHyojiYakuBeforeRead);
		e=findViewById(R.id.editTextNumber);
		radioButtonEtoJ=findViewById(R.id.radioButtonEtoJ);

	}

	public void onButtonClick(View v)
	{
		String str=null;
		if (e.length()!=0) {
			nowIsDecided = true;
			MainActivity.now = Integer.parseInt(e.getText().toString()) - 1;
		}
		switch (((Button)v).getText().toString())
		{
			case "1Qword":
			{
				strQ="1q";
				break;
			}
			case "1Qphrase":
			{
				strQ="ph"+"1q";
				break;
			}
			case "p1Qword":
			{
				strQ="p1q";
				break;
			}
			case "p1Qphrase":
			{
				strQ="ph"+"p1q";
				break;
			}
		}
		Log.d(MainActivity.tag,"strQ="+strQ);
		if (swSkipKioku !=null) {
			skipwords = swSkipKioku.isChecked();
		} else skipwords=false;
		MainActivity.bHyojiYakuBeforeRead=swHyojiBeforeRead.isChecked();
		MainActivity.bEtoJ=radioButtonEtoJ.isChecked();
		startActivity(new Intent(this,MainActivity.class));
	}

	public void onP1qTestButtonClick(View v)
	{
		strQ="p1qTest";
		TestActivity.bSkipMaruBatuButton=swMaruBatu.isChecked();
		startActivity(new Intent(this,TestActivity.class));
	}

	public void on1qTestButtonClick(View v)
	{
		strQ="1qTest";
		TestActivity.bSkipMaruBatuButton=swMaruBatu.isChecked();
		startActivity(new Intent(this,TestActivity.class));
	}

	static int idCheckedA=0,idCheckedB=0,idCheckedC=0,idCheckedOther=R.id.radioButtonAll,nRadioIdForRange=R.id.radioButtonAll;
	public  void onRadioChecked(View v)
	{
		((RadioButton)v).setChecked(true);
		nRadioIdForRange=((RadioButton)v).getId();
		switch (nRadioIdForRange){
			case R.id.radioButtonAV:
			case R.id.radioButtonAN:
			case R.id.radioButtonAA:
			case R.id.radioButtonAM:{
				idCheckedA=nRadioIdForRange;
				if (idCheckedB!=0) ((RadioButton)findViewById(idCheckedB)).setChecked(false);
				if (idCheckedC!=0) ((RadioButton)findViewById(idCheckedC)).setChecked(false);
				if (idCheckedOther!=0) ((RadioButton)findViewById(idCheckedOther)).setChecked(false);
				break;
			}
			case R.id.radioButtonBV:
			case R.id.radioButtonBN:
			case R.id.radioButtonBA:
			case R.id.radioButtonBM:{
				idCheckedB=nRadioIdForRange;
				if (idCheckedA!=0) ((RadioButton)findViewById(idCheckedA)).setChecked(false);
				if (idCheckedC!=0) ((RadioButton)findViewById(idCheckedC)).setChecked(false);
				if (idCheckedOther!=0) ((RadioButton)findViewById(idCheckedOther)).setChecked(false);
				break;
			}
			case R.id.radioButtonCV:
			case R.id.radioButtonCN:
			case R.id.radioButtonCA:
			case R.id.radioButtonCM:{
				idCheckedC=nRadioIdForRange;
				if (idCheckedA!=0) ((RadioButton)findViewById(idCheckedA)).setChecked(false);
				if (idCheckedB!=0) ((RadioButton)findViewById(idCheckedB)).setChecked(false);
				if (idCheckedOther!=0) ((RadioButton)findViewById(idCheckedOther)).setChecked(false);
				break;
			}
			case R.id.radioButtonJukugo:
			case R.id.radioButtonAll:
			default:{
				idCheckedOther=nRadioIdForRange;
				if (idCheckedA!=0) ((RadioButton)findViewById(idCheckedA)).setChecked(false);
				if (idCheckedB!=0) ((RadioButton)findViewById(idCheckedB)).setChecked(false);
				if (idCheckedC!=0) ((RadioButton)findViewById(idCheckedC)).setChecked(false);
				break;
			}
		}

		String str=null;
		switch (nRadioIdForRange){
			case R.id.radioButtonAV:{
				str="radioButtonAV";
				break;
			}
			case R.id.radioButtonAN:{
				str="radioButtonAN";
				break;
			}
			case R.id.radioButtonAA:{
				str="radioButtonAA";
				break;
			}
			case R.id.radioButtonAM:{
				str="radioButtonAM";
				break;
			}
			case R.id.radioButtonBV:{
				str="radioButtonBV";
				break;
			}
			case R.id.radioButtonBN:{
				str="radioButtonBN";
				break;
			}
			case R.id.radioButtonBA:{
				str="radioButtonBA";
				break;
			}
			case R.id.radioButtonBM:{
				str="radioButtonBM";
				break;
			}
			case R.id.radioButtonCV:{
				str="radioButtonCV";
				break;
			}
			case R.id.radioButtonCN:{
				str="radioButtonCN";
				break;
			}
			case R.id.radioButtonCA:{
				str="radioButtonCA";
				break;
			}
			case R.id.radioButtonCM:{
				str="radioButtonCM";
				break;
			}
			case R.id.radioButtonJukugo:{
				str="radioButtonJukugo";
				break;
			}
			case R.id.radioButtonAll:{
				str="radioButtonAll";
				break;
			}
			default:{
				str="default";
				break;
			}
		}
		Log.d("com.gukos.bokotan",str+"idnum:"+(nRadioIdForRange-R.id.radioButtonAV));

	}

	@Override
	public void onResume() {
		super.onResume();
		MainActivity.ResetMediaPlayer();
	}

	public void onAlarmset(View v){
		int h,m,s;
		// 現在時刻を取得
		Calendar calendar = Calendar.getInstance();
		int hour   = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		// 時間選択ダイアログの生成
		TimePickerDialog timepick= new TimePickerDialog(this,
				new TimePickerDialog.OnTimeSetListener() {
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						// 設定 ボタンクリック時の処理
						// 時間をセットする
						Calendar calendar = Calendar.getInstance();
						// Calendarを使って現在の時間をミリ秒で取得
						calendar.setTimeInMillis(System.currentTimeMillis());
						// 設定
						calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
						calendar.set(Calendar.MINUTE,minute);
						//明示的なBroadCast
						Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
						PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
						// アラームをセットする
						AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
						if(am != null){
							am.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pending);
							Toast.makeText(getApplicationContext(), "Set Alarm ", Toast.LENGTH_SHORT).show();
						}
					}
				}, hour, minute, true);
		// 表示
		timepick.show();
	}

}