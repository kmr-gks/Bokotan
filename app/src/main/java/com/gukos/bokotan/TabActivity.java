package com.gukos.bokotan;

import static android.media.AudioManager.STREAM_MUSIC;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_ACTION;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_STOP;
import static com.gukos.bokotan.PlayerService.PLAYERSERVICE_MESSAGE_TYPE;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
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
		
		var backPressedCallback = new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				switch (getTabPageNum()) {
					default:
					case 0: {
						finish();
						break;
					}
					case 1: {
						runOnUiThread(() -> setTabPageNum(0));
						//再生中に戻るボタンを押すと停止
						getApplicationContext().sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP));
						break;
					}
					case 2: {
						runOnUiThread(() -> setTabPageNum(0));
						//クイズをしているなら、ViewModelのデータを保存する。
						new Thread(() -> Dictionary.QuizData.saveQuizData(getApplicationContext())).start();
						getApplicationContext().sendBroadcast(new Intent(QuizCreator.QTHREAD_ACTION_CLICKED).putExtra(QuizCreator.QTHREAD_EXTRA_STOP, 0));
						break;
					}
					case 3: {
						runOnUiThread(() -> setTabPageNum(0));
						break;
					}
				}
			}
		};
		getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
	}
	
	//bundleからデータ取り出し
	private void loadAllData(Bundle bundle) {
		try {
			printCurrentState("読み込み開始" + bundle);
			//単語、文のデータはViewModelから取ってくる。
			//ViewModelが空のときはファイルから読み込む。
			var wpd = WordPhraseData.loadAssets(this);
			Dictionary.initialize(this);
			//正解数、不正解数のデータもViewModelから取ってくる。
			//ViewModelが空のときはSharedPreferenceから読み込む。
			//クイズを終了(戻るボタン)したときにViewModelのデータをSharedPreferenceに書き込む。
			printCurrentState("読み込み完了");
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
	
	/*
	//ボタン操作を検出したい。
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		puts("keyCode="+keyCode+"event="+event.toString());
		return super.onKeyDown(keyCode,event);
	}
	*/
}