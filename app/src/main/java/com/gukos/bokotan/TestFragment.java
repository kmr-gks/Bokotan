package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentTestBinding;



public class TestFragment extends UiManager.FragmentBingding<FragmentTestBinding> {
	public static final String
		QUIZ_ACTION_UI = MyLibrary.packageName + "." + MyLibrary.DebugManager.getClassName(),
		QUIZ_UI_TEXT = "quiz ui text",
		QUIZ_VIEW_NAME = "quiz ui name";
	
	public enum ViewName {
		Mondaibun, Select1, Select2, Select3, Select4
	}
	
	//https://oc-technote.com/android/service%E3%81%8B%E3%82%89activity%E3%81%AB%E5%80%A4%E3%82%92%E6%8A%95%E3%81%92%E3%81%9F%E3%82%8A%E7%94%BB%E9%9D%A2%E3%82%92%E6%9B%B4%E6%96%B0%E3%81%97%E3%81%9F%E3%82%8A%E3%81%99%E3%82%8B%E6%96%B9/
	private Handler drawHandler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String text = bundle.getString(QUIZ_UI_TEXT);
			ViewName viewName= (ViewName) bundle.getSerializable(QUIZ_VIEW_NAME);
			switch (viewName) {
				case Mondaibun: {
					binding.textViewMondai.setText(text);
					break;
				}
				case Select1: {
					binding.buttonSelect1.setText(text);
					break;
				}
				case Select2: {
					binding.buttonSelect2.setText(text);
					break;
				}
				case Select3: {
					binding.buttonSelect3.setText(text);
					break;
				}
				case Select4: {
					binding.buttonSelect4.setText(text);
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
			context.registerReceiver(new DrawReceiver(drawHandler), new IntentFilter(QUIZ_ACTION_UI));
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	static class Seikairitsu {
		final int num;
		final int seitouritu;
		final int toitakazu;
		
		Seikairitsu(int num, int seitouritu, int toitakazu) {
			this.num = num;
			this.seitouritu = seitouritu;
			this.toitakazu = toitakazu;
		}
	}
}