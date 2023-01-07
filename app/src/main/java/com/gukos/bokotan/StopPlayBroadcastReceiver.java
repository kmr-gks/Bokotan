package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.puts;
import static com.gukos.bokotan.MyLibrary.showException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopPlayBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Intent i = new Intent(context, PlaySound.class);
			context.stopService(i);
		} catch (Exception e) {
			showException(context, e);
		}
	}
}