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
import static com.gukos.bokotan.WordPhraseData.DataType.phrase;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;

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
import java.util.TreeMap;

import kotlin.jvm.functions.Function9;

public class WordPhraseData extends ViewModel {
	private static boolean isEmpty = true;
	//1qのunit=8のfrom=0,8,0; p1qunit=5 to=1,5,1
	public static final int[][][] toFindFromAndTo = {
		//1q
		{{1, 233}, {234, 472}, {473, 700}, {701, 919}, {920, 1177}, {1178, 1400}, {1401, 1619}, {1620, 1861}, {1862, 2100}, {2101, 2400}, {1, 700}, {701, 1400}, {1401, 2100}, {1, 2400}},
		//p1q
		{{1, 92}, {93, 362}, {363, 530}, {531, 682}, {683, 883}, {884, 1050}, {1051, 1262}, {1263, 1411}, {1412, 1550}, {1551, 1850}, {1, 530}, {531, 1050}, {1051, 1550}, {1, 1850}},
		//2q
		{{1, 158}, {159, 316}, {317, 405}, {406, 564}, {565, 719}, {720, 808}, {809, 949}, {950, 1108}, {1109, 1179}, {1180, 1704}, {1, 405}, {406, 808}, {809, 1179}, {1, 1704}},
		//p2q
		{{1, 125}, {126, 268}, {269, 373}, {374, 484}, {485, 632}, {633, 735}, {736, 839}, {840, 988}, {989, 1085}, {1086, 1500}, {1, 373}, {374, 735}, {736, 1085}, {1, 1500}},
		//3q
		{},
		//4q
		{},
		//5q
		{},
		//y00
		{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}},
		//y08
		{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
		//y1
		{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
		//y2
		{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
		//y3
		{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}},
		//1qEX
		{{1, 276}, {277, 588}, {589, 840}, {841, 1080}, {1081, 1320}, {1321, 1560}, {1561, 1800}, {1801, 2040}, {2041, 2208}, {2209, 2364}, {2365, 2811}},
		//p1qEX
		{{1, 216}, {217, 432}, {433, 648}, {649, 864}, {865, 1080}, {1081, 1296}, {1297, 1488}, {1489, 1680}, {1681, 1824}, {1825, 1920}, {1920, 2400}},
	};
	public static TreeMap<String, ArrayList<QuizCreator.QuizWordData>> map = new TreeMap<>();
	//TODO:このクラスのメンバはstaticじゃないほうがいい
	public static ArrayList<WordInfo> allData = new ArrayList<>();
	static q_num.skipjouken skipjoken = q_num.skipjouken.kirokunomi;
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
	
	public static ArrayList<QuizCreator.QuizWordData> getList(String key) {
		return map.get(key);
	}
	
	public static ArrayList<QuizCreator.QuizWordData> readToList(String fileName, Context context, DataBook dataBook, String dataQ) {
		ArrayList<QuizCreator.QuizWordData> list = new ArrayList<>();
		String fileName1 = fileName + ".e.txt", fileName2 = fileName + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String dataE, dataJ;
			int i = 0;
			while ((dataE = br1.readLine()) != null && (dataJ = br2.readLine()) != null) {
				list.add(new QuizCreator.QuizWordData(dataE, dataJ, i, dataBook, dataQ));
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
	
	public static void SetNumFromAndTo(int lastnum, int unit) {
		try {
			/*
			PlaySound.from = 1;
			PlaySound.to = lastnum;
			if (unit > 10) unit--;
			if (lastnum == 2400) {
				PlaySound.from = toFindFromAndTo[0][unit][0];
				PlaySound.to = toFindFromAndTo[0][unit][1];
			}
			if (lastnum == 1850) {
				PlaySound.from = toFindFromAndTo[1][unit][0];
				PlaySound.to = toFindFromAndTo[1][unit][1];
			}
			if (lastnum == 1704) {
				PlaySound.from = toFindFromAndTo[2][unit][0];
				PlaySound.to = toFindFromAndTo[2][unit][1];
			}
			if (lastnum == 1500) {
				PlaySound.from = toFindFromAndTo[3][unit][0];
				PlaySound.to = toFindFromAndTo[3][unit][1];
			}
			*/
		} catch (Exception e) {
			showException(e);
		}
	}
	
	enum DataBook {
		passTan, tanjukugo, tanjukugoEx, yumetan, eigoduke, distinction, svl12000
	}
	
	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5, y00, y08, y1, y2, y3;
		
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
	
	public static DataQ toDataQ(String value) {
		switch (value) {
			case "1q": {
				return DataQ.q1;
			}
			case "p1q": {
				return DataQ.qp1;
			}
			case "2q": {
				return DataQ.q2;
			}
			case "p2q": {
				return DataQ.qp2;
			}
			case "3q": {
				return DataQ.q3;
			}
			case "4q": {
				return DataQ.q4;
			}
			case "5q": {
				return DataQ.q5;
			}
			case "00": {
				return DataQ.y00;
			}
			case "08": {
				return DataQ.y08;
			}
			case "1": {
				return DataQ.y1;
			}
			case "2": {
				return DataQ.y2;
			}
			case "3": {
				return DataQ.y3;
			}
			default: {
				return null;
			}
		}
	}
	
	enum DataType {word, phrase, gogengaku, eigoduke_com}
	
	enum DataLang {english, japanese}
	
	public enum q_num {
		test1q(), testp1q(), test2q(), testp2q(), testy00(), testy08(), testy1(), testy2(), testy3(), test1qEx, testp1qEx, testAll;
		
		enum mode {
			word, phrase, randomTest, huseikainomiTest, seitouritsujunTest, wordPlusPhrase
		}
		
		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}
		
		q_num() {
		}
	}
	
	public static class HatsuonKigou {
		
		public static HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();
		
		public static void SetHatsuonKigou(ArrayList<QuizCreator.QuizWordData> list) {
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
		final DataType dataType;
		final DataBook dataBook;
		final DataQ dataQ;
		final q_num.mode mode;
		
		WordInfo(DataBook dataBook, DataQ dataQ, q_num.mode mode,String category, String subCategory, ArrayList<QuizCreator.QuizWordData> list, int localNumber, DataType dataType, String qName) {
			this(dataBook,dataQ,mode,category, subCategory, list.get(localNumber).e, list.get(localNumber).j, localNumber, dataType, qName);
		}
		
		//メインのコンストラクタ
		private WordInfo(DataBook dataBook, DataQ dataQ, q_num.mode mode,String category, String subCategory, String e, String j, int localNumber, DataType dataType, String qName) {
			size++;
			this.dataBook=dataBook;
			this.dataQ=dataQ;
			this.mode=mode;
			this.toushiNumber = size;
			this.category = category;
			if (Objects.isNull(subCategory)) this.subCategory = "";
			else this.subCategory = subCategory;
			this.e = e;
			this.j = j;
			this.localNumber = localNumber;
			this.dataType = dataType;
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
					+ "\nカテゴリ: " + this.category + " " + subCategory
					+ "\n番号:" + this.localNumber
					+ rate
					+ "\n" + this.e
					+ "\n発音:" + getHatsuon(this.e)
					+ "\n" + this.j + "\n"
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
	
	public static TreeMap<String, int[]> seikai = new TreeMap<>(), huseikai = new TreeMap<>();
	public static TreeMap<String, Integer> monme = new TreeMap<>();
	
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
		
		//list,databook,dataq,mode,cat,subcat,e,j,local,datatype,qname
		Function9<DataBook,DataQ,q_num.mode,String,String,ArrayList<QuizCreator.QuizWordData>, Integer,DataType,String,Void> function9=new Function9<DataBook, DataQ, q_num.mode, String, String, ArrayList<QuizCreator.QuizWordData>, Integer, DataType, String, Void>() {
			@Override
			public Void invoke(DataBook dataBook, DataQ dataQ, q_num.mode mode, String s, String s2, ArrayList<QuizCreator.QuizWordData> quizWordData, Integer integer, DataType dataType, String s3) {
				return null;
			}
		};
		
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			var list = readToList(PasstanWord + q, context, passTan, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo(passTan, toDataQ(q), q_num.mode.word,"パス単" + mapQName.get(q), tangoNumToString("パス単" + mapQName.get(q), i), list, i, word, q));
			}
			WordPhraseData.map.put(PasstanWord + q, list);
		}
		
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoWord + q, context, tanjukugo, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo(tanjukugo, toDataQ(q), q_num.mode.word, "単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, word, "tanjukugo" + q));
			}
			WordPhraseData.map.put(TanjukugoWord + q, list);
		}
		
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoEXWord + q, context, tanjukugoEx, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo(tanjukugo, toDataQ(q), q_num.mode.word, "単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, word, "tanjukugo" + q));
			}
			WordPhraseData.map.put(TanjukugoEXWord + q, list);
		}
		
		for (var q : new String[]{"00", "08", "1", "2", "3"}) {
			var list = readToList(YumeWord + q, context, yumetan, "y" + q);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(DataBook.yumetan, toDataQ(q), q_num.mode.word,mapQName.get(q), "Unit" + ((i - 1) / 100 + 1), list, i, word, "y" + q));
			WordPhraseData.map.put(YumeWord + q, list);
		}
		
