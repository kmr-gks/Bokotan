package com.gukos.bokotan;

import com.gukos.bokotan.databinding.FragmentTestBinding;

public class TestFragment extends UiManager.FragmentBingding<FragmentTestBinding> {
	
	public TestFragment() {
		super(FragmentTestBinding::inflate);
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