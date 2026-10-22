package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.Dictionary.HatsuonKigou.getHatsuon;
import static com.gukos.bokotan.Dictionary.QuizData.huseikai;
import static com.gukos.bokotan.Dictionary.QuizData.monme;
import static com.gukos.bokotan.Dictionary.QuizData.seikai;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.intArrayToString;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.stringToIntArray;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Dictionaryクラス
 * すべての単語、文のデータと音声ファイルのパスを提供する
 */
public final class Dictionary {
	public static ArrayList<Entry> allData = new ArrayList<>();
	private static volatile boolean isEmpty = true;
	private static boolean loading;
	private static final Object readyLock = new Object();
	private static final ArrayList<Runnable> readyCallbacks = new ArrayList<>();
	public static final ArrayList<String> loadErrors = new ArrayList<>();

	public static boolean isReady() { return !isEmpty; }

	public static void whenReady(Context context, Runnable callback) {
		boolean start;
		synchronized (readyLock) {
			if (isReady()) { new Handler(Looper.getMainLooper()).post(callback); return; }
			readyCallbacks.add(callback);
			start = !loading;
			loading = true;
		}
		if (start) {
			Context application = context.getApplicationContext();
			new Thread(() -> {
				try { initialize(application); }
				catch (Exception error) { Log.e("Dictionary", "辞書を初期化できません。", error); }
				finally {
					ArrayList<Runnable> callbacks;
					synchronized (readyLock) {
						isEmpty = false; loading = false;
						callbacks = new ArrayList<>(readyCallbacks); readyCallbacks.clear();
					}
					var main = new Handler(Looper.getMainLooper());
					for (var action : callbacks) main.post(action);
				}
			}, "DictionaryLoader").start();
		}
	}

	public static TreeMap<String, ArrayList<Entry>> listsPerBook = new TreeMap<>();

