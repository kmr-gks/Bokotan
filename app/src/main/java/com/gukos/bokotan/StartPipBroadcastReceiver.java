package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartPipBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Intent i = new Intent(context, PipActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			PipActivity.startPIP = true;
			context.startActivity(i);
		} catch (Exception e) {
			showException(context, e);
		}
	}
}