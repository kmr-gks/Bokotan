package com.gukos.bokotan;

import static android.media.AudioManager.STREAM_MUSIC;
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
	private ActivityTabBinding binding;
	private static final java.lang.ref.WeakReference<TabActivity> EMPTY = new java.lang.ref.WeakReference<>(null);
	private static java.lang.ref.WeakReference<TabActivity> current = EMPTY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());

		current = new java.lang.ref.WeakReference<>(this);
		Dictionary.whenReady(getApplicationContext(), () -> {});

		setVolumeControlStream(STREAM_MUSIC);

		binding = DataBindingUtil.setContentView(this, R.layout.activity_tab);
		androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
			var bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()
					| androidx.core.view.WindowInsetsCompat.Type.displayCutout());
			view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
			return insets;
		});
		androidx.core.view.ViewCompat.requestApplyInsets(binding.getRoot());

		TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(this);
		ViewPager2 viewPager = binding.viewpagerMain;
		viewPager.setAdapter(tabPagerAdapter);
		//フラグメント作成できる？
		//tabPagerAdapter.createFragment()
		//初期位置
		viewPager.setCurrentItem(savedInstanceState == null ? 0 : savedInstanceState.getInt("selectedTab", 0));

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
						getApplicationContext().sendBroadcast(new Intent(PLAYERSERVICE_ACTION).putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP).setPackage(getApplicationContext().getPackageName()));
						break;
					}
					case 2: {
						runOnUiThread(() -> setTabPageNum(0));
						//クイズをしているなら、ViewModelのデータを保存する。
						QuizCreator.stopCurrent();
						getApplicationContext().sendBroadcast(new Intent(QuizCreator.QTHREAD_ACTION_CLICKED).putExtra(QuizCreator.QTHREAD_EXTRA_STOP, 0).setPackage(getApplicationContext().getPackageName()));
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

	@Override
	protected void onSaveInstanceState(Bundle state) {
		state.putInt("selectedTab", getTabPageNum());
		super.onSaveInstanceState(state);
	}

	@Override
	protected void onDestroy() {
		if (isFinishing()) QuizCreator.stopCurrent();
		if (current.get() == this) current = EMPTY;
		binding = null;
		super.onDestroy();
	}

	/**
	 * UIスレッドから呼ばなければならない
	 *
	 * @param n タブの番号(左から0から始まる)
	 */
	public static void setTabPageNum(int n) {
		try {
			TabActivity activity = current.get();
			if (activity != null && activity.binding != null) activity.binding.viewpagerMain.setCurrentItem(n);
		} catch (Exception exception) {
			showException(exception);
		}
	}

	public static int getTabPageNum() {
		try {
			TabActivity activity = current.get();
			return activity == null || activity.binding == null ? 0 : activity.binding.tabsMain.getSelectedTabPosition();
		} catch (Exception exception) {
			showException(exception);
			return 0;
		}
	}
}
