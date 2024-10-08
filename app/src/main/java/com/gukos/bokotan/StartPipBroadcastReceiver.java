package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartPipBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			printCurrentState();
			//todo PIPを利用しているときに受信したときは、終了するようにしたい。
			if (!PipActivity.startPIP) {
				context.startActivity(new Intent(context, PipActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
			PipActivity.startPIP = !PipActivity.startPIP;
		} catch (Exception e) {
			showException(context, e);
		}
	}
}