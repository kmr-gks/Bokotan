package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.puts;
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
	//public enum ViewName {Mondaibun,Select1,Select2,all}
	public static final class ViewName{
		public static final int Mondaibun=0,Select1=1,Select2=2;
	}
	private IntentFilter intentFilter;
	
	//https://oc-technote.com/android/service%E3%81%8B%E3%82%89activity%E3%81%AB%E5%80%A4%E3%82%92%E6%8A%95%E3%81%92%E3%81%9F%E3%82%8A%E7%94%BB%E9%9D%A2%E3%82%92%E6%9B%B4%E6%96%B0%E3%81%97%E3%81%9F%E3%82%8A%E3%81%99%E3%82%8B%E6%96%B9/
	private Handler drawHandler=new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			String message = bundle.getString("message");
			int viewName=bundle.getInt("viewName");
			puts("GET   viewname="+viewName+",msg="+message);
			switch (viewName){
				case ViewName.Mondaibun:{
					binding.textViewMondai.setText(message);
					break;
				}
				case ViewName.Select1:{
					binding.buttonSelect1.setText(message);
					break;
				}
				case ViewName.Select2:{
					binding.buttonSelect2.setText(message);
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
			DrawReceiver drawReceiver = new DrawReceiver();
			intentFilter = new IntentFilter();
			intentFilter.addAction("UPDATE_ACTION");
			context.registerReceiver(drawReceiver, intentFilter);
			drawReceiver.registerHandler(drawHandler);
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