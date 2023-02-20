package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;

import kotlin.jvm.functions.Function3;

public class UiManager {
	
	public static ArrayAdapter<String> getAdapterForSpinner(Context context, int stringResourceId) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.custom_spinner, context.getResources().getStringArray(stringResourceId));
		adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown);
		return adapter;
	}
	
	public static final class UiInterface {
		public interface AdapterViewItemSelected extends AdapterView.OnItemSelectedListener {
			@Override
			abstract void onItemSelected(AdapterView<?> adapterView, View view1, int i, long l);
			
			@Override
			default void onNothingSelected(AdapterView<?> adapterView) {
			}
		}
		
		public interface OnSeekBarProgressChange extends SeekBar.OnSeekBarChangeListener {
			@Override//ツマミがドラッグされると呼ばれる
			abstract void onProgressChanged(SeekBar seekBar, int i, boolean b);
			
			@Override//ツマミがタッチされた時に呼ばれる
			default void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override//ツマミがリリースされた時に呼ばれる
			default void onStopTrackingTouch(SeekBar seekBar) {
			}
		}
		
		public interface TextWatcherAfterOnly extends TextWatcher {
			
			@Override
			default void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			default void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
			
			//これをオーバーロードする
			@Override
			abstract void afterTextChanged(Editable editable);
		}
	}
	
	public static abstract class FragmentBingding<TBinding extends ViewDataBinding> extends Fragment {
		public TBinding binding;
		public Function3<LayoutInflater, ViewGroup, Boolean, TBinding> methodInflate;
		
		Context context;
		Activity activity;
		View viewFragment;
		
		public FragmentBingding(Function3<LayoutInflater, ViewGroup, Boolean, TBinding> methodInflate) {
			this.methodInflate = methodInflate;
		}
		
		@Override
		public final View onCreateView(LayoutInflater inflater, ViewGroup container,
		                               Bundle savedInstanceState) {
			//こう書きたいが、javaではできない？
			//binding= Binding.inflate(inflater, container, false);
			binding = methodInflate.invoke(inflater, container, false);
			binding.setLifecycleOwner(this);
			return binding.getRoot();
		}
		
		final <T extends View> T findViewById(int id) {return viewFragment.findViewById(id);}
		
		//ActivityのonCreateに相当
		@Override
		public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
			try {
				super.onViewCreated(view, savedInstanceState);
				context = getContext();
				activity = getActivity();
				viewFragment = view;
			} catch (Exception e) {
				showException(getContext(), e);
			}
		}
	}
}