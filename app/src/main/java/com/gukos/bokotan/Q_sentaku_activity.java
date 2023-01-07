package com.gukos.bokotan;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
import static com.gukos.bokotan.MyLibrary.getBoolData;
import static com.gukos.bokotan.MyLibrary.getIntData;
import static com.gukos.bokotan.MyLibrary.putBoolData;
import static com.gukos.bokotan.MyLibrary.putIntData;
import static com.gukos.bokotan.MyLibrary.showException;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MyLibrary.puts;
import static com.gukos.bokotan.MyLibrary.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeMap;

public class Q_sentaku_activity extends AppCompatActivity {
	static boolean bSkipOboe;
	static boolean nowIsDecided = false;
	static boolean isWordAndPhraseMode = false;
	static Switch swOnlyFirst, switchSkipOboe, swMaruBatu, swHyojiBeforeRead, switchSortHanten;
	RadioButton radioButtonEtoJ;
	EditText editTextNowNumber, editTextPipYoko, editTextPipTate;
	static MyLibrary.q_num sentakuQ = MyLibrary.q_num.testp1q;
	static MyLibrary.q_num.mode WordPhraseOrTest = MyLibrary.q_num.mode.word;
	static MyLibrary.q_num.unit sentakuUnit = MyLibrary.q_num.unit.all;
	static MyLibrary.q_num.shurui sentakuShurui = MyLibrary.q_num.shurui.matome;
	static MyLibrary.q_num.strQ strQenum = MyLibrary.q_num.strQ.strp1q;
	static boolean bSort = true;
	static MyLibrary.q_num.skipjouken skipjoken = MyLibrary.q_num.skipjouken.kirokunomi;
	static CheckBox cbDirTOugou, cbDefaultAdapter, cbAutoStop, checkBoxHatsuonKigou;
	static int nUnit = 5, nShurui = 4, nWordPhraseOrTest = 1;

