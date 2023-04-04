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
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
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
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_SHOW_APPEARED;
import static com.gukos.bokotan.UiManager.getAdapterForSpinner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gukos.bokotan.WordPhraseData.DataBook;
import com.gukos.bokotan.WordPhraseData.DataQ;
import com.gukos.bokotan.databinding.FragmentQSentakuBinding;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class QSentakuFragment extends UiManager.FragmentBingding<FragmentQSentakuBinding> {
	
	//他のクラスからアクセス
	public static SwitchMaterial switchQuizHatsuon, switchQuizOX, switchShouHatsuon;
	public static TreeMap<String, GogenYomu> trGogenYomu;
	
	public static DataBook dataBook;
	public static DataQ dataQ;
	
	public QSentakuFragment() {
		super(FragmentQSentakuBinding::inflate);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			
			//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll()
			// .penaltyLog().build());
			
			new Thread(() -> activity.runOnUiThread(this::initialize)).start();
		} catch (Exception e) {
			showException(getContext(), e);
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
			
			switchShouHatsuon = binding.checkBoxHatsuonkigou;
			QSentakuFragment.switchQuizHatsuon = binding.switchQuizHatsuon;
			QSentakuFragment.switchQuizOX = binding.switchQuizOxKoukaon;
			
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
			
			initializeSettingItem(binding.switchOnlyFirst, true);
			initializeSettingItem(binding.switchHyojiYakuBeforeRead, true);
			initializeSettingItem(binding.switchSkipOboe, true);
			initializeSettingItem(binding.switchSortHanten, false);
			initializeSettingItem(binding.checkBoxAutoStop, false);
			initializeSettingItem(binding.checkBoxHatsuonkigou, false);
			initializeSettingItem(binding.switchQuizHatsuon, true);
			initializeSettingItem(binding.switchQuizOxKoukaon, true);
			
			for (var v : new SwitchMaterial[]{binding.switchOnlyFirst, binding.switchHyojiYakuBeforeRead, binding.switchSkipOboe, binding.switchSortHanten, binding.checkBoxAutoStop, binding.checkBoxHatsuonkigou, binding.switchQuizHatsuon, binding.switchQuizOxKoukaon}) {
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
				//content += fileName + "\n" + getAllPreferenceData(context, fileName) + "\n";
				content += "ファイル名:" + fileName + "\n";
				puts("filaname=" + fileName);
				for (var key : context.getSharedPreferences(fileName, Context.MODE_PRIVATE).getAll().keySet()) {
					content += "\t" + key + "\n";
				}
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
			
			for (var sw : new SwitchMaterial[]{binding.switchOnlyFirst,
				binding.switchHyojiYakuBeforeRead, binding.switchSkipOboe, binding.switchSortHanten
				, binding.checkBoxAutoStop, binding.checkBoxHatsuonkigou}) {
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
			WordPhraseData.Mode mode = WordPhraseData.Mode.word;
			if (view == binding.buttonPhrase) mode = WordPhraseData.Mode.phrase;
			else if (view == binding.buttonWP) mode = WordPhraseData.Mode.wordPlusPhrase;
			Intent intent = new Intent(context, PlayerService.class)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, mode)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, dataBook)
				.putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, dataQ)
				.putExtra(PLAYERSERVICE_EXTRA_SHOW_APPEARED, binding.switchOnlyFirst.isChecked());
			if (binding.editTextNumber.length() > 0) {
				intent.putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, Integer.parseInt(binding.editTextNumber.getText().toString()));
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
					WordPhraseData.skipjoken = WordPhraseData.skipjouken.kirokunomi;
					break;
				}
				case 1: {
					WordPhraseData.skipjoken = WordPhraseData.skipjouken.onlyHugoukaku;
					break;
				}
				case 2: {
					WordPhraseData.skipjoken = WordPhraseData.skipjouken.seikai1;
					break;
				}
				case 3: {
					WordPhraseData.skipjoken = WordPhraseData.skipjouken.huseikai2;
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