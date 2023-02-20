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
import android.widget.RadioButton;
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
			swOnlyFirst = binding.switchOnlyFirst;
			swHyojiBeforeRead = binding.switchHyojiYakuBeforeRead;
			switchSkipOboe = binding.switchSkipOboe;
			swMaruBatu = binding.switchSkipMaruBatu;
			switchSortHanten = binding.switchSortHanten;
			cbAutoStop = binding.checkBoxAutoStop;
			checkBoxHatsuonKigou = binding.checkBoxHatsuonkigou;
			radioButtonEtoJ = binding.radioButtonEtoJ;
			
			int skipoptionId = getIntData(context, dnQSentakuActivity, "RadioButton", R.id.radioButtonOnlyKioku);
			for (var radioButton : new RadioButton[]{binding.radioButtonOnlyKioku, binding.radioButtonOnlyHugoukaku, binding.radioButton1seikai, binding.radioButton2huseikai}) {
				if (skipoptionId == radioButton.getId()) {
					radioButton.setChecked(true);
					break;
				}
			}
			
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
			
			initializeSettingItem(swOnlyFirst, true);
			initializeSettingItem(swHyojiBeforeRead, true);
			initializeSettingItem(switchSkipOboe, true);
			initializeSettingItem(swMaruBatu, true);
			initializeSettingItem(switchSortHanten, false);
			
			for (var v : new Switch[]{swOnlyFirst, swHyojiBeforeRead, switchSkipOboe, swMaruBatu, switchSortHanten}) {
				v.setOnClickListener(MyLibrary.PreferenceManager::onClickSettingItem);
			}
			
			initializeSettingItem(binding.checkBoxDefaultAdapter, true);
			initializeSettingItem(cbAutoStop, false);
			initializeSettingItem(checkBoxHatsuonKigou, false);
			for (var v : new CheckBox[]{binding.checkBoxDefaultAdapter, cbAutoStop, checkBoxHatsuonKigou}) {
				v.setOnClickListener(MyLibrary.PreferenceManager::onClickSettingItem);
			}
			
			binding.spinnerSpace.setSelection(getIntData(context, "spinnerKuuhaku", "selected", 0));
			binding.spinnerSpace.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::SpinnerKuuhakuOnItemSelectedListener);
			binding.spinnerSpace.setAdapter(getAdapterForSpinner(context, R.array.spinner_kuuhaku));
			
			for (var radioButton : new RadioButton[]{binding.radioButtonOnlyKioku,
				binding.radioButtonOnlyHugoukaku, binding.radioButton1seikai, binding.radioButton2huseikai}) {
				radioButton.setOnClickListener(this::onRadioChecked);
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