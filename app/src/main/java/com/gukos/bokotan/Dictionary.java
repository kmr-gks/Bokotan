package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.QuizData.huseikai;
import static com.gukos.bokotan.Dictionary.QuizData.monme;
import static com.gukos.bokotan.Dictionary.QuizData.seikai;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.fileExtension;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getFileNameForTanjukugoEX;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.strDirectoryNameForKuuhaku;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.stringToIntArray;
import static com.gukos.bokotan.MyLibrary.strGaibuDataDirectory;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Dictionaryクラス
 * すべての単語、文のデータと音声ファイルのパスを提供する
 */
public class Dictionary extends ViewModel {
	public static ArrayList<Entry> allData = new ArrayList<>();
	private static boolean isEmpty = true;
	
	public static void initialize(Context context) {
		if (isEmpty) {
			var entries = new ArrayList<Dictionary.Entry>();
			
			//Distinction
			for (var book : new BookName[]{BookName.distinction1, BookName.distinction2, BookName.distinction3}) {
				entries.addAll(readToList(context, Folder.distinction, book, BookQ.none, Datatype.word
					, DataLang.other).subList(0, 5));
			}
			
			//Eigoduke
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
				entries.addAll(readToList(context, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.english).subList(0, 5));
				entries.addAll(readToList(context, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.japanese).subList(0, 5));
			}
			
			//Eigoduke
			for (var book : new BookName[]{BookName.WordEigoduke_eiken_jukugo, BookName.WordEigoduke_eikenp1_jukugo, BookName.WordEigoduke_Toefl_Chokuzen, BookName.WordEigoduke_Toeic_500ten, BookName.WordEigoduke_Toeic_700ten, BookName.WordEigoduke_Toeic_900ten, BookName.WordEigoduke_Toeic_Chokuzen, BookName.WordEigoduke_Toeic_jukugo}) {
				entries.addAll(readToList(context, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.english).subList(0, 5));
				entries.addAll(readToList(context, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.japanese).subList(0, 5));
			}
			
			//Passtan
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(context, Folder.passtan, BookName.PasstanWordData, q, Datatype.word, lang).subList(0, 5));
				}
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(context, Folder.passtan, BookName.PasstanPhrase, q, Datatype.phrase, lang).subList(0, 5));
				}
			}
			
			//svl
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(context, Folder.svl, BookName.SVL12000, BookQ.none, Datatype.mix, lang).subList(0, 5));
			}
			
			//単熟語ex
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1}) {
				for (var book : new BookName[]{BookName.tanjukugoWord, BookName.tanjukugoPhrase, BookName.tanjukugoExWord}) {
					for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
						entries.addAll(readToList(context, Folder.tanjukugo, book, q, (book == BookName.tanjukugoPhrase ? Datatype.phrase : Datatype.word), lang).subList(0, 5));
					}
				}
			}
			
			//ユメタン単語
			for (var q : new BookQ[]{BookQ.y00, BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanWord, q, Datatype.word, lang).subList(0, 5));
				}
			}
			
			//ユメタン文
			for (var q : new BookQ[]{BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanPhrase, q, Datatype.phrase, lang).subList(0, 5));
				}
			}
			
			//英英英単語
			for (var book : new BookName[]{BookName.ei3_jukugo_shokyu, BookName.ei3_jukugo_chukyu, BookName.ei3_tango_toeic800, BookName.ei3_tango_toeic990, BookName.ei3_tango_shokyu, BookName.ei3_tango_chukyu, BookName.ei3_tango_jokyu, BookName.ei3_tango_chojyokyu}) {
				entries.addAll(readToList(context, Folder.ei3, book, BookQ.none, Datatype.word, DataLang.other).subList(0, 5));
			}
			
			//究極の英単語プレミアム
			for (var book : new BookName[]{BookName.kyukyoku_premium_vol1, BookName.kyukyoku_premium_vol2}) {
				entries.addAll(readToList(context, Folder.eitango_joukyuu, book, BookQ.none, Datatype.mix, DataLang.other).subList(0, 5));
			}
			final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
			for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q"}) {
				var fileName = dnTestActivity + q + "Test";
				var array = stringToIntArray(getStringData(context, fileName, keySeikai, ""));
				if (array == null) array = new int[3000];
				seikai.put(fileName, array);
				array = stringToIntArray(getStringData(context, fileName, keyHuseikai, ""));
				if (array == null) array = new int[3000];
				huseikai.put(fileName, array);
				monme.put(fileName, getIntData(context, fileName, N_GENZAI_NAN_MONME, 1));
			}
			allData = entries;
		}
		isEmpty = false;
	}
	
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
	
	public static String getSampleText(Context context) {
		var entries = new ArrayList<Dictionary.Entry>();
		
		//Distinction
		for (var book : new BookName[]{BookName.distinction1, BookName.distinction2, BookName.distinction3}) {
			entries.addAll(readToList(context, Folder.distinction, book, BookQ.none, Datatype.word
				, DataLang.other).subList(0, 5));
		}
		
		//Eigoduke
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
			entries.addAll(readToList(context, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(context, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		//Eigoduke
		for (var book : new BookName[]{BookName.WordEigoduke_eiken_jukugo, BookName.WordEigoduke_eikenp1_jukugo, BookName.WordEigoduke_Toefl_Chokuzen, BookName.WordEigoduke_Toeic_500ten, BookName.WordEigoduke_Toeic_700ten, BookName.WordEigoduke_Toeic_900ten, BookName.WordEigoduke_Toeic_Chokuzen, BookName.WordEigoduke_Toeic_jukugo}) {
			entries.addAll(readToList(context, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(context, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		//Passtan
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(context, Folder.passtan, BookName.PasstanWordData, q, Datatype.word, lang).subList(0, 5));
			}
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(context, Folder.passtan, BookName.PasstanPhrase, q, Datatype.phrase, lang).subList(0, 5));
			}
		}
		
		//svl
		for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
			entries.addAll(readToList(context, Folder.svl, BookName.SVL12000, BookQ.none, Datatype.mix, lang).subList(0, 5));
		}
		
		//単熟語ex
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1}) {
			for (var book : new BookName[]{BookName.tanjukugoWord, BookName.tanjukugoPhrase, BookName.tanjukugoExWord}) {
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(context, Folder.tanjukugo, book, q, (book == BookName.tanjukugoPhrase ? Datatype.phrase : Datatype.word), lang).subList(0, 5));
				}
			}
		}
		
		//ユメタン単語
		for (var q : new BookQ[]{BookQ.y00, BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanWord, q, Datatype.word, lang).subList(0, 5));
			}
		}
		
		//ユメタン文
		for (var q : new BookQ[]{BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanPhrase, q, Datatype.phrase, lang).subList(0, 5));
			}
		}
		
		//英英英単語
		for (var book : new BookName[]{BookName.ei3_jukugo_shokyu, BookName.ei3_jukugo_chukyu, BookName.ei3_tango_toeic800, BookName.ei3_tango_toeic990, BookName.ei3_tango_shokyu, BookName.ei3_tango_chukyu, BookName.ei3_tango_jokyu, BookName.ei3_tango_chojyokyu}) {
			entries.addAll(readToList(context, Folder.ei3, book, BookQ.none, Datatype.word, DataLang.other).subList(0, 5));
		}
		
		//究極の英単語プレミアム
		for (var book : new BookName[]{BookName.kyukyoku_premium_vol1, BookName.kyukyoku_premium_vol2}) {
			entries.addAll(readToList(context, Folder.eitango_joukyuu, book, BookQ.none, Datatype.mix, DataLang.other).subList(0, 5));
		}
		
		StringBuilder content = new StringBuilder();
		for (var entry : entries) content.append(entry).append("\n");
		
		return content.toString();
	}
	
	//削除時
	@Override
	protected void onCleared() {
		super.onCleared();
		printCurrentState("VIewModelが削除されます。");
		isEmpty = true;
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
		tanjukugo,
		tanjukugoEx,
		all;
		
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
		all,
		
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
		
		public static BookQ parse(@Nullable String value) {
			if (value == null) return null;
			switch (value) {
				case "1q": {
					return q1;
				}
				case "p1q": {
					return qp1;
				}
				case "2q": {
					return q2;
				}
				case "p2q": {
					return qp2;
				}
				case "3q": {
					return q3;
				}
				case "4q": {
					return q4;
				}
				case "5q": {
					return q5;
				}
				case "00": {
					return y00;
				}
				case "08": {
					return y08;
				}
				case "1": {
					return y1;
				}
				case "2": {
					return y2;
				}
				case "3": {
					return y3;
				}
				default: {
					return null;
				}
			}
		}
		
		@NonNull
		public String toJapanString() {
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
		
		@NonNull
		public String toString() {
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
				default:
					return super.toString();
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
		static int size = 0;
		public String content;
		public Folder folder;
		public BookName bookName;
		public BookQ bookQ;
		public int numberInBook, toushiNumber;
		public Datatype datatype;
		public DataLang dataLang;
		
		public Entry(String content, Folder folder, BookName bookName, BookQ bookQ, int numberInBook, Datatype datatype, DataLang dataLang) {
			size++;
			this.content = content;
			this.folder = folder;
			this.bookName = bookName;
			this.bookQ = bookQ;
			this.numberInBook = numberInBook;
			this.toushiNumber = size;
			this.datatype = datatype;
			this.dataLang = dataLang;
		}
		
		@NonNull
		@Override
		public String toString() {
			return folder + "/" + bookName + " " + bookQ + " " + numberInBook + " " + datatype + " " + dataLang + " " + content;
		}
		
		public String[] getAllFieldString() {
			return new String[]{
				this.content,
				this.folder.toString(),
				this.bookName.toString(),
				this.bookQ.toJapanString(),
				//String.valueOf(this.numberInBook),
				this.datatype.toString(),
				this.dataLang.toString(),
			};
		}
		
		public String toDetailedString() {
			try {
				final String rate;
				String fileName = dnTestActivity + bookQ.toFileName() + "Test";
				if (QuizData.seikai.get(fileName) != null) {
					int correct = QuizData.seikai.get(fileName)[numberInBook];
					int incorrect = QuizData.huseikai.get(fileName)[numberInBook];
					rate = "\n正解率 " + correct + "/" + (correct + incorrect);
				}
				else rate = "";
				
				return "No. " + this.toushiNumber
					+ "\nカテゴリ: " + this.folder.toString() + " " + this.bookName.toString()
					+ "\n番号:" + this.numberInBook
					+ rate
					+ "\n" + this.content
					+ "\n発音:" + getHatsuon(this.content)
					+ "\nDataBook:" + this.bookName
					+ "\nDataQ:" + this.bookQ
					+ "\nMode:" + this.dataLang
					+ GogenYomuFactory.getGogenString(this.content, true, true);
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}
		
		public String toPath() {
			return toPath(dataLang);
		}
		
		public String toPath(DataLang dataLang) {
			try {
				String str = bookQ.toFileName();
				if (folder == Folder.tanjukugo)
					str = "tanjukugo" + str;
				String strDataQ = str;
				String path = strGaibuDataDirectory;
				String type;
				switch (folder) {
					case passtan: {
						if (datatype == Datatype.word) {
							if (dataLang == DataLang.english)
								type = "英";
							else
								type = "訳";
						}
						else {
							if (dataLang == DataLang.english)
								type = "例";
							else
								type = "日";
						}
						strDataQ = strDirectoryNameForKuuhaku + strDataQ;
						path += strDataQ + String.format("/%04d", numberInBook) + type + fileExtension;
						break;
					}
					case yumetan: {
						if (datatype == Datatype.word) {
							if (dataLang == DataLang.english)
								type = "W英";
							else
								type = "W日";
						}
						else {
							if (dataLang == DataLang.english)
								type = "P英";
							else
								type = "P日";
						}
						strDataQ = strDirectoryNameForKuuhaku + "y" + strDataQ;
						path += strDataQ + "/" + type + String.format("%04d", numberInBook) + fileExtension;
						break;
					}
					case tanjukugo: {
						if (datatype == Datatype.word) {
							if (dataLang == DataLang.english)
								type = "英語";
							else
								type = "日本語";
						}
						else {
							if (dataLang == DataLang.english)
								type = "例文";
							else
								type = "例文日本語";
						}
						path += strDataQ + "/" + getFileNameForTanjukugoEX(type, strDataQ, numberInBook) + fileExtension;
						break;
					}
					default:
						return null;
				}
				return path;
			} catch (Exception e) {
				MyLibrary.ExceptionManager.showException(e);
				return "<不明>";
			}
		}
	}
	
	public static class QuizData {
		
		public static final TreeMap<String, int[]> seikai = new TreeMap<>(), huseikai =
			new TreeMap<>();
		public static final TreeMap<String, Integer> monme = new TreeMap<>();
		public static final TreeMap<String, ArrayList<WordPhraseData.WordInfo>> map = new TreeMap<>();
		//TODO:このクラスのメンバはstaticじゃないほうがいい
		public static final ArrayList<WordPhraseData.WordInfo> allData = new ArrayList<>();
		public final static String
			PasstanWord = "Passtan/WordData", PasstanPhrase = "Passtan/Phrase", TanjukugoWord = "TanjukugoEX/Word", TanjukugoEXWord = "TanjukugoEX/EXWord", TanjukugoPhrase = "TanjukugoEX/Phrase", YumeWord = "Yumetan/WordDataYume", Svl = "SVL/SVL12000", distinction = "distinction/";
		static skipjouken skipjoken = skipjouken.kirokunomi;
		
		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}
	}
}