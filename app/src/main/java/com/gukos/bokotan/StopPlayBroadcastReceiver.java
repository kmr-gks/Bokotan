package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.puts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopPlayBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		puts("StopPlayBroadcastReceiver");
		Intent i=new Intent(context,PlaySound.class);
		context.stopService(i);
	}
}
