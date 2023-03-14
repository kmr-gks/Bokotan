package com.gukos.bokotan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {
	public static final String[] TAB_NAMES = new String[]{"級選択", "再生画面", "テスト", "全文検索"};
	
	public TabPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
	}
	
	@NonNull
	@Override
	public Fragment createFragment(int position) {
		final Fragment fragment;
		switch (position) {
			default:
			case 0: {
				fragment = new QSentakuFragment();
				break;
			}
			case 1: {
				fragment = new PlayerFragment();
				break;
			}
			case 2: {
				fragment = new TestFragment();
				break;
			}
			case 3: {
				fragment = new KensakuFragment();
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