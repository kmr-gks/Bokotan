package com.gukos.bokotan;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.function.Function;

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