package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_ACTION;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_STOP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_TYPE;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
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

import java.util.ArrayList;

public class PipActivity extends Activity {
	static boolean startPIP = false;
	public static int pipYoko = 16;
	public static int pipTate = 9;
	private static ActivityPipBinding binding;
	
	public static final String
		PIP_ACTION_UI = "pau",
		PIP_VIEW_TEXT = "pvt",
		PIP_VIEW_NAME = "pvn";
	
	public enum PipViewName {
		num, eng, jpn
	}
	
	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			PipViewName pipViewName =
				(PipViewName) bundle.getSerializable(PIP_VIEW_NAME);
			final TextView textViewToHandle;
			switch (pipViewName) {
				case num: {
					textViewToHandle = binding.textViewNo;
					break;
				}
				case eng: {
					textViewToHandle = binding.textViewPipEng;
					break;
				}
				case jpn: {
					textViewToHandle = binding.textViewPipJpn;
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
			binding = DataBindingUtil.setContentView(this, R.layout.activity_pip);
			this.registerReceiver(new DrawReceiver(drawHandler), new IntentFilter(PIP_ACTION_UI));
			
			//pip
			PictureInPictureParams.Builder pictureInPictureParams = new PictureInPictureParams.Builder();
			//Aspect ratio is too extreme (must be between 0.418410 and 2.390000).
			if (pipYoko > 0 && pipTate > 0 && 0.418410 < 1.0 * pipYoko / pipTate && 1.0 * pipYoko / pipTate < 2.39) {
				pictureInPictureParams.setAspectRatio(new Rational(pipYoko, pipTate));
			}
			else {
				pictureInPictureParams.setAspectRatio(new Rational(16, 9));
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				pictureInPictureParams.setSeamlessResizeEnabled(true);
			}
			final ArrayList<RemoteAction> actions = new ArrayList<>();
			actions.add(
				new RemoteAction(Icon.createWithResource(PipActivity.this, android.R.drawable.ic_media_pause), "ends", "content:", PendingIntent.getBroadcast(this, 0, new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP), PendingIntent.FLAG_IMMUTABLE))
			);
			pictureInPictureParams.setActions(actions);
			enterPictureInPictureMode(pictureInPictureParams.build());
			
		} catch (Exception e) {
			showException(this, e);
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