	public static TreeMap<String, GogenYomu> trGogenYomu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_q_sentaku);

			//new AlertDialog.Builder(this).setTitle("title").setMessage("message").setPositiveButton("ok",null).create().show();
			swOnlyFirst = findViewById(R.id.switchOnlyFirst);
			switchSkipOboe = findViewById(R.id.switchSkipOboe);
			swMaruBatu = findViewById(R.id.switchSkipMaruBatu);
			swHyojiBeforeRead = findViewById(R.id.switchHyojiYakuBeforeRead);
			editTextNowNumber = findViewById(R.id.editTextNumber);
			editTextPipYoko = findViewById(R.id.editTextPipYoko);
			editTextPipTate = findViewById(R.id.editTextPipTate);
			switchSortHanten = findViewById(R.id.switchSortHanten);

			editTextPipYoko.setText(String.valueOf(getIntData(this, "editText", "editTextPipYoko", 16)));
			pipYoko = Integer.parseInt(editTextPipYoko.getText().toString());
			editTextPipYoko.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					try {
						if (s.length() > 0) {
							pipYoko = Integer.parseInt(s.toString());
							putIntData(getApplicationContext(), "editText", "editTextPipYoko", pipYoko);
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}
			});
			editTextPipTate.setText(String.valueOf(getIntData(this, "editText", "editTextPipTate", 9)));
			pipTate = Integer.parseInt(editTextPipTate.getText().toString());
			editTextPipTate.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {

				}

				@Override
				public void afterTextChanged(Editable s) {
					try {
						if (s.length() > 0) {
							pipTate = Integer.parseInt(s.toString());
							putIntData(getApplicationContext(), "editText", "editTextPipTate", pipTate);
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}
			});

			swOnlyFirst.setChecked(getBoolData(this, "swOnlyFirst", "checked", true));
			swOnlyFirst.setOnClickListener(view -> putBoolData(this, "swOnlyFirst", "checked", ((Switch) view).isChecked()));
			swHyojiBeforeRead.setChecked(getBoolData(this, "swHyojiBeforeRead", "checked", true));
			swHyojiBeforeRead.setOnClickListener(view -> putBoolData(this, "swHyojiBeforeRead", "checked", ((Switch) view).isChecked()));
			switchSkipOboe.setChecked(getBoolData(this, "switchSkipOboe", "checked", true));
			switchSkipOboe.setOnClickListener(view -> putBoolData(this, "switchSkipOboe", "checked", ((Switch) view).isChecked()));
			swMaruBatu.setChecked(getBoolData(this, "swMaruBatu", "checked", true));
			swMaruBatu.setOnClickListener(view -> putBoolData(this, "swMaruBatu", "checked", ((Switch) view).isChecked()));
			switchSortHanten.setChecked(getBoolData(this, "switchSortHanten", "checked", false));
			switchSortHanten.setOnClickListener(view -> putBoolData(this, "switchSortHanten", "checked", ((Switch) view).isChecked()));

			radioButtonEtoJ = findViewById(R.id.radioButtonEtoJ);

			cbDirTOugou = findViewById(R.id.checkBoxDirTougou);
			cbDefaultAdapter = findViewById(R.id.checkBoxDefaultAdapter);
			cbAutoStop = findViewById(R.id.checkBoxAutoStop);
			checkBoxHatsuonKigou = findViewById(R.id.checkBoxHatsuonkigou);

			cbDirTOugou.setChecked(getBoolData(this, "cbDirTOugou", "checked", false));
			cbDirTOugou.setOnClickListener(view -> putBoolData(this, "cbDirTOugou", "checked", ((CheckBox) view).isChecked()));
			cbDefaultAdapter.setChecked(getBoolData(this, "cbDefaultAdapter", "checked", false));
			cbDefaultAdapter.setOnClickListener(view -> putBoolData(this, "cbDefaultAdapter", "checked", ((CheckBox) view).isChecked()));
			cbAutoStop.setChecked(getBoolData(this, "cbAutoStop", "checked", false));
			cbAutoStop.setOnClickListener(view -> putBoolData(this, "cbAutoStop", "checked", ((CheckBox) view).isChecked()));
			checkBoxHatsuonKigou.setChecked(getBoolData(this, "checkBoxHatsuonKigou", "checked", false));
			checkBoxHatsuonKigou.setOnClickListener(view -> putBoolData(this, "checkBoxHatsuonKigou", "checked", ((CheckBox) view).isChecked()));

			Spinner spinnerHanni = findViewById(R.id.spinnerHanni);
			Spinner spinnerHinsi = findViewById(R.id.spinnerHinsi);
			Spinner spinnerMode = findViewById(R.id.spinnerMode);
			Spinner spinnerKuuhaku = findViewById(R.id.spinnerKuuhaku);

			spinnerHanni.setSelection(getIntData(this, "spinnerHanni", "selected", 4));
			spinnerHinsi.setSelection(getIntData(this, "spinnerHinsi", "selected", 3));
			spinnerMode.setSelection(getIntData(this, "spinnerMode", "selected", 2));
			spinnerKuuhaku.setSelection(getIntData(this, "spinnerKuuhaku", "selected", 0));

			spinnerHanni.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					try {
						MyLibrary.putIntData(getApplicationContext(), "spinnerHanni", "selected", i);
						switch (i) {
							case 0: {
								nUnit = 1;
								sentakuUnit = MyLibrary.q_num.unit.deruA;
								break;
							}
							case 1: {
								nUnit = 2;
								sentakuUnit = MyLibrary.q_num.unit.deruB;
								break;
							}
							case 2: {
								nUnit = 3;
								sentakuUnit = MyLibrary.q_num.unit.deruC;
								break;
							}
							case 3: {
								nUnit = 4;
								sentakuUnit = MyLibrary.q_num.unit.Jukugo;
								break;
							}
							case 4: {
								nUnit = 5;
								sentakuUnit = MyLibrary.q_num.unit.all;
								nShurui = 4;
								sentakuShurui = MyLibrary.q_num.shurui.matome;
								break;
							}
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				}
			});
			spinnerHinsi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					try {
						MyLibrary.putIntData(getApplicationContext(), "spinnerHinsi", "selected", i);
						switch (i) {
							case 0: {
								nShurui = 1;
								sentakuShurui = MyLibrary.q_num.shurui.verb;
								break;
							}
							case 1: {
								nShurui = 2;
								sentakuShurui = MyLibrary.q_num.shurui.noum;
								break;
							}
							case 2: {
								nShurui = 3;
								sentakuShurui = MyLibrary.q_num.shurui.adjective;
								break;
							}
							case 3: {
								nShurui = 4;
								sentakuShurui = MyLibrary.q_num.shurui.matome;
								break;
							}
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				}
			});
			spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					try {
						MyLibrary.putIntData(getApplicationContext(), "spinnerMode", "selected", i);
						switch (i) {
							case 0: {
								//単語
								nWordPhraseOrTest = 1;
								WordPhraseOrTest = MyLibrary.q_num.mode.word;
								break;
							}
							case 1: {
								//文
								nWordPhraseOrTest = 2;
								WordPhraseOrTest = MyLibrary.q_num.mode.phrase;
								break;
							}
							case 2: {
								//単語+文
								nWordPhraseOrTest = 6;
								WordPhraseOrTest = MyLibrary.q_num.mode.wordPlusPhrase;
								break;
							}
							case 3: {
								//ランダムテスト
								nWordPhraseOrTest = 3;
								WordPhraseOrTest = MyLibrary.q_num.mode.randomTest;
								break;
							}
							case 4: {
								//正答率テスト
								nWordPhraseOrTest = 4;
								WordPhraseOrTest = MyLibrary.q_num.mode.huseikainomiTest;
								break;
							}
							case 5: {
								//順番テスト
								nWordPhraseOrTest = 5;
								WordPhraseOrTest = MyLibrary.q_num.mode.seitouritsujunTest;
								break;
							}
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				}
			});
			spinnerKuuhaku.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					try {
						putIntData(getApplicationContext(), "spinnerKuuhaku", "selected", i);
						switch (i) {
							default:
							case 0: {
								strDirectoryNameForKuuhaku = "";
								break;
							}
							case 1: {
								strDirectoryNameForKuuhaku = "autocut-";
								break;
							}
							case 2: {
								strDirectoryNameForKuuhaku = "manucut-";
								break;
							}
						}
					} catch (Exception e) {
						showException(getApplicationContext(), e);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				}
			});

			trGogenYomu = new GogenYomuFactory(this).getTrGogenYomu();

			((TextView) findViewById(R.id.textViewVersion)).setText("Version:" + getVersionName(this));

			((RadioButton) findViewById(getIntData(this, MyLibrary.DataName.qSentakuActivity, "RadioButton", R.id.radioButtonOnlyKioku))).setChecked(true);

			//権限リクエスト
			//最前面に表示
			// https://maku77.github.io/android/ui/always-top.html
			ArrayList<String> permissions = new ArrayList<>();
			for (String strPermission : new String[]{
					READ_EXTERNAL_STORAGE,
					READ_MEDIA_AUDIO,
					POST_NOTIFICATIONS,
					BLUETOOTH, BLUETOOTH_ADMIN,
					BLUETOOTH_CONNECT,
					BLUETOOTH_SCAN,
			}) {
				if (ContextCompat.checkSelfPermission(this, strPermission) != PackageManager.PERMISSION_GRANTED) {
					permissions.add(strPermission);
				}
			}
			if (!permissions.isEmpty()) {
				ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 101);
			}
			//権限の確認 SYSTEM_ALERT_WINDOW
			//https://akira-watson.com/android/windowmanager.html
			if (!Settings.canDrawOverlays(this)) {
				Intent intent = new Intent(ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
				startActivity(intent);
			}

			EarbudsConnectReceiver ecr = new EarbudsConnectReceiver(this, () -> {
				if (cbAutoStop.isChecked())
					MyLibrary.makeToastForLong(this, "有線イヤホン接続");
			}, () -> {
				if (cbAutoStop.isChecked()) {
					MyLibrary.makeToastForLong(this, "有線イヤホン切断");
					Intent intent = new Intent(this, PlaySound.class);
					stopService(intent);
				}
			}, () -> {
				if (cbAutoStop.isChecked())
					MyLibrary.makeToastForLong(this, "bluetothイヤホン接続");
			}, () -> {
				if (cbAutoStop.isChecked()) {
					MyLibrary.makeToastForLong(this, "bluetoothイヤホン切断");
					Intent intent = new Intent(this, PlaySound.class);
					stopService(intent);
				}
			});

			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			showException(this, e);
		}
	}

	public void onSelectQ(View v) {
		try {
			if (editTextNowNumber.length() != 0) {
				nowIsDecided = true;
				MainActivity.now = Integer.parseInt(editTextNowNumber.getText().toString()) - 1;
			}
			if (switchSkipOboe != null) {
				bSkipOboe = switchSkipOboe.isChecked();
			} else bSkipOboe = false;
			MainActivity.bHyojiYakuBeforeRead = swHyojiBeforeRead.isChecked();
			MainActivity.bEnglishToJapaneseOrder = radioButtonEtoJ.isChecked();
			bSort = ((Switch) findViewById(R.id.switchSortHanten)).isChecked();
			switch (v.getId()) {
				case R.id.button1q: {
					strQ = "1q";
					strQenum = MyLibrary.q_num.strQ.str1q;
					sentakuQ = MyLibrary.q_num.test1q;
					break;
				}
				default:
				case R.id.buttonP1q: {
					strQ = "p1q";
					strQenum = MyLibrary.q_num.strQ.strp1q;
					sentakuQ = MyLibrary.q_num.testp1q;
					break;
				}
				case R.id.button2q: {
					strQ = "2q";
					strQenum = MyLibrary.q_num.strQ.str2q;
					sentakuQ = MyLibrary.q_num.test2q;
					break;
				}
				case R.id.buttonP2q: {
					strQ = "p2q";
					strQenum = MyLibrary.q_num.strQ.strp2q;
					sentakuQ = MyLibrary.q_num.testp2q;
					break;
				}
				case R.id.button1qEx: {
					strQ = "tanjukugo1q";
					sentakuQ = MyLibrary.q_num.test1qEx;
					break;
				}
				case R.id.buttonP1qEx: {
					strQ = "tanjukugop1q";
					sentakuQ = MyLibrary.q_num.testp1qEx;
					break;
				}
				case R.id.buttonYume0_0:
				case R.id.buttonYume0_8: {
					strQ = "y08";
					strQenum = MyLibrary.q_num.strQ.stry08;
					sentakuQ = MyLibrary.q_num.testy08;
					break;
				}
				case R.id.buttonYume1: {
					strQ = "y1";
					strQenum = MyLibrary.q_num.strQ.stry1;
					sentakuQ = MyLibrary.q_num.testy1;
					break;
				}
				case R.id.buttonYume2: {
					strQ = "y2";
					strQenum = MyLibrary.q_num.strQ.stry2;
					sentakuQ = MyLibrary.q_num.testy2;
					break;
				}
				case R.id.buttonYume3: {
					strQ = "y3";
					strQenum = MyLibrary.q_num.strQ.stry3;
					sentakuQ = MyLibrary.q_num.testy3;
					break;
				}
			}
			isWordAndPhraseMode = false;
			switch (nWordPhraseOrTest) {
				//単語
				default:
				case 1: {
					if (strQ.endsWith("1q") || strQ.startsWith("y")) {
						startActivity(new Intent(this, MainActivity.class));
					} else {
						Toast.makeText(this, "単語は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				//文
				case 2: {
					if (!strQ.endsWith("1q") && !strQ.endsWith("p1q") && !strQ.startsWith("y")) {
						Toast.makeText(this, "文は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
						return;
					}
					strQ = "ph" + strQ;

					startActivity(new Intent(this, MainActivity.class));
					break;
				}
				//単語+文
				case 6: {
					if (!strQ.endsWith("1q") && !strQ.endsWith("p1q") && !strQ.startsWith("y")) {
						Toast.makeText(this, "単語+文は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
						return;
					}
					isWordAndPhraseMode = true;
					startActivity(new Intent(this, MainActivity.class));
					break;
				}
				//テスト
				case 3:
				case 4:
				case 5: {
					strQ = strQ + "Test";
					TestActivity.bSkipMaruBatuButton = swMaruBatu.isChecked();
					startActivity(new Intent(this, TestActivity.class));
					break;
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}

	public void onKensakuButtonClicked(View v) {
		try {
			//new AlertDialog.Builder(this).setTitle("title").setMessage("message").setPositiveButton("ok",null).create().show();
			startActivity(new Intent(this, KensakuActivity.class));
		} catch (Exception e) {
			showException(this, e);
		}
	}

	public void onRadioChecked(View v) {
		try {
			putIntData(this, MyLibrary.DataName.qSentakuActivity, "RadioButton", v.getId());
			switch (v.getId()) {
				case R.id.radioButtonOnlyKioku: {
					skipjoken = MyLibrary.q_num.skipjouken.kirokunomi;
					break;
				}
				case R.id.radioButton1seikai: {
					skipjoken = MyLibrary.q_num.skipjouken.seikai1;
					break;
				}
				case R.id.radioButton2huseikai: {
					skipjoken = MyLibrary.q_num.skipjouken.huseikai2;
					break;
				}
				case R.id.radioButtonOnlyHugoukaku: {
					skipjoken = MyLibrary.q_num.skipjouken.onlyHugoukaku;
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}


	public void onAlarmset(View v) {
		try {
			// 現在時刻を取得
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			// 時間選択ダイアログの生成
			TimePickerDialog timepick = new TimePickerDialog(this,
					(view, hourOfDay, minute1) -> {
						// 設定 ボタンクリック時の処理
						// 時間をセットする
						Calendar calendar1 = Calendar.getInstance();
						// Calendarを使って現在の時間をミリ秒で取得
						calendar1.setTimeInMillis(System.currentTimeMillis());
						// 設定
						calendar1.set(Calendar.HOUR_OF_DAY, hourOfDay);
						calendar1.set(Calendar.MINUTE, minute1);
						//明示的なBroadCast
						Intent intent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);
						PendingIntent pending = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
						// アラームをセットする
						AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
						if (am != null) {
							am.setExact(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), pending);
							Toast.makeText(getApplicationContext(), "Set Alarm ", Toast.LENGTH_SHORT).show();
						}
					}, hour, minute, true);
			// 表示
			timepick.show();
		} catch (Exception e) {
			showException(this, e);
		}
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

		} catch (Exception e) {
			showException(this, e);
			return null;
		}
	}
}