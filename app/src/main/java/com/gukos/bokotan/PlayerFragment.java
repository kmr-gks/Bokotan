package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getMethodName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_ACTION;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_NOW;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_STOP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_TYPE;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gukos.bokotan.databinding.FragmentPlayerBinding;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class PlayerFragment extends UiManager.FragmentBinding<FragmentPlayerBinding> {
	public static boolean isInitialized = false;
	private DrawReceiver receiver;
	public static final String
			PLAYER_ACTION_UI_CHANGE = "player_action_ui_change";
	public static final String PLAYER_VIEW_NAME = "player_view_name";
	public static final String PLAYER_VIEW_TEXT = "player_view_text";
	public static final String PLAYER_VIEW_SINGLE_LINE = "pvsl";

	public enum PlayerViewName {
		genzai, tvcount, hatsuon, subJ, subE, eng, jpn, path
	}

	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (binding == null) return;
			Bundle bundle = msg.getData();
			if (bundle.containsKey(PlayerService.PLAYER_STATE_CHANGED)) {
				renderState();
				return;
			}
			PlayerViewName viewName = (PlayerViewName) bundle.getSerializable(PLAYER_VIEW_NAME);
			final TextView textViewToHandle;
			switch (Objects.requireNonNull(viewName)) {
				case genzai: {
					textViewToHandle = binding.textViewGenzai;
					break;
				}
				case tvcount: {
					textViewToHandle = binding.textViewCount;
					break;
				}
				case hatsuon: {
					textViewToHandle = binding.textViewHatsuonKigou;
					break;
				}
				case subJ: {
					textViewToHandle = binding.textViewSubtitleJpn;
					break;
				}
				case subE: {
					textViewToHandle = binding.textViewSubtitleEng;
					break;
				}
				case eng: {
					textViewToHandle = binding.textViewEng;
					break;
				}
				case jpn: {
					textViewToHandle = binding.textViewJpn;
					break;
				}
				case path: {
					textViewToHandle = binding.textViewPath;
					break;
				}
				default: {
					throw new IllegalStateException("view name is invalid");
				}
			}
			if (bundle.containsKey(PLAYER_VIEW_TEXT)) {
				textViewToHandle.setText(bundle.getString(PLAYER_VIEW_TEXT));
			}
			if (bundle.containsKey(PLAYER_VIEW_SINGLE_LINE)) {
				//setSingleLineを使用すると後半が表示されない場合があるため使わない
				if (bundle.getBoolean(PLAYER_VIEW_SINGLE_LINE)) {
					textViewToHandle.setLines(1);
				} else {
					textViewToHandle.setMaxLines(5);
				}
			}
		}
	};

	public PlayerFragment() {
		super(FragmentPlayerBinding::inflate);
	}

	//ActivityのonCreateに相当
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			try {
				//UI設定
				receiver = new DrawReceiver(drawHandler);
				ContextCompat.registerReceiver(context, receiver, new IntentFilter(PLAYER_ACTION_UI_CHANGE), ContextCompat.RECEIVER_NOT_EXPORTED);

				binding.buttonToBegin.setOnClickListener(this::onResetButtonClick);
				binding.buttonNowChange.setOnClickListener(this::onChangeNumber);
				binding.buttonPip.setOnClickListener(this::onPIPButtonClicked);
				binding.seekBarEng.setOnSeekBarChangeListener((UiManager.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				binding.seekBarEng.setProgress(getIntData(context, "SeekBar", "english", 5));
				onSpeedSeekBar(binding.seekBarEng, binding.seekBarEng.getProgress(), true);
				binding.seekBarJpn.setOnSeekBarChangeListener((UiManager.UiInterface.OnSeekBarProgressChange) this::onSpeedSeekBar);
				binding.seekBarJpn.setProgress(getIntData(context, "SeekBar", "japanese", 10));
				onSpeedSeekBar(binding.seekBarJpn, binding.seekBarJpn.getProgress(), true);
				binding.buttonStopService.setOnClickListener(this::onPlayerServiceStop);
				binding.buttonStartSamplePlayback.setOnClickListener(v -> context.startForegroundService(
						new Intent(context, PlayerService.class)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.word)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.sample)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.none)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND, PlayerService.SkipContidion.all)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_NUM, 0.0)
								.putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, PlayerService.SkipThreshold.eqormore)));
				binding.buttonChoosePlaybackRange.setOnClickListener(v -> TabActivity.setTabPageNum(0));

				isInitialized = true;
				renderState();
				context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).setPackage(context.getPackageName())
						.putExtra(PLAYERSERVICE_MESSAGE_TYPE, PlayerService.PLAYERSERVICE_MESSAGE_REFRESH));
			} catch (Exception e) {
				showException(context, e);
			}

			puts(getMethodName() + " ended");
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}

	@Override public void onResume() {
		super.onResume();
		renderState();
	}

	private void renderState() {
		if (binding == null) return;
		boolean running = PlayerService.isRunning();
		binding.playerContent.setVisibility(running ? View.VISIBLE : View.GONE);
		binding.playerStartPanel.setVisibility(running ? View.GONE : View.VISIBLE);
		binding.playerStatus.setText(PlayerService.statusMessage(requireContext()));
	}

	@Override
	public void onDestroyView() {
		isInitialized = false;
		if (receiver != null) { context.unregisterReceiver(receiver); receiver = null; }
		drawHandler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	public void onPlayerServiceStop(View view) {
		puts(getMethodName());
		Intent broadcastIntent = new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP).setPackage(context.getPackageName());
		context.sendBroadcast(broadcastIntent);
	}

	public void onResetButtonClick(View view) {
		try {
			context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_NOW).putExtra(PLAYERSERVICE_MESSAGE_NOW, 0).setPackage(context.getPackageName()));
		} catch (Exception e) {
			showException(context, e);
		}
	}

	public void onChangeNumber(View view) {
		var entries = new ArrayList<>(PlayerService.wordDataList);
		if (entries.isEmpty()) { MyLibrary.DisplayOutput.makeToastForShort(context, "再生中のデータがありません。"); return; }
		var labels = new ArrayList<String>();
		for (var entry : entries) labels.add(entry.numberInBook + ": " + entry.e + " " + entry.j);
		new AlertDialog.Builder(context).setTitle("単語を選択してください。")
				.setItems(labels.toArray(new String[0]), (dialog, index) -> context.sendBroadcast(
						new Intent(PLAYERSERVICE_ACTION).setPackage(context.getPackageName())
								.putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_NOW)
								.putExtra(PLAYERSERVICE_MESSAGE_NOW, index))).show();
	}

	public void onPIPButtonClicked(View view) {
		try {
			startActivity(new Intent(context, PipActivity.class)
					.putExtra(PipActivity.PIP_TV_FIRST_ENG, binding.textViewEng.getText().toString())
					.putExtra(PipActivity.PIP_TV_FIRST_JPN, binding.textViewJpn.getText().toString()));
		} catch (Exception e) {
			showException(context, e);
		}
	}

	private void onSpeedSeekBar(SeekBar seekBar, int i, boolean b) {
		try {
			final float speed = 1 + i * 0.1f;
			if (seekBar.getId() == R.id.seekBarEng) {
				//英語
				binding.textViewSeekBarEng.setText(String.format(Locale.getDefault(), "英語 x%.1f", speed));
				putIntData(context, "SeekBar", "english", i);
				PlayerService.dPlaySpeedEng = speed;
			} else if (seekBar.getId() == R.id.seekBarJpn) {
				//日本語
				binding.textViewSeekBarJpn.setText(String.format(Locale.getDefault(), "日本語 x%.1f", speed));
				putIntData(context, "SeekBar", "japanese", i);
				PlayerService.dPlaySpeedJpn = speed;
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
}
