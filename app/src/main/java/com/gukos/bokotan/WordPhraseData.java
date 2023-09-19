package com.gukos.bokotan;

import static com.gukos.bokotan.Dictionary.BookQ;
import static com.gukos.bokotan.Dictionary.Datatype;
import static com.gukos.bokotan.Dictionary.Folder;
import static com.gukos.bokotan.Dictionary.Folder.eigoduke;
import static com.gukos.bokotan.Dictionary.Folder.passtan;
import static com.gukos.bokotan.Dictionary.Folder.svl;
import static com.gukos.bokotan.Dictionary.Folder.tanjukugo;
import static com.gukos.bokotan.Dictionary.Folder.tanjukugoEx;
import static com.gukos.bokotan.Dictionary.Folder.yumetan;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName.dnTestActivity;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getStringData;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.stringToIntArray;
import static com.gukos.bokotan.MyLibrary.tangoNumToString;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;
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
import java.util.function.Function;

public class WordPhraseData extends ViewModel {
	private static boolean isEmpty = true;
	
	public static void putDataToMap(String fileName, Context context, Folder dataBook, String q, String category, Function<Integer, String> getSubCategory, String qName, boolean mapput) {
		var list = new ArrayList<WordInfo>();
		String fileName1 = fileName + ".e.txt", fileName2 = fileName + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String dataE, dataJ;
			int i1 = 0;
			while ((dataE = br1.readLine()) != null && (dataJ = br2.readLine()) != null) {
				list.add(new WordInfo(dataE, dataJ, i1, dataBook, q));
				i1++;
			}
			is1.close();
			is2.close();
			br1.close();
			br2.close();
		} catch (Exception e) {
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル" + fileName1 + "または" + fileName2 + "が見つかりません。").setPositiveButton("ok", null).create().show();
		}
		if (getSubCategory == null) {
			for (int i = 1; i < list.size(); i++) {
				Dictionary.QuizData.allData.add(new WordInfo(dataBook, BookQ.parse(q), Datatype.word, category, null, list, i, qName));
			}
		}
		else {
			for (int i = 1; i < list.size(); i++) {
				Dictionary.QuizData.allData.add(new WordInfo(dataBook, BookQ.parse(q), Datatype.word, category, getSubCategory.apply(i), list, i, qName));
			}
		}
		if (mapput) {
			Dictionary.QuizData.map.put(fileName, list);
		}
	}
	
	//assetsフォルダーからデータを読み込む
	//ViewMOdelによりデータが保持されている場合は何もしない。
	public static WordPhraseData loadAssets(Context context) {
		if (isEmpty) {
			//wordphrasedataの読み取り
			//ファイルを開いて読み込む
			var startTime = System.currentTimeMillis();
			WordInfo.size = 0;
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
			
			for (var q1 : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
				putDataToMap(Dictionary.QuizData.PasstanWord + q1, context, passtan, q1, "パス単" + mapQName.get(q1), integer -> tangoNumToString("パス単" + mapQName.get(q1), integer), q1, true);
			
			for (var q1 : new String[]{"1q", "p1q"})
				putDataToMap(Dictionary.QuizData.TanjukugoWord + q1, context, tanjukugo, q1, "単熟語EX" + mapQName.get(q1), integer -> tangoNumToString("単熟語EX" + mapQName.get(q1), integer),
				             "tanjukugo" + q1, true);
			
			for (var q1 : new String[]{"1q", "p1q"})
				putDataToMap(Dictionary.QuizData.TanjukugoEXWord + q1, context, tanjukugoEx, q1, "単熟語EX" + mapQName.get(q1), integer -> tangoNumToString("単熟語EX" + mapQName.get(q1), integer), "tanjukugo" + q1, true);
			
			for (var q1 : new String[]{"00", "08", "1", "2", "3"})
				putDataToMap(Dictionary.QuizData.YumeWord + q1, context, yumetan, q1, mapQName.get(q1), integer -> "Unit" + ((integer - 1) / 100 + 1), "y" + q1, true);
			
			//語源データも読み込む
			int gogenNum = 0;
			QSentakuFragment.trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
			
			for (var map : QSentakuFragment.trGogenYomu.entrySet())
				Dictionary.QuizData.allData.add(new WordInfo(null, null, Datatype.word, "読む語源学", null, map.getKey(), map.getValue().wordJpn, ++gogenNum, null));
			
			//英語漬け.comから読み込み
			for (var q1 : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q", "-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten", "-Toeic-Chokuzen", "-Toeic-jukugo",})
				putDataToMap("Eigoduke.com/" + "WordDataEigoduke" + q1, context, eigoduke, q1, "英語漬け" + mapQName.get(q1), null, null, false);
			
			for (int num = 1; num <= 10; num++) {
				var q1 = "-toeic (" + num + ")";
				putDataToMap("Eigoduke.com/" + "WordDataEigoduke" + q1, context, eigoduke, q1, "英語漬け" + "TOEIC" + num, null, null, false);
			}
			
			//distinction
			for (var d : new int[]{1, 2, 3, 4})
				putDataToMap(Dictionary.QuizData.distinction + "d" + d + "word", context, Folder.distinction, null, "Distinction" + d, integer -> tangoNumToString("Distinction" + d, integer), null, false);
			
			//フレーズ
			for (var q1 : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"})
				putDataToMap(Dictionary.QuizData.PasstanPhrase + q1, context, passtan, q1, "パス単" + mapQName.get(q1), integer -> tangoNumToString("パス単" + mapQName.get(q1), integer), q1, true);
			
			for (var q1 : new String[]{"1q", "p1q"})
				putDataToMap(Dictionary.QuizData.TanjukugoPhrase + q1, context, tanjukugo, q1, "単熟語EX" + mapQName.get(q1), integer -> tangoNumToString("単熟語EX" + mapQName.get(q1), integer), "tanjukugo" + q1, true);
			
			for (var q1 : new String[]{"d1phrase12", "d2phrase1"})
				putDataToMap(Dictionary.QuizData.distinction + q1, context, Folder.distinction, q1, "Distinction" + mapQName.get(q1), integer -> tangoNumToString("Distinction" + mapQName.get(q1), integer), null, false);
			
			//SVL12000辞書
			putDataToMap(Dictionary.QuizData.Svl, context, svl, null, "SVL", integer -> Integer.toString((integer - 1) / 1000 + 1), null, true);
			//SetHatsuonKigou(Dictionary.QuizData.map.get(Dictionary.QuizData.Svl));
			
			var endTime = System.currentTimeMillis();
			printCurrentState("経過時間:" + (endTime - startTime) / 1000f);
			final String keySeikai = "keySeikai", keyHuseikai = "keyHuseikai";
			for (var q : new String[]{"1q", "p1q", "y1", "y2", "y3", "tanjukugo1q", "tanjukugop1q"}) {
				var fileName = dnTestActivity + q + "Test";
				var array = stringToIntArray(getStringData(context, fileName, keySeikai, ""));
				if (array == null) array = new int[3000];
				Dictionary.QuizData.seikai.put(fileName, array);
				array = stringToIntArray(getStringData(context, fileName, keyHuseikai, ""));
				if (array == null) array = new int[3000];
				Dictionary.QuizData.huseikai.put(fileName, array);
				Dictionary.QuizData.monme.put(fileName, getIntData(context, fileName, N_GENZAI_NAN_MONME, 1));
			}
			isEmpty = false;
		}
		return new ViewModelProvider((ViewModelStoreOwner) context).get(WordPhraseData.class);
	}
	
	//削除時
	@Override
	protected void onCleared() {
		super.onCleared();
		printCurrentState("VIewModelが削除されます。");
		isEmpty = true;
	}
	
	static class WordInfo {
		static int size = 0;
		final int toushiNumber, numberInBook;
		//qNameは問題の正解数不正解数を表示するためにtreemapを参照するキーに使う。
		final String category, e, j, subCategory, qName;
		final Folder folder;
		final BookQ bookQ;
		final Datatype datatype;
		
		/**
		 * readToListメソッドからのみ参照する
		 *
		 * @param e
		 * @param j
		 * @param localNumber
		 * @param dataBook
		 */
		@Deprecated
		private WordInfo(String e, String j, int localNumber, Folder dataBook, String dataQ) {
			this(dataBook, BookQ.parse(dataQ), null, null, null, e, j, localNumber, null);
		}
		
		/**
		 * String e,jを省略する。
		 *
		 * @param dataBook
		 * @param dataQ
		 * @param mode
		 * @param category
		 * @param subCategory
		 * @param list
		 * @param localNumber
		 * @param qName
		 */
		private WordInfo(Folder dataBook, BookQ dataQ, Datatype mode, String category, String subCategory, ArrayList<WordInfo> list, int localNumber, String qName) {
			this(dataBook, dataQ, mode, category, subCategory, list.get(localNumber).e, list.get(localNumber).j, localNumber, qName);
		}
		
		/**
		 * メインのコンストラクタ
		 * 全パラメーターを指定する
		 *
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
		private WordInfo(Folder dataBook, BookQ dataQ, Datatype mode, String category, String subCategory, String e, String j, int localNumber, String qName) {
			size++;
			this.folder = dataBook;
			this.bookQ = dataQ;
			this.datatype = mode;
			this.toushiNumber = size;
			this.category = category;
			if (Objects.isNull(subCategory)) this.subCategory = "";
			else this.subCategory = subCategory;
			this.e = e;
			this.j = j;
			this.numberInBook = localNumber;
			this.qName = qName;
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