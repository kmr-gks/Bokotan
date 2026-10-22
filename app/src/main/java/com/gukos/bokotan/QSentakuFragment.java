package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.BookQ;
import static com.gukos.bokotan.Dictionary.Datatype;
import static com.gukos.bokotan.Dictionary.Folder;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.makeToastForShort;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getAllFileNames;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.initializeSettingItem;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putStringData;
import static com.gukos.bokotan.MyLibrary.getBuildDate;
import static com.gukos.bokotan.PipActivity.pipTate;
import static com.gukos.bokotan.PipActivity.pipYoko;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_BOOK;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_DATA_Q;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_MODE;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_SHOW_APPEARED;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_NUM;
import static com.gukos.bokotan.UiManager.getAdapterForSpinner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gukos.bokotan.databinding.FragmentQSentakuBinding;


public class QSentakuFragment extends UiManager.FragmentBinding<FragmentQSentakuBinding> {
    private final android.os.Handler importHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final androidx.activity.result.ActivityResultLauncher<String[]> zipPicker = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null || ImportService.busy) return;
                Context app = requireContext().getApplicationContext();
                try { app.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch (SecurityException ignored) { }
                app.startForegroundService(new Intent(app, ImportService.class).setData(uri)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
            });
    private final androidx.activity.result.ActivityResultLauncher<String> exportPicker = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri == null) return;
                try { PreferenceBackup.exportTo(requireContext(), uri); makeToastForShort(requireContext(), "設定を保存しました。"); }
                catch (Exception error) { makeToastForShort(requireContext(), "設定の保存に失敗しました。"); }
            });
    private final androidx.activity.result.ActivityResultLauncher<String[]> settingsPicker = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null || ImportService.busy) return;
                try {
                    QuizCreator.stopCurrent(); requireContext().stopService(new Intent(requireContext(), PlayerService.class));
                    PreferenceBackup.importFrom(requireContext(), uri);
                    Dictionary.reload(requireContext(), () -> { if (isAdded()) requireActivity().recreate(); });
                } catch (Exception error) { makeToastForShort(requireContext(), "設定の形式または読み込みに問題があります。"); }
            });
    private final Runnable showImportStatus = new Runnable() {
        @Override public void run() {
            if (binding == null) return;
            binding.textImportStatus.setText(ImportService.status);
            binding.buttonImportZip.setEnabled(!ImportService.busy && Dictionary.isReady());
            binding.buttonCancelImport.setVisibility(ImportService.busy ? View.VISIBLE : View.GONE);
            if (ImportService.completed) {
                ImportService.completed = false;
                putIntData(context, "spinnerBookQ", "selected", 9);
                requireActivity().recreate(); return;
            }
            importHandler.postDelayed(this, 500);
        }
    };
    @Override public void onDestroyView() {
        importHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

	private final String keySkipCondition = "skipCondition",
			keySkipThreshold = "skipThreshold",
			skipThresholdNum = "skipThresholdNum",
			settingFileName = "uisetting";

	//他のクラスからアクセス


	public static Folder dataBook;
	public static BookQ dataQ;

	public QSentakuFragment() {
		super(FragmentQSentakuBinding::inflate);
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);

			initialize();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}

	public void initialize() {
		try {
			//バージョン表記
			binding.textViewVersion.setText(getBuildDate(context));
			binding.buttonPrivacyPolicy.setOnClickListener(v -> {
				String url = getString(R.string.privacy_policy_url);
				try {
					startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
							.addCategory(Intent.CATEGORY_BROWSABLE));
				} catch (android.content.ActivityNotFoundException error) {
					new AlertDialog.Builder(requireContext())
							.setTitle(R.string.privacy_policy)
							.setMessage(getString(R.string.privacy_browser_unavailable, url))
							.setPositiveButton(android.R.string.ok, null).show();
				}
			});


			binding.buttonPrefExport.setOnClickListener(this::onExportPrefsButton);
			binding.buttonPrefImp.setOnClickListener(this::onImportPrefsButton);
			binding.buttonWriteTest.setVisibility(View.GONE);
			binding.buttonImportZip.setOnClickListener(v -> new AlertDialog.Builder(context)
					.setTitle("個人データを取り込む")
					.setMessage("端末またはクラウドからZIPを選択します。取得には通信が発生する場合があります。取り込み成功後は既存の個人データを置き換えます。")
					.setPositiveButton("ZIPを選択", (dialog, which) -> zipPicker.launch(new String[]{"*/*"}))
					.setNegativeButton("キャンセル", null).show());
			binding.buttonCancelImport.setOnClickListener(v -> context.startService(new Intent(context, ImportService.class).setAction("cancel")));
			importHandler.post(showImportStatus);
			binding.buttonShowSettingNew.setOnClickListener(this::onShowSettingNew);


			binding.editTextPipYoko.setText(String.valueOf(getIntData(context, "editText", "editTextPipYoko", 16)));
			pipYoko = Integer.parseInt(binding.editTextPipYoko.getText().toString());
			binding.editTextPipYoko.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
				try {
					if (editable.length() > 0) {
						pipYoko = Integer.parseInt(editable.toString());
						putIntData(context, "editText", "editTextPipYoko", pipYoko);
					}
				} catch (Exception e) {
					showException(context, e);
				}
			});

			binding.editTextPipTate.setText(String.valueOf(getIntData(context, "editText", "editTextPipTate", 9)));
			pipTate = Integer.parseInt(binding.editTextPipTate.getText().toString());
			binding.editTextPipTate.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) editable -> {
				{
					try {
						if (editable.length() > 0) {
							pipTate = Integer.parseInt(editable.toString());
							putIntData(context, "editText", "editTextPipTate", pipTate);
						}
					} catch (Exception e) {
						showException(context, e);
					}
				}
			});

			initializeSettingItem(binding.switchOnlyFirst, true);
			initializeSettingItem(binding.switchHyojiYakuBeforeRead, true);
			initializeSettingItem(binding.switchSkipOboe, true);
			initializeSettingItem(binding.switchSortHanten, false);
			initializeSettingItem(binding.checkBoxAutoStop, false);
			initializeSettingItem(binding.checkBoxHatsuonkigou, false);
			initializeSettingItem(binding.switchQuizHatsuon, true);
			initializeSettingItem(binding.switchQuizOxKoukaon, true);

			for (var v : new SwitchMaterial[]{binding.switchOnlyFirst, binding.switchHyojiYakuBeforeRead, binding.switchSkipOboe, binding.switchSortHanten, binding.checkBoxAutoStop, binding.checkBoxHatsuonkigou, binding.switchQuizHatsuon, binding.switchQuizOxKoukaon}) {
				v.setOnCheckedChangeListener(UiManager.Listener::onClickSettingItem);
			}

			initializeRadioButtonCondition(binding);
			initializeRadioButtonThreshold(binding);

			binding.radioButtonPlayAll.setOnClickListener(this::onSkipConditionChanged);
			binding.radioButtonSeikai.setOnClickListener(this::onSkipConditionChanged);
			binding.radioButtonHuseikai.setOnClickListener(this::onSkipConditionChanged);
			binding.radioButtonSeikaiRate.setOnClickListener(this::onSkipConditionChanged);
			double thresholdNum = Double.parseDouble(getStringData(context, settingFileName, skipThresholdNum, "1"));
			binding.editNumberThreshold.setText(String.valueOf(thresholdNum));

			binding.radioButtonEqOrMore.setOnClickListener(this::onSkipThresholdChanged);
			binding.radioButtonEqOrLess.setOnClickListener(this::onSkipThresholdChanged);

			binding.spinnerSpace.setAdapter(getAdapterForSpinner(context, R.array.spinner_kuuhaku));
			binding.spinnerSpace.setSelection(getIntData(context, "spinnerKuuhaku", "selected", 0));
			binding.spinnerSpace.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerKuuhakuOnItemSelectedListener);

			binding.spinnerHyojijun.setAdapter(getAdapterForSpinner(context, R.array.spinner_hyojijun));
			binding.spinnerHyojijun.setSelection(getIntData(context, "spinnerHyojijun", "selected", 0));
			binding.spinnerHyojijun.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerHyojijunOnItemSelectedListener);

			binding.spinnerBookQ.setAdapter(getAdapterForSpinner(context, R.array.spinner_book_q));
			binding.spinnerBookQ.setSelection(getIntData(context, "spinnerBookQ", "selected", 8));
			Dictionary.whenReady(context, () -> {
				if (binding != null && Dictionary.allData.stream().allMatch(entry -> entry.folder == Folder.sample)) binding.spinnerBookQ.setSelection(8);
			});
			binding.spinnerBookQ.setOnItemSelectedListener((UiManager.UiInterface.AdapterViewItemSelected) this::spinnerBookQOnItemSelectedListener);

			binding.buttonWord.setOnClickListener(this::onPlayQuizStart);
			binding.buttonPhrase.setOnClickListener(this::onPlayQuizStart);
			binding.buttonWP.setOnClickListener(this::onPlayQuizStart);
			binding.buttonQuiz.setOnClickListener(this::onPlayQuizStart);
		} catch (Exception e) {
			showException(context, e);
		}
	}

	private void onShowSettingNew(View view) {
		try {
			var content = new StringBuilder();
			for (var fileName : getAllFileNames()) {
				content.append("ファイル名:").append(fileName).append("\n");
				puts("filaname=" + fileName);
				for (var key : context.getSharedPreferences(fileName, Context.MODE_PRIVATE).getAll().keySet()) {
					content.append("\t").append(key).append("\n");
				}
			}
			new AlertDialog.Builder(context)
					.setTitle(((Button) view).getText())
					.setMessage(content.toString())
					.create()
					.show();
		} catch (Exception exception) {
			showException(context, exception);
		}
	}

    private void onExportPrefsButton(View view) {
        if (ImportService.busy) return;
        Dictionary.QuizData.saveQuizData(context);
        exportPicker.launch("bokotan-settings.json");
    }
    private void onImportPrefsButton(View view) {
        if (!ImportService.busy) settingsPicker.launch(new String[]{"application/json", "text/plain"});
    }

	private void onPlayQuizStart(View view) {
		if (ImportService.busy) { makeToastForShort(context, "個人データを取り込み中です。"); return; }
		if (!Dictionary.isReady()) { makeToastForShort(context, "辞書を読み込み中です。少し待ってから開始してください。"); return; }
		double enteredThreshold;
		Integer enteredPosition = null;
		try {
			enteredThreshold = Double.parseDouble(binding.editNumberThreshold.getText().toString());
			if (!Double.isFinite(enteredThreshold) || enteredThreshold < 0) throw new NumberFormatException();
			if (binding.editTextNumber.length() > 0) {
				enteredPosition = Integer.parseInt(binding.editTextNumber.getText().toString());
				if (enteredPosition < 0) throw new NumberFormatException();
			}
		} catch (NumberFormatException error) { makeToastForShort(context, "開始位置と条件には0以上の数値を入力してください。"); return; }
		PlayerService.SkipContidion skipContidion;
		PlayerService.SkipThreshold skipThreshold;
		//スピナーから本と級を取得
		switch (binding.spinnerBookQ.getSelectedItemPosition()) {
			case 0: {
				dataBook = Folder.yumetan;
				dataQ = BookQ.y1;
				break;
			}
			case 1: {
				dataBook = Folder.yumetan;
				dataQ = BookQ.y2;
				break;
			}
			case 2: {
				dataBook = Folder.yumetan;
				dataQ = BookQ.y3;
				break;
			}
			case 3: {
				dataBook = Folder.passtan;
				dataQ = BookQ.q1;
				break;
			}
			default:
			case 4: {
				dataBook = Folder.passtan;
				dataQ = BookQ.qp1;
				break;
			}
			case 5: {
				dataBook = Folder.tanjukugo;
				dataQ = BookQ.q1;
				break;
			}
			case 6: {
				dataBook = Folder.tanjukugo;
				dataQ = BookQ.qp1;
				break;
			}
			case 9: { dataBook = Folder.personal; dataQ = BookQ.none; break; }
			case 8: {
				dataBook = Folder.sample;
				dataQ = BookQ.none;
				break;
			}
			case 7: {
				dataBook = Folder.all;
				dataQ = BookQ.all;
				break;
			}
		}
		if ((dataBook == Folder.sample || dataBook == Folder.personal) && (view == binding.buttonPhrase || view == binding.buttonWP)) {
			makeToastForShort(context, "このデータには単語のみ収録しています。"); return;
		}

		int conditionId = binding.radioGroupSkipCondition.getCheckedRadioButtonId();
		if (conditionId == R.id.radioButtonSeikai) {
			skipContidion = PlayerService.SkipContidion.seikaisu;
		} else if (conditionId == R.id.radioButtonHuseikai) {
			skipContidion = PlayerService.SkipContidion.huseikai;
		} else if (conditionId == R.id.radioButtonSeikaiRate) {
			skipContidion = PlayerService.SkipContidion.seikairate;
		} else {
			skipContidion = PlayerService.SkipContidion.all;
		}
		if (binding.radioGroupSkipCompare.getCheckedRadioButtonId() == R.id.radioButtonEqOrLess) {
			skipThreshold = PlayerService.SkipThreshold.eqorless;
		} else {
			skipThreshold = PlayerService.SkipThreshold.eqormore;
		}
		//しきい値が設定されている場合
		if (binding.editNumberThreshold.length() > 0) {
			putStringData(getContext(), settingFileName, skipThresholdNum, binding.editNumberThreshold.getText().toString());
		}
		if (view == binding.buttonQuiz) {
			//問題出題を開始
			TabActivity.setTabPageNum(2);
			QuizCreator.build(context, dataBook, dataQ, skipContidion, enteredThreshold, skipThreshold);
		} else {
			//再生開始
			TabActivity.setTabPageNum(1);
			Datatype mode = Datatype.word;
			if (view == binding.buttonPhrase) mode = Datatype.phrase;
			else if (view == binding.buttonWP) mode = Datatype.mix;
			Intent intent = new Intent(context, PlayerService.class)
					.putExtra(PLAYERSERVICE_EXTRA_MODE, mode)
					.putExtra(PLAYERSERVICE_EXTRA_BOOK, dataBook)
					.putExtra(PLAYERSERVICE_EXTRA_DATA_Q, dataQ)
					.putExtra(PLAYERSERVICE_EXTRA_SKIP_COND, skipContidion)
					.putExtra(PLAYERSERVICE_EXTRA_SKIP_THRES_NUM, enteredThreshold)
					.putExtra(PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, skipThreshold)
					.putExtra(PLAYERSERVICE_EXTRA_SHOW_APPEARED, binding.switchOnlyFirst.isChecked());
			//開始位置が設定されている場合
			if (binding.editTextNumber.length() > 0) {
				intent.putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, enteredPosition);
			}
			binding.editTextNumber.setText("");
			context.startForegroundService(intent);
		}
	}

	private void onSkipConditionChanged(View view) {
		putIntData(getContext(), settingFileName, keySkipCondition, view.getId());
	}

	private void onSkipThresholdChanged(View view) {
		putIntData(getContext(), settingFileName, keySkipThreshold, view.getId());
	}

	private void initializeRadioButtonCondition(FragmentQSentakuBinding binding) {
		int id = getIntData(requireContext(), settingFileName, keySkipCondition, 0);
		for (var radioButton : new RadioButton[]{binding.radioButtonPlayAll, binding.radioButtonSeikai, binding.radioButtonHuseikai, binding.radioButtonSeikaiRate}) {
			if (radioButton.getId() == id) radioButton.setChecked(true);
		}
	}

	private void initializeRadioButtonThreshold(FragmentQSentakuBinding binding) {
		int id = getIntData(requireContext(), settingFileName, keySkipThreshold, 0);
		for (var radioButton : new RadioButton[]{binding.radioButtonEqOrMore, binding.radioButtonEqOrLess}) {
			if (radioButton.getId() == id) radioButton.setChecked(true);
		}
	}

	private void spinnerKuuhakuOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerKuuhaku", "selected", i);
			switch (i) {
				default:
				case 0: {
					strDirectoryNameForKuuhaku = "";
					break;
				}
				case 1: {
					strDirectoryNameForKuuhaku = "autocut-";
					break;
				}
				case 2: {
					strDirectoryNameForKuuhaku = "manucut-";
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}

	private void spinnerHyojijunOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerHyojijun", "selected", i);
			switch (i) {
				default:
				case 0: {
					Dictionary.QuizData.skipjoken = Dictionary.QuizData.skipjouken.kirokunomi;
					break;
				}
				case 1: {
					Dictionary.QuizData.skipjoken = Dictionary.QuizData.skipjouken.onlyHugoukaku;
					break;
				}
				case 2: {
					Dictionary.QuizData.skipjoken = Dictionary.QuizData.skipjouken.seikai1;
					break;
				}
				case 3: {
					Dictionary.QuizData.skipjoken = Dictionary.QuizData.skipjouken.huseikai2;
					break;
				}
			}
		} catch (Exception e) {
			showException(context, e);
		}
	}

	private void spinnerBookQOnItemSelectedListener(AdapterView<?> adapterView, View view1, int i, long l) {
		try {
			putIntData(context, "spinnerBookQ", "selected", i);
		} catch (Exception e) {
			showException(context, e);
		}
	}
}
