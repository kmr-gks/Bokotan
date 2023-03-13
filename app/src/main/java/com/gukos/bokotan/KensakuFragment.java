package com.gukos.bokotan;

import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.contains;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.ends;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.starts;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.setStringColored;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugo;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.DataQ.q1;
import static com.gukos.bokotan.WordPhraseData.DataQ.qp1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y08;
import static com.gukos.bokotan.WordPhraseData.DataQ.y1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y2;
import static com.gukos.bokotan.WordPhraseData.DataQ.y3;
import static com.gukos.bokotan.WordPhraseData.DataType.word;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.gukos.bokotan.databinding.FragmentKensakuBinding;

import java.util.function.BiFunction;
import java.util.function.Function;

public class KensakuFragment extends UiManager.FragmentBingding<FragmentKensakuBinding> {
	
	enumKensakuHouhou kensakuHouhou = starts;
	private String key;
	
	enum enumKensakuHouhou {
		starts, contains, ends;
		
		@NonNull
		public String toString() {
			switch (this) {
				default:
				case starts: {
					return "で始まる";
				}
				case contains: {
					return "を含む";
				}
				case ends: {
					return "で終わる";
				}
			}
		}
	}
	
	KensakuFragment() {
		super(FragmentKensakuBinding::inflate);
	}
	
	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			
			//UI設定
			kensakuHouhou = starts;
			binding.buttonKensakuHouhou.setText(kensakuHouhou.toString());
			binding.buttonKensakuHouhou.setOnClickListener(v -> {
				switch (kensakuHouhou) {
					case starts: {
						kensakuHouhou = contains;
						break;
					}
					case contains: {
						kensakuHouhou = ends;
						break;
					}
					case ends: {
						kensakuHouhou = starts;
						break;
					}
				}
				binding.buttonKensakuHouhou.setText(kensakuHouhou.toString());
				//人工的に文字を変更して再検索
				onSearchViewTextChange(binding.searchView.getQuery().toString());
			});
			binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}
				
				@Override
				public boolean onQueryTextChange(String newText) {
					return onSearchViewTextChange(newText);
				}
			});
			
			onKensakuEnd(WordPhraseData.allData.size());
			Function<Integer,Void> a= integer -> {
				onKensakuEnd(integer);
				return null;
			};
			binding.listViewKensakuResult.setAdapter(new WordSearchAdapter<>(context, R.layout.my_simple_list_item_1, WordPhraseData.allData, this::onKensakuEnd));
			binding.listViewKensakuResult.setOnItemClickListener(this::onItemClick);
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	private boolean onSearchViewTextChange(String newText) {
		//フィルターする
		//ListView#setFilterTextは内部的にListView#getAdapter#getFilter
		// を呼び出している。また、ポップアップが表示されてしまう。
		
		var adapter = (WordSearchAdapter<WordPhraseData.WordInfo>) binding.listViewKensakuResult.getAdapter();
		if (newText.length() > 0) {
			key = newText.toLowerCase();
			//検索方法を指定する
			BiFunction<String, String, Boolean> biFunction;
			switch (kensakuHouhou) {
				default:
				case starts: {
					biFunction = String::startsWith;
					break;
				}
				case contains: {
					biFunction = String::contains;
					break;
				}
				case ends: {
					biFunction = String::endsWith;
					break;
				}
			}
			//setStringColored:検索ワードで色をつける
			adapter.filter((wordInfo) -> {
				for (var field : wordInfo.getAllFieldString()) {
					if (biFunction.apply(field.toLowerCase(), key)) {
						return true;
					}
				}
				return false;
			}, data -> setStringColored(data.toString(), key));
		}
		else {
			key=null;
			//検索欄が空、条件をクリアして全単語表示
			adapter.resetFilter();
		}
		return false;
	}
	
	private void onKensakuEnd(int count) {
		binding.textViewKensakuResultCount.setText(count + "件");
	}
	
	private void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		try {
			var item = adapterView.getItemAtPosition(i);
			WordPhraseData.WordInfo ld = (WordPhraseData.WordInfo) item;
			new AlertDialog.Builder(context)
				.setTitle(setStringColored(ld.toushiNumber + " : " + ld.e, key))
				.setMessage(setStringColored(ld.toDetailedString(), key))
				.setPositiveButton("閉じる", null)
				.setNeutralButton("英→日発音", (dialogInterface, i1) -> playEnglishAndJapanese(ld))
				.setNegativeButton("英語発音", (dialogInterface, i1) -> playEnglish(ld))
				.create()
				.show();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	void playEnglishAndJapanese(WordPhraseData.WordInfo ld) {
		try {
			String path;
			switch (ld.category) {
				case "パス単1級": {
					path = MyLibrary.FileDirectoryManager.getPath(passTan, q1, ld.dataType, english, ld.localNumber);
					break;
				}
				case "パス単準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(passTan, qp1, ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugo1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugop1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "ユメタン0": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y08, word, english, ld.localNumber);
					break;
				}
				case "ユメタン1": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y1, word, english, ld.localNumber);
					break;
				}
				case "ユメタン2": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y2, word, english, ld.localNumber);
					break;
				}
				case "ユメタン3": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y3, word, english, ld.localNumber);
					break;
				}
				default: {
					MyLibrary.DisplayOutput.makeToastForShort(context, ld.category + "の音声の再生には対応していません。");
					return;
				}
			}
			MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(mp -> {
				String pathJpn;
				switch (ld.category) {
					case "パス単1級": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(passTan, q1, ld.dataType, japanese, ld.localNumber);
						break;
					}
					case "パス単準1級": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(passTan, qp1, ld.dataType, japanese, ld.localNumber);
						break;
					}
					case "単熟語EX1級": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugo1q", ld.dataType, japanese, ld.localNumber);
						break;
					}
					case "単熟語EX準1級": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugop1q", ld.dataType, japanese, ld.localNumber);
						break;
					}
					case "ユメタン0": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(yumetan, y08, word, japanese, ld.localNumber);
						break;
					}
					case "ユメタン1": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(yumetan, y1, word, japanese, ld.localNumber);
						break;
					}
					case "ユメタン2": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(yumetan, y2, word, japanese, ld.localNumber);
						break;
					}
					case "ユメタン3": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(yumetan, y3, word, japanese, ld.localNumber);
						break;
					}
					default: {
						return;
					}
				}
				try {
					MediaPlayer mpJpn = MediaPlayer.create(context, Uri.parse(pathJpn));
					mpJpn.start();
				} catch (Exception e) {
					showException(context, e);
				}
			});
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	void playEnglish(WordPhraseData.WordInfo ld) {
		try {
			String path;
			switch (ld.category) {
				case "パス単1級": {
					path = MyLibrary.FileDirectoryManager.getPath(passTan, q1, ld.dataType, english, ld.localNumber);
					break;
				}
				case "パス単準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(passTan, qp1, ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugo1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugo, "tanjukugop1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "ユメタン0": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y08, word, english, ld.localNumber);
					break;
				}
				case "ユメタン1": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y1, word, english, ld.localNumber);
					break;
				}
				case "ユメタン2": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y2, word, english, ld.localNumber);
					break;
				}
				case "ユメタン3": {
					path = MyLibrary.FileDirectoryManager.getPath(yumetan, y3, word, english, ld.localNumber);
					break;
				}
				default: {
					MyLibrary.DisplayOutput.makeToastForShort(context, ld.category + "の音声の再生には対応していません。");
					return;
				}
			}
			
			MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(path));
			mediaPlayer.start();
		} catch (Exception e) {
			showException(context, e);
		}
	}
}