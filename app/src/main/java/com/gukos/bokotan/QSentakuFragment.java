package com.gukos.bokotan;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.Context.ALARM_SERVICE;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
import static android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
import static com.gukos.bokotan.CommonVariables.strQ;
import static com.gukos.bokotan.CommonVariables.swOnlyFirst;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.makeToastForShort;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.debug_tag;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.openWriteFileWithExistCheck;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.readFromFile;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnQSentakuActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.fnAppSettings;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getAllFileNames;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getAllPreferenceData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getSetting;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.initializeSettingItem;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putAllData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putAllSetting;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.MyLibrary.getBuildDate;
import static com.gukos.bokotan.MyLibrary.getNowTime;
import static com.gukos.bokotan.MyLibrary.strExceptionFIlePath;
import static com.gukos.bokotan.MyLibrary.stringBokotanDirPath;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.SettingFragment.cbAutoStop;
import static com.gukos.bokotan.SettingFragment.swHyojiBeforeRead;
import static com.gukos.bokotan.SettingFragment.swMaruBatu;
import static com.gukos.bokotan.SettingFragment.switchSkipOboe;
import static com.gukos.bokotan.SettingFragment.switchSortHanten;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QSentakuFragment extends Fragment {
	
	private Context context;
	private FragmentActivity activity;
	private View viewFragment;
	
	private <T extends View> T findViewById(int id) {return viewFragment.findViewById(id);}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//スーパーメソッド呼び出しは不要、ここに処理は書かない
		return inflater.inflate(R.layout.fragment_q_sentaku, container, false);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			
			//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
			// .penaltyLog().build());
			
			context = getContext();
			activity = getActivity();
			viewFragment = view;
			new Thread(() -> activity.runOnUiThread(this::initialize)).start();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public void initialize() {
		try {
			Spinner spinnerHanni = findViewById(R.id.spinnerHanni);
			spinnerHanni.setSelection(getIntData(context, "spinnerHanni", "selected", 4));
			spinnerHanni.setOnItemSelectedListener((MyLibrary.UiInterface.AdapterViewItemSelected) this::SpinnerHanniOnItemSelectedListener);
			
			Spinner spinnerHinsi = findViewById(R.id.spinnerHinsi);
			spinnerHinsi.setSelection(getIntData(context, "spinnerHinsi", "selected", 3));
			spinnerHinsi.setOnItemSelectedListener((MyLibrary.UiInterface.AdapterViewItemSelected) this::SpinnerHinsiOnItemSelectedListener);
			
			Spinner spinnerMode = findViewById(R.id.spinnerMode);
			spinnerMode.setSelection(getIntData(context, "spinnerMode", "selected", 2));
			spinnerMode.setOnItemSelectedListener((MyLibrary.UiInterface.AdapterViewItemSelected) this::SpinnerModeOnItemSelectedListener);
			
			CommonVariables.trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
			
			//バージョン表記
			this.<TextView>findViewById(R.id.textViewVersion).setText(getBuildDate(context));
			
			//権限リクエスト
			//最前面に表示
			// https://maku77.github.io/android/ui/always-top.html
			ArrayList<String> needPermissions = new ArrayList<>();
			ArrayList<String> allPermissions = new ArrayList<>(List.of(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, BLUETOOTH, BLUETOOTH_ADMIN));
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
				allPermissions.add(BLUETOOTH_CONNECT);
				allPermissions.add(BLUETOOTH_SCAN);
			}
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
				allPermissions.add(READ_MEDIA_AUDIO);
				allPermissions.add(POST_NOTIFICATIONS);
			}
			for (var permission : allPermissions) {
				if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					needPermissions.add(permission);
				}
			}
			if (!needPermissions.isEmpty()) {
				activity.requestPermissions(needPermissions.toArray(new String[0]), 101);
			}
			//ファイル読み書きの確認
			//https://developer.android.com/training/data-storage/manage-all-files?hl=ja
			//https://takusan23.github.io/Bibouroku/2020/05/04/Android11%E3%81%AEMANAGE-EXTERNAL-STORAGE%E3%82%92%E8%A9%A6%E3%81%99/
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				if (!Environment.isExternalStorageManager()) {
					startActivity(new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
				}
			}
			
			EarbudsConnectReceiver ecr = new EarbudsConnectReceiver(context, () -> {
				if (cbAutoStop.isChecked())
					MyLibrary.DisplayOutput.makeToastForLong(context, "有線イヤホン接続");
			}, () -> {
				if (cbAutoStop.isChecked()) {
					MyLibrary.DisplayOutput.makeToastForLong(context, "有線イヤホン切断");
					Intent intent1 = new Intent(context, PlaySound.class);
					context.stopService(intent1);
				}
			}, () -> {
				if (cbAutoStop.isChecked())
					MyLibrary.DisplayOutput.makeToastForLong(context, "bluetothイヤホン接続");
			}, () -> {
				if (cbAutoStop.isChecked()) {
					MyLibrary.DisplayOutput.makeToastForLong(context, "bluetoothイヤホン切断");
					Intent intent1 = new Intent(context, PlaySound.class);
					context.stopService(intent1);
				}
			});
			
			activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			
			
			//TODO:タブ表示
			
			
			findViewById(R.id.buttonPrefExport).setOnClickListener(this::onExportPrefsButton);
			findViewById(R.id.buttonPrefImp).setOnClickListener(this::onImportPrefsButton);
			findViewById(R.id.buttonWriteTest).setOnClickListener(this::onWriteText);
			findViewById(R.id.buttonAlarm).setOnClickListener(this::onAlarmset);
			
			for (int id : new int[]{R.id.button1q, R.id.buttonP1q, R.id.button2q, R.id.buttonP2q,
				R.id.buttonAll, R.id.buttonYume0_0, R.id.buttonYume0_8, R.id.buttonYume1,
				R.id.buttonYume2, R.id.buttonYume3, R.id.button1qEx, R.id.buttonP1qEx}) {
				findViewById(id).setOnClickListener(this::onSelectQ);
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onExportPrefsButton(View view) {
		try {
			for (var fileName : getAllFileNames()) {
				String strFilePath = stringBokotanDirPath + fileName + ".txt";
				FileWriter fileWriter = openWriteFileWithExistCheck(context, strFilePath, false);
				fileWriter.write(getAllPreferenceData(context, fileName));
				fileWriter.close();
			}
			makeToastForShort(context, "設定の書き込みに成功しました。");
		} catch (Exception exception) {
			Log.d(debug_tag + "filewriting", exception.getMessage() + exception.getClass().getTypeName());
			MyLibrary.DisplayOutput.makeToastForLong(context, "設定の書き込みに失敗しました。");
		}
	}
	
	private void onImportPrefsButton(View view) {
		try {
			putAllSetting(context, readFromFile(context, stringBokotanDirPath + fnAppSettings + ".txt"));
			for (int id : new int[]{R.id.switchOnlyFirst, R.id.switchHyojiYakuBeforeRead, R.id.switchSkipOboe, R.id.switchSkipMaruBatu, R.id.switchSortHanten}) {
				((Switch) findViewById(id)).setChecked(getSetting(context, "id" + id, true));
			}
			for (int id : new int[]{R.id.checkBoxDefaultAdapter, R.id.checkBoxAutoStop, R.id.checkBoxHatsuonkigou}) {
				((CheckBox) findViewById(id)).setChecked(getSetting(context, "id" + id, false));
			}
			for (var fileName : getAllFileNames()) {
				final String strFilePath = stringBokotanDirPath + fileName + ".txt";
				putAllData(context, fileName, readFromFile(context, strFilePath));
			}
			makeToastForShort(context, "設定の読み込みに成功しました。");
		} catch (Exception exception) {
			Log.d(debug_tag + "filewriting", exception.getMessage() + exception.getClass().getTypeName());
			MyLibrary.DisplayOutput.makeToastForLong(context, "設定の読み込みに失敗しました。");
		}
	}
	
	private void onWriteText(View view) {
		try {
			FileWriter fileWriter = openWriteFileWithExistCheck(context, strExceptionFIlePath, false);
			fileWriter.write("書き込みテスト: " + MyLibrary.packageName + "(" + getNowTime() + ")");
			fileWriter.close();
		} catch (Exception exception) {
			Log.d(debug_tag + "filewriting", exception.getMessage() + exception.getClass().getTypeName());
			String message = "ファイル書き込みに失敗" + exception.getMessage();
			MyLibrary.DisplayOutput.makeToastForLong(context, message);
			return;
		}
		MyLibrary.DisplayOutput.makeToastForLong(context, "ファイル書き込みに成功しました。");
	}
	
	private void onSelectQ(View v) {
		try {
			EditText editTextNowNumber = findViewById(R.id.editTextNumber);
			if (editTextNowNumber.length() != 0) {
				CommonVariables.nowIsDecided = true;
				CommonVariables.now = Integer.parseInt(editTextNowNumber.getText().toString()) - 1;
			}
			else {
				CommonVariables.nowIsDecided = false;
			}
			if (switchSkipOboe != null) {
				CommonVariables.bSkipOboe = switchSkipOboe.isChecked();
			}
			else CommonVariables.bSkipOboe = false;
			CommonVariables.bHyojiYakuBeforeRead = swHyojiBeforeRead.isChecked();
			CommonVariables.bEnglishToJapaneseOrder = SettingFragment.radioButtonEtoJ.isChecked();
			CommonVariables.bSort = switchSortHanten.isChecked();
			switch (v.getId()) {
				case R.id.button1q: {
					strQ = "1q";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.str1q;
					CommonVariables.sentakuQ = WordPhraseData.q_num.test1q;
					break;
				}
				default:
				case R.id.buttonP1q: {
					strQ = "p1q";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.strp1q;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testp1q;
					break;
				}
				case R.id.button2q: {
					strQ = "2q";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.str2q;
					CommonVariables.sentakuQ = WordPhraseData.q_num.test2q;
					break;
				}
				case R.id.buttonP2q: {
					strQ = "p2q";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.strp2q;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testp2q;
					break;
				}
				case R.id.button1qEx: {
					strQ = "tanjukugo1q";
					CommonVariables.sentakuQ = WordPhraseData.q_num.test1qEx;
					break;
				}
				case R.id.buttonP1qEx: {
					strQ = "tanjukugop1q";
					CommonVariables.sentakuQ = WordPhraseData.q_num.testp1qEx;
					break;
				}
				case R.id.buttonYume0_0:
				case R.id.buttonYume0_8: {
					strQ = "y08";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.stry08;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testy08;
					break;
				}
				case R.id.buttonYume1: {
					strQ = "y1";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.stry1;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testy1;
					break;
				}
				case R.id.buttonYume2: {
					strQ = "y2";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.stry2;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testy2;
					break;
				}
				case R.id.buttonYume3: {
					strQ = "y3";
					CommonVariables.strQenum = WordPhraseData.q_num.strQ.stry3;
					CommonVariables.sentakuQ = WordPhraseData.q_num.testy3;
					break;
				}
			}
			CommonVariables.isWordAndPhraseMode = false;
			switch (CommonVariables.nWordPhraseOrTest) {
				//単語
				default:
				case 1: {
					if (strQ.endsWith("1q") || strQ.startsWith("y")) {
						onStartPlaying();
					}
					else {
						Toast.makeText(context, "単語は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				//文
				case 2: {
					if (!strQ.endsWith("1q") && !strQ.endsWith("p1q") && !strQ.startsWith("y")) {
						Toast.makeText(context, "文は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
						return;
					}
					strQ = "ph" + strQ;
					onStartPlaying();
					break;
				}
				//単語+文
				case 6: {
					if (!strQ.endsWith("1q") && !strQ.endsWith("p1q") && !strQ.startsWith("y")) {
						Toast.makeText(context, "単語+文は1級、準1級、ユメタンのみです。", Toast.LENGTH_SHORT).show();
						return;
					}
					CommonVariables.isWordAndPhraseMode = true;
					onStartPlaying();
					break;
				}
				//テスト
				case 3:
				case 4:
				case 5: {
					strQ = strQ + "Test";
					TestActivity.bSkipMaruBatuButton = swMaruBatu.isChecked();
					startActivity(new Intent(context, TestActivity.class));
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onStartPlaying() {
		//再生
		TabActivity.setTabPageNum(2);
		PlayerFragment.initialize(getContext());
		context.startForegroundService(new Intent(context, PlaySound.class));
	}
	
	private void onAlarmset(View v) {
		try {
			// 現在時刻を取得
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			
			// 時間選択ダイアログの生成
			TimePickerDialog timepick = new TimePickerDialog(context,
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
				                                                 Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
				                                                 PendingIntent pending = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
				                                                 // アラームをセットする
				                                                 AlarmManager am =
					                                                 (AlarmManager) context.getSystemService(ALARM_SERVICE);
				                                                 if (am != null) {
					                                                 am.setExact(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), pending);
					                                                 Toast.makeText(context, "Set Alarm ", Toast.LENGTH_SHORT).show();
				                                                 }
			                                                 }, hour, minute, true);
			// 表示
			timepick.show();
		} catch (Exception e) {
			showException(context, e);
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
			showException(context, e);
			return null;
		}
	}
	
	public void SpinnerHanniOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			MyLibrary.PreferenceManager.putIntData(context, "spinnerHanni", "selected", i);
			switch (i) {
				case 0: {
					CommonVariables.nUnit = 1;
					CommonVariables.sentakuUnit = WordPhraseData.q_num.unit.deruA;
					break;
				}
				case 1: {
					CommonVariables.nUnit = 2;
					CommonVariables.sentakuUnit = WordPhraseData.q_num.unit.deruB;
					break;
				}
				case 2: {
					CommonVariables.nUnit = 3;
					CommonVariables.sentakuUnit = WordPhraseData.q_num.unit.deruC;
					break;
				}
				case 3: {
					CommonVariables.nUnit = 4;
					CommonVariables.sentakuUnit = WordPhraseData.q_num.unit.Jukugo;
					break;
				}
				case 4: {
					CommonVariables.nUnit = 5;
					CommonVariables.sentakuUnit = WordPhraseData.q_num.unit.all;
					CommonVariables.nShurui = 4;
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void SpinnerHinsiOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			MyLibrary.PreferenceManager.putIntData(context, "spinnerHinsi", "selected", i);
			switch (i) {
				case 0: {
					CommonVariables.nShurui = 1;
					break;
				}
				case 1: {
					CommonVariables.nShurui = 2;
					break;
				}
				case 2: {
					CommonVariables.nShurui = 3;
					break;
				}
				case 3: {
					CommonVariables.nShurui = 4;
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void SpinnerModeOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			MyLibrary.PreferenceManager.putIntData(context, "spinnerMode", "selected", i);
			switch (i) {
				case 0: {
					//単語
					CommonVariables.nWordPhraseOrTest = 1;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.word;
					break;
				}
				case 1: {
					//文
					CommonVariables.nWordPhraseOrTest = 2;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.phrase;
					break;
				}
				case 2: {
					//単語+文
					CommonVariables.nWordPhraseOrTest = 6;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.wordPlusPhrase;
					break;
				}
				case 3: {
					//ランダムテスト
					CommonVariables.nWordPhraseOrTest = 3;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.randomTest;
					break;
				}
				case 4: {
					//正答率テスト
					CommonVariables.nWordPhraseOrTest = 4;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.huseikainomiTest;
					break;
				}
				case 5: {
					//順番テスト
					CommonVariables.nWordPhraseOrTest = 5;
					CommonVariables.WordPhraseOrTest = WordPhraseData.q_num.mode.seitouritsujunTest;
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
}