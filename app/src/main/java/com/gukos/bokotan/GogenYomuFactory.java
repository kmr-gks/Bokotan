package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionHandler.showException;
import static com.gukos.bokotan.MainActivity.wordE;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeMap;

class GogenYomu{
	public String wordEng,wordJpn,bunrui,gogen1,gogen2,gogen3,sankou;
	public GogenYomu(String wordEng,String wordJpn,String bunrui,String gogen1,String gogen2,String gogen3,String sankou){
		this.wordEng=wordEng;
		this.wordJpn=wordJpn;
		this.bunrui=tikan(bunrui);
		this.gogen1=gogen1;
		this.gogen2=gogen2;
		this.gogen3=gogen3;
		this.sankou=sankou;
	}

	public String getGogenString(boolean containsBunrui, boolean containsSankou){
		String ans="語源:"+this.gogen1;
		if (containsBunrui){
			ans="分類:"+this.bunrui+"\n"+ans;
		}
		if (this.gogen2.length()>0) ans+="+"+this.gogen2;
		if (this.gogen3.length()>0) ans+="+"+this.gogen3;
		if (containsSankou&&this.sankou.length()>0) ans+="\n参考:"+this.sankou;
		return ans;
	}

	@NonNull
	public String toString(){
		return "英"+this.wordEng+"\t日"+this.wordJpn+"\t分類"+this.bunrui+"\t語源1"+this.gogen1+"\t語源2"+this.gogen2+"\t語源3"+this.gogen3+"\t参考"+this.sankou;
	}
	private static String tikan(String stringWith_){
		String ans="";
		for(char ch:stringWith_.toCharArray()){
			if(ch=='_') ans+=',';
			else ans+=ch;
		}
		return ans;
	}
}

public class GogenYomuFactory {
	private static final TreeMap<String,GogenYomu> trGogenYomu=new TreeMap<>();
	final String strFileNameYomuGogenGaku = "読む語源学全内容2.csv";

	public GogenYomuFactory(Context context){
		try{
			InputStream is=context.getAssets().open(strFileNameYomuGogenGaku);
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"SJIS"));
			String str;
			while((str=br.readLine())!=null) {
				String[] data=str.split(",");
				trGogenYomu.put(data[2],new GogenYomu(data[2],data[3],data[4],data[5],data[7],data[9],data[11]));
			}
			is.close();
			br.close();
		}catch (Exception e){
			showException(e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル"+ strFileNameYomuGogenGaku +"が見つかりません。").setPositiveButton("ok",null).create().show();
		}
	}
	public TreeMap<String, GogenYomu> getTrGogenYomu(){return trGogenYomu;}

	static String getGogenString(int now, boolean containsBunrui, boolean containsSankou){
		return getGogenString(wordE[now], containsBunrui, containsSankou);
	}
	static String getGogenString(String word, boolean containsBunrui, boolean containsSankou){
		try {
			GogenYomu gy = trGogenYomu.get(word);
			return gy == null ? "" : gy.getGogenString(containsBunrui,containsSankou);
		}catch (Exception e){
			showException(e);
			return "(null string)";
		}
	}
}
