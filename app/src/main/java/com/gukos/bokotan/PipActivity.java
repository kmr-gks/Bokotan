package com.gukos.bokotan;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;
import static com.gukos.bokotan.MyLibrary.ExceptionHandler.showException;
import static com.gukos.bokotan.MyLibrary.puts;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.PictureInPictureUiState;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class PipActivity extends Activity {
	static TextView tvPipE,tvPipJ,tvNum;
	static boolean startPIP=false;

	public final int REQUEST_CODE_ACTION_HOGE_A=0;
	public static final int CONTROL_TYPE_A=12,CONTROL_TYPE_B=103;
	public static final String ACTION_HOGE="11";
	public static final String EXTRA_CONTROL_TYPE="100";
	private final String strTitle="testtitle",strContent="testcontent";
	private BroadcastReceiver receiver;

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
		PictureInPictureParams.Builder pictureInPictureParams= new PictureInPictureParams.Builder();
		pictureInPictureParams.setAspectRatio(new Rational(16,9));
		//https://qiita.com/YANOKURO/items/64568d2e62593ac2c389
		//RemoteAction
		final ArrayList<RemoteAction>actions=new ArrayList<>();
		PendingIntent intent=PendingIntent.getBroadcast(this,0,new Intent(this,PipControlBroadcastReceiver.class).putExtra(EXTRA_CONTROL_TYPE,CONTROL_TYPE_A),FLAG_IMMUTABLE);
		PendingIntent intent2=PendingIntent.getBroadcast(this,0,new Intent(this,PipControlBroadcastReceiver.class).putExtra(EXTRA_CONTROL_TYPE,CONTROL_TYPE_B),FLAG_IMMUTABLE);
		final Icon icon= Icon.createWithResource(PipActivity.this, android.R.drawable.ic_media_pause);
		final Icon icon2= Icon.createWithResource(PipActivity.this, android.R.drawable.ic_media_play);
		actions.add(new RemoteAction(icon,strTitle,strContent,intent));
		actions.add(new RemoteAction(icon2,strTitle,strContent,intent2));
		pictureInPictureParams.setActions(actions);
		enterPictureInPictureMode(pictureInPictureParams.build());


		//デフォルト
		//enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
	}

	static void ChangeText(String strE,String strJpn,int num) {
		try {
			tvPipE.setText(strE);
			tvPipJ.setText(strJpn);
			tvNum.setText(String.format("%d",num));
		}catch (Exception e){
			showException(e);
		}
	}

	public void exitPIP(View v){
		finish();
	}
}
