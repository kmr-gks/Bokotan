package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.tangoNumToString;
import static com.gukos.bokotan.WordPhraseData.DataType.phrase;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class WordPhraseData {
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
	public static TreeMap<String,ArrayList<QuizCreator.QuizWordData>> map=new TreeMap<>();
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
	
	public static ArrayList<QuizCreator.QuizWordData> getList(String key){
		return map.get(key);
	}
	
	public WordPhraseData(String strQ, Context context) {
		String fileName1 = strQ + ".e.txt", fileName2 = strQ + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String str;
			int i = 0;
			while ((str = br1.readLine()) != null) {
				e[i] = str;
				i++;
			}
			i = 0;
			while ((str = br2.readLine()) != null) {
				j[i] = str;
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
	}
	
	public WordPhraseData(String strQ, Context context, ArrayList<QuizCreator.QuizWordData> list, DataBook dataBook, String dataQ) {
		String fileName1 = strQ + ".e.txt", fileName2 = strQ + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String dataE, dataJ;
			int i = 0;
			while ((dataE = br1.readLine()) != null && (dataJ = br2.readLine()) != null) {
				list.add(new QuizCreator.QuizWordData(dataE, dataJ, i, dataBook,dataQ));
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
	}
	
	public static ArrayList<QuizCreator.QuizWordData> readToList(String strQ, Context context, DataBook dataBook, String dataQ){
		ArrayList<QuizCreator.QuizWordData> list=new ArrayList<>();
		new WordPhraseData(strQ,context,list,dataBook,dataQ);
		return list;
	}
	
	public static ArrayList<WordPhraseData.WordInfo> readData_Kensaku(Context context){
		//ファイルを開いて読み込む
		var trGogenYomu = new GogenYomuFactory(context).getTrGogenYomu();
		ArrayList<WordPhraseData.WordInfo> allData=new ArrayList<>();
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
		
		//パス単単語
		for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			WordPhraseData w = new WordPhraseData(PasstanWord + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("パス単" + mapQName.get(Q), tangoNumToString("パス単" + mapQName.get(Q), i), w.e[i], w.j[i], i, word));
		}
		
		//単熟語EX単語
		for (String Q : new String[]{"1q", "p1q"}) {
			WordPhraseData w = new WordPhraseData(TanjukugoWord + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(Q), tangoNumToString("単熟語EX" + mapQName.get(Q), i), w.e[i], w.j[i], i, word));
			WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + Q, context);
			for (int i = 1; i < Math.min(wx.e.length, wx.j.length); i++)
				if (wx.e[i] != null && wx.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(Q), "Unit EX", wx.e[i], wx.j[i], i, word));
		}
		
		//ユメタン単語
		for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
			WordPhraseData w = new WordPhraseData(YumeWord + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo(mapQName.get(Q), "Unit" + ((i - 1) / 100 + 1), w.e[i], w.j[i], i, word));
		}
		
		//語源データも読み込む
		int gogenNum = 0;
		for (TreeMap.Entry<String, GogenYomu> map : trGogenYomu.entrySet())
			allData.add(new WordPhraseData.WordInfo("読む語源学", map.getKey(), map.getValue().wordJpn, ++gogenNum, WordPhraseData.DataType.gogengaku));
		
		//英語漬け.comから読み込み
		for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q",
			"-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten",
			"-Toeic-Chokuzen", "-Toeic-jukugo",}) {
			WordPhraseData wpd = new WordPhraseData("Eigoduke.com/" + "WordDataEigoduke" + Q, context);
			for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
				if (wpd.e[i] != null && wpd.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("英語漬け" + mapQName.get(Q), wpd.e[i], wpd.j[i], i, WordPhraseData.DataType.eigoduke_com));
		}
		for (int num = 1; num <= 10; num++) {
			String Q = "-toeic (" + num + ")";
			WordPhraseData wpd = new WordPhraseData("Eigoduke.com/" + "WordDataEigoduke" + Q, context);
			for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
				if (wpd.e[i] != null && wpd.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("英語漬け" + "TOEIC" + num, wpd.e[i], wpd.j[i], i, WordPhraseData.DataType.eigoduke_com));
		}
		
		//distinction
		for (int d = 1; d <= 4; d++) {
			WordPhraseData w = new WordPhraseData(WordPhraseData.distinction + "d" + d + "word", context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("Distinction" + d, tangoNumToString("Distinction" + d, i), w.e[i], w.j[i], i, word));
		}
		
		//フレーズ
		for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
			WordPhraseData w = new WordPhraseData(PasstanPhrase + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("パス単" + mapQName.get(Q), tangoNumToString("パス単" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
		}
		
		//単熟語EX単語
		for (String Q : new String[]{"1q", "p1q"}) {
			WordPhraseData w = new WordPhraseData(TanjukugoPhrase + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("単熟語EX" + mapQName.get(Q), tangoNumToString("単熟語EX" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
		}
		
		//distinction
		for (String Q : new String[]{"d1phrase12", "d2phrase1"}) {
			WordPhraseData w = new WordPhraseData(WordPhraseData.distinction + Q, context);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new WordPhraseData.WordInfo("Distinction" + mapQName.get(Q), tangoNumToString("Distinction" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
		}
		
		//SVL12000辞書
		WordPhraseData wordPhraseData = new WordPhraseData(Svl, context);
		for (int i = 1; i < Math.min(wordPhraseData.e.length, wordPhraseData.j.length); i++)
			if (wordPhraseData.e[i] != null && wordPhraseData.j[i] != null)
				allData.add(new WordPhraseData.WordInfo("SVL", Integer.toString((i - 1) / 1000 + 1), wordPhraseData.e[i], wordPhraseData.j[i], i, word));
		
		return allData;
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
		passTan, tanjukugo, tanjukugoEx, yumetan,
	}
	
	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5, y00, y08, y1, y2, y3;
		
		@NonNull
		@Override
		public String toString() {
			switch (this){
				case q1: return "1q";
				case qp1: return "p1q";
				case q2: return "2q";
				case qp2: return "p2q";
				case q3: return "3q";
				case q4: return "4q";
				case q5: return "5q";
				default:return super.toString();
			}
		}
	}
	
	public static DataQ toDataQ(String string){
		switch (string){
			case "1q": return DataQ.q1;
			case "p1q": return DataQ.qp1;
			case "2q": return DataQ.q2;
			case "p2q": return DataQ.qp2;
			case "3q": return DataQ.q3;
			case "4q": return DataQ.q4;
			case "5q": return DataQ.q5;
			case "y00": return DataQ.y00;
			case "y08": return DataQ.y08;
			case "y1": return DataQ.y1;
			case "y2": return DataQ.y2;
			case "y3": return DataQ.y3;
			default:throw new IllegalArgumentException("Qの文字列が不正");
		}
	}
	
	enum DataType {word, phrase, gogengaku, eigoduke_com}
	
	enum DataLang {english, japanese}
	
	public enum q_num {
		test1q(), testp1q(), test2q(), testp2q(), testy00(), testy08(), testy1(), testy2(), testy3(), test1qEx, testp1qEx, testAll;
		
		enum strQ {
			str1q("1q"), strp1q("p1q"), str2q("2q"), strp2q("p2q"), str3q("3q"), str4q("4q"), str5q("5q"), stry00("y00"), stry08("y08"), stry1("y1"), stry2("y2"), stry3("y3"), ex1q("tanjukugo1q"), all("all");
			
			strQ(String s) {
				getQ = s;
			}
			
			final String getQ;
		}
		
		enum mode {
			word, phrase, randomTest, huseikainomiTest, seitouritsujunTest, wordPlusPhrase
		}
		
		enum unit {
			deruA, deruB, deruC, Jukugo, all
		}
		
		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}
		
		q_num() {
		}
	}
	
	public static class HatsuonKigou {
		
		private static final HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();
		
		public static void SetHatsuonKigou(Context context) {
			try {
				//発音記号のためにSVL読み込み
				if (hashMapHatsuonKigou.size() == 0) {
					WordPhraseData wordPhraseDataSVL = new WordPhraseData(Svl, context);
					for (int i = 1; i < Math.min(wordPhraseDataSVL.e.length, wordPhraseDataSVL.j.length); i++)
						if (wordPhraseDataSVL.e[i] != null && wordPhraseDataSVL.j[i] != null)
							hashMapHatsuonKigou.put(wordPhraseDataSVL.e[i], wordPhraseDataSVL.j[i]);
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
		final String category, e, j, subCategory;
		final DataType dataType;
		private static Map<String, String> mapQName = new HashMap<>() {{
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
		
		WordInfo(String category, String e, String j, int localNumber, DataType dataType) {
			this(category, null, e, j, localNumber, dataType);
		}
		
		WordInfo(String category, String subCategory, String e, String j, int localNumber, DataType dataType) {
			size++;
			this.toushiNumber = size;
			this.category = category;
			if (Objects.isNull(subCategory)) this.subCategory = "";
			else this.subCategory = subCategory;
			this.e = e;
			this.j = j;
			this.localNumber = localNumber;
			this.dataType = dataType;
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
				return "No. " + this.toushiNumber
					+ "\nカテゴリ: " + this.category
					+ " " + subCategory + "\n番号:"
					+ this.localNumber
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
}