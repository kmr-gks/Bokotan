package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.PipActivity.CONTROL_TYPE_A;
import static com.gukos.bokotan.PipActivity.CONTROL_TYPE_B;
import static com.gukos.bokotan.PipActivity.EXTRA_CONTROL_TYPE;
import static com.gukos.bokotan.PlayerFragment.playing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PipControlBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			
			new PlayerFragment().onStartStopButtonClick(null);
			
			final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
			switch (controlType) {
				case CONTROL_TYPE_A: {
					// action hoge A
					//再生停止
					//MainActivity.javaのonStartStopButtonClickより
					context.stopService(intent);
					playing = false;
					//MyLibrary.PreferenceManager.putIntData(context, "MainActivity" + "now",strQ + "now", now);
					break;
				}
				case CONTROL_TYPE_B: {
					if (intent == null) {
						//intent = new Intent(context, PlaySound.class);
						intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
						context.startForegroundService(intent);
						playing = true;
					}
					break;
				}
			}
		} catch (Exception e) {
			showException(e);
		}
	}
}