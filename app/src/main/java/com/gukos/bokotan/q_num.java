package com.gukos.bokotan;

public enum q_num {
	test1q(), testp1q(),test2q(),testp2q(),test3q(),test4q(),test5q(),testy00(),testy08(),testy1(),testy2(),testy3();
	enum strQ{
		str1q("1q"),strp1q("p1q"),str2q("2q"),strp2q("p2q"),str3q("3q"),str4q("4q"),str5q("5q"),stry00("y00"),stry08("y08"),stry1("y1"),stry2("y2"),stry3("y3");
		strQ(String s){
			getQ=s;
		}
		final String getQ;
	}
	enum mode{
		word,phrase,test,exTest,sortTest,wordPlusPhrase
	}
	enum unit{
		deruA,deruB,deruC,Jukugo,all
	}
	enum shurui{
		verb,noum,adjective,matome
	}
	enum skipjouken{
		kirokunomi,seikai1, huseikai2
	}

	q_num() {}
}
