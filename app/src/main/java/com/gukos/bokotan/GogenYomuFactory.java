package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.Q_sentaku_activity.trGogenYomu;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;



public class GogenYomuFactory {
	private static TreeMap<String,GogenYomu> trGogenYomu=new TreeMap<>();
	private String strFileNameYomuGogenGaku="読む語源学全内容2.csv";
	public GogenYomuFactory(Context context){
		try{
			InputStream is=context.getAssets().open(strFileNameYomuGogenGaku);
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"SJIS"));
			String str,text="";
			int i=0;
			while((str=br.readLine())!=null) {
				String[] data=str.split(",");
				trGogenYomu.put(data[2],new GogenYomu(data[2],data[3],data[4],data[5],data[7],data[9],data[11]));
				i++;
			}
			is.close();
			br.close();
		}catch (Exception e){
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル"+strFileNameYomuGogenGaku+"が見つかりません。").setPositiveButton("ok",null).create().show();
		}
	}
	public TreeMap<String, GogenYomu> getTrGogenYomu(){return trGogenYomu;}

	static String getGogenString(int now){
		GogenYomu gy=trGogenYomu.get(wordE[now]);
		return gy==null?"":gy.getGogenStringShort();
	}
}
