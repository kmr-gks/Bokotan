package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DisplayOutput.getClassName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.getMethodName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.puts;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.gukos.bokotan.databinding.ActivityTabBinding;

public class TabActivity extends AppCompatActivity {
	public static ActivityTabBinding binding;
	private String toString(Bundle bundle){
		if (bundle==null) return "null";
		else return bundle.toString();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab);
		
		//StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog
		// ().build());
		
		puts(getClassName()+getMethodName()+"bundle="+toString(savedInstanceState));
		
		binding = ActivityTabBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(this);
		ViewPager2 viewPager = binding.viewpagerMain;
		viewPager.setAdapter(tabPagerAdapter);
		
		new TabLayoutMediator(binding.tabsMain, binding.viewpagerMain,
		                      (tab, position) -> {
								  puts("tab="+tab.toString()+"pos="+position);
			                      tab.setText(TabPagerAdapter.TAB_NAMES[position]);
		                      }).attach();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		puts(getClassName()+getMethodName());
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		puts(getClassName()+getMethodName());
	}
}