package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.view.View;
import android.widget.AdapterView;

public interface AdapterViewItemSelected extends AdapterView.OnItemSelectedListener {
	@Override
	abstract void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l);
	
	@Override
	default void onNothingSelected(AdapterView<?> adapterView) {
	}
}