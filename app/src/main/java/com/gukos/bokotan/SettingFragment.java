package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnQSentakuActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.initializeSettingItem;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.UiManager.getAdapterForSpinner;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentSettingBinding;

public class SettingFragment extends UiManager.FragmentBingding<FragmentSettingBinding> {
	//他のクラスからアクセス
	public static CheckBox cbAutoStop, checkBoxHatsuonKigou;
	public static Switch switchSkipOboe, swHyojiBeforeRead, switchSortHanten, swMaruBatu;
	public static RadioButton radioButtonEtoJ;
	//ビュー
	static Switch swOnlyFirst;
	
	public SettingFragment() {
		super(FragmentSettingBinding::inflate);
	}
	
	//ActivityのonCreateに相当
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			new Thread(() -> activity.runOnUiThread(this::initialize)).start();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public void initialize() {
		try {
			//ここでnull例外発生
			RadioButton radioButtonSkipOption = findViewById(getIntData(context, dnQSentakuActivity, "RadioButton", R.id.radioButtonOnlyKioku));
			if (radioButtonSkipOption == null)
				radioButtonSkipOption = findViewById(R.id.radioButtonOnlyKioku);
			radioButtonSkipOption.setChecked(true);
			
			radioButtonEtoJ = findViewById(R.id.radioButtonEtoJ);
			
			EditText editTextPipYoko = findViewById(R.id.editTextPipYoko);
			editTextPipYoko.setText(String.valueOf(getIntData(context, "editText", "editTextPipYoko", 16)));
			pipYoko = Integer.parseInt(editTextPipYoko.getText().toString());
			editTextPipYoko.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
				try {
					if (editable.length() > 0) {
						pipYoko = Integer.parseInt(editable.toString());
						putIntData(context, "editText", "editTextPipYoko", pipYoko);
					}
				} catch (Exception e) {
					showException(context, e);
				}
			});
			
			EditText editTextPipTate = findViewById(R.id.editTextPipTate);
			editTextPipTate.setText(String.valueOf(getIntData(context, "editText", "editTextPipTate", 9)));
			pipTate = Integer.parseInt(editTextPipTate.getText().toString());
			editTextPipTate.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
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
			
			swOnlyFirst = findViewById(R.id.switchOnlyFirst);
			initializeSettingItem(swOnlyFirst, true);
			
			swHyojiBeforeRead = findViewById(R.id.switchHyojiYakuBeforeRead);
			initializeSettingItem(swHyojiBeforeRead, true);
			
			switchSkipOboe = findViewById(R.id.switchSkipOboe);
			initializeSettingItem(switchSkipOboe, true);
			
			swMaruBatu = findViewById(R.id.switchSkipMaruBatu);
			initializeSettingItem(swMaruBatu, true);
			
			switchSortHanten = findViewById(R.id.switchSortHanten);
			initializeSettingItem(switchSortHanten, false);
			
			for (var v : new Switch[]{swOnlyFirst, swHyojiBeforeRead, switchSkipOboe, swMaruBatu, switchSortHanten}) {
				v.setOnClickListener(MyLibrary.PreferenceManager::onClickSettingItem);
			}
			
			CheckBox cbDefaultAdapter = findViewById(R.id.checkBoxDefaultAdapter);
			cbAutoStop = findViewById(R.id.checkBoxAutoStop);
			checkBoxHatsuonKigou = findViewById(R.id.checkBoxHatsuonkigou);
			initializeSettingItem(cbDefaultAdapter, true);
			initializeSettingItem(cbAutoStop, false);
			initializeSettingItem(checkBoxHatsuonKigou, false);
			for (var v : new CheckBox[]{cbDefaultAdapter, cbAutoStop, checkBoxHatsuonKigou}) {
				v.setOnClickListener(MyLibrary.PreferenceManager::onClickSettingItem);
			}
			
			
			Spinner spinnerKuuhaku = findViewById(R.id.spinnerSpace);
			spinnerKuuhaku.setSelection(getIntData(context, "spinnerKuuhaku", "selected", 0));
			spinnerKuuhaku.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::SpinnerKuuhakuOnItemSelectedListener);
			spinnerKuuhaku.setAdapter(getAdapterForSpinner(context, R.array.spinner_kuuhaku));
			
			for (int id : new int[]{R.id.radioButtonOnlyKioku, R.id.radioButtonOnlyHugoukaku,
				R.id.radioButton1seikai, R.id.radioButton2huseikai}) {
				findViewById(id).setOnClickListener(this::onRadioChecked);
			}
		} catch (Exception exception) {
			showException(context, exception);
		}
	}
	
	public void SpinnerKuuhakuOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
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
	
	private void onRadioChecked(View v) {
		try {
			putIntData(context, dnQSentakuActivity, "RadioButton", v.getId());
			switch (v.getId()) {
				case R.id.radioButtonOnlyKioku: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.kirokunomi;
					break;
				}
				case R.id.radioButton1seikai: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.seikai1;
					break;
				}
				case R.id.radioButton2huseikai: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.huseikai2;
					break;
				}
				case R.id.radioButtonOnlyHugoukaku: {
					WordPhraseData.skipjoken = WordPhraseData.q_num.skipjouken.onlyHugoukaku;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
}