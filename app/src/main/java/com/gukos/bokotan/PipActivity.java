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
import android.graphics.Rect;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Rational;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.databinding.DataBindingUtil;

import com.gukos.bokotan.databinding.ActivityPipBinding;

import java.util.ArrayList;
import java.util.Objects;

public class PipActivity extends Activity {
	static boolean startPIP = false;
	public static int pipYoko = 16;
	public static int pipTate = 9;
	private ActivityPipBinding binding;
	private DrawReceiver receiver;
	private boolean initialPipScheduled;

	/**
	 * //最初に渡す値(putExtraやgetStringExtraで使用)
	 * PIP_TV_FIRST_ENG //pip画面に最初に表示する英語
	 * PIP_TV_FIRST_JPN //pip画面に最初に表示する日本語
	 * //PIPのUIを別スレッドから変更するハンドラーで使用:
	 * PIP_ACTION_UI
	 * PIP_VIEW_TEXT
	 * PIP_VIEW_SINGLE_LINE
	 * PIP_VIEW_NAME
	 */
	public static final String
			PIP_TV_FIRST_ENG = "ptve",
			PIP_TV_FIRST_JPN = "ptvfj",
			PIP_ACTION_UI = "pau",
			PIP_VIEW_TEXT = "pvt",
			PIP_VIEW_SINGLE_LINE = "pvsl",
			PIP_VIEW_NAME = "pvn";

	public enum PipViewName {
		num, eng, jpn
	}

	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (binding == null) return;
			Bundle bundle = msg.getData();
			PipViewName pipViewName = BundleCompat.getSerializable(bundle, PIP_VIEW_NAME, PipViewName.class);
			final TextView textViewToHandle;
			switch (Objects.requireNonNull(pipViewName)) {
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

			if (bundle.containsKey(PIP_VIEW_TEXT)) {
				textViewToHandle.setText(bundle.getString(PIP_VIEW_TEXT));
			}
			if (bundle.containsKey(PIP_VIEW_SINGLE_LINE)) {
				//setSingleLineを使用すると後半が表示されない場合があるため使わない
				if (bundle.getBoolean(PIP_VIEW_SINGLE_LINE)) {
					textViewToHandle.setLines(1);
				} else {
					textViewToHandle.setMaxLines(5);
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			startPIP = true;
			binding = DataBindingUtil.setContentView(this, R.layout.activity_pip);
			receiver = new DrawReceiver(drawHandler);
			ContextCompat.registerReceiver(this, receiver, new IntentFilter(PIP_ACTION_UI), ContextCompat.RECEIVER_NOT_EXPORTED);

			sendBroadcast(new Intent(PLAYERSERVICE_ACTION).setPackage(getPackageName())
					.putExtra(PLAYERSERVICE_MESSAGE_TYPE, PlayerService.PLAYERSERVICE_MESSAGE_REFRESH));

			//最初に表示する文字列を取得
			binding.textViewPipEng.setText(getIntent().getStringExtra(PIP_TV_FIRST_ENG));
			binding.textViewPipJpn.setText(getIntent().getStringExtra(PIP_TV_FIRST_JPN));

			// Wait for a real content rectangle, then retain the explicit PIP-button behaviour.
			binding.getRoot().addOnLayoutChangeListener((view, left, top, right, bottom,
					oldLeft, oldTop, oldRight, oldBottom) -> updatePipParams());

		} catch (Exception e) {
			showException(this, e);
		}
	}

	private void updatePipParams() {
		if (binding == null || isFinishing() || isDestroyed()) return;
		Rect bounds = new Rect();
		if (!binding.getRoot().getGlobalVisibleRect(bounds) || bounds.isEmpty()) return;
		try {
			PictureInPictureParams.Builder builder = new PictureInPictureParams.Builder()
					.setSourceRectHint(bounds);
			// Aspect ratio must be between 0.418410 and 2.390000.
			if (pipYoko > 0 && pipTate > 0 && 0.418410 < 1.0 * pipYoko / pipTate && 1.0 * pipYoko / pipTate < 2.39) {
				builder.setAspectRatio(new Rational(pipYoko, pipTate));
			} else {
				builder.setAspectRatio(new Rational(16, 9));
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
				builder.setAutoEnterEnabled(true);
				// This window contains text, so use the non-video resize transition.
				builder.setSeamlessResizeEnabled(false);
			}
			ArrayList<RemoteAction> actions = new ArrayList<>();
			actions.add(new RemoteAction(Icon.createWithResource(this, android.R.drawable.ic_media_pause),
					getString(R.string.stop_playback), getString(R.string.stop_playback), PendingIntent.getBroadcast(this, 0,
					new Intent(PLAYERSERVICE_ACTION).setPackage(getPackageName())
							.putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP), PendingIntent.FLAG_IMMUTABLE)));
			PictureInPictureParams params = builder.setActions(actions).build();
			setPictureInPictureParams(params);
			if (!initialPipScheduled) {
				initialPipScheduled = true;
				binding.getRoot().post(() -> {
					if (binding != null && !isFinishing() && !isDestroyed() && !isInPictureInPictureMode()) {
						try {
							enterPictureInPictureMode(params);
						} catch (Exception error) {
							showException(this, error);
						}
					}
				});
			}
		} catch (Exception error) {
			showException(this, error);
		}
	}

	@Override protected void onDestroy() {
		if (receiver != null) { unregisterReceiver(receiver); receiver = null; }
		drawHandler.removeCallbacksAndMessages(null);
		binding = null; startPIP = false;
		super.onDestroy();
	}

}
