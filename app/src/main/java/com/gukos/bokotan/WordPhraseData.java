package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.app.AlertDialog;
import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class WordPhraseData {
	public final String[] e = new String[20000];
	public final String[] j = new String[20000];
	public final static String
			PasstanWord = "Passtan/WordData",
			PasstanPhrase = "Passtan/Phrase",
			TanjukugoWord = "TanjukugoEX/Word",
			TanjukugoEXWord = "TanjukugoEX/EXWord",
			TanjukugoPhrase = "TanjukugoEX/Phrase",
			YumeWord = "Yumetan/WordDataYume",
			Svl = "SVL/SVL12000",
			distinction = "distinction/";

	public WordPhraseData(String strQ, Context context) {
		String fileName1 = strQ + ".e.txt", fileName2 = strQ + ".j.txt";
		try {
			InputStream is1 = context.getAssets().open(fileName1), is2 = context.getAssets().open(fileName2);
			BufferedReader br1 = new BufferedReader(new InputStreamReader(is1)), br2 = new BufferedReader(new InputStreamReader(is2));
			String str;
			int i = 0;
			while ((str = br1.readLine()) != null) {
				e[i] = str;
				i++;
			}
			i = 0;
			while ((str = br2.readLine()) != null) {
				j[i] = str;
				i++;
			}
			is1.close();
			is2.close();
			br1.close();
			br2.close();
		} catch (Exception e) {
			showException(context, e);
			new AlertDialog.Builder(context).setTitle("エラー").setMessage("ファイル" + fileName1 + "または" + fileName2 + "が見つかりません。").setPositiveButton("ok", null).create().show();
		}
	}

	enum DataBook {
		passTan, tanjukugoEX, yumetan,
	}

	enum DataQ {
		q1, qp1, q2, qp2, q3, q4, q5, y00, y08, y1, y2, y3,
	}

	enum DataType {word, phrase, gogengaku, eigoduke_com}

	enum DataLang {english, japanese}

	public enum q_num {
		test1q(), testp1q(), test2q(), testp2q(), testy00(), testy08(), testy1(), testy2(), testy3(), test1qEx, testp1qEx;

		enum strQ {
			str1q("1q"), strp1q("p1q"), str2q("2q"), strp2q("p2q"), str3q("3q"), str4q("4q"), str5q("5q"), stry00("y00"), stry08("y08"), stry1("y1"), stry2("y2"), stry3("y3"), ex1q("tanjukugo1q");

			strQ(String s) {
				getQ = s;
			}

			final String getQ;
		}

		enum mode {
			word, phrase, randomTest, huseikainomiTest, seitouritsujunTest, wordPlusPhrase
		}

		enum unit {
			deruA, deruB, deruC, Jukugo, all
		}

		enum shurui {
			verb, noum, adjective, matome
		}

		enum skipjouken {
			kirokunomi, seikai1, huseikai2, onlyHugoukaku
		}

		q_num() {
		}
	}

	public static class HatsuonKigou {

		private static final HashMap<String, String> hashMapHatsuonKigou = new HashMap<>();

		public static void SetHatsuonKigou(Context context) {
			try {
				//発音記号のためにSVL読み込み
				if (hashMapHatsuonKigou.size() == 0) {
					WordPhraseData wordPhraseDataSVL = new WordPhraseData(Svl, context);
					for (int i = 1; i < Math.min(wordPhraseDataSVL.e.length, wordPhraseDataSVL.j.length); i++)
						if (wordPhraseDataSVL.e[i] != null && wordPhraseDataSVL.j[i] != null)
							hashMapHatsuonKigou.put(wordPhraseDataSVL.e[i], wordPhraseDataSVL.j[i]);
				}
			} catch (Exception e) {
				showException(e);
			}
		}

		public static String getHatsuon(String strEnglishWord) {
			try {
				String strDictionary = hashMapHatsuonKigou.get(strEnglishWord);
				String ans = "";
				if (strDictionary != null) {
					int start;
					int result = strDictionary.indexOf("【発音】");
					if (result != -1) {
						start = result + 4;
					} else {
						result = strDictionary.indexOf("【発音！】");
						start = result + 5;
					}
					int end = strDictionary.indexOf("、", start);
					if (end == -1) end = strDictionary.length() - 1;
					ans = strDictionary.substring(start, end);
				}
				return ans;
			} catch (Exception e) {
				showException(e);
				return "<不明>";
			}
		}
	}
}