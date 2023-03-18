package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.現在何問目;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.intArrayToString;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.putStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.stringToIntArray;
import static com.gukos.bokotan.MyLibrary.tangoNumToString;
import static com.gukos.bokotan.WordPhraseData.DataBook.eigoduke;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.svl12000;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEx;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Function;

public class WordPhraseData extends ViewModel {
	private static boolean isEmpty = true;
	
	public static final TreeMap<String, int[]> seikai = new TreeMap<>();
	public static final TreeMap<String, int[]> huseikai = new TreeMap<>();
	public static final TreeMap<String, Integer> monme = new TreeMap<>();
	
	public static final TreeMap<String, ArrayList<WordInfo>> map = new TreeMap<>();
	//TODO:このクラスのメンバはstaticじゃないほうがいい
	public static final ArrayList<WordInfo> allData = new ArrayList<>();
	static skipjouken skipjoken = skipjouken.kirokunomi;
	public final String[] e = new String[20000], j = new String[20000];
	public final static String
		PasstanWord = "Passtan/WordData",
		PasstanPhrase = "Passtan/Phrase",
		TanjukugoWord = "TanjukugoEX/Word",
		TanjukugoEXWord = "TanjukugoEX/EXWord",
		TanjukugoPhrase = "TanjukugoEX/Phrase",
		YumeWord = "Yumetan/WordDataYume",
		Svl = "SVL/SVL12000",
		distinction = "distinction/";
	
	enum DataBook {
		passTan, tanjukugo, tanjukugoEx, yumetan, eigoduke, distinction, svl12000
	}
	
	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5, y00, y08, y1, y2, y3;
		
