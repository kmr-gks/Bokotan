package com.gukos.bokotan;

import android.widget.SeekBar;

public interface OnSeekBarProgressChange extends SeekBar.OnSeekBarChangeListener{
	@Override//ツマミがドラッグされると呼ばれる
	abstract void onProgressChanged(SeekBar seekBar, int i, boolean b);
	
	@Override//ツマミがタッチされた時に呼ばれる
	default void onStartTrackingTouch(SeekBar seekBar) {
	}
	
	@Override//ツマミがリリースされた時に呼ばれる
	default void onStopTrackingTouch(SeekBar seekBar) {
	}
}