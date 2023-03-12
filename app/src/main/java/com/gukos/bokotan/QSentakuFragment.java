package com.gukos.bokotan;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;
import static android.Manifest.permission.BLUETOOTH_CONNECT;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_MEDIA_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;
import static com.gukos.bokotan.KensakuFragment.allData;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.makeToastForShort;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.debug_tag;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.openWriteFileWithExistCheck;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.readFromFile;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
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
import static com.gukos.bokotan.MyLibrary.tangoNumToString;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.UiManager.getAdapterForSpinner;
import static com.gukos.bokotan.WordPhraseData.DataBook.eigoduke;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.svl12000;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEx;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataType.phrase;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.Svl;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoPhrase;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;
import static com.gukos.bokotan.WordPhraseData.readToList;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gukos.bokotan.WordPhraseData.DataBook;
import com.gukos.bokotan.WordPhraseData.DataQ;
import com.gukos.bokotan.WordPhraseData.q_num;
import com.gukos.bokotan.databinding.FragmentQSentakuBinding;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class QSentakuFragment extends UiManager.FragmentBingding<FragmentQSentakuBinding> {
	
	//他のクラスからアクセス
	public static SwitchMaterial switchSkipOboe;
	public static SwitchMaterial swHyojiBeforeRead;
	public static SwitchMaterial switchSortHanten;
	public static SwitchMaterial cbAutoStop;
	public static SwitchMaterial checkBoxHatsuonKigou;
	public static SwitchMaterial swOnlyFirst;
	public static SwitchMaterial switchQuizHatsuon;
	public static SwitchMaterial switchQuizOX;
	public static RadioButton radioButtonEtoJ;
	public static TreeMap<String, GogenYomu> trGogenYomu;
	static int nWordPhraseOrTest = 1;
	public static Boolean isReadingData=true;
	
	public QSentakuFragment() {
		super(FragmentQSentakuBinding::inflate);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			
			//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
			// .penaltyLog().build());
			
			new Thread(this::readAllData).start();
			new Thread(() -> activity.runOnUiThread(this::initialize)).start();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	private void readAllData() {
		//wordphrasedataの読み取り
		printCurrentState("start");
		//ファイルを開いて読み込む
		WordPhraseData.WordInfo.size = 0;
		Map<String, String> mapQName = new HashMap<>() {{
			put("1q", "1級");
			put("p1q", "準1級");
			put("2q", "2級");
			put("p2q", "準2級");
			put("3q", "3級");
			put("4q", "4級");
			put("5q", "5級");
			put("00", "ユメタン0基礎");
			put("08", "ユメタン0");
			put("1", "ユメタン1");
			put("2", "ユメタン2");
			put("3", "ユメタン3");
			put("-eiken-jukugo", "英検熟語");
			put("-eikenp1-jukugo", "英検熟語(準1)");
			put("-Toefl-Chokuzen", "TOEFL直前");
			put("-Toeic-500ten", "TOEIC500点");
			put("-Toeic-700ten", "TOEIC700点");
			put("-Toeic-900ten", "TOEIC900点");
			put("-Toeic-Chokuzen", "TOEIC直前");
			put("-Toeic-jukugo", "TOEIC熟語");
			put("d1phrase12", "1");
			put("d2phrase1", "2");
			
		}};
		
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			var list = readToList(PasstanWord + q, context, passTan, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo("パス単" + mapQName.get(q), tangoNumToString("パス単" + mapQName.get(q), i), list, i, word));
			}
			WordPhraseData.map.put(PasstanWord + q, list);
		}
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoWord + q, context, tanjukugo, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, word));
			}
			WordPhraseData.map.put(TanjukugoWord + q, list);
		}
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoEXWord + q, context, tanjukugoEx, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, word));
			}
			WordPhraseData.map.put(TanjukugoEXWord + q, list);
		}
		for (var q : new String[]{"00", "08", "1", "2", "3"}) {
			var list = readToList(YumeWord + q, context, yumetan, "y" + q);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(mapQName.get(q), "Unit" + ((i - 1) / 100 + 1), list, i, word));
			WordPhraseData.map.put(YumeWord + q, list);
		}
		//語源データも読み込む
		int gogenNum = 0;
		QSentakuFragment.trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
		for (TreeMap.Entry<String, GogenYomu> map : QSentakuFragment.trGogenYomu.entrySet()) {
			allData.add(new WordPhraseData.WordInfo("読む語源学", map.getKey(), map.getValue().wordJpn, ++gogenNum, WordPhraseData.DataType.gogengaku));
		}
		
		//英語漬け.comから読み込み
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q", "-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten", "-Toeic-Chokuzen", "-Toeic-jukugo",}) {
			var list = readToList("Eigoduke.com/" + "WordDataEigoduke" + q, context, eigoduke, mapQName.get(q));
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo("英語漬け" + mapQName.get(q), list, i, WordPhraseData.DataType.eigoduke_com));
		}
		
		for (int num = 1; num <= 10; num++) {
			var q = "-toeic (" + num + ")";
			var list = readToList("Eigoduke.com/" + "WordDataEigoduke" + q, context, eigoduke, "英語漬け" + "TOEIC" + num);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo("英語漬け" + "TOEIC" + num, list, i, WordPhraseData.DataType.eigoduke_com));
		}
		
		//distinction
		for (int d = 1; d <= 4; d++) {
			var list = readToList(WordPhraseData.distinction + "d" + d + "word", context, DataBook.distinction, "Distinction" + d);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo("Distinction" + d, tangoNumToString("Distinction" + d, i), list, i, word));
		}
		//フレーズ
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			var list = readToList(PasstanPhrase + q, context, passTan, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo("パス単" + mapQName.get(q), tangoNumToString("パス単" + mapQName.get(q), i), list, i, phrase));
			}
			WordPhraseData.map.put(PasstanPhrase + q, list);
		}
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoPhrase + q, context, tanjukugo, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, phrase));
			}
			WordPhraseData.map.put(TanjukugoPhrase + q, list);
		}
		for (var q : new String[]{"d1phrase12", "d2phrase1"}) {
			var list = readToList(WordPhraseData.distinction + q, context, DataBook.distinction, q);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo("Distinction" + mapQName.get(q), tangoNumToString("Distinction" + mapQName.get(q), i), list, i, phrase));
		}
		//SVL12000辞書
		var list = readToList(Svl, context, svl12000, "svl");
		for (int i = 1; i < list.size(); i++)
			allData.add(new WordPhraseData.WordInfo("SVL", Integer.toString((i - 1) / 1000 + 1), list, i, word));
		//コピー
		SetHatsuonKigou(list);
		
		printCurrentState("end");
		activity.runOnUiThread(() -> makeToastForShort(context, "読み込み完了"));
		synchronized (isReadingData) {
			isReadingData = false;
		}
	}
	
	public void initialize() {
		try {
			//バージョン表記
			binding.textViewVersion.setText(getBuildDate(context));
			
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
			/*
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
			*/
			
			//TODO:タブ表示
			
			binding.buttonPrefExport.setOnClickListener(this::onExportPrefsButton);
			binding.buttonPrefImp.setOnClickListener(this::onImportPrefsButton);
			binding.buttonWriteTest.setOnClickListener(this::onWriteText);
			binding.buttonShowSettingNew.setOnClickListener(this::onShowSettingNew);
			
			QSentakuFragment.swOnlyFirst = binding.switchOnlyFirst;
			QSentakuFragment.swHyojiBeforeRead = binding.switchHyojiYakuBeforeRead;
			QSentakuFragment.switchSkipOboe = binding.switchSkipOboe;
			QSentakuFragment.switchSortHanten = binding.switchSortHanten;
			QSentakuFragment.switchQuizHatsuon = binding.switchQuizHatsuon;
			QSentakuFragment.switchQuizOX = binding.switchQuizOxKoukaon;
			QSentakuFragment.cbAutoStop = binding.checkBoxAutoStop;
			QSentakuFragment.checkBoxHatsuonKigou = binding.checkBoxHatsuonkigou;
			QSentakuFragment.radioButtonEtoJ = binding.radioButtonEtoJ;
			
			binding.editTextPipYoko.setText(String.valueOf(getIntData(context, "editText", "editTextPipYoko", 16)));
			pipYoko = Integer.parseInt(binding.editTextPipYoko.getText().toString());
			binding.editTextPipYoko.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
				try {
					if (editable.length() > 0) {
						pipYoko = Integer.parseInt(editable.toString());
						putIntData(context, "editText", "editTextPipYoko", pipYoko);
					}
				} catch (Exception e) {
					showException(context, e);
				}
			});
			
			binding.editTextPipTate.setText(String.valueOf(getIntData(context, "editText", "editTextPipTate", 9)));
			pipTate = Integer.parseInt(binding.editTextPipTate.getText().toString());
			binding.editTextPipTate.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
				{
					try {
						if (editable.length() > 0) {
							pipTate = Integer.parseInt(editable.toString());
							putIntData(context, "editText", "editTextPipTate", pipTate);
						}
					} catch (Exception e) {
						showException(context, e);
					}
				}
			});
			
			initializeSettingItem(QSentakuFragment.swOnlyFirst, true);
			initializeSettingItem(QSentakuFragment.swHyojiBeforeRead, true);
			initializeSettingItem(QSentakuFragment.switchSkipOboe, true);
			initializeSettingItem(QSentakuFragment.switchSortHanten, false);
			initializeSettingItem(QSentakuFragment.cbAutoStop, false);
			initializeSettingItem(QSentakuFragment.checkBoxHatsuonKigou, false);
			initializeSettingItem(binding.switchQuizHatsuon, true);
			initializeSettingItem(binding.switchQuizOxKoukaon, true);
			
			for (var v : new SwitchMaterial[]{QSentakuFragment.swOnlyFirst, QSentakuFragment.swHyojiBeforeRead, QSentakuFragment.switchSkipOboe, QSentakuFragment.switchSortHanten, QSentakuFragment.cbAutoStop, QSentakuFragment.checkBoxHatsuonKigou, binding.switchQuizHatsuon, binding.switchQuizOxKoukaon}) {
				v.setOnCheckedChangeListener(UiManager.Listener::onClickSettingItem);
			}
			
			binding.spinnerSpace.setAdapter(getAdapterForSpinner(context, R.array.spinner_kuuhaku));
			binding.spinnerSpace.setSelection(getIntData(context, "spinnerKuuhaku", "selected", 0));
			binding.spinnerSpace.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerKuuhakuOnItemSelectedListener);
			
			binding.spinnerHyojijun.setAdapter(getAdapterForSpinner(context, R.array.spinner_hyojijun));
			binding.spinnerHyojijun.setSelection(getIntData(context, "spinnerHyojijun", "selected", 0));
			binding.spinnerHyojijun.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerHyojijunOnItemSelectedListener);
			
			binding.spinnerBookQ.setAdapter(getAdapterForSpinner(context, R.array.spinner_book_q));
			binding.spinnerBookQ.setSelection(getIntData(context, "spinnerBookQ", "selected", 4));
			binding.spinnerBookQ.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerBookQOnItemSelectedListener);
			
			binding.buttonWord.setOnClickListener(this::onPlayQuizStart);
			binding.buttonPhrase.setOnClickListener(this::onPlayQuizStart);
			binding.buttonWP.setOnClickListener(this::onPlayQuizStart);
			binding.buttonQuiz.setOnClickListener(this::onPlayQuizStart);
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onShowSettingNew(View view) {
		try {
			String content = "";
			for (var fileName : getAllFileNames()) {
				content += fileName + "\n" + getAllPreferenceData(context, fileName) + "\n";
			}
			new AlertDialog.Builder(context)
				.setTitle(((Button) view).getText())
				.setMessage(content)
				.create()
				.show();
		} catch (Exception exception) {
			showException(context, exception);
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
			
			for (var sw : new SwitchMaterial[]{swOnlyFirst, swHyojiBeforeRead, switchSkipOboe, switchSortHanten, cbAutoStop, checkBoxHatsuonKigou}) {
				sw.setChecked(getSetting(context, "id" + sw.getId(), true));
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
	
	private void onPlayQuizStart(View view) {
		//スピナーから本と級を取得
		DataBook dataBook;
		DataQ dataQ;
		switch (binding.spinnerBookQ.getSelectedItemPosition()) {
			case 0: {
				dataBook = DataBook.yumetan;
				dataQ = DataQ.y1;
				break;
			}
			case 1: {
				dataBook = DataBook.yumetan;
				dataQ = DataQ.y2;
				break;
			}
			case 2: {
				dataBook = DataBook.yumetan;
				dataQ = DataQ.y3;
				break;
			}
			case 3: {
				dataBook = DataBook.passTan;
				dataQ = DataQ.q1;
				break;
			}
			default:
			case 4: {
				dataBook = DataBook.passTan;
				dataQ = DataQ.qp1;
				break;
			}
			case 5: {
				dataBook = DataBook.tanjukugo;
				dataQ = DataQ.q1;
				break;
			}
			case 6: {
				dataBook = DataBook.tanjukugo;
				dataQ = DataQ.qp1;
				break;
			}
		}
		if (view == binding.buttonQuiz) {
			TabActivity.setTabPageNum(2);
			QuizCreator.build(context, dataBook, dataQ);
		}
		else {
			TabActivity.setTabPageNum(1);
			q_num.mode mode = q_num.mode.word;
			if (view == binding.buttonPhrase) mode = q_num.mode.phrase;
			else if (view == binding.buttonWP) mode = q_num.mode.wordPlusPhrase;
			Intent intent=new Intent(context, PlayerService.class)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, mode)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, dataBook)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, dataQ);
			if(binding.editTextNumber.length()>0){
				intent.putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, Integer.parseInt(  binding.editTextNumber.getText().toString()));
			}
			binding.editTextNumber.setText("");
			context.startForegroundService(intent);
		}
	}
	
	private void spinnerKuuhakuOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerKuuhaku", "selected", i);
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
			showException(context, e);
		}
	}
	
	private void spinnerHyojijunOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerHyojijun", "selected", i);
			switch (i) {
				default:
				case 0: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.kirokunomi;
					break;
				}
				case 1: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.onlyHugoukaku;
					break;
				}
				case 2: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.seikai1;
					break;
				}
				case 3: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.huseikai2;
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void spinnerBookQOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerBookQ", "selected", i);
		} catch (Exception e) {
			showException(context, e);
		}
	}
}