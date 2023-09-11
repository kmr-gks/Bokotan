package com.gukos.assetstest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
	
	TextView textViewConsole;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textViewConsole=findViewById(R.id.textViewConsole);
		
		var listD1=Dictionary.readToList("dictionaries/Distinction_new/dist1.txt", this, Dictionary.Division.distinction, Dictionary.BookName.distinction1, Dictionary.BookQ.none, Dictionary.Datatype.word, Dictionary.DataLang.other);
		
		String content="";
		for (int i=0;i<Math.min(listD1.size(),5);i++) {
			content+=listD1.get(i).content+"\n";
		}
		textViewConsole.setText(content);
	}
}