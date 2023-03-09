package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.getClassName;
import static com.gukos.bokotan.MyLibrary.DebugManager.getMethodName;
import static com.gukos.bokotan.MyLibrary.DebugManager.puts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.gukos.bokotan.databinding.ActivityTabBinding;

public class TabActivity extends AppCompatActivity {
	private static ActivityTabBinding binding;
	
	private String toString(Bundle bundle) {
		if (bundle == null) return "null";
		else return bundle.toString();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
		
		puts(getClassName() + getMethodName() + ",bundle=" + toString(savedInstanceState));
		
		binding = DataBindingUtil.setContentView(this, R.layout.activity_tab);
		
		TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(this);
		ViewPager2 viewPager = binding.viewpagerMain;
		viewPager.setAdapter(tabPagerAdapter);
		//フラグメント作成できる？
		//tabPagerAdapter.createFragment()
		//初期位置
		viewPager.setCurrentItem(0);
		
		new TabLayoutMediator(binding.tabsMain, binding.viewpagerMain, (tab, position) -> tab.setText(TabPagerAdapter.TAB_NAMES[position])).attach();
	}
	
	/**
	 * UIスレッドから呼ばなければならない
	 *
	 * @param n タブの番号(左から0から始まる)
	 */
	public static void setTabPageNum(int n) {
		try {
			TabActivity.binding.tabsMain.getTabAt(n).select();
		} catch (Exception exception) {
			MyLibrary.ExceptionManager.showException(exception);
		}
	}
	
	public static int getTabPageNum() {
		try {
			return TabActivity.binding.tabsMain.getSelectedTabPosition();
		} catch (Exception exception) {
			MyLibrary.ExceptionManager.showException(exception);
			return 0;
		}
	}
	
	@Override
	public void onBackPressed() {
		//戻るボタン
		if (getTabPageNum() != 0) runOnUiThread(() -> setTabPageNum(0));
		else super.onBackPressed();
	}
	
	/*
	//ボタン操作を検出したい。
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		puts("keyCode="+keyCode+"event="+event.toString());
		return super.onKeyDown(keyCode,event);
	}
	*/
}