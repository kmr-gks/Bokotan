package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DisplayOutput.getClassName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.getMethodName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.puts;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {
	
	public static final String[] TAB_NAMES = new String[]{"設定", "級選択", "再生画面", "全文検索"};
	private static Fragment fragment = null;
	private static SettingFragment settingFragment;
	private static QSentakuFragment qSentakuFragment;
	private static PlayerFragment playerFragment;
	private static KensakuFragment kensakuFragment;
	
	public TabPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		settingFragment = new SettingFragment();
		qSentakuFragment = new QSentakuFragment();
		playerFragment = new PlayerFragment();
		kensakuFragment = new KensakuFragment();
	}
	
	@NonNull
	@Override
	public Fragment createFragment(int position) {
		puts(getClassName() + getMethodName() + ",pos=" + position);
		switch (position) {
			case 0: {
				fragment = settingFragment;
				break;
			}
			case 1: {
				fragment = qSentakuFragment;
				break;
			}
			case 2: {
				fragment = playerFragment;
				break;
			}
			case 3: {
				fragment = kensakuFragment;
				break;
			}
		}
		return fragment;
	}
	
	@Override
	public int getItemCount() {
		return TAB_NAMES.length;
	}
}