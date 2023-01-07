package com.gukos.bokotan;

import static android.content.Context.MODE_PRIVATE;
import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MainActivity.playing;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MyLibrary.puts;
import static com.gukos.bokotan.PipActivity.ACTION_HOGE;
import static com.gukos.bokotan.PipActivity.CONTROL_TYPE_A;
import static com.gukos.bokotan.PipActivity.CONTROL_TYPE_B;
import static com.gukos.bokotan.PipActivity.EXTRA_CONTROL_TYPE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class PipControlBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		puts("PipControlBroadcastReceiver");
		if (intent == null || !ACTION_HOGE.equals(intent.getAction())) {
			puts("onReceive");
			//return;
		}

		new MainActivity().onStartStopButtonClick(null);

		final int controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0);
		switch (controlType) {
			case CONTROL_TYPE_A: {
				// action hoge A
				//再生停止
				//MainActivity.javaのonStartStopButtonClickより
				puts("controltypeA");
				context.stopService(intent);
				playing=false;
				context.getSharedPreferences("MainActivity"+"now",MODE_PRIVATE).edit().putInt(strQ + "now", now).apply();
				break;
			}
			case CONTROL_TYPE_B:{
				puts("controlB");
				if (intent==null){
					intent=new Intent(context,PlaySound.class);
					intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
					context.startForegroundService(intent);
					playing=true;
				}
				break;
			}
		}
	}
}

