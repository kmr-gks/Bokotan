package com.gukos.bokotan;

import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.contains;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.ends;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.starts;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.setStringColored;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPathPs;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;

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
			Function<Integer, Void> a = integer -> {
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
			key = null;
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
			WordPhraseData.WordInfo wordInfo = (WordPhraseData.WordInfo) item;
			new AlertDialog.Builder(context)
				.setTitle(setStringColored(wordInfo.toushiNumber + " : " + wordInfo.e, key))
				.setMessage(setStringColored(wordInfo.toDetailedString(), key))
				.setPositiveButton("閉じる", null)
				.setNeutralButton("英→日発音", (dialogInterface, i1) -> playEnglishAndJapanese(wordInfo))
				.setNegativeButton("英語発音", (dialogInterface, i1) -> playEnglish(wordInfo))
				.create()
				.show();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	void playEnglishAndJapanese(WordPhraseData.WordInfo wordInfo) {
		try {
			printCurrentState("path="+getPathPs(wordInfo.dataBook, wordInfo.dataQ, wordInfo.mode, english, wordInfo.localNumber));
			MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.parse(getPathPs(wordInfo.dataBook, wordInfo.dataQ, wordInfo.mode, english, wordInfo.localNumber)));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(mp -> {
				try {
					MediaPlayer.create(context, Uri.parse(getPathPs(wordInfo.dataBook, wordInfo.dataQ, wordInfo.mode, japanese, wordInfo.localNumber))).start();
				} catch (Exception e) {
					showException(context, e);
				}
			});
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	void playEnglish(WordPhraseData.WordInfo wordInfo) {
		try {
			printCurrentState("path="+getPathPs(wordInfo.dataBook, wordInfo.dataQ, wordInfo.mode, english, wordInfo.localNumber));
			MediaPlayer.create(context, Uri.parse(getPathPs(wordInfo.dataBook, wordInfo.dataQ, wordInfo.mode, english, wordInfo.localNumber))).start();
		} catch (Exception e) {
			showException(context, e);
		}
	}
}