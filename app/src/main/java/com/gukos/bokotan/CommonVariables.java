package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.HashMap;
import java.util.TreeMap;

public final class CommonVariables {
	public static final String tag = "E/";
	public static final int[][][] toFindFromAndTo = {
			//1q
			{{1, 233}, {234, 472}, {473, 700}, {701, 919}, {920, 1177}, {1178, 1400}, {1401, 1619}, {1620, 1861}, {1862, 2100}, {2101, 2400}, {1, 700}, {701, 1400}, {1401, 2100}, {1, 2400}},
			//p1q
			{{1, 92}, {93, 362}, {363, 530}, {531, 682}, {683, 883}, {884, 1050}, {1051, 1262}, {1263, 1411}, {1412, 1550}, {1551, 1850}, {1, 530}, {531, 1050}, {1051, 1550}, {1, 1850}},
			//2q
			{{1, 158}, {159, 316}, {317, 405}, {406, 564}, {565, 719}, {720, 808}, {809, 949}, {950, 1108}, {1109, 1179}, {1180, 1704}, {1, 405}, {406, 808}, {809, 1179}, {1, 1704}},
			//p2q
			{{1, 125}, {126, 268}, {269, 373}, {374, 484}, {485, 632}, {633, 735}, {736, 839}, {840, 988}, {989, 1085}, {1086, 1500}, {1, 373}, {374, 735}, {736, 1085}, {1, 1500}},
			//3q
			{},
			//4q
			{},
			//5q
			{},
			//y00
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}},
			//y08
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
			//y1
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
			//y2
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {801, 900}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000}, {1, 1000},},
			//y3
			{{1, 100}, {101, 200}, {201, 300}, {301, 400}, {401, 500}, {501, 600}, {601, 700}, {701, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}, {1, 800}},
			//1qEX
			{{1, 276}, {277, 588}, {589, 840}, {841, 1080}, {1081, 1320}, {1321, 1560}, {1561, 1800}, {1801, 2040}, {2041, 2208}, {2209, 2364}, {2365, 2811}},
			//p1qEX
			{{1, 216}, {217, 432}, {433, 648}, {649, 864}, {865, 1080}, {1081, 1296}, {1297, 1488}, {1489, 1680}, {1681, 1824}, {1825, 1920}, {1920, 2400}},
	};
	//1qのunit=8のfrom=0,8,0
	//p1qunit=5 to=1,5,1
	//SentakuActivity.javaから
	public static final HashMap<String, String> hashMapKishutu = new HashMap<>();
	public static final int[] nSeikaisuu = new int[3000];
	public static final int[] nHuseikaisuu = new int[3000];
	public static int now;
	public static TextView tvWordEng;
	public static TextView tvWordJpn;
	public static TextView tvGenzai;
	public static TextView tvsubE;
	public static TextView tvsubJ;
	public static TextView tvNumSeikaisuu;
	public static TextView tvSeikaisu;
	public static TextView tvGogen;
	public static TextView textViewPath;
	public static TextView textViewHatsuonKigou;
	public static String strQ = null;//開始時には決まっているf
	public static boolean playing = false;
	public static String[] wordE;
	public static String[] wordJ;
	public static String[] strPhraseJ;
	public static int lastnum;
	public static boolean isPhraseMode;
	public static double dPlaySpeedEng = 1.5;
	public static double dPlaySpeedJpn = 1.5;
	public static boolean bHyojiYakuBeforeRead = true;
	public static boolean bEnglishToJapaneseOrder = true;
	public static int nFrom;
	public static int nTo;
	public static int from;
	public static int to;
	public static String[] strPhraseE;
	public static TreeMap<String, GogenYomu> trGogenYomu;
	//ビュー
	static Switch swOnlyFirst;
	static Switch switchSkipOboe;
	static Switch swMaruBatu;
	static Switch swHyojiBeforeRead;
	static Switch switchSortHanten;
	static CheckBox cbDirTOugou;
	static CheckBox cbDefaultAdapter;
	static CheckBox cbAutoStop;
	static CheckBox checkBoxHatsuonKigou;
	static Spinner spinnerHanni;
	static Spinner spinnerHinsi;
	static Spinner spinnerMode;
	static Spinner spinnerKuuhaku;
	static TextView textViewVersion;
	static boolean bSkipOboe;
	static boolean nowIsDecided = false;
	static boolean isWordAndPhraseMode = false;
	static WordPhraseData.q_num sentakuQ = WordPhraseData.q_num.testp1q;
	static WordPhraseData.q_num.mode WordPhraseOrTest = WordPhraseData.q_num.mode.word;
	static WordPhraseData.q_num.unit sentakuUnit = WordPhraseData.q_num.unit.all;
	static WordPhraseData.q_num.shurui sentakuShurui = WordPhraseData.q_num.shurui.matome;
	static WordPhraseData.q_num.strQ strQenum = WordPhraseData.q_num.strQ.strp1q;
	static boolean bSort = true;
	static WordPhraseData.q_num.skipjouken skipjoken = WordPhraseData.q_num.skipjouken.kirokunomi;
	static int nUnit = 5;
	static int nShurui = 4;
	static int nWordPhraseOrTest = 1;
	
	public static void SetNumFromAndTo(int lastnum, int unit) {
		try {
			from = 1;
			to = lastnum;
			if (unit > 10) unit--;
			if (lastnum == 2400) {
				from = toFindFromAndTo[0][unit][0];
				to = toFindFromAndTo[0][unit][1];
			}
			if (lastnum == 1850) {
				from = toFindFromAndTo[1][unit][0];
				to = toFindFromAndTo[1][unit][1];
			}
			if (lastnum == 1704) {
				from = toFindFromAndTo[2][unit][0];
				to = toFindFromAndTo[2][unit][1];
			}
			if (lastnum == 1500) {
				from = toFindFromAndTo[3][unit][0];
				to = toFindFromAndTo[3][unit][1];
			}
		} catch (Exception e) {
			showException(e);
		}
	}
}