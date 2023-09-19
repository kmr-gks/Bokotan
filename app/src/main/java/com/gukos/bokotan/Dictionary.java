package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.Dictionary.HatsuonKigou.getHatsuon;
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
import static com.gukos.bokotan.MyLibrary.PreferenceManager.intArrayToString;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.stringToIntArray;
import static com.gukos.bokotan.MyLibrary.strGaibuDataDirectory;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Dictionaryクラス
 * すべての単語、文のデータと音声ファイルのパスを提供する
 */
public class Dictionary extends ViewModel {
	public static ArrayList<Entry> allData = new ArrayList<>();
	private static boolean isEmpty = true;
	public static TreeMap<String, ArrayList<Entry>> listsPerBook = new TreeMap<>();
	
	/**
	 * 言語を指定してファイルからデータを読み込む
	 *
	 * @param context
	 * @param folder
	 * @param bookName
	 * @param bookQ
	 * @param datatype
	 * @param dataLang 英語または日本語を指定する
	 * @return
	 */
	public static ArrayList<Entry> readToList(Context context, Folder folder, BookName bookName, BookQ bookQ, Datatype datatype, DataLang dataLang) {
		final var dictionaryPath = "dictionaries/";
		var fileName = dictionaryPath + folder.toDirName() + "/" + bookName.toFileName() + bookQ.toFileName();
		var list = new ArrayList<Entry>();
		if (dataLang == DataLang.japanese) {
			fileName += ".j.txt";
		}
		else if (dataLang == DataLang.english) {
			fileName += ".e.txt";
		}
		try {
			var is = context.getAssets().open(fileName);
			var br = new BufferedReader(new InputStreamReader(is));
			String content;
			var i = 0;
			//ユメタンフレーズのときは最初の空白行が含まれていないため手動で追加する
			if (bookName == BookName.yumetanPhrase) {
				list.add(new Entry("null string", folder, bookName, bookQ, i, datatype, dataLang));
				i++;
			}
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
	 * 日本語、英語のデータを同時に読み込む。
	 *
	 * @param context
	 * @param folder
	 * @param bookName
	 * @param bookQ
	 * @param datatype
	 * @return
	 */
	public static ArrayList<Entry> readToList(Context context, Folder folder, BookName bookName, BookQ bookQ, Datatype datatype) {
		final var dictionaryPath = "dictionaries/";
		var fileNameE = dictionaryPath + folder.toDirName() + "/" + bookName.toFileName() + bookQ.toFileName() + ".e.txt";
		var fileNameJ = dictionaryPath + folder.toDirName() + "/" + bookName.toFileName() + bookQ.toFileName() + ".j.txt";
		var list = new ArrayList<Entry>();
		try {
			var isE = context.getAssets().open(fileNameE);
			var isJ = context.getAssets().open(fileNameJ);
			var brE = new BufferedReader(new InputStreamReader(isE));
			var brJ = new BufferedReader(new InputStreamReader(isJ));
			String contentE, contentJ;
			var i = 0;
			//ユメタンフレーズのときは最初の空白行が含まれていないため手動で追加する
			if (bookName == BookName.yumetanPhrase) {
				list.add(new Entry("null string", "null string", folder, bookName, bookQ, i, datatype));
				i++;
			}
			while ((contentE = brE.readLine()) != null && (contentJ = brJ.readLine()) != null) {
				list.add(new Entry(contentE, contentJ, folder, bookName, bookQ, i, datatype));
				i++;
			}
			isE.close();
			isJ.close();
			brE.close();
			brJ.close();
		} catch (Exception e) {
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル" + fileNameE + "または" + fileNameJ + "が見つかりません。").setPositiveButton("ok", null).create().show();
		}
		return list;
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
	
	//assetsフォルダーからデータを読み込む。ViewModelによりデータが保持されている場合は何もしない。
	public static Dictionary initialize(Context context) {
		if (isEmpty) {
			var entries = new ArrayList<Dictionary.Entry>();
			
			//Distinction
			for (var book : new BookName[]{BookName.distinction1, BookName.distinction2, BookName.distinction3}) {
				entries.addAll(readToList(context, Folder.distinction, book, BookQ.none, Datatype.word, DataLang.other));
			}
			
			//Eigoduke
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
				entries.addAll(readToList(context, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word));
			}
			
			//Eigoduke
			for (var book : new BookName[]{BookName.WordEigoduke_eiken_jukugo, BookName.WordEigoduke_eikenp1_jukugo, BookName.WordEigoduke_Toefl_Chokuzen, BookName.WordEigoduke_Toeic_500ten, BookName.WordEigoduke_Toeic_700ten, BookName.WordEigoduke_Toeic_900ten, BookName.WordEigoduke_Toeic_Chokuzen, BookName.WordEigoduke_Toeic_jukugo}) {
				entries.addAll(readToList(context, Folder.eigoduke, book, BookQ.none, Datatype.word));
			}
			
			//Passtan
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
				entries.addAll(readToList(context, Folder.passtan, BookName.PasstanWordData, q, Datatype.word));
				entries.addAll(readToList(context, Folder.passtan, BookName.PasstanPhrase, q, Datatype.phrase));
				addList(BookName.PasstanWordData, q, readToList(context, Folder.passtan, BookName.PasstanWordData, q, Datatype.word));
				addList(BookName.PasstanPhrase, q, readToList(context, Folder.passtan, BookName.PasstanPhrase, q, Datatype.phrase));
			}
			
			//svl
			entries.addAll(readToList(context, Folder.svl, BookName.SVL12000, BookQ.none, Datatype.mix));
			SetHatsuonKigou(readToList(context, Folder.svl, BookName.SVL12000, BookQ.none, Datatype.mix));
			
			//単熟語ex
			for (var q : new BookQ[]{BookQ.q1, BookQ.qp1}) {
				for (var book : new BookName[]{BookName.tanjukugoWord, BookName.tanjukugoPhrase, BookName.tanjukugoExWord}) {
					entries.addAll(readToList(context, Folder.tanjukugo, book, q, (book == BookName.tanjukugoPhrase ? Datatype.phrase : Datatype.word)));
					addList(book, q, readToList(context, Folder.tanjukugo, book, q, (book == BookName.tanjukugoPhrase ? Datatype.phrase : Datatype.word)));
				}
			}
			
			//ユメタン単語
			for (var q : new BookQ[]{BookQ.y00, BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
				entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanWord, q, Datatype.word));
				addList(BookName.yumetanWord, q, readToList(context, Folder.yumetan, BookName.yumetanWord, q, Datatype.word));
			}
			
			//ユメタン文
			for (var q : new BookQ[]{BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
				entries.addAll(readToList(context, Folder.yumetan, BookName.yumetanPhrase, q, Datatype.phrase));
				addList(BookName.yumetanPhrase, q, readToList(context, Folder.yumetan, BookName.yumetanPhrase, q, Datatype.phrase));
			}
			
			//英英英単語
			for (var book : new BookName[]{BookName.ei3_jukugo_shokyu, BookName.ei3_jukugo_chukyu, BookName.ei3_tango_toeic800, BookName.ei3_tango_toeic990, BookName.ei3_tango_shokyu, BookName.ei3_tango_chukyu, BookName.ei3_tango_jokyu, BookName.ei3_tango_chojyokyu}) {
				entries.addAll(readToList(context, Folder.ei3, book, BookQ.none, Datatype.word, DataLang.other));
			}
			
			//究極の英単語プレミアム
			for (var book : new BookName[]{BookName.kyukyoku_premium_vol1, BookName.kyukyoku_premium_vol2}) {
				entries.addAll(readToList(context, Folder.eitango_joukyuu, book, BookQ.none, Datatype.mix, DataLang.other));
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
			isEmpty = false;
		}
		return new ViewModelProvider((ViewModelStoreOwner) context).get(Dictionary.class);
	}
	
	private static void addList(BookName bookName, BookQ bookQ, ArrayList<Entry> list) {
		listsPerBook.put(bookName.toString() + bookQ.toString(), list);
	}
	
	public static ArrayList<Entry> getList(BookName bookName, BookQ bookQ) {
		return listsPerBook.get(bookName.toString() + bookQ.toString());
	}
	
	public static ArrayList<Entry> getList(String key) {
		return listsPerBook.get(key);
	}
	
	/**
	 * DataLang:データの言語 japanese:日本語 english:英語
	 */
	public enum DataLang {
		japanese,
		english,
		both,
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
		public String content, e, j;
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
		
		public Entry(String e, String j, Folder folder, BookName bookName, BookQ bookQ, int numberInBook, Datatype datatype) {
			size++;
			this.e = e;
			this.j = j;
			this.folder = folder;
			this.bookName = bookName;
			this.bookQ = bookQ;
			this.numberInBook = numberInBook;
			this.toushiNumber = size;
			this.datatype = datatype;
			this.dataLang = DataLang.both;
		}
		
		@NonNull
		@Override
		public String toString() {
			if (dataLang == DataLang.both) {
				return folder + "/" + bookName + " " + bookQ + " " + numberInBook + " " + datatype + " " + e + " " + j;
			}
			else {
				return folder + "/" + bookName + " " + bookQ + " " + numberInBook + " " + datatype + " " + dataLang + " " + content;
			}
		}
		
		public String[] getAllFieldString() {
			return new String[]{
				this.content,
				this.e,
				this.j,
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
				var text = (dataLang == DataLang.both ? "英語:" + this.e
					+ "\n日本語:" + this.j : this.content);
				return "No. " + this.toushiNumber
					+ "\nカテゴリ: " + this.folder.toString() + " " + this.bookName.toString()
					+ "\n番号:" + this.numberInBook
					+ rate
					+ "\n" + this.content
					+ "\n" + text
					+ "\n発音:" + getHatsuon(this.content)
					+ "\nDataBook:" + this.bookName
					+ "\nDataQ:" + this.bookQ
					+ "\nMode:" + this.dataLang
					+ GogenYomuFactory.getGogenString(this.content, true, true)
					+ GogenYomuFactory.getGogenString(this.e, true, true);
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
		//TODO:このクラスのメンバはstaticじゃないほうがいい
		static skipjouken skipjoken = skipjouken.kirokunomi;
		
		public static void saveQuizData(Context context) {
			printCurrentState("quizの情報を保存しています。");
			final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
			for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q"}) {
				var fileName = dnTestActivity + q + "Test";
				putStringData(context, fileName, keySeikai, intArrayToString(seikai.get(fileName)));
				putStringData(context, fileName, keyHuseikai, intArrayToString(huseikai.get(fileName)));
				putIntData(context, fileName, N_GENZAI_NAN_MONME, monme.get(fileName));
			}
		}
		
		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}
	}
	
	public static class HatsuonKigou {
		
		public static final HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();
		
		//todo 引数の型をArrayListにするとエラーになる原因を調べる
		public static void SetHatsuonKigou(List<Entry> list) {
			try {
				//発音記号のためにSVL読み込み
				if (hashMapHatsuonKigou.size() == 0)
					for (int i = 1; i < list.size(); i++)
						hashMapHatsuonKigou.put(list.get(i).e, list.get(i).j);
			} catch (Exception e) {
				showException(e);
			}
		}
		
		public static String getHatsuon(String strEnglishWord) {
			try {
				String strDictionary = hashMapHatsuonKigou.get(strEnglishWord);
				String ans = "";
				if (strDictionary != null) {
					int start;
					int result = strDictionary.indexOf("【発音】");
					if (result != -1) {
						start = result + 4;
					}
					else {
						result = strDictionary.indexOf("【発音！】");
						start = result + 5;
					}
					int end = strDictionary.indexOf("、", start);
					if (end == -1) end = strDictionary.length() - 1;
					ans = strDictionary.substring(start, end);
				}
				return ans;
			} catch (Exception e) {
				showException(e);
				return "";
			}
		}
	}
	
	static class Unit {
		public static final int[][][] toFindFromAndTo = {
			//1q
			{{1, 233}, {234, 472}, {473, 700}, {701, 919}, {920, 1177}, {1178, 1400}, {1401, 1619}, {1620, 1861}, {1862, 2100}, {2101, 2400},},
			//p1q
			{{1, 92}, {93, 362}, {363, 530}, {531, 682}, {683, 883}, {884, 1050}, {1051, 1262}, {1263, 1411}, {1412, 1550}, {1551, 1850},},
			//2q
			{{1, 158}, {159, 316}, {317, 405}, {406, 564}, {565, 719}, {720, 808}, {809, 949}, {950, 1108}, {1109, 1179}, {1180, 1704},},
			//p2q
			{{1, 125}, {126, 268}, {269, 373}, {374, 484}, {485, 632}, {633, 735}, {736, 839}, {840, 988}, {989, 1085}, {1086, 1500},},
			//3q
			{},
			//4q
			{},
			//5q
			{},
			//y00
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800},},
			//y08
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800},},
			//y1
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {901, 1000},},
			//y2
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {901, 1000},},
			//y3
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800},},
			//1qEX
			{{1, 276}, {277, 588}, {589, 840}, {841, 1080}, {1081, 1320}, {1321, 1560}, {1561, 1800}, {1801, 2040}, {2041, 2208}, {2209, 2364}, {2365, 2811}},
			//p1qEX
			{{1, 216}, {217, 432}, {433, 648}, {649, 864}, {865, 1080}, {1081, 1296}, {1297, 1488}, {1489, 1680}, {1681, 1824}, {1825, 1920}, {1920, 2400}},
			//総合
			{
				{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {901, 1000},
				{1001, 1100}, {1101, 200}, {1201, 1300}, {1301, 1400}, {1401, 1500}, {1501, 1600}, {1601, 1700}, {1701, 1800}, {1801, 1900}, {1901, 2000},
				{2001, 2100}, {2101, 2200}, {2201, 2300}, {2301, 2400}, {2401, 2500}, {2501, 2600}, {2601, 2700}, {2701, 2800},
				{2801, 2892}, {2893, 3162}, {3163, 3330}, {3331, 3482}, {3483, 3683}, {3684, 3850}, {3851, 4062}, {4063, 4211}, {4212, 4350}, {4351, 4650},
				{4651, 4883}, {4884, 5122}, {5123, 5350}, {5351, 5569}, {5570, 5827}, {5828, 6050}, {6051, 6269}, {6270, 6511}, {6512, 6750}, {6751, 7050},
			},
		};
	}
}