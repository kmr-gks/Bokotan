package com.gukos.bokotan;

import static android.content.Context.MODE_PRIVATE;
import static com.gukos.bokotan.MainActivity.toFindFromAndTo;
import static com.gukos.bokotan.WordPhraseData.Svl;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Objects;

public final class MyLibrary {

	public static String strDirectoryNameForKuuhaku = "";

	static void showException(Context context, Exception e, String stringAdditional) {
		//スタックトレースを文字列に変換
		//https://arsinput.hatenablog.jp/entry/2020/11/21/120000

		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		String strStackTrace = stringWriter.toString();

		String strMessage = "例外:" + stringAdditional + "\nメッセージ:" + e.getMessage() + "\n型名(" + e.getClass().getTypeName() + ")\n発生箇所\n" + strStackTrace;
		Log.e(debug_tag, strMessage);
		if (context != null) makeToastForLong(context, strMessage.substring(0, 50));

		try {
			String strExceptionFIlePath = strGaibuDataDirectory + "exceptions.txt";
			if (Files.notExists(Paths.get(strExceptionFIlePath))) {
				//ファイルがなければ新規作成
				boolean r = new File(strExceptionFIlePath).createNewFile();
			}
			FileWriter fileWriter = new FileWriter(strExceptionFIlePath, true);
			fileWriter.write(getNowTime() + "\n" + strMessage + "\n\n");
			fileWriter.close();
		} catch (Exception exception) {
			if (context != null)
				makeToastForLong(context, exception.getMessage() + "\n" + exception.getClass().getTypeName());
		}
	}

	static void showException(Context context, Exception e) {
		showException(context, e, "");
	}

	static void showException(Exception e) {
		showException(null, e, "");
	}

	static void showException(Exception e, String stringAdditional) {
		showException(null, e, stringAdditional);
	}

