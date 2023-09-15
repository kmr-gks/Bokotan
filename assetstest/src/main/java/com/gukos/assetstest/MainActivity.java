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
		for (var book : new BookName[]{BookName.distinction1, BookName.distinction2, BookName.distinction3}) {
			entries.addAll(readToList(this, Folder.distinction, book, BookQ.none, Datatype.word
				, DataLang.other).subList(0, 5));
		}
		for (var q : new BookQ[]{BookQ.q1, BookQ.qp1, BookQ.q2, BookQ.qp2, BookQ.q3, BookQ.q4, BookQ.q5}) {
			entries.addAll(readToList(this, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(this, Folder.eigoduke, BookName.WordEigoduke, q, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		for (var book : new BookName[]{BookName.WordEigoduke_eiken_jukugo, BookName.WordEigoduke_eikenp1_jukugo, BookName.WordEigoduke_Toefl_Chokuzen, BookName.WordEigoduke_Toeic_500ten, BookName.WordEigoduke_Toeic_700ten, BookName.WordEigoduke_Toeic_900ten, BookName.WordEigoduke_Toeic_Chokuzen, BookName.WordEigoduke_Toeic_jukugo}) {
			entries.addAll(readToList(this, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.english).subList(0, 5));
			entries.addAll(readToList(this, Folder.eigoduke, book, BookQ.none, Datatype.word, DataLang.japanese).subList(0, 5));
		}
		
		entries.addAll(readToList(this, Folder.IELTS, BookName.kanzenkouryaku_ielts_3500, BookQ.none, Datatype.word, DataLang.other).subList(0, 5));
		
		StringBuilder content = new StringBuilder();
		for (var entry : entries) content.append(entry).append("\n");
		textViewConsole.setText(content.toString());
	}
}