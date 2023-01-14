package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DisplayOutput.getClassName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.getMethodName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.puts;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.putsE;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TabPagerAdapter extends FragmentStateAdapter {
	
	public static final String[] TAB_NAMES = new String[]{"級選択","再生画面","全文検索"};
	public static Fragment fragment=null;
	public static QSentakuFragment qSentakuFragment;
	public static PlayerFragment playerFragment;
	public static KensakuFragment kensakuFragment;
	
	public TabPagerAdapter(FragmentActivity fragmentActivity) {
		super(fragmentActivity);
		puts(getClassName()+getMethodName());
		qSentakuFragment = new QSentakuFragment();
		playerFragment = new PlayerFragment();
		kensakuFragment = new KensakuFragment();
		putsE("playerFragment="+playerFragment+",activity="+playerFragment.getActivity());
	}
	
	@NonNull
	@Override
	public Fragment createFragment(int position) {
		puts(getClassName()+getMethodName()+",pos="+position);
		switch (position) {
			case 0: {
				fragment = qSentakuFragment;
				break;
			}
			case 1: {
				fragment = playerFragment;
				break;
			}
			case 2: {
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