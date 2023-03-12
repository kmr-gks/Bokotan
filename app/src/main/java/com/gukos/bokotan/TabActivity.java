package com.gukos.bokotan;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
		
		printCurrentState();
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
	private void loadAllData(Bundle bundle){
		printCurrentState("読み込み開始");
		WordPhraseData.readAllData(this);
		printCurrentState("読み込み完了");
	}
	
	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onRestoreInstanceState(savedInstanceState, persistentState);
	}
	
	//データ保存
	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		printCurrentState();
	}
	
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
		super.onSaveInstanceState(outState, outPersistentState);
		printCurrentState();
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