		public static DataQ parse(@Nullable String value) {
			if (value==null) return null;
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
		@Override
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
	
	enum DataLang {english, japanese}
	
	enum Mode {
		word, phrase, wordPlusPhrase
	}
	
	enum skipjouken {
		kirokunomi, seikai1, huseikai2, onlyHugoukaku
	}
	
	private static void putDataToMap(String fileName,Context context,DataBook dataBook,String q, String category,Function<Integer,String> getSubCategory,String qName, boolean mapput) {
		var list = readToList(fileName, context, dataBook);
		if (getSubCategory == null) {
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordInfo(dataBook, DataQ.parse(q), Mode.word, category, null, list, i, qName));
			}
		}
		else {
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordInfo(dataBook, DataQ.parse(q), Mode.word, category, getSubCategory.apply(i), list, i, qName));
			}
		}
		if (mapput) {
			WordPhraseData.map.put(fileName, list);
		}
	}
	
	public static ArrayList<WordInfo> getList(String key) {
		return map.get(key);
	}
	
	public static ArrayList<WordInfo> readToList(String fileName, Context context, DataBook dataBook) {
		ArrayList<WordInfo> list = new ArrayList<>();
		String fileName1 = fileName + ".e.txt", fileName2 = fileName + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String dataE, dataJ;
			int i = 0;
			while ((dataE = br1.readLine()) != null && (dataJ = br2.readLine()) != null) {
				list.add(new WordInfo(dataE, dataJ, i, dataBook));
				i++;
			}
			is1.close();
			is2.close();
			br1.close();
			br2.close();
		} catch (Exception e) {
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル" + fileName1 + "または" + fileName2 + "が見つかりません。").setPositiveButton("ok", null).create().show();
		}
		return list;
	}
	
	public static class HatsuonKigou {
		
		public static final HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();
		
		public static void SetHatsuonKigou(ArrayList<WordInfo> list) {
			try {
				//発音記号のためにSVL読み込み
				if (hashMapHatsuonKigou.size() == 0) {
					for (int i = 1; i < list.size(); i++)
						hashMapHatsuonKigou.put(list.get(i).e, list.get(i).j);
				}
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
				return "<不明>";
			}
		}
	}
	
	static class WordInfo {
		static int size = 0;
		final int toushiNumber, localNumber;
		//qNameは問題の正解数不正解数を表示するためにtreemapを参照するキーに使う。
		final String category, e, j, subCategory, qName;
		final DataBook dataBook;
		final DataQ dataQ;
		final Mode mode;
		
		/**
		 * readToListメソッドからのみ参照する
		 * @param e
		 * @param j
		 * @param localNumber
		 * @param dataBook
		 */
		@Deprecated
		public WordInfo(String e,String j,int localNumber,DataBook dataBook){
			this(dataBook,null,null,null,null,e,j,localNumber,null);
		}
		
		/**
		 * String e,jを省略する。
		 * @param dataBook
		 * @param dataQ
		 * @param mode
		 * @param category
		 * @param subCategory
		 * @param list
		 * @param localNumber
		 * @param qName
		 */
		WordInfo(DataBook dataBook, DataQ dataQ, Mode mode, String category, String subCategory, ArrayList<WordInfo> list, int localNumber, String qName) {
			this(dataBook, dataQ, mode, category, subCategory, list.get(localNumber).e, list.get(localNumber).j, localNumber, qName);
		}
		
		/**
		 * メインのコンストラクタ
		 * 全パラメーターを指定する
		 * @param dataBook
		 * @param dataQ
		 * @param mode
		 * @param category
		 * @param subCategory
		 * @param e
		 * @param j
		 * @param localNumber
		 * @param qName
		 */
		private WordInfo(DataBook dataBook, DataQ dataQ, Mode mode, String category, String subCategory, String e, String j, int localNumber, String qName) {
			size++;
			this.dataBook = dataBook;
			this.dataQ = dataQ;
			this.mode = mode;
			this.toushiNumber = size;
			this.category = category;
			if (Objects.isNull(subCategory)) this.subCategory = "";
			else this.subCategory = subCategory;
			this.e = e;
			this.j = j;
			this.localNumber = localNumber;
			this.qName = qName;
		}
		
		String[] getAllFieldString() {
			return new String[]{
				this.e,
				this.j,
				this.category,
				this.subCategory,
				String.valueOf(this.localNumber),
			};
		}
		
		@NonNull
		public String toString() {
			try {
				String string = category + " " + subCategory + " " + e + " " + j;
				return string.substring(0, Math.min(string.length(), 50));
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}
		
		public String toDetailedString() {
			try {
				final String rate;
				String fileName = dnTestActivity + qName + "Test";
				if (WordPhraseData.seikai.get(fileName) != null) {
					int correct = WordPhraseData.seikai.get(fileName)[localNumber];
					int incorrect = WordPhraseData.huseikai.get(fileName)[localNumber];
					rate = "\n正解率 " + correct + "/" + (correct + incorrect);
				}
				else rate = "";
				
				return "No. " + this.toushiNumber
					+ "\nカテゴリ: " + this.category + " " + this.subCategory
					+ "\n番号:" + this.localNumber
					+ rate
					+ "\n" + this.e
					+ "\n発音:" + getHatsuon(this.e)
					+ "\n" + this.j + "\n"
					+"\nDataBook:"+this.dataBook
					+"\nDataQ:"+this.dataQ
					+"\nMode:"+this.mode
					+ GogenYomuFactory.getGogenString(this.e, true, true);
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}
	}
	
	//assetsフォルダーからデータを読み込む
	//ViewMOdelによりデータが保持されている場合は何もしない。
	public static WordPhraseData loadAssets(Context context) {
		if (isEmpty) {
			readAllData(context);
			final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
			for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q"}) {
				var fileName = dnTestActivity + q + "Test";
				var array = stringToIntArray(getStringData(context, fileName, keySeikai, ""));
				if (array == null) array = new int[3000];
				seikai.put(fileName, array);
				array = stringToIntArray(getStringData(context, fileName, keyHuseikai, ""));
				if (array == null) array = new int[3000];
				huseikai.put(fileName, array);
				monme.put(fileName, getIntData(context, fileName, 現在何問目, 1));
			}
			isEmpty = false;
		}
		return new ViewModelProvider((ViewModelStoreOwner) context).get(WordPhraseData.class);
	}
	
	public static void saveQuizData(Context context) {
		printCurrentState("quizの情報を保存しています。");
		final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
		for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q"}) {
			var fileName = dnTestActivity + q + "Test";
			putStringData(context, fileName, keySeikai, intArrayToString(seikai.get(fileName)));
			putStringData(context, fileName, keyHuseikai, intArrayToString(huseikai.get(fileName)));
			putIntData(context, fileName, 現在何問目, monme.get(fileName));
		}
	}
	
	//削除時
	@Override
	protected void onCleared() {
		super.onCleared();
		printCurrentState("VIewModelが削除されます。");
		isEmpty = true;
	}
	
	public static void readAllData(Context context) {
		//wordphrasedataの読み取り
		//ファイルを開いて読み込む
		var startTime = System.currentTimeMillis();
		WordPhraseData.WordInfo.size = 0;
		Map<String, String> mapQName = new HashMap<>() {{
			put("1q", "1級");
			put("p1q", "準1級");
			put("2q", "2級");
			put("p2q", "準2級");
			put("3q", "3級");
			put("4q", "4級");
			put("5q", "5級");
			put("00", "ユメタン0基礎");
			put("08", "ユメタン0");
			put("1", "ユメタン1");
			put("2", "ユメタン2");
			put("3", "ユメタン3");
			put("-eiken-jukugo", "英検熟語");
			put("-eikenp1-jukugo", "英検熟語(準1)");
			put("-Toefl-Chokuzen", "TOEFL直前");
			put("-Toeic-500ten", "TOEIC500点");
			put("-Toeic-700ten", "TOEIC700点");
			put("-Toeic-900ten", "TOEIC900点");
			put("-Toeic-Chokuzen", "TOEIC直前");
			put("-Toeic-jukugo", "TOEIC熟語");
			put("d1phrase12", "1");
			put("d2phrase1", "2");
			
		}};
		
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
			putDataToMap(PasstanWord + q, context, passTan, q, "パス単" + mapQName.get(q), integer -> tangoNumToString("パス単" + mapQName.get(q), integer), q, true);
		
		for (var q : new String[]{"1q", "p1q"})
			putDataToMap(TanjukugoWord + q, context, tanjukugo, q, "単熟語EX" + mapQName.get(q), integer -> tangoNumToString("単熟語EX" + mapQName.get(q), integer),
			             "tanjukugo" + q, true);
		
		for (var q : new String[]{"1q", "p1q"})
			putDataToMap(TanjukugoEXWord + q, context, tanjukugoEx, q, "単熟語EX" + mapQName.get(q), integer -> tangoNumToString("単熟語EX" + mapQName.get(q), integer), "tanjukugo" + q, true);
		
		for (var q : new String[]{"00", "08", "1", "2", "3"})
			putDataToMap(YumeWord + q, context, yumetan, q, mapQName.get(q), integer -> "Unit" + ((integer - 1) / 100 + 1), "y" + q, true);
		
		//語源データも読み込む
		int gogenNum = 0;
		QSentakuFragment.trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
		
		for (var map : QSentakuFragment.trGogenYomu.entrySet())
			allData.add(new WordInfo(null, null, Mode.word, "読む語源学", null, map.getKey(), map.getValue().wordJpn, ++gogenNum, null));
		
		//英語漬け.comから読み込み
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q", "-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten", "-Toeic-Chokuzen", "-Toeic-jukugo",})
			putDataToMap("Eigoduke.com/" + "WordDataEigoduke" + q, context, eigoduke, q, "英語漬け" + mapQName.get(q), null, null, false);
		
		for (int num = 1; num <= 10; num++){
			var q = "-toeic (" + num + ")";
			putDataToMap("Eigoduke.com/" + "WordDataEigoduke" + q,context,eigoduke,q,"英語漬け" + "TOEIC" + num,null,null,false);
		}
		
		//distinction
		for (var d:new int[]{1,2,3,4})
			putDataToMap(WordPhraseData.distinction + "d" + d + "word", context, DataBook.distinction, null, "Distinction" + d, integer -> tangoNumToString("Distinction" + d, integer), null, false);
		
		//フレーズ
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
			putDataToMap(PasstanPhrase + q, context, passTan, q, "パス単" + mapQName.get(q), integer -> tangoNumToString("パス単" + mapQName.get(q), integer), q, true);
		
		for (var q : new String[]{"1q", "p1q"})
			putDataToMap(TanjukugoPhrase + q, context, tanjukugo, q, "単熟語EX" + mapQName.get(q), integer -> tangoNumToString("単熟語EX" + mapQName.get(q), integer), "tanjukugo" + q, true);
		
		for (var q : new String[]{"d1phrase12", "d2phrase1"})
			putDataToMap(WordPhraseData.distinction + q, context, DataBook.distinction, q, "Distinction" + mapQName.get(q), integer -> tangoNumToString("Distinction" + mapQName.get(q), integer), null, false);
		
		//SVL12000辞書
		putDataToMap(Svl,context,svl12000,null,"SVL", integer ->Integer.toString((integer - 1) / 1000 + 1),null,true );
		SetHatsuonKigou(map.get(Svl));
		
		var endTime = System.currentTimeMillis();
		printCurrentState("経過時間:" + (endTime - startTime) / 1000f);
	}
}

class Unit {
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
	};
}