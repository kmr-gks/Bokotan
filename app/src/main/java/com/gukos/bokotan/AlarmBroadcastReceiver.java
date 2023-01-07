package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			// toast で受け取りを確認
			Toast.makeText(context, "Received ", Toast.LENGTH_LONG).show();
			context.startActivity(new Intent(context, MainActivity.class));
		} catch (Exception e) {
			showException(context, e);
		}
	}
}