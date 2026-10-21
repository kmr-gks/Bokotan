package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;

import com.gukos.bokotan.databinding.FragmentTestBinding;

import java.util.Objects;


//このクラスで定義されているメソッドやラムダ式は全てメインスレッドで実行される(UI処理に関わるため)
public class TestFragment extends UiManager.FragmentBinding<FragmentTestBinding> {

	public static boolean isInitialized = false;
	private DrawReceiver receiver;

	public static final String
			QUIZ_ACTION_UI_CHANGE = "quiz_action_ui_change",
			QUIZ_VIEW_TEXT_STRING = "quiz_view_text",
			QUIZ_VIEW_TEXT_CHARSEQ = "qvtcs",
			QUIZ_VIEW_COLOR = "quiz_view_color",
			QUIZ_VIEW_PROPERTIES = "quiz_view_properties",
			QUIZ_VIEW_NAME = "quiz_view_name";

	public enum ViewProperties {
		Text, TextColor
	}

	public enum ViewName {
		monme, Marubatsu, Editorial, No, Mondaibun, Hint, Select1, Select2, Select3, Select4, Idontknow,
		Debug
	}

	//https://oc-technote.com/android/service%E3%81%8B%E3%82%89activity%E3%81%AB%E5%80%A4%E3%82%92%E6%8A%95%E3%81%92%E3%81%9F%E3%82%8A%E7%94%BB%E9%9D%A2%E3%82%92%E6%9B%B4%E6%96%B0%E3%81%97%E3%81%9F%E3%82%8A%E3%81%99%E3%82%8B%E6%96%B9/
	private final Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			if (binding == null) return;
			Bundle bundle = msg.getData();
			if (bundle.containsKey(QuizCreator.QUIZ_STATE_CHANGED)) {
				renderState();
				return;
			}
			ViewName viewName = Objects.requireNonNull(BundleCompat.getSerializable(bundle, QUIZ_VIEW_NAME, ViewName.class));
			ViewProperties viewProperties = Objects.requireNonNull(BundleCompat.getSerializable(bundle, QUIZ_VIEW_PROPERTIES, ViewProperties.class));

			final TextView textViewToHandle;
			switch (viewName) {
				case monme: {
					textViewToHandle = binding.textViewMonme;
					break;
				}
				case Marubatsu: {
					textViewToHandle = binding.textViewMaruBatsu;
					break;
				}
				case Editorial: {
					textViewToHandle = binding.textViewEditorial;
					break;
				}
				case No: {
					textViewToHandle = binding.textViewNo;
					break;
				}
				case Mondaibun: {
					textViewToHandle = binding.textViewMondai;
					break;
				}
				case Hint: {
					textViewToHandle = binding.buttonHint;
					break;
				}
				case Select1: {
					textViewToHandle = binding.buttonSelect1;
					break;
				}
				case Select2: {
					textViewToHandle = binding.buttonSelect2;
					break;
				}
				case Select3: {
					textViewToHandle = binding.buttonSelect3;
					break;
				}
				case Select4: {
					textViewToHandle = binding.buttonSelect4;
					break;
				}
				case Idontknow: {
					textViewToHandle = binding.buttonIdontKnow;
					break;
				}
				case Debug: {
					textViewToHandle = binding.textViewDebug;
					break;
				}
				default: {
					throw new IllegalStateException("view name is invalid");
				}
			}
			switch (viewProperties) {
				case Text: {
					if (bundle.containsKey(QUIZ_VIEW_TEXT_CHARSEQ)) {
						textViewToHandle.setText(bundle.getCharSequence(QUIZ_VIEW_TEXT_CHARSEQ));
					} else {
						textViewToHandle.setText(bundle.getString(QUIZ_VIEW_TEXT_STRING));
					}
					break;
				}
				case TextColor: {
					textViewToHandle.setTextColor(bundle.getInt(QUIZ_VIEW_COLOR));
					break;
				}
			}
		}
	};

	public TestFragment() {
		super(FragmentTestBinding::inflate);
	}


	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			receiver = new DrawReceiver(drawHandler);
			ContextCompat.registerReceiver(context, receiver, new IntentFilter(QUIZ_ACTION_UI_CHANGE), ContextCompat.RECEIVER_NOT_EXPORTED);
			binding.buttonSelect1.setOnClickListener(this::onChoice);
			binding.buttonSelect2.setOnClickListener(this::onChoice);
			binding.buttonSelect3.setOnClickListener(this::onChoice);
			binding.buttonSelect4.setOnClickListener(this::onChoice);
			binding.buttonIdontKnow.setOnClickListener(this::onChoice);
			binding.buttonStartSampleQuiz.setOnClickListener(v -> QuizCreator.build(context,
					Dictionary.Folder.sample, Dictionary.BookQ.none, PlayerService.SkipContidion.all,
					0, PlayerService.SkipThreshold.eqormore));
			binding.buttonChooseQuizRange.setOnClickListener(v -> TabActivity.setTabPageNum(0));
			binding.buttonEndQuiz.setOnClickListener(v -> QuizCreator.stopCurrent());

			isInitialized = true;
			QuizCreator.refreshCurrent();
			renderState();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}

	@Override public void onResume() {
		super.onResume();
		QuizCreator.refreshCurrent();
		renderState();
	}

	private void renderState() {
		if (binding == null) return;
		boolean hasQuestion = QuizCreator.hasQuestion();
		binding.quizContent.setVisibility(hasQuestion ? View.VISIBLE : View.GONE);
		binding.quizStartPanel.setVisibility(hasQuestion ? View.GONE : View.VISIBLE);
		binding.quizStatus.setText(QuizCreator.statusMessage(requireContext()));
		binding.buttonStartSampleQuiz.setEnabled(!QuizCreator.isRunning());
	}

	@Override
	public void onDestroyView() {
		isInitialized = false;
		if (receiver != null) { context.unregisterReceiver(receiver); receiver = null; }
		drawHandler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	public void onChoice(View view) {
		int choice = -1;
		if (view == binding.buttonSelect1) {
			choice = 1;
		} else if (view == binding.buttonSelect2) {
			choice = 2;
		} else if (view == binding.buttonSelect3) {
			choice = 3;
		} else if (view == binding.buttonSelect4) {
			choice = 4;
		}

		context.sendBroadcast(new Intent(QuizCreator.QTHREAD_ACTION_CLICKED).putExtra(QuizCreator.QTHREAD_EXTRA_CHOICE, choice).setPackage(context.getPackageName()));
	}
}
