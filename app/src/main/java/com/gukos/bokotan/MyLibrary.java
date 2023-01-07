package com.gukos.bokotan;

import static android.content.Context.MODE_PRIVATE;
import static com.gukos.bokotan.CommonVariables.toFindFromAndTo;

import static java.lang.Math.min;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public final class MyLibrary {

	public static final String stringDownloadPath = "/storage/emulated/0/Download/";
	public static final String stringBokotanDirPath=stringDownloadPath+"bokotan/";
	public static final String strGaibuDataDirectory = stringDownloadPath + "data/";
	public static final String strExceptionFIlePath = stringBokotanDirPath + "exceptions.txt";
	public static final String packageName=MyLibrary.class.getPackage().getName();

	public static final class ExceptionManager {

		public static final String debug_tag = "tagexception";
		
		public static void showException(Context context, Exception e, String stringAdditional) {
			//スタックトレースを文字列に変換
			//https://arsinput.hatenablog.jp/entry/2020/11/21/120000

			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));

			String strMessage =
					"例外:" + stringAdditional + "\nメッセージ:" + e.getMessage() + "\n型名(" + e.getClass().getTypeName() +")\nメソッド名"+MyLibrary.DisplayOutput.getClassName(5)+MyLibrary.DisplayOutput.getMethodName(5)+ "\n発生箇所\n" + stringWriter;
			Log.e(debug_tag, strMessage);
			if (context != null) DisplayOutput.makeToastForLong(context, strMessage);

			try {
				if (Files.notExists(Paths.get(strExceptionFIlePath))) {
					//ファイルがなければ新規作成
					new File(strExceptionFIlePath).createNewFile();
				}
				FileWriter fileWriter = new FileWriter(strExceptionFIlePath, true);
				fileWriter.write(getNowTime() + "\n" + strMessage + "\n\n");
				fileWriter.close();
			} catch (Exception exception) {
				if (context != null) {
					Log.d(debug_tag + "filewriting", exception.getMessage() + exception.getClass().getTypeName());
					DisplayOutput.makeToastForLong(context, exception.getMessage() + "\n" + exception.getClass().getTypeName());
				}
			}
		}
		
		public static void showException(Context context, Exception e) {
			showException(context, e, "");
		}
		
		public static void showException(Exception e) {
			showException(null, e, "");
		}
		
		public static void showException(Exception e, String stringAdditional) {
			showException(null, e, stringAdditional);
		}
	}

	public static final class PreferenceManager{

		static final String fnAppSettings="appsettings";

		public static String getAllPreferenceDate(Context context,String strFileName) {
			try {
				var data = context.getSharedPreferences(strFileName, MODE_PRIVATE).getAll();
				JSONObject jsonObject = new JSONObject();
				for (var entry : data.entrySet()) {
					jsonObject.put(entry.getKey(), entry.getValue());
				}
				return jsonObject.toString();
			} catch (Exception e) {
				ExceptionManager.showException(e);
				return "共有プリファレンスのデータの取得に失敗";
			}
		}

		public static void putSetting(Context context,String strKey,boolean value) {
			MyLibrary.PreferenceManager.putBoolData(context, PreferenceManager.fnAppSettings, strKey, value);
		}

		public static boolean getSetting(Context context,String strKey,boolean defaultvalue) {
			return MyLibrary.PreferenceManager.getBoolData(context, PreferenceManager.fnAppSettings, strKey, defaultvalue);
		}

		public static void putBoolData(Context context, String strFileName, String strKey, boolean value) {
			try {
				if (strFileName.contains("1q")) DisplayOutput.puts("strFileName="+strFileName);
				context.getSharedPreferences(strFileName, MODE_PRIVATE).edit().putBoolean(strKey, value).apply();
			} catch (Exception e) {
				ExceptionManager.showException(context, e);
			}
		}

		public static boolean getBoolData(Context context, String strFileName, String strKey, boolean defaultvalue) {
			return context.getSharedPreferences(strFileName, MODE_PRIVATE).getBoolean(strKey, defaultvalue);
		}

		public static void putIntData(Context context, String strFileName, String strKey, int value) {
			try {
				if (strFileName.contains("1q")) DisplayOutput.puts("strFileName="+strFileName);
				context.getSharedPreferences(strFileName, MODE_PRIVATE).edit().putInt(strKey, value).apply();
			} catch (Exception e) {
				ExceptionManager.showException(context, e);
			}
		}

		public static int getIntData(Context context, String strFileName, String strKey, int defaultvalue) {
			return context.getSharedPreferences(strFileName, MODE_PRIVATE).getInt(strKey, defaultvalue);
		}

		public static void initializeSettingItem(Switch view,boolean defaultvalue) {
			try {
				view.setChecked(MyLibrary.PreferenceManager.getSetting(view.getContext(), "id" + view.getId(), defaultvalue));
			} catch (Exception e) {
				ExceptionManager.showException(e);
			}
		}

		public static void initializeSettingItem(CheckBox view,boolean defaultvalue) {
			try {
				view.setChecked(MyLibrary.PreferenceManager.getSetting(view.getContext(), "id" + view.getId(), defaultvalue));
			} catch (Exception e) {
				ExceptionManager.showException(e);
			}
		}

		public static void onClickSettingItem(View view) {
			try {
				if (view instanceof Switch)
					MyLibrary.PreferenceManager.putSetting(view.getContext(), "id" + view.getId(), ((Switch) view).isChecked());
				if (view instanceof CheckBox)
					MyLibrary.PreferenceManager.putSetting(view.getContext(), "id" + view.getId(), ((CheckBox) view).isChecked());
			} catch (Exception e) {
				ExceptionManager.showException(e);
			}
		}

		public static class DataName {
			public static String dnTestActivity = "testActivity";
			public static String dnQSentakuActivity = "qSentakuActivity";
			public static String 現在何問目 = "nGenzaiNanMonme", 単語正解数 = "nWordSeikaisuu", 単語不正解数 = "nWordHuseikaisuu";
		}
	}

	public static final class FileDirectoryManager {
		public static String strDirectoryNameForKuuhaku = "";
		
		public static FileWriter openWriteFileWithExistCheck(Context context, String stringFileName,boolean append) {
			try {
				if (Files.notExists(Paths.get(stringBokotanDirPath))) {
					//フォルダがなければ作成
					new File(stringBokotanDirPath).mkdirs();
				}
				if (Files.notExists(Paths.get(stringFileName))) {
					//ファイルがなければ新規作成
					new File(stringFileName).createNewFile();
				}
				return new FileWriter(stringFileName, append);
			} catch (Exception exception) {
				ExceptionManager.showException(context, exception);
				return null;
			}
		}

		public static String getPath(WordPhraseData.DataBook dataBook, WordPhraseData.DataQ dataQ, WordPhraseData.DataType dataType, WordPhraseData.DataLang dataLang, int tangoNum) {
			try {
				return getPath(dataBook, dataQ, dataType, dataLang, tangoNum, true);
			} catch (Exception e) {
				ExceptionManager.showException(e);
				return "<不明>";
			}
		}

		public static String getPath(WordPhraseData.DataBook dataBook, String str, WordPhraseData.DataType dataType, WordPhraseData.DataLang dataLang, int tangoNum) {
			try {
				return getPath(dataBook, str, dataType, dataLang, tangoNum, true);
			} catch (Exception e) {
				ExceptionManager.showException(e);
				return "<不明>";
			}
		}

		public static String getPath(WordPhraseData.DataBook dataBook, WordPhraseData.DataQ dataQ, WordPhraseData.DataType dataType, WordPhraseData.DataLang dataLang, int tangoNum, boolean dirTougou) {
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
				ExceptionManager.showException(e);
			}
			return "<不明>";
		}

		public static String getPath(WordPhraseData.DataBook dataBook, String strDataQ, WordPhraseData.DataType dataType, WordPhraseData.DataLang dataLang, int tangoNum, boolean dirTougou) {
			try {
				String path = strGaibuDataDirectory;
				String type;
				switch (dataBook) {
					case passTan: {
						if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.english) type = "英";
						else if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.japanese) type = "訳";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.english)
							type = "例";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.japanese)
							type = "日";
						else return null;
						if (dataType == WordPhraseData.DataType.phrase && !dirTougou && !strDataQ.startsWith("ph"))
							strDataQ = "ph" + strDataQ;
						strDataQ = strDirectoryNameForKuuhaku + strDataQ;
						path += strDataQ + String.format("/%04d", tangoNum) + type + ".mp3";
						break;
					}
					case yumetan: {
						if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.english) type = "W英";
						else if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.japanese)
							type = "W日";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.english)
							type = "P英";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.japanese)
							type = "P日";
						else return null;
						strDataQ = strDirectoryNameForKuuhaku + strDataQ;
						path += strDataQ + "/" + type + String.format("%04d", tangoNum) + ".mp3";
						break;
					}
					case tanjukugoEX: {
						if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.english) type = "英語";
						else if (dataType == WordPhraseData.DataType.word && dataLang == WordPhraseData.DataLang.japanese)
							type = "日本語";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.english)
							type = "例文";
						else if (dataType == WordPhraseData.DataType.phrase && dataLang == WordPhraseData.DataLang.japanese)
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
				ExceptionManager.showException(e);
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
				ExceptionManager.showException(e);
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
				ExceptionManager.showException(e);
				return "<不明>";
			}
		}
	}

	public static final class DisplayOutput {
		private static final int nMaxLengthOfToastString=50;
		
		public static void makeToastForShort(Context context, String strMessage) {
			try {
				Toast.makeText(context, strMessage.substring(0, min(strMessage.length(), nMaxLengthOfToastString)), Toast.LENGTH_SHORT).show();
			} catch (Exception e) {}
		}

		public static void makeToastForLong(Context context, String strMessage) {
			try {
				Toast.makeText(context, strMessage.substring(0, min(strMessage.length(), nMaxLengthOfToastString)), Toast.LENGTH_LONG).show();
			} catch (Exception e) {}
		}

		public static void puts(String str) {
			try {
				Log.d(ExceptionManager.debug_tag+" "+DisplayOutput.getDeviceName(), str);
			} catch (Exception e) {
				//showException(e);
			}
		}

		public static String getClassName(int hierarchyOfStack){
			//return new Object(){}.getClass().getEnclosingClass().getSimpleName();
			return Thread.currentThread().getStackTrace()[hierarchyOfStack].getClassName().substring(packageName.length()+1)+"#";
		}

		public static String getClassName(){
			return getClassName(4);
		}

		public static String getMethodName(int hierarchyOfStack){
			//return new Object(){}.getClass().getEnclosingMethod().getName();
			return Thread.currentThread().getStackTrace()[hierarchyOfStack].getMethodName();
		}

		public static String getMethodName(){
			return getMethodName(4);
		}
		
		public static String getDeviceName(){return Build.PRODUCT+","+Build.VERSION.RELEASE;}

		public static CharSequence setStringColored(String stringSource, String stringKey) {
			try {
				if (stringKey == null || stringKey.equals("")) return stringSource;
				stringSource = stringSource.replaceAll(System.getProperty("line.separator"), "<br>");
				int index = stringSource.indexOf(stringKey);
				if (index == -1) return stringSource;
				stringSource = stringSource.substring(0, index) + "<font color=\"Red\">" + stringKey + "</font>" + stringSource.substring(index + stringKey.length());
				return HtmlCompat.fromHtml(stringSource, HtmlCompat.FROM_HTML_MODE_COMPACT);
			} catch (Exception e) {
				ExceptionManager.showException(e);
				return stringSource;
			}
		}
	}
	
	public static String getNowTime() {
		return getNowTime("yyyy年MM月dd日 H:mm");
	}
	
	public static String getNowTime(String stringFormat) {
		try {
			return DateTimeFormatter.ofPattern(stringFormat).format(LocalDateTime.now(ZoneId.of("Asia" + "/Tokyo")));
		} catch (Exception e) {
			//showException(e);
			return "時刻:<不明>";
		}
	}

	public static String getBuildDate(Context context) {
		//https://ingaouhou.com/archives/3786
		//https://takusan.negitoro.dev/posts/android_build_date_resource/
		try {
			return " Build:" + context.getString(R.string.buildDate);
		} catch (Exception e) {
			ExceptionManager.showException(context, e);
			return " Build:不明";
		}
	}

	public static String tangoNumToString(String category, int num) {
		try {
			String ans = null;
			String[] derudo = {"出る度A", "出る度B", "出る度C", "熟語"};
			String[] hinsi = {"動詞", "名詞", "形容詞", ""};
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
			ExceptionManager.showException(e);
			return "<不明>";
		}
	}

	private static boolean isIn(int num, int from, int to) {
		return from <= num && num <= to;
	}
	
	public static void sleep(){
		try {
			sleep(100);
		}catch (Exception exception){
			MyLibrary.ExceptionManager.showException(exception);
		}
	}
	
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		}catch (Exception exception){
			MyLibrary.ExceptionManager.showException(exception);
		}
	}
}