package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.puts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartPipBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		puts("StartPipBroadcastReceiver");
		Intent i=new Intent(context,PipActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PipActivity.startPIP=true;
		context.startActivity(i);
	}
}
