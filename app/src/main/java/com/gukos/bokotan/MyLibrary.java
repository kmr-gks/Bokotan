package com.gukos.bokotan;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public final class MyLibrary{
	enum DataBook{
		passTan,tanjukugoEX,yumetan,gogengaku,eigoduke
	}
	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5,y00,y08,y1,y2,y3, toefl,toeic
	}
	enum DataType {word, phrase, gogengaku,eigoduke_com}
	enum DataLang{english,japanese}

	public static final String debug_tag="E/ exception";

	public static void makeToastForShort(Context context, String strMessage){
		Toast.makeText(context,strMessage,Toast.LENGTH_SHORT).show();
	}
	public static void makeToastForLong(Context context,String strMessage){
		Toast.makeText(context,strMessage,Toast.LENGTH_LONG).show();
	}

	public static void putData(Context context, String strFileName, String strKey, boolean value){
		context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).edit().putBoolean(strKey,value).apply();
	}

	public static boolean getData(Context context,String strFileName,String strKey,boolean defaultvalue){
		return context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).getBoolean(strKey,defaultvalue);
	}

	public static void putData(Context context,String strFileName,String strKey,int value){
		context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).edit().putInt(strKey,value).apply();
	}

	public static int getData(Context context,String strFileName,String strKey,int defaultvalue){
		return context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).getInt(strKey,defaultvalue);
	}

	public static void putData(Context context,String strFileName,String strKey,float value){
		context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).edit().putFloat(strKey,value).apply();
	}

	public static float getData(Context context,String strFileName,String strKey,float defaultvalue){
		return context.getSharedPreferences(strFileName,Context.MODE_PRIVATE).getFloat(strKey,defaultvalue);
	}

	public static final class ExceptionHandler {
		static void showException(Exception e){
			Log.e(debug_tag,"メッセージ:"+e.getMessage()+"型名"+e.getClass().getTypeName());
		}

	}
	public static void puts(String str){
		Log.d(debug_tag,str);
	}

	public static String getPath(DataBook dataBook, DataQ dataQ, DataType dataType, DataLang dataLang, int tangoNum){
		return getPath(dataBook,dataQ,dataType,dataLang,tangoNum,true);
	}
	public static String getPath(
			DataBook dataBook,
			DataQ dataQ,
			DataType dataType,
			DataLang dataLang,
			int tangoNum,
			boolean dirTougou){
		puts("getPath:"+
				"\nbook="+dataBook+
				"\nq="+dataQ+
				"\ntype"+dataType+
				"\nlang"+dataLang+
				"\nnum"+tangoNum+
				"\ntougou="+dirTougou);
		String path="/storage/emulated/0/Download/data/";
		String q,type,lang;
		switch(dataQ){
			case q1:{
				q="1q";
				break;
			}
			case qp1:{
				q="p1q";
				break;
			}
			case q2:{
				q="2q";
				break;
			}
			case qp2:{
				q="p2q";
				break;
			}
			case q3:{
				q="3q";
				break;
			}
			case q4:{
				q="4q";
				break;
			}
			case q5:{
				q="5q";
				break;
			}
			case y00:
			case y08:
			case y1:
			case y2:
			case y3:{
				q=dataQ.toString();
				break;
			}
			default:return null;
		}
		puts("aaa");
		switch (dataBook){
			case passTan:{
				if (dataType==DataType.word&&dataLang==DataLang.english) type="英";
				else if (dataType==DataType.word&&dataLang==DataLang.japanese) type="訳";
				else if (dataType==DataType.phrase&&dataLang==DataLang.english) type="例";
				else if (dataType==DataType.phrase&&dataLang==DataLang.japanese) type="日";
				else return null;
				if (dataType==DataType.phrase&&!dirTougou) q="ph"+q;
				path+=q+String.format("/%04d",tangoNum)+type+".mp3";
				puts("databook:"+path);
				break;
			}
			case yumetan:{
				if (dataType==DataType.word&&dataLang==DataLang.english) type="W英";
				else if (dataType==DataType.word&&dataLang==DataLang.japanese) type="W日";
				else if (dataType==DataType.phrase&&dataLang==DataLang.english) type="P英";
				else if (dataType==DataType.phrase&&dataLang==DataLang.japanese) type="P日";
				else return null;
				path+=q+"/"+type+String.format("%04d", tangoNum)+".mp3";
				break;
			}
			case tanjukugoEX:{
				if (dataType==DataType.word&&dataLang==DataLang.english) type="英語";
				else if (dataType==DataType.word&&dataLang==DataLang.japanese) type="日本語";
				else if (dataType==DataType.phrase&&dataLang==DataLang.english) type="例文";
				else if (dataType==DataType.phrase&&dataLang==DataLang.japanese) type=null;
				else return null;
				path+="tanjukugo"+q+"/"+type+String.format("%04d",tangoNum)+".mp3";
				break;
			}
			default:
				return null;
		}
		puts("path="+path);
		return path;
	}

}