		//語源データも読み込む
		int gogenNum = 0;
		QSentakuFragment.trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
		for (var map : QSentakuFragment.trGogenYomu.entrySet()) {
			allData.add(new WordPhraseData.WordInfo(null, null, q_num.mode.word,"読む語源学", null, map.getKey(), map.getValue().wordJpn, ++gogenNum, WordPhraseData.DataType.gogengaku, null));
		}
		
		//英語漬け.comから読み込み
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q", "-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten", "-Toeic-Chokuzen", "-Toeic-jukugo",}) {
			var list = readToList("Eigoduke.com/" + "WordDataEigoduke" + q, context, eigoduke, mapQName.get(q));
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(eigoduke, null, q_num.mode.word, "英語漬け" + mapQName.get(q), null, list, i, WordPhraseData.DataType.eigoduke_com, null));
		}
		
		for (int num = 1; num <= 10; num++) {
			var q = "-toeic (" + num + ")";
			var list = readToList("Eigoduke.com/" + "WordDataEigoduke" + q, context, eigoduke, "英語漬け" + "TOEIC" + num);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(eigoduke, null, q_num.mode.word,"英語漬け" + "TOEIC" + num, null, list, i, WordPhraseData.DataType.eigoduke_com, null));
		}
		
		//distinction
		for (int d = 1; d <= 4; d++) {
			var list = readToList(WordPhraseData.distinction + "d" + d + "word", context, DataBook.distinction, "Distinction" + d);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(DataBook.distinction, null, q_num.mode.word, "Distinction" + d, tangoNumToString("Distinction" + d, i), list, i, word, null));
		}
		
		//フレーズ
		for (var q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			var list = readToList(PasstanPhrase + q, context, passTan, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo(passTan, toDataQ(q), q_num.mode.phrase, "パス単" + mapQName.get(q), tangoNumToString("パス単" + mapQName.get(q), i), list, i, phrase, q));
			}
			WordPhraseData.map.put(PasstanPhrase + q, list);
		}
		
		for (var q : new String[]{"1q", "p1q"}) {
			var list = readToList(TanjukugoPhrase + q, context, tanjukugo, q);
			for (int i = 1; i < list.size(); i++) {
				allData.add(new WordPhraseData.WordInfo(tanjukugo, toDataQ(q), q_num.mode.phrase, "単熟語EX" + mapQName.get(q), tangoNumToString("単熟語EX" + mapQName.get(q), i), list, i, phrase, "tanjukugo" + q));
			}
			WordPhraseData.map.put(TanjukugoPhrase + q, list);
		}
		
		for (var q : new String[]{"d1phrase12", "d2phrase1"}) {
			var list = readToList(WordPhraseData.distinction + q, context, DataBook.distinction, q);
			for (int i = 1; i < list.size(); i++)
				allData.add(new WordPhraseData.WordInfo(DataBook.distinction, null, q_num.mode.phrase, "Distinction" + mapQName.get(q), tangoNumToString("Distinction" + mapQName.get(q), i), list, i, phrase, null));
		}
		
		//SVL12000辞書
		var list = readToList(Svl, context, svl12000, "svl");
		for (int i = 1; i < list.size(); i++)
			allData.add(new WordPhraseData.WordInfo(svl12000,null,null,"SVL", Integer.toString((i - 1) / 1000 + 1), list, i, word, null));
		//コピー
		SetHatsuonKigou(list);
		
		var endTime = System.currentTimeMillis();
		printCurrentState("経過時間:" + (endTime - startTime) / 1000f);
	}
}