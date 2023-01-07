package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.puts;
import static com.gukos.bokotan.MyLibrary.showException;

import android.app.AlertDialog;
import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordPhraseData {
	public final String[]e=new String[20000];
	public final String[] j=new String[20000];
	public final String strQName;
	public final static String
			PasstanWord="Passtan/WordData",
			PasstanPhrase="Passtan/Phrase",
			TanjukugoWord ="TanjukugoEX/Word",
			TanjukugoEXWord ="TanjukugoEX/EXWord",
			TanjukugoPhrase ="TanjukugoEX/Phrase",
			YumeWord="Yumetan/WordDataYume",
			Svl="SVL/SVL12000",
			distinction="distinction/";
	public WordPhraseData(String strQ, Context context){
		this.strQName=strQ;
		String fileName1=strQ+".e.txt",fileName2=strQ+".j.txt";
		try{
			InputStream is1=context.getAssets().open(fileName1),is2=context.getAssets().open(fileName2);
			BufferedReader br1=new BufferedReader(new InputStreamReader(is1)),br2=new BufferedReader(new InputStreamReader(is2));
			String str;
			int i=0;
			while((str=br1.readLine())!=null) {
				e[i]=str;
				i++;
			}
			i=0;
			while((str=br2.readLine())!=null) {
				j[i]=str;
				i++;
			}
			is1.close();
			is2.close();
			br1.close();
			br2.close();
		}catch (Exception e){
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル"+fileName1+"または"+fileName2+"が見つかりません。").setPositiveButton("ok",null).create().show();
		}
	}
}
