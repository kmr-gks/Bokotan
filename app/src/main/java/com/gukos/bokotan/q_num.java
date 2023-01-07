package com.gukos.bokotan;

public enum q_num {
	test1q(0), testp1q(1),test2q(2),testp2q(3),test3q(4),test4q(5),test5q(6);
	enum strQ{
		str1q("1q"),strp1q("p1q"),str2q("2q"),strp2q("p2q"),str3q("3q"),str4q("4q"),str5q("5q");
		private strQ(String s){
			getQ=s;
		}
		String getQ;
	}
	enum mode{
		word,phrase,test,exTest,sortTest
	}
	enum unit{
		deruA,deruB,deruC,Jukugo,all
	}
	enum shurui{
		verb,noum,adjective,matome
	}

	private q_num(int id) {}
}
