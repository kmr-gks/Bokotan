package com.gukos.bokotan;

public class GogenYomu{
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
	public String getGogenStringShort(){
		String ans=/*"分類:"+this.bunrui+*/"\t語源:"+this.gogen1;
		if (this.gogen2.length()>0) ans+="+"+this.gogen2;
		if (this.gogen3.length()>0) ans+="+"+this.gogen3;
		if (this.sankou.length()>0) ans+="\n参考:"+this.sankou;
		return ans;
	}
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

