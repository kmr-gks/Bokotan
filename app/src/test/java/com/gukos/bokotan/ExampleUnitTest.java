package com.gukos.bokotan;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() {
		assertEquals(4, 2 + 2);
	}
	
	@Test
	public void aaa(){
		ArrayList<Integer> list1=new ArrayList<>(4);
		ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
		assertEquals(4,list1.size());
		assertEquals(4,list2.size());
		assertEquals(null,new String());
	}
}