	static String getNowTime() {
		try {
			return LocalDateTime.now(ZoneId.of("Asia/Tokyo")).toString();
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	enum DataBook {
		passTan, tanjukugoEX, yumetan, gogengaku, eigoduke
	}

	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5, y00, y08, y1, y2, y3, toefl, toeic
	}

	enum DataType {word, phrase, gogengaku, eigoduke_com}

	enum DataLang {english, japanese}

	public static final String debug_tag = "tagexception";
	private static final String strGaibuDataDirectory = "/storage/emulated/0/Download/data/";

	public static void makeToastForShort(Context context, String strMessage) {
		try {
			Toast.makeText(context, strMessage, Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			showException(e);
		}
	}

	public static void makeToastForLong(Context context, String strMessage) {
		try {
			Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			showException(e);
		}
	}

	public static class DataName {
		public static String testActivity = "testActivity";
		public static String qSentakuActivity = "qSentakuActivity";
		public static String nGenzaiNanMonme = "nGenzaiNanMonme", 単語正解数 = "nWordSeikaisuu", 単語不正解数 = "nWordHuseikaisuu";
	}

	public static void putBoolData(Context context, String strFileName, String strKey, boolean value) {
		try {
			context.getSharedPreferences(strFileName, MODE_PRIVATE).edit().putBoolean(strKey, value).apply();
		} catch (Exception e) {
			showException(e);
		}
	}

	public static boolean getBoolData(Context context, String strFileName, String strKey, boolean defaultvalue) {
		return context.getSharedPreferences(strFileName, MODE_PRIVATE).getBoolean(strKey, defaultvalue);
	}

	public static void putIntData(Context context, String strFileName, String strKey, int value) {
		try {
			context.getSharedPreferences(strFileName, MODE_PRIVATE).edit().putInt(strKey, value).apply();
		} catch (Exception e) {
			showException(e);
		}
	}

	public static int getIntData(Context context, String strFileName, String strKey, int defaultvalue) {
		return context.getSharedPreferences(strFileName, MODE_PRIVATE).getInt(strKey, defaultvalue);
	}

	public static void puts(String str) {
		try {
			Log.d(debug_tag, str);
		} catch (Exception e) {
			showException(e);
		}
	}

	public static String getPath(DataBook dataBook, DataQ dataQ, DataType dataType, DataLang dataLang, int tangoNum) {
		try {
			return getPath(dataBook, dataQ, dataType, dataLang, tangoNum, true);
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	public static String getPath(DataBook dataBook, String str, DataType dataType, DataLang dataLang, int tangoNum) {
		try {
			return getPath(dataBook, str, dataType, dataLang, tangoNum, true);
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	public static String getPath(DataBook dataBook, DataQ dataQ, DataType dataType, DataLang dataLang, int tangoNum, boolean dirTougou) {
		try {
			String q;
			switch (dataQ) {
				case q1: {
					q = "1q";
					break;
				}
				case qp1: {
					q = "p1q";
					break;
				}
				case q2: {
					q = "2q";
					break;
				}
				case qp2: {
					q = "p2q";
					break;
				}
				case q3: {
					q = "3q";
					break;
				}
				case q4: {
					q = "4q";
					break;
				}
				case q5: {
					q = "5q";
					break;
				}
				case y00:
				case y08:
				case y1:
				case y2:
				case y3: {
					q = dataQ.toString();
					break;
				}
				default:
					return null;
			}
			return getPath(dataBook, q, dataType, dataLang, tangoNum, dirTougou);
		} catch (Exception e) {
			showException(e);
		}
		return "<不明>";
	}

	public static String getPath(DataBook dataBook, String strDataQ, DataType dataType, DataLang dataLang, int tangoNum, boolean dirTougou) {
		try {
			String path = strGaibuDataDirectory;
			String type;
			switch (dataBook) {
				case passTan: {
					if (dataType == DataType.word && dataLang == DataLang.english) type = "英";
					else if (dataType == DataType.word && dataLang == DataLang.japanese) type = "訳";
					else if (dataType == DataType.phrase && dataLang == DataLang.english)
						type = "例";
					else if (dataType == DataType.phrase && dataLang == DataLang.japanese)
						type = "日";
					else return null;
					if (dataType == DataType.phrase && !dirTougou && !strDataQ.startsWith("ph"))
						strDataQ = "ph" + strDataQ;
					strDataQ = strDirectoryNameForKuuhaku + strDataQ;
					path += strDataQ + String.format("/%04d", tangoNum) + type + ".mp3";
					break;
				}
				case yumetan: {
					if (dataType == DataType.word && dataLang == DataLang.english) type = "W英";
					else if (dataType == DataType.word && dataLang == DataLang.japanese)
						type = "W日";
					else if (dataType == DataType.phrase && dataLang == DataLang.english)
						type = "P英";
					else if (dataType == DataType.phrase && dataLang == DataLang.japanese)
						type = "P日";
					else return null;
					strDataQ = strDirectoryNameForKuuhaku + strDataQ;
					path += strDataQ + "/" + type + String.format("%04d", tangoNum) + ".mp3";
					break;
				}
				case tanjukugoEX: {
					if (dataType == DataType.word && dataLang == DataLang.english) type = "英語";
					else if (dataType == DataType.word && dataLang == DataLang.japanese)
						type = "日本語";
					else if (dataType == DataType.phrase && dataLang == DataLang.english)
						type = "例文";
					else if (dataType == DataType.phrase && dataLang == DataLang.japanese)
						type = "例文日本語";
					else return null;
					path += strDataQ + "/" + getFileNameForTanjukugoEX(type, strDataQ, tangoNum) + ".mp3";
					break;
				}
				default:
					return null;
			}
			return path;
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	public static String getJosiPath(char joshi) {
		try {
			String path = strGaibuDataDirectory + "postpositional/";
			switch (joshi) {
				case 'を': {
					path += "wo";
					break;
				}
				case 'に': {
					path += "ni";
					break;
				}
				case 'の': {
					path += "no";
					break;
				}
				case 'で': {
					path += "de";
					break;
				}
				default:
					return null;
			}
			path += ".mp3";
			return path;
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	private static String getFileNameForTanjukugoEX(String strType, String strQTanjukugo, int num) {
		try {
			String ans = null;
			final int[][] dfNum = {
					{46, 47, 49, 44, 39, 39, 43, 44, 49, 47},
					{57, 56, 56, 48, 40, 45, 48, 45, 38, 47},};
			switch (strQTanjukugo) {
				case "tanjukugo" + "1q": {
					if (num <= 2364) {
						ans = strType + String.format("%04d", num);
						break;
					}
					if (Objects.equals(strType, "日本語")) {
						strType = "DF日";
					} else if (Objects.equals(strType, "英語")) {
						strType = "DF英";
					}
					num -= 2364;
					for (int i = 0; i < dfNum[0].length; i++) {
						if (num <= dfNum[0][i]) {
							ans = strType + String.format("%02d_%02d", i + 1, num);
							break;
						} else num -= dfNum[0][i];
					}
					break;
				}
				case "tanjukugo" + "p1q": {
					if (num <= 1920) {
						ans = strType + String.format("%04d", num);
						break;
					}
					if (Objects.equals(strType, "日本語")) {
						strType = "DF日";
					} else if (Objects.equals(strType, "英語")) {
						strType = "DF英";
					}
					num -= 1920;
					for (int i = 0; i < dfNum[1].length; i++) {
						if (num <= dfNum[1][i]) {
							ans = strType + String.format("%02d_%02d", i + 1, num);
							break;
						} else num -= dfNum[1][i];
					}
					break;
				}
			}
			return ans;
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	public enum q_num {
		test1q(), testp1q(), test2q(), testp2q(), test3q(), test4q(), test5q(), testy00(), testy08(), testy1(), testy2(), testy3(), test1qEx, testp1qEx;

		enum strQ {
			str1q("1q"), strp1q("p1q"), str2q("2q"), strp2q("p2q"), str3q("3q"), str4q("4q"), str5q("5q"), stry00("y00"), stry08("y08"), stry1("y1"), stry2("y2"), stry3("y3"), ex1q("tanjukugo1q");

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

		enum shurui {
			verb, noum, adjective, matome
		}

		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}

		q_num() {
		}
	}

	public static String tangoNumToString(String category, int num) {
		try {
			String ans = null;
			String[] derudo = {"出る度A", "出る度B", "出る度C", "熟語"};
			String[] hinsi = {"動詞", "名詞", "形容詞", ""};
			int subCategoryNum = 0;
			if (category.startsWith("パス単")) {
				int qnum;
				switch (category) {
					case "パス単" + "1級": {
						qnum = 0;
						break;
					}
					case "パス単" + "準1級": {
						qnum = 1;
						break;
					}
					case "パス単" + "2級": {
						qnum = 2;
						break;
					}
					case "パス単" + "準2級": {
						qnum = 3;
						break;
					}
					default:
						return null;
				}
				for (int i = 0; i < 10; i++) {
					if (isIn(num, toFindFromAndTo[qnum][i][0], toFindFromAndTo[qnum][i][1])) {
						ans = derudo[i / 3];
						if (i != 9) ans += hinsi[i % 3];
						break;
					}
				}
			}
			if (category.startsWith("単熟語EX")) {
				int qnum;
				switch (category) {
					case "単熟語EX" + "1級": {
						qnum = 12;
						break;
					}
					case "単熟語EX" + "準1級": {
						qnum = 13;
						break;
					}
					default:
						return null;
				}
				for (int i = 0; i <= 10; i++) {
					if (isIn(num, toFindFromAndTo[qnum][i][0], toFindFromAndTo[qnum][i][1])) {
						ans = "Unit" + (i + 1);
						if (i == 10) ans = "UnitEX";
						break;
					}
				}
			}
			return ans;
		} catch (Exception e) {
			showException(e);
			return "<不明>";
		}
	}

	private static boolean isIn(int num, int from, int to) {
		return from <= num && num <= to;
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
				return "<不明>";
			}
		}
	}

	public static CharSequence setStringColored(String stringSource, String stringKey) {
		try {
			if (stringKey == null || stringKey.equals("")) return stringSource;
			stringSource = stringSource.replaceAll(System.getProperty("line.separator"), "<br>");
			int index = stringSource.indexOf(stringKey);
			if (index == -1) return stringSource;
			stringSource = stringSource.substring(0, index) + "<font color=\"Red\">" + stringKey + "</font>" + stringSource.substring(index + stringKey.length());
			return HtmlCompat.fromHtml(stringSource, HtmlCompat.FROM_HTML_MODE_COMPACT);
		} catch (Exception e) {
			showException(e);
			return stringSource;
		}
	}
}