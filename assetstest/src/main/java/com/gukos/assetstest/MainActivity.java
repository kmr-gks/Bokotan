package com.gukos.assetstest;

import static com.gukos.assetstest.Dictionary.readToList;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gukos.assetstest.Dictionary.BookName;
import com.gukos.assetstest.Dictionary.BookQ;
import com.gukos.assetstest.Dictionary.DataLang;
import com.gukos.assetstest.Dictionary.Datatype;
import com.gukos.assetstest.Dictionary.Folder;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
	
	TextView textViewConsole;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textViewConsole = findViewById(R.id.textViewConsole);
		
		var entries = new ArrayList<Dictionary.Entry>();
		
		//Distinction
		for (var book : new BookName[]{BookName.distinction1, BookName.distinction2, BookName.distinction3}) {
			entries.addAll(readToList(this, Folder.distinction, book, BookQ.none, Datatype.word
				, DataLang.other).subList(0, 5));
		}
		
		//Eigoduke
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
			entries.addAll(readToList(this, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(this, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		//Eigoduke
		for (var book : new BookName[]{BookName.WordEigoduke_eiken_jukugo, BookName.WordEigoduke_eikenp1_jukugo, BookName.WordEigoduke_Toefl_Chokuzen, BookName.WordEigoduke_Toeic_500ten, BookName.WordEigoduke_Toeic_700ten, BookName.WordEigoduke_Toeic_900ten, BookName.WordEigoduke_Toeic_Chokuzen, BookName.WordEigoduke_Toeic_jukugo}) {
			entries.addAll(readToList(this, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(this, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		//Passtan
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(this, Folder.passtan, BookName.PasstanWordData, q, Datatype.word, lang).subList(0, 5));
			}
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(this, Folder.passtan, BookName.PasstanPhrase, q, Datatype.phrase, lang).subList(0, 5));
			}
		}
		
		//svl
		for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
			entries.addAll(readToList(this, Folder.svl, BookName.SVL12000, BookQ.none, Datatype.mix, lang).subList(0, 5));
		}
		
		//単熟語ex
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1}) {
			for (var book : new BookName[]{BookName.tanjukugoWord, BookName.tanjukugoPhrase, BookName.tanjukugoExWord}) {
				for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
					entries.addAll(readToList(this, Folder.tanjukugo, book, q, (book == BookName.tanjukugoPhrase ? Datatype.phrase : Datatype.word), lang).subList(0, 5));
				}
			}
		}
		
		//ユメタン単語
		for (var q : new BookQ[]{BookQ.y00, BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(this, Folder.yumetan, BookName.yumetanWord, q, Datatype.word, lang).subList(0, 5));
			}
		}
		
		//ユメタン文
		for (var q : new BookQ[]{BookQ.y08, BookQ.y1, BookQ.y2, BookQ.y3}) {
			for (var lang : new DataLang[]{DataLang.english, DataLang.japanese}) {
				entries.addAll(readToList(this, Folder.yumetan, BookName.yumetanPhrase, q, Datatype.phrase, lang).subList(0, 5));
			}
		}
		
		//英英英単語
		for (var book : new BookName[]{BookName.ei3_jukugo_shokyu, BookName.ei3_jukugo_chukyu, BookName.ei3_tango_toeic800, BookName.ei3_tango_toeic990, BookName.ei3_tango_shokyu, BookName.ei3_tango_chukyu, BookName.ei3_tango_jokyu, BookName.ei3_tango_chojyokyu}) {
			entries.addAll(readToList(this, Folder.ei3, book, BookQ.none, Datatype.word, DataLang.other).subList(0, 5));
		}
		
		//究極の英単語プレミアム
		for (var book : new BookName[]{BookName.kyukyoku_premium_vol1, BookName.kyukyoku_premium_vol2}) {
			entries.addAll(readToList(this, Folder.eitango_joukyuu, book, BookQ.none, Datatype.mix, DataLang.other).subList(0, 5));
		}
		
		StringBuilder content = new StringBuilder();
		for (var entry : entries) content.append(entry).append("\n");
		textViewConsole.setText(content.toString());
	}
}