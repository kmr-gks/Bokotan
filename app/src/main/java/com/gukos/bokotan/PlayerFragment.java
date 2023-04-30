package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.getMethodName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_ACTION;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_NOW;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_STOP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_TYPE;
import static com.gukos.bokotan.QSentakuFragment.dataBook;
import static com.gukos.bokotan.QSentakuFragment.dataQ;
import static com.gukos.bokotan.Unit.toFindFromAndTo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentPlayerBinding;

import java.util.ArrayList;

public class PlayerFragment extends UiManager.FragmentBingding<FragmentPlayerBinding> {
	public static Boolean isInitialized = false;
	public static final String
		PLAYER_ACTION_UI_CHANGE = "player_action_ui_change",
		PLAYER_VIEW_PROPERTIES = "player_view_properties",
	
	PLAYER_VIEW_NAME = "player_view_name",
		PLAYER_VIEW_TEXT = "player_view_text",
		PLAYER_VIEW_COLOR = "player_view_color",
		PLAYER_VIEW_SINGLE_LINE = "pvsl";
	
	public enum PlayerViewProperties {
		Text, TextColor, line
	}
	
	public enum PlayerViewName {
		genzai, tvcount, hatsuon, subJ, subE, eng, jpn, path, Debug
	}
	
	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			PlayerViewName viewName = (PlayerViewName) bundle.getSerializable(PLAYER_VIEW_NAME);
			PlayerViewProperties viewProperties = (PlayerViewProperties) bundle.getSerializable(PLAYER_VIEW_PROPERTIES);
			final TextView textViewToHandle;
			switch (viewName) {
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
				case Debug: {
					//textViewToHandle=binding.
					//break;
				}
				default: {
					throw new IllegalStateException("view name is invalid");
				}
			}
			switch (viewProperties) {
				case Text: {
					textViewToHandle.setText(bundle.getString(PLAYER_VIEW_TEXT));
					break;
				}
				case TextColor: {
					textViewToHandle.setTextColor(bundle.getInt(PLAYER_VIEW_COLOR));
					break;
				}
				case line: {
					//setSingleLineを使用すると後半が表示されない場合があるため使わない
					if (bundle.getBoolean(PLAYER_VIEW_SINGLE_LINE)) {
						textViewToHandle.setLines(1);
					}
					else {
						textViewToHandle.setMaxLines(5);
					}
					break;
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
				context.registerReceiver(new DrawReceiver(drawHandler), new IntentFilter(PLAYER_ACTION_UI_CHANGE));
				
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
				
				synchronized (isInitialized) {
					isInitialized = true;
				}
			} catch (Exception e) {
				showException(context, e);
			}
			
			puts(getMethodName() + " ended");
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public static void initialize(Context context) {
		try {
			//再生開始
			puts(getClassName() + getMethodName() + " start");
			
			//バグ対策は不要になった
			/*
			put("smooth out 〜", "pass" + "p1q");//1799
			put("grow into 〜", "p1q");          //1675
			put("accrue", "pass" + "1q");        //1568
			*/
			
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onPlayerServiceStop(View view) {
		puts(getMethodName());
		Intent broadcastIntent = new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP);
		context.sendBroadcast(broadcastIntent);
	}
	
	public void onResetButtonClick(View view) {
		try {
			context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_NOW).putExtra(PLAYERSERVICE_MESSAGE_NOW, 1));
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void onChangeNumber(View view) {
		try {
			final int[][] fromTo;
			final String[] unit = new String[100];
			switch (dataBook) {
				default:
				case passTan: {
					int i = 0;
					for (var derudo : new String[]{"出る度A", "出る度B", "出る度C"}) {
						for (var hinshi : new String[]{"動詞", "名詞", "形容詞"}) {
							unit[i] = derudo + hinshi;
							i++;
						}
					}
					unit[i] = "熟語";
					if (dataQ == WordPhraseData.DataQ.q1) {
						fromTo = toFindFromAndTo[0];
					}
					else {
						fromTo = toFindFromAndTo[1];
					}
					break;
				}
				case yumetan: {
					for (int i = 0; i < 10; i++) {
						unit[i] = "Unit" + (i + 1);
					}
					switch (dataQ) {
						case y00: {
							fromTo = toFindFromAndTo[7];
							break;
						}
						case y08: {
							fromTo = toFindFromAndTo[8];
							break;
						}
						default:
						case y1: {
							fromTo = toFindFromAndTo[9];
							break;
						}
						case y2: {
							fromTo = toFindFromAndTo[10];
							break;
						}
						case y3: {
							fromTo = toFindFromAndTo[11];
							break;
						}
					}
					break;
				}
				case tanjukugo: {
					for (int i = 0; i < 10; i++) {
						unit[i] = "Unit" + (i + 1);
					}
					unit[10] = "Unit Ex";
					if (dataQ == WordPhraseData.DataQ.q1) {
						fromTo = toFindFromAndTo[12];
					}
					else {
						fromTo = toFindFromAndTo[13];
					}
					break;
				}
				case all: {
					ArrayList<String> listUnit = new ArrayList<>();
					for (int i = 1; i < 10 + 1; i++) {
						listUnit.add("ユメタン1:Unit" + i);
					}
					for (int i = 1; i < 10 + 1; i++) {
						listUnit.add("ユメタン2:Unit" + i);
					}
					for (int i = 1; i < 8 + 1; i++) {
						listUnit.add("ユメタン3:Unit" + i);
					}
					for (var derudo : new String[]{"出る度A", "出る度B", "出る度C"}) {
						for (var hinshi : new String[]{"動詞", "名詞", "形容詞"}) {
							listUnit.add("パス単準1級" + derudo + hinshi);
						}
					}
					listUnit.add("パス単準1級" + "熟語");
					for (var derudo : new String[]{"出る度A", "出る度B", "出る度C"}) {
						for (var hinshi : new String[]{"動詞", "名詞", "形容詞"}) {
							listUnit.add("パス単1級" + derudo + hinshi);
						}
					}
					listUnit.add("パス単1級" + "熟語");
					
					for (int i = 0; i < listUnit.size(); i++) {
						unit[i] = listUnit.get(i);
					}
					fromTo = toFindFromAndTo[14];
					break;
				}
			}
			var unitAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
			for (int i = 0; i < fromTo.length; i++) {
				var pair = fromTo[i];
				unitAdapter.add(unit[i] + " (" + pair[0] + "～" + pair[1] + ")");
			}
			new AlertDialog.Builder(context)
				.setTitle("unit:")
				.setSingleChoiceItems(unitAdapter, 0, (dialogInterface, index) -> {
					puts("i=" + index);
					dialogInterface.dismiss();
					var wordAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_single_choice);
					for (int i = fromTo[index][0]; i <= fromTo[index][1]; i++) {
						var data = PlayerService.wordDataList.get(i);
						wordAdapter.add(i + "," + data.localNumber + ", " + data.e + " " + data.j);
					}
					new AlertDialog.Builder(context)
						.setTitle("単語を選択してください。")
						.setSingleChoiceItems(wordAdapter, 0, (dialog, i) -> {
							dialog.dismiss();
							context.sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_NOW).putExtra(PLAYERSERVICE_MESSAGE_NOW, fromTo[index][0] + i));
						})
						.create()
						.show();
				})
				.create()
				.show();
		} catch (Exception exception) {
			showException(context, exception);
		}
	}
	
	public void onPIPButtonClicked(View view) {
		try {
			if (PipActivity.startPIP) {
				//PIPを終了したい
			}
			else {
				startActivity(new Intent(context, PipActivity.class).putExtra(PipActivity.PIP_TV_FIRST_ENG, binding.textViewEng.getText()).putExtra(PipActivity.PIP_TV_FIRST_JPN, binding.textViewJpn.getText()));
			}
			PipActivity.startPIP = !PipActivity.startPIP;
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void onSpeedSeekBar(SeekBar seekBar, int i, boolean b) {
		try {
			final float speed = 1 + i * 0.1f;
			if (seekBar.getId() == R.id.seekBarEng) {
				//英語
				binding.textViewSeekBarEng.setText(String.format("英語 x%.1f", speed));
				putIntData(context, "SeekBar", "english", i);
				PlayerService.dPlaySpeedEng = speed;
			}
			else if (seekBar.getId() == R.id.seekBarJpn) {
				//日本語
				binding.textViewSeekBarJpn.setText(String.format("日本語 x%.1f", speed));
				putIntData(context, "SeekBar", "japanese", i);
				PlayerService.dPlaySpeedJpn = speed;
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}
}