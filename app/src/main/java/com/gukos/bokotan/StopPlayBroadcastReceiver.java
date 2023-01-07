package com.gukos.bokotan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopPlayBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent i=new Intent(context,PlaySound.class);
		context.stopService(i);
	}
}
