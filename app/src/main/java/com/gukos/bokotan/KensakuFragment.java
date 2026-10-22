package com.gukos.bokotan;

import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.contains;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.ends;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.starts;
import static com.gukos.bokotan.MyLibrary.DebugManager.printCurrentState;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.setStringColored;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import com.gukos.bokotan.databinding.FragmentKensakuBinding;

import java.util.function.BiFunction;

public class KensakuFragment extends UiManager.FragmentBinding<FragmentKensakuBinding> {

	enumKensakuHouhou kensakuHouhou = starts;
	private String key;
	private Button buttonKensakuHouhou;
	private SearchView searchView;
	private ListView listViewKensakuResult;
	private TextView textViewKensakuResultCount;
	private Context context;
	private MediaPlayer player;
	private WordSearchAdapter<Dictionary.Entry> searchAdapter;

	public KensakuFragment() {
		super(FragmentKensakuBinding::inflate);
	}

	enum enumKensakuHouhou {
		starts, contains, ends;

		@NonNull
		public String toString() {
			switch (this) {
				case contains: {
					return "を含む";
				}
				case ends: {
					return "で終わる";
				}
				default: {// starts
					return "で始まる";
				}
			}
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);

			//UI設定

			buttonKensakuHouhou = view.findViewById(R.id.buttonKensakuHouhou);
			searchView = view.findViewById(R.id.searchView);
			listViewKensakuResult = view.findViewById(R.id.listViewKensakuResult);
			textViewKensakuResultCount = view.findViewById(R.id.textViewKensakuResultCount);
			context = getContext();

			kensakuHouhou = starts;
			buttonKensakuHouhou.setText(kensakuHouhou.toString());
			buttonKensakuHouhou.setOnClickListener(v -> {
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
				buttonKensakuHouhou.setText(kensakuHouhou.toString());
				//人工的に文字を変更して再検索
				onSearchViewTextChange(searchView.getQuery().toString());
			});
			searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
				@Override
				public boolean onQueryTextSubmit(String query) {
					return false;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					return onSearchViewTextChange(newText);
				}
			});

			Dictionary.whenReady(requireContext(), () -> {
				if (binding == null) return;
				onKensakuEnd(Dictionary.allData.size());
				searchAdapter = new WordSearchAdapter<>(context, R.layout.my_simple_list_item_1,
						new java.util.ArrayList<>(Dictionary.allData), this::onKensakuEnd);
				listViewKensakuResult.setAdapter(searchAdapter);
				onSearchViewTextChange(searchView.getQuery().toString());
			});
			listViewKensakuResult.setOnItemClickListener(this::onItemClick);
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}

	private boolean onSearchViewTextChange(String newText) {
		//フィルターする
		//ListView#setFilterTextは内部的にListView#getAdapter#getFilter
		// を呼び出している。また、ポップアップが表示されてしまう。

		var adapter = searchAdapter;
		if (adapter == null || binding == null) return false;
		if (!newText.isEmpty()) {
			key = newText.toLowerCase(java.util.Locale.ROOT);
			//検索方法を指定する
			BiFunction<String, String, Boolean> biFunction;
			switch (kensakuHouhou) {
				case contains: {
					biFunction = String::contains;
					break;
				}
				case ends: {
					biFunction = String::endsWith;
					break;
				}
				default: { // starts
					biFunction = String::startsWith;
					break;
				}
			}
			//setStringColored:検索ワードで色をつける
			adapter.setFilter((wordInfo) -> {
				for (var field : wordInfo.getAllFieldString()) {
					if (field != null && biFunction.apply(field.toLowerCase(java.util.Locale.ROOT), key)) {
						return true;
					}
				}
				return false;
			}, data -> {
				var dataString = data.toString();
				return setStringColored(dataString.substring(0, Math.min(dataString.length(), 100)), key);
			});
		} else {
			key = null;
			//検索欄が空、条件をクリアして全単語表示
			adapter.resetFilter();
		}
		requireActivity().runOnUiThread(adapter::notifyDataSetChanged);
		return false;
	}

	private void onKensakuEnd(int count) {
		if (binding != null) textViewKensakuResultCount.setText(getString(R.string.search_result_count, count));
	}

	private void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		try {
			var item = adapterView.getItemAtPosition(i);
			Dictionary.Entry wordInfo = (Dictionary.Entry) item;
			new AlertDialog.Builder(context)
					.setTitle(setStringColored(wordInfo.numberInBook + " : " + wordInfo.content, key))
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

	void playEnglishAndJapanese(Dictionary.Entry entry) {
		play(entry, Dictionary.DataLang.english, () -> play(entry, Dictionary.DataLang.japanese, null));
	}

	void playEnglish(Dictionary.Entry entry) { play(entry, Dictionary.DataLang.english, null); }

	private void play(Dictionary.Entry entry, Dictionary.DataLang lang, Runnable next) {
		releasePlayer();
		String path = entry.toPath(lang);
		try {
			if (path == null) { MyLibrary.DisplayOutput.makeToastForShort(context, "このデータには音声がありません。"); return; }
			player = AudioSource.create(context, path);
			if (player == null) { MyLibrary.DisplayOutput.makeToastForShort(context, "音声が見つかりません。"); return; }
			player.setOnCompletionListener(mp -> { releasePlayer(); if (binding != null && next != null) next.run(); });
			player.setOnErrorListener((mp, what, extra) -> { releasePlayer(); return true; });
			player.start();
		} catch (Exception error) { releasePlayer(); showException(context, error); }
	}

	private void releasePlayer() {
		if (player != null) { player.release(); player = null; }
	}

	@Override public void onDestroyView() {
		releasePlayer();
		if (listViewKensakuResult != null) listViewKensakuResult.setAdapter(null);
		if (searchView != null) searchView.setOnQueryTextListener(null);
		buttonKensakuHouhou = null; searchView = null; listViewKensakuResult = null;
		textViewKensakuResultCount = null; searchAdapter = null; context = null;
		super.onDestroyView();
	}
}
