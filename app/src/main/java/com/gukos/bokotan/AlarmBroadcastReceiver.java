package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.strQ;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// toast で受け取りを確認
		Toast.makeText(context, "Received ", Toast.LENGTH_LONG).show();
		context.startActivity(new Intent(context,MainActivity.class));
	}
}