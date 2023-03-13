package com.gukos.bokotan;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.gukos.bokotan.databinding.ActivityTabBinding;

public class TabActivity extends AppCompatActivity {
	private static ActivityTabBinding binding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
		
		new Thread(() -> loadAllData(savedInstanceState)).start();
		
		setVolumeControlStream(STREAM_MUSIC);
		
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
	
	//bundleからデータ取り出し
	private void loadAllData(Bundle bundle) {
		try {
			printCurrentState("読み込み開始" + bundle);
			var data=WordPhraseData.builder(this);
			printCurrentState("読み込み完了");
		} catch (Exception exception) {
			showException(this, exception);
		}
	}
	
	//onCreateの方が先に呼ばれる
	//protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {}
	
	//データ保存
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		try {
			super.onSaveInstanceState(outState);
			printCurrentState("データ保存中");
		} catch (Exception exception) {
			showException(this, exception);
		}
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
			showException(exception);
		}
	}
	
	public static int getTabPageNum() {
		try {
			return TabActivity.binding.tabsMain.getSelectedTabPosition();
		} catch (Exception exception) {
			showException(exception);
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