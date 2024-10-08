package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentTestBinding;


//このクラスで定義されているメソッドやラムダ式は全てメインスレッドで実行される(UI処理に関わるため)
public class TestFragment extends UiManager.FragmentBingding<FragmentTestBinding> {
	
	public static Boolean isInitialized = false;
	
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
			Bundle bundle = msg.getData();
			ViewName viewName = (ViewName) bundle.getSerializable(QUIZ_VIEW_NAME);
			ViewProperties viewProperties = (ViewProperties) bundle.getSerializable(QUIZ_VIEW_PROPERTIES);
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
					}
					else {
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
			context.registerReceiver(new DrawReceiver(drawHandler), new IntentFilter(QUIZ_ACTION_UI_CHANGE), Context.RECEIVER_NOT_EXPORTED);
			binding.buttonSelect1.setOnClickListener(this::onChoice);
			binding.buttonSelect2.setOnClickListener(this::onChoice);
			binding.buttonSelect3.setOnClickListener(this::onChoice);
			binding.buttonSelect4.setOnClickListener(this::onChoice);
			binding.buttonIdontKnow.setOnClickListener(this::onChoice);
			
			synchronized (isInitialized) {
				isInitialized = true;
			}
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public void onChoice(View view) {
		int choice = -1;
		if (view == binding.buttonSelect1) {
			choice = 1;
		}
		else if (view == binding.buttonSelect2) {
			choice = 2;
		}
		else if (view == binding.buttonSelect3) {
			choice = 3;
		}
		else if (view == binding.buttonSelect4) {
			choice = 4;
		}
		
		context.sendBroadcast(new Intent(QuizCreator.QTHREAD_ACTION_CLICKED).putExtra(QuizCreator.QTHREAD_EXTRA_CHOICE, choice));
	}
}