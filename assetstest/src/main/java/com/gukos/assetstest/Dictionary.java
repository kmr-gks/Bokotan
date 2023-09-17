package com.gukos.assetstest;


import static com.gukos.assetstest.MyLibrary.ExceptionManager.showException;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Dictionaryクラス
 * すべての単語、文のデータと音声ファイルのパスを提供する
 */
public class Dictionary {
	public static ArrayList<Entry> readToList(Context context, Folder folder, BookName bookName, BookQ bookQ, Datatype datatype, DataLang dataLang) {
		final String dictionaryPath = "dictionaries/";
		String fileName =
			dictionaryPath + folder.toDirName() + "/" + bookName.toFileName() + bookQ.toFileName();
		ArrayList<Entry> list = new ArrayList<>();
		if (dataLang == DataLang.japanese) {
			fileName += ".j.txt";
		}
		else if (dataLang == DataLang.english) {
			fileName += ".e.txt";
		}
		MyLibrary.DebugManager.printCurrentState(fileName);
		try {
			InputStream is = context.getAssets().open(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String content;
			int i = 0;
			while ((content = br.readLine()) != null) {
				list.add(new Entry(content, folder, bookName, bookQ, i, datatype, dataLang));
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
	
	/**
	 * Folder:大分類 親フォルダ名に対応する
	 */
	public enum Folder {
		distinction,
		eigoduke,
		yumetan,
		ei3,
		eitango_joukyuu,
		passtan,
		svl,
		tanjukugo;
		
		public String toDirName() {
			switch (this) {
				case distinction:
					return "Distinction";
				case eigoduke:
					return "Eigoduke.com";
				case ei3:
					return "英英英単語";
				case eitango_joukyuu:
					return "英単語上級";
				case passtan:
					return "Passtan";
				case svl:
					return "SVL";
				case tanjukugo:
					return "Tanjukugo";
				case yumetan:
					return "Yumetan";
				default:
					return "";
			}
		}
	}
	
	/**
	 * BookName:本の名前
	 */
	public enum BookName {
		distinction1,
		distinction2,
		distinction3,
		distinction4,
		WordEigoduke,
		WordEigoduke_eiken_jukugo,
		WordEigoduke_eikenp1_jukugo,
		WordEigoduke_Toefl_Chokuzen,
		WordEigoduke_Toeic_500ten,
		WordEigoduke_Toeic_700ten,
		WordEigoduke_Toeic_900ten,
		WordEigoduke_Toeic_Chokuzen,
		WordEigoduke_Toeic_jukugo,
		PasstanWordData,
		PasstanPhrase,
		SVL12000,
		tanjukugoWord,
		tanjukugoPhrase,
		tanjukugoExWord,
		yumetanWord,
		yumetanPhrase,
		ei3_jukugo_shokyu,
		ei3_jukugo_chukyu,
		ei3_tango_toeic800,
		ei3_tango_toeic990,
		ei3_tango_shokyu,
		ei3_tango_chukyu,
		ei3_tango_jokyu,
		ei3_tango_chojyokyu,
		kyukyoku_premium_vol1,
		kyukyoku_premium_vol2,
		;
		
		public String toFileName() {
			switch (this) {
				case distinction1:
					return "distinction1.txt";
				case distinction2:
					return "distinction2.txt";
				case distinction3:
					return "distinction3.txt";
				case distinction4:
					return "distinction4.txt";
				case WordEigoduke:
					return "WordEigoduke";
				case WordEigoduke_eiken_jukugo:
					return "WordEigoduke-eiken-jukugo";
				case WordEigoduke_eikenp1_jukugo:
					return "WordEigoduke-eikenp1-jukugo";
				case WordEigoduke_Toefl_Chokuzen:
					return "WordEigoduke-Toefl-Chokuzen";
				case WordEigoduke_Toeic_500ten:
					return "WordEigoduke-Toeic-500ten";
				case WordEigoduke_Toeic_700ten:
					return "WordEigoduke-Toeic-700ten";
				case WordEigoduke_Toeic_900ten:
					return "WordEigoduke-Toeic-900ten";
				case WordEigoduke_Toeic_Chokuzen:
					return "WordEigoduke-Toeic-Chokuzen";
				case WordEigoduke_Toeic_jukugo:
					return "WordEigoduke-Toeic-jukugo";
				case PasstanWordData:
					return "WordData";
				case PasstanPhrase:
					return "Phrase";
				case SVL12000:
					return "SVL12000";
				case tanjukugoWord:
					return "Word";
				case tanjukugoPhrase:
					return "Phrase";
				case tanjukugoExWord:
					return "EXWord";
				case yumetanWord:
					return "WordDataYume";
				case yumetanPhrase:
					return "PhraseDataYume";
				case ei3_jukugo_shokyu:
					return "英英英熟語初級編.txt";
				case ei3_jukugo_chukyu:
					return "英英英熟語中級編.txt";
				case ei3_tango_toeic800:
					return "英英英単語TOEICスコア800.txt";
				case ei3_tango_toeic990:
					return "英英英単語TOEICスコア990.txt";
				case ei3_tango_shokyu:
					return "英英英単語初級編.txt";
				case ei3_tango_chukyu:
					return "英英英単語中級編.txt";
				case ei3_tango_jokyu:
					return "英英英単語上級編.txt";
				case ei3_tango_chojyokyu:
					return "英英英単語超上級編.txt";
				case kyukyoku_premium_vol1:
					return "究極の英単語プレミアムVol1.txt";
				case kyukyoku_premium_vol2:
					return "究極の英単語プレミアムVol.2_EJ（英日）.txt";
				default:
					return "";
			}
		}
	}
	
	/**
	 * BookQ:本について、級分けがあればそのデータを記録する
	 */
	public enum BookQ {
		q1,
		qp1,
		q2,
		qp2,
		q3,
		q4,
		q5,
		y00,
		y08,
		y1,
		y2,
		y3,
		
		none;
		
		public String toFileName() {
			switch (this) {
				case q1:
					return "1q";
				case qp1:
					return "p1q";
				case q2:
					return "2q";
				case qp2:
					return "p2q";
				case q3:
					return "3q";
				case q4:
					return "4q";
				case q5:
					return "5q";
				case y00:
					return "00";
				case y08:
					return "08";
				case y1:
					return "1";
				case y2:
					return "2";
				case y3:
					return "3";
				default:
					return "";
			}
		}
		
		@NonNull
		@Override
		public String toString() {
			switch (this) {
				case q1:
					return "1級";
				case qp1:
					return "準1級";
				case q2:
					return "2級";
				case qp2:
					return "準2級";
				case q3:
					return "3級";
				case q4:
					return "4級";
				case q5:
					return "5級";
				case y00:
					return "00";
				case y08:
					return "08";
				case y1:
					return "1";
				case y2:
					return "2";
				case y3:
					return "3";
				case none:
					return "級なし";
				default:
					return "";
			}
		}
	}
	
	/**
	 * Datatype:データの種類 word:単語または熟語 phrase:文
	 */
	public enum Datatype {
		word,
		phrase,
		mix;
		
		public String toFileName() {
			switch (this) {
				case word:
					return "Word";
				case phrase:
					return "Phrase";
				default:
					return "";
			}
		}
		
		@NonNull
		@Override
		public String toString() {
			switch (this) {
				case word:
					return "単語";
				case phrase:
					return "文";
				case mix:
					return "混合";
				default:
					return "";
			}
		}
	}
	
	/**
	 * DataLang:データの言語 japanese:日本語 english:英語
	 */
	public enum DataLang {
		japanese,
		english,
		other;
		
		@NonNull
		@Override
		public String toString() {
			switch (this) {
				case japanese:
					return "日本語";
				case english:
					return "英語";
				case other:
					return "その他";
				default:
					return "";
			}
		}
	}
	
	public static class Entry {
		public String content;
		public Folder folder;
		public BookName bookName;
		public BookQ bookQ;
		public int numberInBook;
		public Datatype datatype;
		public DataLang dataLang;
		
		public Entry(String content, Folder folder, BookName bookName, BookQ bookQ, int numberInBook, Datatype datatype, DataLang dataLang) {
			this.content = content;
			this.folder = folder;
			this.bookName = bookName;
			this.bookQ = bookQ;
			this.numberInBook = numberInBook;
			this.datatype = datatype;
			this.dataLang = dataLang;
		}
		
		@NonNull
		@Override
		public String toString() {
			return folder + "/" + bookName + " " + bookQ + " " + numberInBook + " " + datatype + " " + dataLang + " " + content;
		}
	}
}