package com.gukos.bokotan;


import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.initializeSettingItem;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.UiManager.getAdapterForSpinner;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gukos.bokotan.databinding.FragmentSettingBinding;

public class SettingFragment extends UiManager.FragmentBingding<FragmentSettingBinding> {
	//他のクラスからアクセス
	public static SwitchMaterial switchSkipOboe, swHyojiBeforeRead, switchSortHanten, cbAutoStop, checkBoxHatsuonKigou,swOnlyFirst;
	public static RadioButton radioButtonEtoJ;
	
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
			switchSortHanten = binding.switchSortHanten;
			cbAutoStop = binding.checkBoxAutoStop;
			checkBoxHatsuonKigou = binding.checkBoxHatsuonkigou;
			radioButtonEtoJ = binding.radioButtonEtoJ;
			
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
			initializeSettingItem(switchSortHanten, false);
			initializeSettingItem(cbAutoStop, false);
			initializeSettingItem(checkBoxHatsuonKigou, false);
			
			for (var v : new SwitchMaterial[]{swOnlyFirst, swHyojiBeforeRead, switchSkipOboe,
				switchSortHanten, cbAutoStop, checkBoxHatsuonKigou}) {
				v.setOnCheckedChangeListener(UiManager.Listener::onClickSettingItem);
			}
			
			binding.spinnerSpace.setAdapter(getAdapterForSpinner(context, R.array.spinner_kuuhaku));
			binding.spinnerSpace.setSelection(getIntData(context, "spinnerKuuhaku", "selected", 0));
			binding.spinnerSpace.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerKuuhakuOnItemSelectedListener);
			
			binding.spinnerHyojijun.setAdapter(getAdapterForSpinner(context, R.array.spinner_hyojijun));
			binding.spinnerHyojijun.setSelection(getIntData(context, "spinnerHyojijun", "selected", 0));
			binding.spinnerHyojijun.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerHyojijunOnItemSelectedListener);
		} catch (Exception exception) {
			showException(context, exception);
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
}