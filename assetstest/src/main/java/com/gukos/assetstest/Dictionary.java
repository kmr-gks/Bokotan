package com.gukos.assetstest;


import static com.gukos.assetstest.MyLibrary.ExceptionManager.showException;

import android.app.AlertDialog;
import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Dictionaryクラス
 * すべての単語、文のデータと音声ファイルのパスを提供する
 */
public class Dictionary {
	public static class Entry {
		public String content;
		public Division division;
		public BookName bookName;
		public BookQ bookQ;
		public int numberInBook;
		public Datatype datatype;
		public DataLang dataLang;
		
		public Entry(String content, Division division, BookName bookName, BookQ bookQ, int numberInBook, Datatype datatype, DataLang dataLang) {
			this.content = content;
			this.division = division;
			this.bookName = bookName;
			this.bookQ = bookQ;
			this.numberInBook = numberInBook;
			this.datatype = datatype;
			this.dataLang = dataLang;
		}
	}
	
	/**
	 * Division:大分類 親フォルダ名に対応する
	 */
	public enum Division {
		distinction,
		eigoduke,
		passtan,
		svl,
		tanjukugo,
		yumetan,
	}
	
	/**
	 * BookName:本の名前
	 */
	public enum BookName {
		distinction1,
		distinction2,
		distinction3,
		distinction4,
	}
	
	/**
	 * BookQ:本について、級分けがあればそのデータを記録する
	 */
	public enum BookQ {
		q1,
		none,
	}
	
	/**
	 * Datatype:データの種類 word:単語または熟語 phrase:文
	 */
	public enum Datatype {
		word,
		phrase,
	}
	
	/**
	 * DataLang:データの言語 japanese:日本語 english:英語
	 */
	public enum DataLang {
		japanese,
		english,
		other,
	}
	
	public static ArrayList<Entry> readToList(String fileName, Context context, Division division, BookName bookName
		, BookQ bookQ, Datatype datatype, DataLang dataLang) {
		ArrayList<Entry> list = new ArrayList<>();
		if (dataLang == DataLang.japanese) {
			fileName += ".j.txt";
		}
		else if (dataLang == DataLang.english) {
			fileName += ".e.txt";
		}
		try {
			InputStream is = context.getAssets().open(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String content;
			int i = 0;
			while ((content = br.readLine()) != null) {
				list.add(new Entry(content, division, bookName, bookQ, i, datatype, dataLang));
				i++;
			}
			is.close();
			br.close();
		} catch (Exception e) {
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル" + fileName + "が見つかりません。").setPositiveButton("ok", null).create().show();
		}
		return list;
	}
}