	// 欠損した辞書は空のプリセットとして扱う。UIは読み込み結果を見て開始を拒否する。
	public static ArrayList<Entry> readToList(Context context, Folder folder, BookName book,
			BookQ q, Datatype type, DataLang lang) {
		String filename = "dictionaries/" + folder.toDirName() + "/" + book.toFileName() + q.toFileName()
				+ (lang == DataLang.japanese ? ".j.txt" : lang == DataLang.english ? ".e.txt" : "");
		var list = new ArrayList<Entry>();
		try (var reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename), StandardCharsets.UTF_8))) {
			String line;
			int number = 0;
			while ((line = reader.readLine()) != null) list.add(new Entry(line, folder, book, q, number++, type, lang));
		} catch (java.io.IOException error) {
			Log.w("Dictionary", filename + ": " + error.getMessage());
			loadErrors.add(filename + ": " + error.getMessage());
			list.clear();
		}
		return list;
	}

	public static ArrayList<Entry> readToList(Context context, Folder folder, BookName book, BookQ q, Datatype type) {
		String prefix = "dictionaries/" + folder.toDirName() + "/" + book.toFileName() + q.toFileName();
		String key = book.toString() + q.toString();
		if (listsPerBook.containsKey(key)) return listsPerBook.get(key);
		var list = new ArrayList<Entry>();
		try (var english = new InputStreamReader(context.getAssets().open(prefix + ".e.txt"), StandardCharsets.UTF_8);
			 var japanese = new InputStreamReader(context.getAssets().open(prefix + ".j.txt"), StandardCharsets.UTF_8)) {
			var pairs = DictionaryText.readPairs(english, japanese);
			int number = 0;
			if (book == BookName.yumetanPhrase && !pairs.isEmpty()) {
				list.add(new Entry("", "", folder, book, q, number++, type));
			}
			for (var pair : pairs) list.add(new Entry(pair[0], pair[1], folder, book, q, number++, type));
		} catch (java.io.IOException error) {
			Log.w("Dictionary", prefix + ": " + error.getMessage());
			loadErrors.add(prefix + ": " + error.getMessage());
		}
		listsPerBook.put(key, list);
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
		tanjukugo,
		tanjukugoEx,
		sample,
		personal,
		all;

		@Override public String toString() { return this == sample ? "サンプル" : this == personal ? "個人データ" : name(); }

		public String toDirName() {
			switch (this) {
				case sample:
					return "Sample";
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
		sampleWord,
		personalWord,
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
				case sampleWord:
					return "Words";
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
					return "究極の英単語"+"premium"+"Vol1.txt";
				case kyukyoku_premium_vol2:
					return "究極の英単語"+"premium"+"Vol.2_EJ（英日）.txt";
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

	// assetsをプロセス内で一度だけ読み込む。画面はwhenReadyで完了通知を受け取る。
	public static synchronized void initialize(Context context) {
		if (isEmpty) {
			var entries = new ArrayList<Dictionary.Entry>();
			entries.addAll(readToList(context, Folder.sample, BookName.sampleWord, BookQ.none, Datatype.word));
			PrivateData.load(context, entries);

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
			for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q", "sample", "private-" + PrivateData.id}) {
				var fileName = dnTestActivity + q + "Test";
				var array = restoreCounters(context, fileName, keySeikai);
				if (array == null) array = new int[3000];
				seikai.put(fileName, array);
				array = restoreCounters(context, fileName, keyHuseikai);
				if (array == null) array = new int[3000];
				huseikai.put(fileName, array);
				monme.put(fileName, getIntData(context, fileName, N_GENZAI_NAN_MONME, 1));
			}
			entries.removeIf(entry -> entry.dataLang == DataLang.both
					? !entry.isUsable() : !StudyRules.hasText(entry.content));
			allData = entries;
			isEmpty = false;
		}

	}

    static void reload(Context context, Runnable ready) {
        synchronized (Dictionary.class) {
            listsPerBook.clear(); loadErrors.clear();
            HatsuonKigou.hashMapHatsuonKigou.clear();
            isEmpty = true;
        }
        whenReady(context, ready);
    }

	private static int[] restoreCounters(Context context, String file, String key) {
		try { return stringToIntArray(getStringData(context, file, key, "")); }
		catch (NumberFormatException error) { Log.w("Dictionary", "不正な成績データ: " + file, error); return null; }
	}

	private static void addList(BookName bookName, BookQ bookQ, ArrayList<Entry> list) {
		listsPerBook.put(bookName.toString() + bookQ.toString(), list);
	}

	public static ArrayList<Entry> getList(BookName bookName, BookQ bookQ) {
		return listsPerBook.getOrDefault(bookName.toString() + bookQ.toString(), new ArrayList<>());
	}

	public static ArrayList<Entry> getList(String key) {
		return listsPerBook.getOrDefault(key, new ArrayList<>());
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
		public final String content, e, j;
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
			if (dataLang==DataLang.japanese){
				this.e = "null string";
				this.j = content;
			} else if (dataLang==DataLang.english){
				this.e = content;
				this.j = "null string";
			} else {
				e=j="null string";
			}
		}

		public Entry(String e, String j, Folder folder, BookName bookName, BookQ bookQ, int numberInBook, Datatype datatype) {
			size++;
			this.e = e;
			this.j = j;
			this.content = e;
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
			} else {
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
				if (isUsable() && datatype == Datatype.word &&
						(bookName == BookName.personalWord || bookName == BookName.sampleWord || bookName == BookName.PasstanWordData || bookName == BookName.yumetanWord || bookName == BookName.tanjukugoWord)) {
					int[] scores = QuizData.scores(this);
					rate = "\n正解率 " + scores[0] + "/" + (scores[0] + scores[1]);
				} else rate = "";
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
						+ "\nMode:" + this.dataLang;
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}

		public boolean isUsable() {
			return StudyRules.hasText(e) && StudyRules.hasText(j);
		}

		String privateEnglishAudio, privateJapaneseAudio, privatePackageId;

		public String toPath(DataLang lang) {
			if (folder == Folder.personal) return lang == DataLang.english ? privateEnglishAudio : lang == DataLang.japanese ? privateJapaneseAudio : null;
			if (folder == Folder.sample) {
				if (datatype != Datatype.word || (lang != DataLang.english && lang != DataLang.japanese)) return null;
				return AudioSource.ASSET_PREFIX + "sample-audio/" + String.format(Locale.ROOT, "%04d", numberInBook)
						+ (lang == DataLang.english ? "-en" : "-ja") + ".btv";
			}
			return MyLibrary.FileDirectoryManager.getPathPs(folder, bookQ, datatype, lang, numberInBook);
		}
	}

	public static class QuizData {

		public static final TreeMap<String, int[]> seikai = new TreeMap<>(), huseikai =
				new TreeMap<>();
		public static final TreeMap<String, Integer> monme = new TreeMap<>();
		static skipjouken skipjoken = skipjouken.kirokunomi;

		public static String keyFor(Entry entry) {
			if (entry.folder == Folder.sample) return dnTestActivity + "sampleTest";
			if (entry.folder == Folder.personal) return dnTestActivity + "private-" + entry.privatePackageId + "Test";
			return dnTestActivity + (entry.folder == Folder.tanjukugo ? "tanjukugo" : "") + entry.bookQ + "Test";
		}

		private static void ensure(Entry entry) {
			String key = keyFor(entry);
			int size = Math.max(1, entry.numberInBook + 1);
			for (var map : Arrays.asList(seikai, huseikai)) {
				int[] counts = map.get(key);
				if (counts == null || counts.length < size) map.put(key, Arrays.copyOf(counts == null ? new int[0] : counts, size));
			}
			monme.putIfAbsent(key, 1);
		}

		public static synchronized int[] scores(Entry entry) {
			ensure(entry);
			String key = keyFor(entry);
			return new int[]{seikai.get(key)[entry.numberInBook], huseikai.get(key)[entry.numberInBook]};
		}

		public static synchronized void record(Entry entry, boolean correct) {
			ensure(entry);
			(correct ? seikai : huseikai).get(keyFor(entry))[entry.numberInBook]++;
		}

		public static synchronized int questionNumber(Entry entry) {
			ensure(entry);
			String key = keyFor(entry);
			int number = monme.get(key);
			monme.put(key, number + 1);
			return number;
		}

		public static synchronized void saveQuizData(Context context) {
			printCurrentState("quizの情報を保存しています。");
			final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
			for (String fileName : new ArrayList<>(seikai.keySet())) {
				putStringData(context, fileName, keySeikai, intArrayToString(seikai.get(fileName)));
				putStringData(context, fileName, keyHuseikai, intArrayToString(huseikai.get(fileName)));
				putIntData(context, fileName, N_GENZAI_NAN_MONME, Objects.requireNonNull(monme.get(fileName)));
			}
		}

		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}
	}

	public static class HatsuonKigou {

		public static final HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();

		public static void SetHatsuonKigou(ArrayList<Entry> list) {
			try {
				//発音記号のためにSVL読み込み
				if (hashMapHatsuonKigou.isEmpty())
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
					} else {
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