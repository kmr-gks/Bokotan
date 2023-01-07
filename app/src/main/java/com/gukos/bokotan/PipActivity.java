package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.widget.TextView;

public class PipActivity extends Activity {

	final String tag=MainActivity.tag+"PIP";
	static TextView tvPipE,tvPipJ,tvNum;
	static boolean startPIP=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (!startPIP){
			finish();
			return;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pip);
		setTheme(R.style.bokotan_NoTitleBar);

		tvPipE=findViewById(R.id.tvPipE);
		tvPipJ=findViewById(R.id.tvPipJ);
		tvNum=findViewById(R.id.tvNum);

		PipActivity.ChangeText(wordE[now], wordJ[now], now);

		//pip
		enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
	}

	static void ChangeText(String e,String j,int num) {
		if (tvPipE!=null) {
			tvPipE.setText(e);
			tvPipJ.setText(j);
			tvNum.setText(String.format("%d",num));
		}
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	public void onClicked(View v){
		finish();
	}
}
