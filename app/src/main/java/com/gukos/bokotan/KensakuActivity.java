package com.gukos.bokotan;

import static com.gukos.bokotan.MyLibrary.*;
import static com.gukos.bokotan.MyLibrary.DataBook.passTan;
import static com.gukos.bokotan.MyLibrary.DataBook.tanjukugoEX;
import static com.gukos.bokotan.MyLibrary.DataBook.yumetan;
import static com.gukos.bokotan.MyLibrary.DataLang.english;
import static com.gukos.bokotan.MyLibrary.DataLang.japanese;
import static com.gukos.bokotan.MyLibrary.DataQ.q1;
import static com.gukos.bokotan.MyLibrary.DataQ.qp1;
import static com.gukos.bokotan.MyLibrary.DataQ.y08;
import static com.gukos.bokotan.MyLibrary.DataQ.y1;
import static com.gukos.bokotan.MyLibrary.DataQ.y2;
import static com.gukos.bokotan.MyLibrary.DataQ.y3;
import static com.gukos.bokotan.MyLibrary.DataType.phrase;
import static com.gukos.bokotan.MyLibrary.DataType.word;
import static com.gukos.bokotan.MyLibrary.ExceptionHandler.showException;
import static com.gukos.bokotan.Q_sentaku_activity.cbDefaultAdapter;
import static com.gukos.bokotan.Q_sentaku_activity.cbDirTOugou;
import static com.gukos.bokotan.Q_sentaku_activity.trGogenYomu;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXPhrase;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class KensakuActivity extends AppCompatActivity {

	static class ListData {
		static int size = 0;
		int toushiNumber, localNumber;
		String category, e, j;
		DataType dataType;

		ListData(String category, String e, String j, int localNumber, DataType dataType) {
			size++;
			this.toushiNumber = size;
			this.category = category;
			this.e = e;
			this.j = j;
			this.localNumber = localNumber;
			this.dataType = dataType;
		}

		@NonNull
		public String toString() {
			return toushiNumber + " " + category + " " + e + " " + j;
		}
	}

	//private ArrayList<String> allData=new ArrayList(),resultData=new ArrayList();
	private final ArrayList<ListData> allData = new ArrayList<>();
	private ArrayList<ListData> resultData = new ArrayList<>();
	private ListView lvResult;
	private TextView tvResultCount;
	private Button buttonCLear;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kensaku);

		EditText etKey = findViewById(R.id.editTextTextPersonName);
		lvResult = findViewById(R.id.listViewResult);
		tvResultCount = findViewById(R.id.textViewResultCount);
		buttonCLear = findViewById(R.id.buttonClear);

		etKey.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				try {
					//文字入力時
					String key = editable.toString();
					resultData.clear();
					for (ListData ld : allData) {
						//TODO:nullチェック
						if (ld.category.toLowerCase().contains(key.toLowerCase()) || ld.e.toLowerCase().contains(key.toLowerCase()) || ld.j.toLowerCase().contains(key.toLowerCase())) {
							resultData.add(ld);
						}
					}
					tvResultCount.setText(resultData.size() + "件");
					setListView(lvResult, resultData);
				}catch(Exception e){
					showException(e);
				}
			}
		});

		buttonCLear.setOnClickListener(view -> etKey.setText(""));

		//ファイルを開いて読み込む
		ListData.size = 0;
		Map<String, String> mapQName = new HashMap() {{
			put("1q", "1級");
			put("p1q", "準1級");
			put("2q", "2級");
			put("p2q", "準2級");
			put("3q", "3級");
			put("4q", "4級");
			put("5q", "5級");
			put("00", "ユメタン0基礎");
			put("08", "ユメタン0");
			put("1", "ユメタン1");
			put("2", "ユメタン2");
			put("3", "ユメタン3");
			put("-eiken-jukugo","英検熟語");
			put("-eikenp1-jukugo","英検熟語(準1)");
			put("-Toefl-Chokuzen","TOEFL直前");
			put("-Toeic-500ten","TOEIC500点");
			put("-Toeic-700ten","TOEIC700点");
			put("-Toeic-900ten","TOEIC900点");
			put("-Toeic-Chokuzen","TOEIC直前");
			put("-Toeic-jukugo","TOEIC熟語");
		}};

		//パス単単語
		for (String Q : new String[]{"1q", "p1q", "2q", "p2q","3q","4q","5q"}) {
			WordPhraseData w = new WordPhraseData(PasstanWord + Q, this);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new ListData("パス単"+mapQName.get(Q), w.e[i], w.j[i], i, word));
		}

		//単熟語EX単語
		for (String Q : new String[]{"1q"}) {
			WordPhraseData w = new WordPhraseData(TanjukugoEXWord + Q, this);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new ListData("単熟語EX"+mapQName.get(Q), w.e[i], w.j[i], i, word));
		}

		//ユメタン単語
		for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
			WordPhraseData w = new WordPhraseData(YumeWord + Q, this);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new ListData(mapQName.get(Q), w.e[i], w.j[i], i, word));
		}

		//語源データも読み込む
		for (TreeMap.Entry<String, GogenYomu> map : trGogenYomu.entrySet())
			allData.add(new ListData("読む語源学", map.getKey(), map.getValue().wordJpn, 0, DataType.gogengaku));

		//英語漬け.comから読み込み
		for (String Q:new String[]{"1q", "p1q", "2q", "p2q","3q","4q","5q",
				"-eiken-jukugo","-eikenp1-jukugo","-Toefl-Chokuzen","-Toeic-500ten","-Toeic-700ten","-Toeic-900ten",
				"-Toeic-Chokuzen","-Toeic-jukugo",}){
			WordPhraseData wpd=new WordPhraseData("Eigoduke.com/"+"WordDataEigoduke"+Q,this);
			for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
				if (wpd.e[i] != null && wpd.j[i] != null)
					allData.add(new ListData("英語漬け"+mapQName.get(Q), wpd.e[i], wpd.j[i], i, DataType.eigoduke_com));
		}
		for(int num=1;num<=10;num++){
			String Q="-toeic ("+num+")";
			WordPhraseData wpd=new WordPhraseData("Eigoduke.com/"+"WordDataEigoduke"+Q,this);
			for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
				if (wpd.e[i] != null && wpd.j[i] != null)
					allData.add(new ListData("英語漬け"+"TOEIC"+num, wpd.e[i], wpd.j[i], i, DataType.eigoduke_com));
		}

		//フレーズ
		for (String Q : new String[]{"1q", "p1q", "2q", "p2q","3q","4q","5q"}) {
			WordPhraseData w = new WordPhraseData(PasstanPhrase + Q, this);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new ListData("パス単"+mapQName.get(Q), w.e[i], w.j[i], i, phrase));
		}
		//単熟語EX単語
		for (String Q : new String[]{"1q"}) {
			WordPhraseData w = new WordPhraseData(TanjukugoEXPhrase + Q, this);
			for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
				if (w.e[i] != null && w.j[i] != null)
					allData.add(new ListData("単熟語EX"+mapQName.get(Q), w.e[i], w.j[i], i, phrase));
		}


		//コピー
		resultData = new ArrayList<>(allData);
		tvResultCount.setText(resultData.size() + "件");

		setListView(lvResult, resultData);
	}

	private void setListView(ListView lv, ArrayList<ListData> ar) {
		if (cbDefaultAdapter.isChecked()) {
			lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ar));
		} else {
			List<TextView> list = new ArrayList<>();
			for (ListData ld : ar) {
				TextView tv = new TextView(this);
				tv.setText(ld.toString());
				tv.setTextColor(getColor(R.color.textcolor));
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				tv.setHeight(120);
				tv.setGravity(Gravity.CENTER | Gravity.START);
				list.add(tv);
			}
			lv.setAdapter(new ArrayAdapter<TextView>(this, 0, list) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					return getItem(position);
				}
			});
		}


		lv.setOnItemClickListener((adapterView, view, i, l) -> {
			ListData ld = ar.get(i);
			new AlertDialog.Builder(this)
					.setTitle(ld.toushiNumber + " : " + ld.e)
					.setMessage("No. " + ld.toushiNumber + "\nカテゴリ: " + ld.category + (ld.dataType == DataType.gogengaku ? "" : " 番号:" + ld.localNumber) + "\n" + ld.e + "\n" + ld.j + "\n" + GogenYomuFactory.getGogenString(ld.e, true, true))
					.setPositiveButton("閉じる",null)
					.setNeutralButton("英→日発音",(dialogInterface, i1) -> {
						playEnglishAndJapanese(ld);
					})
					.setNegativeButton("英語発音", (dialogInterface, i1) -> {
						playEnglish(ld);
					})
					.create()
					.show();
		});
	}

	void playEnglishAndJapanese(@NonNull ListData ld){
		String path;
		switch (ld.category) {
			case "パス単1級": {
				path=getPath(passTan, q1,ld.dataType, english,ld.localNumber,cbDirTOugou.isChecked());
				break;
			}
			case "パス単準1級": {
				path=getPath(passTan, qp1,ld.dataType, english,ld.localNumber,cbDirTOugou.isChecked());
				break;
			}
			case "単熟語EX1級":{
				path=getPath(tanjukugoEX,q1,ld.dataType,english,ld.localNumber);
				break;
			}
			case "ユメタン0": {
				path=getPath(yumetan,y08,word,english,ld.localNumber);
				break;
			}
			case "ユメタン1": {
				path=getPath(yumetan,y1,word,english,ld.localNumber);
				break;
			}
			case "ユメタン2": {
				path=getPath(yumetan,y2,word,english,ld.localNumber);
				break;
			}
			case "ユメタン3": {
				path=getPath(yumetan,y3,word,english,ld.localNumber);
				break;
			}
			default: {
				makeToastForShort(this,ld.category+"の音声の再生には対応していません。");
				return;
			}
		}
		try {
			MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(mp -> {
				String pathJpn;
				switch (ld.category) {
					case "パス単1級": {
						pathJpn=getPath(passTan, q1,ld.dataType, japanese,ld.localNumber,cbDirTOugou.isChecked());
						break;
					}
					case "パス単準1級": {
						pathJpn=getPath(passTan, qp1,ld.dataType, japanese,ld.localNumber,cbDirTOugou.isChecked());
						break;
					}
					case "単熟語EX1級":{
						pathJpn=getPath(tanjukugoEX,q1,ld.dataType,japanese,ld.localNumber);
						break;
					}
					case "ユメタン0": {
						pathJpn = getPath(yumetan,y08,word,japanese,ld.localNumber);
						break;
					}
					case "ユメタン1": {
						pathJpn = getPath(yumetan,y1,word,japanese,ld.localNumber);
						break;
					}
					case "ユメタン2": {
						pathJpn = getPath(yumetan,y2,word,japanese,ld.localNumber);
						break;
					}
					case "ユメタン3": {
						pathJpn = getPath(yumetan,y3,word,japanese,ld.localNumber);
						break;
					}
					default: {
						return;
					}
				}
				try {
					MediaPlayer mpJpn = MediaPlayer.create(this, Uri.parse(pathJpn));
					mpJpn.start();
				} catch (Exception e) {
					showException(e);
				}
			});
		} catch (Exception e) {
			showException(e);
		}
	}
	void playEnglish(@NonNull ListData ld){
		String path;
		switch (ld.category) {
			case "パス単1級": {
				path=getPath(passTan, q1,ld.dataType, english,ld.localNumber,cbDirTOugou.isChecked());
				break;
			}
			case "パス単準1級": {
				path=getPath(passTan, qp1,ld.dataType, english,ld.localNumber,cbDirTOugou.isChecked());
				break;
			}
			case "単熟語EX1級":{
				path=getPath(tanjukugoEX,q1,ld.dataType,english,ld.localNumber);
				break;
			}
			case "ユメタン0": {
				path=getPath(yumetan,y08,word,english,ld.localNumber);
				break;
			}
			case "ユメタン1": {
				path=getPath(yumetan,y1,word,english,ld.localNumber);
				break;
			}
			case "ユメタン2": {
				path=getPath(yumetan,y2,word,english,ld.localNumber);
				break;
			}
			case "ユメタン3": {
				path=getPath(yumetan,y3,word,english,ld.localNumber);
				break;
			}
			default: {
				makeToastForShort(this,ld.category+"の音声の再生には対応していません。");
				return;
			}
		}
		try {
			MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
			mediaPlayer.start();
		} catch (Exception e) {
			showException(e);
		}
	}

	void playEnglishAndJapanese_old(@NonNull ListData ld){
		String path;
		switch (ld.category) {
			case "パス単1級": {
				if (ld.dataType== phrase){
					if (cbDirTOugou.isChecked()){
						path = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}else{
						path = "/storage/emulated/0/Download/data/" + "ph1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}
				}else{
					//単語
					path = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "英" + ".mp3";
				}
				break;
			}
			case "パス単準1級": {
				if (ld.dataType== phrase){
					if (cbDirTOugou.isChecked()){
						path = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}else{
						path = "/storage/emulated/0/Download/data/" + "php1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}
				}else{
					//単語
					path = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "英" + ".mp3";
				}
				break;
			}
			case "ユメタン0": {
				path = "/storage/emulated/0/Download/data/" + "y08" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン1": {
				path = "/storage/emulated/0/Download/data/" + "y1" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン2": {
				path = "/storage/emulated/0/Download/data/" + "y2" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン3": {
				path = "/storage/emulated/0/Download/data/" + "y3" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			default: {
				makeToastForShort(this,ld.category+"の音声の再生には対応していません。");
				return;
			}
		}
		try {
			MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(mp -> {
				String pathJpn;
				switch (ld.category) {
					case "パス単1級": {
						if (ld.dataType== phrase){
							if (cbDirTOugou.isChecked()){
								pathJpn = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "日" + ".mp3";
							}else{
								pathJpn = "/storage/emulated/0/Download/data/" + "ph1q" + '/' + String.format("%04d", ld.localNumber) + "日" + ".mp3";
							}
						}else{
							//単語
							pathJpn = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "訳" + ".mp3";
						}
						break;
					}
					case "パス単準1級": {
						if (ld.dataType== phrase){
							if (cbDirTOugou.isChecked()){
								pathJpn = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "日" + ".mp3";
							}else{
								pathJpn = "/storage/emulated/0/Download/data/" + "php1q" + '/' + String.format("%04d", ld.localNumber) + "日" + ".mp3";
							}
						}else{
							//単語
							pathJpn = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "訳" + ".mp3";
						}
						break;
					}
					case "ユメタン0": {
						pathJpn = "/storage/emulated/0/Download/data/" + "y08" + '/' + String.format("W日%04d", ld.localNumber) + ".mp3";
						break;
					}
					case "ユメタン1": {
						pathJpn = "/storage/emulated/0/Download/data/" + "y1" + '/' + String.format("W日%04d", ld.localNumber) + ".mp3";
						break;
					}
					case "ユメタン2": {
						pathJpn = "/storage/emulated/0/Download/data/" + "y2" + '/' + String.format("W日%04d", ld.localNumber) + ".mp3";
						break;
					}
					case "ユメタン3": {
						pathJpn = "/storage/emulated/0/Download/data/" + "y3" + '/' + String.format("W日%04d", ld.localNumber) + ".mp3";
						break;
					}
					default: {
						return;
					}
				}
				try {
					MediaPlayer mpJpn = MediaPlayer.create(this, Uri.parse(pathJpn));
					mpJpn.start();
				} catch (Exception e) {
					showException(e);
				}
			});
		} catch (Exception e) {
			showException(e);
		}
	}
	void playEnglish_old(@NonNull ListData ld){
		String path;
		switch (ld.category) {
			case "パス単1級": {
				if (ld.dataType== phrase){
					if (cbDirTOugou.isChecked()){
						path=getPath(passTan, q1,ld.dataType, english,ld.localNumber,cbDirTOugou.isChecked());
						path = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}else{
						path = "/storage/emulated/0/Download/data/" + "ph1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}
				}else{
					//単語
					path = "/storage/emulated/0/Download/data/" + "1q" + '/' + String.format("%04d", ld.localNumber) + "英" + ".mp3";
				}
				break;
			}
			case "パス単準1級": {
				if (ld.dataType== phrase){
					if (cbDirTOugou.isChecked()){
						path = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}else{
						path = "/storage/emulated/0/Download/data/" + "php1q" + '/' + String.format("%04d", ld.localNumber) + "例" + ".mp3";
					}
				}else{
					//単語
					path = "/storage/emulated/0/Download/data/" + "p1q" + '/' + String.format("%04d", ld.localNumber) + "英" + ".mp3";
				}
				break;
			}
			case "ユメタン0": {
				path = "/storage/emulated/0/Download/data/" + "y08" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン1": {
				path = "/storage/emulated/0/Download/data/" + "y1" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン2": {
				path = "/storage/emulated/0/Download/data/" + "y2" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			case "ユメタン3": {
				path = "/storage/emulated/0/Download/data/" + "y3" + '/' + String.format("W英%04d", ld.localNumber) + ".mp3";
				break;
			}
			default: {
				makeToastForShort(this,ld.category+"の音声の再生には対応していません。");
				return;
			}
		}
		try {
			MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.parse(path));
			mediaPlayer.start();
		} catch (Exception e) {
			showException(e);
		}
	}
}