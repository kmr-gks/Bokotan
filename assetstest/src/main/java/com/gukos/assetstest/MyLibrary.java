package com.gukos.assetstest;

import static java.lang.Math.min;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.text.HtmlCompat;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MyLibrary {
	public static final String
		stringDownloadPath = "/storage/emulated/0/Download/",
		stringBokotanDirPath = stringDownloadPath + "bokotan/",
		strGaibuDataDirectory = stringDownloadPath + "data/",
		strExceptionFIlePath = stringBokotanDirPath + "exceptions.txt",
		packageName = MyLibrary.class.getPackage().getName();
	public static final class ExceptionManager {
		
		public static final String debug_tag = "tagexception";
		
		public static void showException(Context context, Exception e, String stringAdditional) {
			//スタックトレースを文字列に変換
			//https://arsinput.hatenablog.jp/entry/2020/11/21/120000
			
			StringWriter stringWriter = new StringWriter();
			e.printStackTrace(new PrintWriter(stringWriter));
			
			String strMessage =
				"例外 " + stringAdditional + "\nメッセージ:" + e.getMessage() + "\n型名 " + e.getClass().getTypeName() + "\nクラス " + DebugManager.getClassName(5) + "\nメソッド " + DebugManager.getMethodName(5) + "\n発生箇所\n" + stringWriter;
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
	public static final class DisplayOutput {
		private static final int nMaxLengthOfToastString = 50;
		
		public static void makeToastForShort(Context context, String strMessage) {
			try {
				Toast.makeText(context, strMessage.substring(0, min(strMessage.length(), nMaxLengthOfToastString)), Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				DebugManager.printCurrentState(e.getMessage());
			}
		}
		
		public static void makeToastForLong(Context context, String strMessage) {
			try {
				Toast.makeText(context, strMessage.substring(0, min(strMessage.length(), nMaxLengthOfToastString)), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				DebugManager.printCurrentState(e.getMessage());
			}
		}
		
		public static CharSequence setStringColored(String source, String key) {
			try {
				if (key == null || key.equals("") || !source.contains(key)) return source;
				source = source.replaceAll(System.getProperty("line.separator"), "<br>");
				int index = source.indexOf(key);
				source = source.substring(0, index) + "<font color=\"Red\">" + key + "</font>" + source.substring(index + key.length());
				return HtmlCompat.fromHtml(source, HtmlCompat.FROM_HTML_MODE_COMPACT);
			} catch (Exception e) {
				ExceptionManager.showException(e);
				return source;
			}
		}
	}
	
	public static final class DebugManager {
		private static final int defaultHierarchy = 4;
		
		public static void puts(String str) {
			try {
				Log.d(ExceptionManager.debug_tag + " " + getDeviceName(), str);
			} catch (Exception e) {
				//showException(e);
			}
		}
		
		public static void putsE(String str) {
			try {
				Log.e(ExceptionManager.debug_tag + " " + getDeviceName(), str);
			} catch (Exception e) {
				//showException(e);
			}
		}
		
		public static String getClassName(int hierarchyOfStack) {
			//return new Object(){}.getClass().getEnclosingClass().getSimpleName();
			return Thread.currentThread().getStackTrace()[hierarchyOfStack].getClassName().substring(packageName.length() + 1);
		}
		
		public static String getClassName() {
			return getClassName(defaultHierarchy);
		}
		
		public static String getMethodName(int hierarchyOfStack) {
			//return new Object(){}.getClass().getEnclosingMethod().getName();
			//return Thread.currentThread().getStackTrace()[hierarchyOfStack].getMethodName();
			String name=Thread.currentThread().getStackTrace()[hierarchyOfStack].getMethodName();
			if (name.startsWith("lambda$")) {
				name="lambda";
			}
			return name;
		}
		
		public static String getMethodName() {
			return getMethodName(defaultHierarchy);
		}
		
		public static int getNowLine(int hierarchyOfStack) {
			return Thread.currentThread().getStackTrace()[hierarchyOfStack].getLineNumber();
		}
		
		public static int getNowLine() {
			return getNowLine(defaultHierarchy);
		}
		
		public static String getNowThreadName() {
			String name = Thread.currentThread().getName();
			if (name.startsWith("Thread")) {
				name = name.substring(6);
			}
			return name;
		}
		
		public static void printCurrentState() {
			//引数のdefaultHierarchyを省略すると正しく動作しない。
			DebugManager.puts("スレッド" + getNowThreadName() + ",クラス" + DebugManager.getClassName(defaultHierarchy) + ",メソッド" + DebugManager.getMethodName(defaultHierarchy) + ",行" + DebugManager.getNowLine(defaultHierarchy) + ", ");
		}
		
		public static void printCurrentState(String string) {
			//引数のdefaultHierarchyを省略すると正しく動作しない。
			DebugManager.puts("スレッド" + getNowThreadName() + ",クラス" + DebugManager.getClassName(defaultHierarchy) + ",メソッド" + DebugManager.getMethodName(defaultHierarchy) + ",行" + DebugManager.getNowLine(defaultHierarchy) + ", " + string);
		}
		
		public static void printCurrentState(Object object) {
			//引数のdefaultHierarchyを省略すると正しく動作しない。
			DebugManager.puts("スレッド" + getNowThreadName() + ",クラス" + DebugManager.getClassName(defaultHierarchy) + ",メソッド" + DebugManager.getMethodName(defaultHierarchy) + ",行" + DebugManager.getNowLine(defaultHierarchy) + ", " + object);
		}
		
		public static String getDeviceName() {return Build.PRODUCT + "," + Build.VERSION.RELEASE;}
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
}