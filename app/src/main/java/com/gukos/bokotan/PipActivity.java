package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Rational;
import android.view.View;
import android.widget.TextView;

import androidx.databinding.DataBindingUtil;

import com.gukos.bokotan.databinding.ActivityPipBinding;

public class PipActivity extends Activity {
	static boolean startPIP = false;
	public static final int CONTROL_TYPE_A = 12, CONTROL_TYPE_B = 103;
	public static final String EXTRA_CONTROL_TYPE = "100";
	public static int pipYoko = 16, pipTate = 9;
	private static ActivityPipBinding binding;
	
	public static final String
		PIP_ACTION_UI="pau",
		PIP_VIEW_TEXT="pvt",
		PIP_VIEW_NAME="pvn";
	
	public enum PipViewName {
		num,eng,jpn
	}
	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			PipViewName pipViewName =
				(PipViewName) bundle.getSerializable(PIP_VIEW_NAME);
			final TextView textViewToHandle;
			switch (pipViewName) {
				case num:{
					textViewToHandle=binding.textViewNo;
					break;
				}
				case eng:{
					textViewToHandle=binding.textViewPipEng;
					break;
				}
				case jpn:{
					textViewToHandle=binding.textViewPipJpn;
					break;
				}
				default: {
					throw new IllegalStateException("view name is invalid");
				}
			}
			
			textViewToHandle.setText(bundle.getString(PIP_VIEW_TEXT));
			
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			if (!startPIP) {
				finish();
				return;
			}
			super.onCreate(savedInstanceState);
			//setContentView(R.layout.activity_pip);
			binding= DataBindingUtil.setContentView(this,R.layout.activity_pip);
			this.registerReceiver(new DrawReceiver(drawHandler),new IntentFilter(PIP_ACTION_UI));
			//PipActivity.ChangeText(wordE[now], wordJ[now], now);
			
			//pip
			PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams.Builder();
			//Aspect ratio is too extreme (must be between 0.418410 and 2.390000).
			if (pipYoko > 0 && pipTate > 0 && 0.418410 < 1.0 * pipYoko / pipTate && 1.0 * pipYoko / pipTate < 2.39) {
				pictureInPictureParams.setAspectRatio(new Rational(pipYoko, pipTate));
			}
			else {
				pictureInPictureParams.setAspectRatio(new Rational(16, 9));
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				pictureInPictureParams.setTitle("ボコ単稼働中");
				pictureInPictureParams.setSubtitle("PIPで表示しています");
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				pictureInPictureParams.setSeamlessResizeEnabled(true);
			}
			//https://qiita.com/YANOKURO/items/64568d2e62593ac2c389
			//RemoteAction
		/*
		final ArrayList<RemoteAction>actions=new ArrayList<>();
		PendingIntent intent=PendingIntent.getBroadcast(this,0,new Intent(this,PipControlBroadcastReceiver.class).putExtra(EXTRA_CONTROL_TYPE,CONTROL_TYPE_A),FLAG_IMMUTABLE);
		PendingIntent intent2=PendingIntent.getBroadcast(this,0,new Intent(this,PipControlBroadcastReceiver.class).putExtra(EXTRA_CONTROL_TYPE,CONTROL_TYPE_B),FLAG_IMMUTABLE);
		final Icon icon= Icon.createWithResource(PipActivity.this, android.R.drawable.ic_media_pause);
		final Icon icon2= Icon.createWithResource(PipActivity.this, android.R.drawable.ic_media_play);
		String strTitle = "testtitle";
		String strContent = "testcontent";
		actions.add(new RemoteAction(icon, strTitle, strContent,intent));
		actions.add(new RemoteAction(icon2, strTitle, strContent,intent2));
		pictureInPictureParams.setActions(actions);
		*/
			enterPictureInPictureMode(pictureInPictureParams.build());
			
			
			//デフォルト
			//enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(new Rational(16,9)).build());
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	static void ChangeText(String strE, String strJpn, int num) {
		if (startPIP)
			try {
				binding.textViewPipEng.setText(strE);
				binding.textViewPipJpn.setText(strJpn);
				binding.textViewNo.setText(String.format("%d", num));
			} catch (Exception e) {
				showException(e);
			}
	}
	
	public void exitPIP(View v) {
		try {
			finish();
			startPIP = false;
		} catch (Exception e) {
			showException(this, e);
		}
	}
}