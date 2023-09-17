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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import java.util.function.BiFunction;

//UiManager.FragmentBingdingを継承するとメモリが開放されたときに
//java.lang.RuntimeException: Unable to start activity ComponentInfo{com.gukos.bokotan/com.gukos.bokotan.TabActivity}: androidx.fragment.app.Fragment$InstantiationException: Unable to instantiate fragment com.gukos.bokotan.KensakuFragment: could not find Fragment constructor
//が発生するので、継承せず、データバインディングを使用しない
//TODO: データバインディングを使用し、UiManager.FragmentBingdingを継承しながら、メモリ解放後に落ちないようにする。
public class KensakuFragment extends Fragment {
	
	enumKensakuHouhou kensakuHouhou = starts;
	private String key;
	private Button buttonKensakuHouhou;
	private SearchView searchView;
	private ListView listViewKensakuResult;
	private TextView textViewKensakuResultCount;
	private Context context;
	
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
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_kensaku, container, false);
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
			
			printCurrentState("Dictionary.allData.size()=" + Dictionary.allData.size());
			onKensakuEnd(Dictionary.allData.size());
			listViewKensakuResult.setAdapter(new WordSearchAdapter<>(context, R.layout.my_simple_list_item_1, Dictionary.allData, this::onKensakuEnd));
			listViewKensakuResult.setOnItemClickListener(this::onItemClick);
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	private boolean onSearchViewTextChange(String newText) {
		//フィルターする
		//ListView#setFilterTextは内部的にListView#getAdapter#getFilter
		// を呼び出している。また、ポップアップが表示されてしまう。
		
		var adapter = (WordSearchAdapter<Dictionary.Entry>) listViewKensakuResult.getAdapter();
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
		getActivity().runOnUiThread(adapter::notifyDataSetChanged);
		return false;
	}
	
	private void onKensakuEnd(int count) {
		textViewKensakuResultCount.setText(count + "件");
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
	
	void playEnglishAndJapanese(Dictionary.Entry wordInfo) {
		try {
			var mediaPlayer = MediaPlayer.create(context, Uri.parse(wordInfo.toPath(Dictionary.DataLang.english)));
			mediaPlayer.start();
			mediaPlayer.setOnCompletionListener(mp -> {
				try {
					MediaPlayer.create(context, Uri.parse(wordInfo.toPath(Dictionary.DataLang.japanese))).start();
				} catch (Exception e) {
					//showException(context, e);
				}
			});
		} catch (Exception e) {
			//showException(context, e);
		}
	}
	
	void playEnglish(Dictionary.Entry wordInfo) {
		try {
			MediaPlayer.create(context, Uri.parse(wordInfo.toPath(Dictionary.DataLang.english))).start();
		} catch (Exception e) {
			//showException(context, e);
		}
	}
}