package com.gukos.bokotan;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

public class UiManager {
	
	public static ArrayAdapter<String> getAdapterForSpinner(Context context, int stringResourceId) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.custom_spinner, context.getResources().getStringArray(stringResourceId));
		adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
		return adapter;
	}
	
	public static final class UiInterface {
		public interface AdapterViewItemSelected extends AdapterView.OnItemSelectedListener {
			@Override
			abstract void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l);
			
			@Override
			default void onNothingSelected(AdapterView<?> adapterView) {
			}
		}
		
		public interface OnSeekBarProgressChange extends SeekBar.OnSeekBarChangeListener {
			@Override//ツマミがドラッグされると呼ばれる
			abstract void onProgressChanged(SeekBar seekBar, int i, boolean b);
			
			@Override//ツマミがタッチされた時に呼ばれる
			default void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override//ツマミがリリースされた時に呼ばれる
			default void onStopTrackingTouch(SeekBar seekBar) {
			}
		}
		
		public interface TextWatcherAfterOnly extends TextWatcher {
			
			@Override
			default void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			default void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
			
			//これをオーバーロードする
			@Override
			abstract void afterTextChanged(Editable editable);
		}
	}
}