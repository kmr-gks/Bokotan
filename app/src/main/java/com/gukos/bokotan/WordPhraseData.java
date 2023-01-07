package com.gukos.bokotan;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordPhraseData {
	public String[]e=new String[3000],j=new String[3000];
	public WordPhraseData(String strQ, Context context){
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
			Log.d("E/","IOerror"+e.getMessage());
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル"+fileName1+"または"+fileName2+"が見つかりません。").setPositiveButton("ok",null).create().show();
		}
	}
}
