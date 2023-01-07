package com.gukos.bokotan;

public enum q_num {
	test1q(0), testp1q(1),test2q(2),testp2q(3),test3q(4),test4q(5),test5q(6),testy00(7),testy08(8),testy1(9),testy2(10),testy3(11);
	enum strQ{
		str1q("1q"),strp1q("p1q"),str2q("2q"),strp2q("p2q"),str3q("3q"),str4q("4q"),str5q("5q"),stry00("y00"),stry08("y08"),stry1("y1"),stry2("y2"),stry3("y3");
		private strQ(String s){
			getQ=s;
		}
		String getQ;
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

	private q_num(int id) {}
}
