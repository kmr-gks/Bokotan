package com.gukos.bokotan;

import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.contains;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.ends;
import static com.gukos.bokotan.KensakuFragment.enumKensakuHouhou.starts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.tangoNumToString;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEX;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.DataQ.q1;
import static com.gukos.bokotan.WordPhraseData.DataQ.qp1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y08;
import static com.gukos.bokotan.WordPhraseData.DataQ.y1;
import static com.gukos.bokotan.WordPhraseData.DataQ.y2;
import static com.gukos.bokotan.WordPhraseData.DataQ.y3;
import static com.gukos.bokotan.WordPhraseData.DataType.phrase;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;
import static com.gukos.bokotan.WordPhraseData.PasstanPhrase;
import static com.gukos.bokotan.WordPhraseData.PasstanWord;
import static com.gukos.bokotan.WordPhraseData.Svl;
import static com.gukos.bokotan.WordPhraseData.TanjukugoEXWord;
import static com.gukos.bokotan.WordPhraseData.TanjukugoPhrase;
import static com.gukos.bokotan.WordPhraseData.TanjukugoWord;
import static com.gukos.bokotan.WordPhraseData.YumeWord;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.gukos.bokotan.databinding.FragmentKensakuBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiFunction;

public class KensakuFragment extends UiManager.FragmentBingding<FragmentKensakuBinding> {
	
	public static TreeMap<String, GogenYomu> trGogenYomu;
	Thread threadInitial = null, threadSearch = null;
	boolean threadSearchIsRunning = true;
	
	KensakuFragment() {
		super(FragmentKensakuBinding::inflate);
	}
	
	static class WordInfo {
		static int size = 0;
		final int toushiNumber, localNumber;
		final String category, e, j, subCategory;
		final WordPhraseData.DataType dataType;
		
		WordInfo(String category, String e, String j, int localNumber, WordPhraseData.DataType dataType) {
			this(category, null, e, j, localNumber, dataType);
		}
		
		WordInfo(String category, String subCategory, String e, String j, int localNumber, WordPhraseData.DataType dataType) {
			size++;
			this.toushiNumber = size;
			this.category = category;
			if (Objects.isNull(subCategory)) this.subCategory = "";
			else this.subCategory = subCategory;
			this.e = e;
			this.j = j;
			this.localNumber = localNumber;
			this.dataType = dataType;
		}
		
		String[] getAllFieldString() {
			return new String[]{
				this.e,
				this.j,
				this.category,
				this.subCategory,
				String.valueOf(this.localNumber),
			};
		}
		
		public String toString() {
			try {
				String string = category + " " + subCategory + " " + e + " " + j;
				return string.substring(0, Math.min(string.length(), 50));
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}
		
		public String toDetailedString() {
			try {
				return "No. " + this.toushiNumber
					+ "\nカテゴリ: " + this.category
					+ " " + subCategory + "\n番号:"
					+ this.localNumber
					+ "\n" + this.e
					+ "\n発音:" + getHatsuon(this.e)
					+ "\n" + this.j + "\n"
					+ GogenYomuFactory.getGogenString(this.e, true, true);
			} catch (Exception e) {
				showException(e);
			}
			return "<不明>";
		}
	}
	
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
		
		public int toInt() {
			switch (this) {
				default:
				case starts: {
					return 1;
				}
				case contains: {
					return 2;
				}
				case ends: {
					return 3;
				}
			}
		}
		
		public enumKensakuHouhou toEnumKensakuHouhou(int i) {
			switch (i) {
				default:
				case 1: {
					return starts;
				}
				case 2: {
					return contains;
				}
				case 3: {
					return ends;
				}
			}
		}
	}
	
	private final ArrayList<WordInfo> allData = new ArrayList<>();
	private ArrayList<WordInfo> resultData = new ArrayList<>();
	enumKensakuHouhou kensakuHouhou = starts;
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		try {
			super.onViewCreated(view, savedInstanceState);
			new Thread(() -> activity.runOnUiThread(this::initialize)).start();
		} catch (Exception e) {
			showException(getContext(), e);
		}
	}
	
	public void initialize() {
		try {
			binding.editTextKensakuKey.addTextChangedListener((UiManager.UiInterface.TextWatcherAfterOnly) this::EditTextChanged);
			binding.buttonClearKey.setOnClickListener(v -> binding.editTextKensakuKey.setText(""));
			
			kensakuHouhou = kensakuHouhou.toEnumKensakuHouhou(MyLibrary.PreferenceManager.getIntData(context, "enumKensakuHouhou", "kensakuhouhou", kensakuHouhou.toInt()));
			binding.buttonKensakuHouhou.setText(kensakuHouhou.toString());
			binding.buttonKensakuHouhou.setOnClickListener(v -> {
				try {
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
					binding.editTextKensakuKey.setText(binding.editTextKensakuKey.getText());
				} catch (Exception e) {
					MyLibrary.ExceptionManager.showException(context, e);
				}
			});
			
			threadInitial = new Thread(() -> {
				try {
					setData();
				} catch (Exception e) {
					MyLibrary.ExceptionManager.showException(context, e);
				}
			});
			threadInitial.start();
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	public void setData() {
		try {
			//ファイルを開いて読み込む
			WordInfo.size = 0;
			Map<String, String> mapQName = new HashMap<>() {{
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
				put("-eiken-jukugo", "英検熟語");
				put("-eikenp1-jukugo", "英検熟語(準1)");
				put("-Toefl-Chokuzen", "TOEFL直前");
				put("-Toeic-500ten", "TOEIC500点");
				put("-Toeic-700ten", "TOEIC700点");
				put("-Toeic-900ten", "TOEIC900点");
				put("-Toeic-Chokuzen", "TOEIC直前");
				put("-Toeic-jukugo", "TOEIC熟語");
				put("d1phrase12", "1");
				put("d2phrase1", "2");
			}};
			
			//パス単単語
			for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
				WordPhraseData w = new WordPhraseData(PasstanWord + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("パス単" + mapQName.get(Q), tangoNumToString("パス単" + mapQName.get(Q), i), w.e[i], w.j[i], i, word));
			}
			
			//単熟語EX単語
			for (String Q : new String[]{"1q", "p1q"}) {
				WordPhraseData w = new WordPhraseData(TanjukugoWord + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("単熟語EX" + mapQName.get(Q), tangoNumToString("単熟語EX" + mapQName.get(Q), i), w.e[i], w.j[i], i, word));
				WordPhraseData wx = new WordPhraseData(TanjukugoEXWord + Q, context);
				for (int i = 1; i < Math.min(wx.e.length, wx.j.length); i++)
					if (wx.e[i] != null && wx.j[i] != null)
						allData.add(new WordInfo("単熟語EX" + mapQName.get(Q), "Unit EX", wx.e[i], wx.j[i], i, word));
			}
			
			//ユメタン単語
			for (String Q : new String[]{"00", "08", "1", "2", "3"}) {
				WordPhraseData w = new WordPhraseData(YumeWord + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo(mapQName.get(Q), "Unit" + ((i - 1) / 100 + 1), w.e[i], w.j[i], i, word));
			}
			
			//語源データも読み込む
			int gogenNum = 0;
			for (TreeMap.Entry<String, GogenYomu> map : trGogenYomu.entrySet())
				allData.add(new WordInfo("読む語源学", map.getKey(), map.getValue().wordJpn, ++gogenNum, WordPhraseData.DataType.gogengaku));
			
			//英語漬け.comから読み込み
			for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q",
				"-eiken-jukugo", "-eikenp1-jukugo", "-Toefl-Chokuzen", "-Toeic-500ten", "-Toeic-700ten", "-Toeic-900ten",
				"-Toeic-Chokuzen", "-Toeic-jukugo",}) {
				WordPhraseData wpd = new WordPhraseData("Eigoduke.com/" + "WordDataEigoduke" + Q, context);
				for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
					if (wpd.e[i] != null && wpd.j[i] != null)
						allData.add(new WordInfo("英語漬け" + mapQName.get(Q), wpd.e[i], wpd.j[i], i, WordPhraseData.DataType.eigoduke_com));
			}
			for (int num = 1; num <= 10; num++) {
				String Q = "-toeic (" + num + ")";
				WordPhraseData wpd = new WordPhraseData("Eigoduke.com/" + "WordDataEigoduke" + Q, context);
				for (int i = 1; i < Math.min(wpd.e.length, wpd.j.length); i++)
					if (wpd.e[i] != null && wpd.j[i] != null)
						allData.add(new WordInfo("英語漬け" + "TOEIC" + num, wpd.e[i], wpd.j[i], i, WordPhraseData.DataType.eigoduke_com));
			}
			
			//distinction
			for (int d = 1; d <= 4; d++) {
				WordPhraseData w = new WordPhraseData(WordPhraseData.distinction + "d" + d + "word", context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("Distinction" + d, tangoNumToString("Distinction" + d, i), w.e[i], w.j[i], i, word));
			}
			
			//フレーズ
			for (String Q : new String[]{"1q", "p1q", "2q", "p2q", "3q", "4q", "5q"}) {
				WordPhraseData w = new WordPhraseData(PasstanPhrase + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("パス単" + mapQName.get(Q), tangoNumToString("パス単" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
			}
			
			//単熟語EX単語
			for (String Q : new String[]{"1q", "p1q"}) {
				WordPhraseData w = new WordPhraseData(TanjukugoPhrase + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("単熟語EX" + mapQName.get(Q), tangoNumToString("単熟語EX" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
			}
			
			//distinction
			for (String Q : new String[]{"d1phrase12", "d2phrase1"}) {
				WordPhraseData w = new WordPhraseData(WordPhraseData.distinction + Q, context);
				for (int i = 1; i < Math.min(w.e.length, w.j.length); i++)
					if (w.e[i] != null && w.j[i] != null)
						allData.add(new WordInfo("Distinction" + mapQName.get(Q), tangoNumToString("Distinction" + mapQName.get(Q), i), w.e[i], w.j[i], i, phrase));
			}
			
			//SVL12000辞書
			WordPhraseData wordPhraseData = new WordPhraseData(Svl, context);
			for (int i = 1; i < Math.min(wordPhraseData.e.length, wordPhraseData.j.length); i++)
				if (wordPhraseData.e[i] != null && wordPhraseData.j[i] != null)
					allData.add(new WordInfo("SVL", Integer.toString((i - 1) / 1000 + 1), wordPhraseData.e[i], wordPhraseData.j[i], i, word));
			
			//コピー
			resultData = new ArrayList<>(allData);
			SetHatsuonKigou(context);
			
			//別スレッドからUIを変更するときに必要
			activity.runOnUiThread(() -> {
				try {
					binding.textViewKensakuResultCount.setText(resultData.size() + "件");
					setListView(binding.listViewKensakuResult, resultData, null, null);
				} catch (Exception e) {
					showException(context, e);
				}
			});
		} catch (Exception e) {
			showException(context, e);
		}
		
	}
	
	@Override
	public void onPause() {
		try {
			super.onPause();
			MyLibrary.PreferenceManager.putIntData(context, "enumKensakuHouhou", "kensakuhouhou", kensakuHouhou.toInt());
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	private void setListView(ListView lv, ArrayList<WordInfo> wordInfoList, ArrayList<CharSequence> titleList, String key) {
		try {
			if (titleList == null || key.length() == 0) {
				lv.setAdapter(new ArrayAdapter<>(context, R.layout.my_simple_list_item_1, wordInfoList));
			}
			else {
				lv.setAdapter(new ArrayAdapter<>(context, R.layout.my_simple_list_item_1, titleList));
			}
			lv.setOnItemClickListener((adapterView, view, i, l) -> {
				try {
					WordInfo ld = wordInfoList.get(i);
					new AlertDialog.Builder(context)
						.setTitle(MyLibrary.DisplayOutput.setStringColored(ld.toushiNumber + " : " + ld.e, key))
						.setMessage(MyLibrary.DisplayOutput.setStringColored(ld.toDetailedString(), key))
						.setPositiveButton("閉じる", null)
						.setNeutralButton("英→日発音", (dialogInterface, i1) -> playEnglishAndJapanese(ld))
						.setNegativeButton("英語発音", (dialogInterface, i1) -> playEnglish(ld))
						.create()
						.show();
				} catch (Exception e) {
					showException(context, e);
				}
			});
		} catch (Exception e) {
			showException(context, e);
		}
	}
	
	void playEnglishAndJapanese(WordInfo ld) {
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
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugo1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugop1q", ld.dataType, english, ld.localNumber);
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
						pathJpn = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugo1q", ld.dataType, japanese, ld.localNumber);
						break;
					}
					case "単熟語EX準1級": {
						pathJpn = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugop1q", ld.dataType, japanese, ld.localNumber);
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
	
	void playEnglish(WordInfo ld) {
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
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugo1q", ld.dataType, english, ld.localNumber);
					break;
				}
				case "単熟語EX準1級": {
					path = MyLibrary.FileDirectoryManager.getPath(tanjukugoEX, "tanjukugop1q", ld.dataType, english, ld.localNumber);
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
	
	public void EditTextChanged(Editable editable) {
		try {
			if (threadSearchIsRunning) {
				threadSearchIsRunning = false;
				threadSearch = null;
			}
			threadSearch = new Thread(() -> {
				synchronized (this) {
					//文字入力時
					resultData.clear();
					ArrayList<CharSequence> titleList = new ArrayList<>();
					String key = editable.toString().toLowerCase();//小文字に変換
					//何も検索欄に入力されていないとき
					if (key.length() == 0) {
						resultData = new ArrayList<>(allData);
						//ラムダ式の制約により実行できない。
						//titleList=null;
					}
					else {
						BiFunction<String, String, Boolean> biFunction;
						//Function2<String,String,Boolean>f2=(a,b)->true;
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
						activity.runOnUiThread(() -> {
							for (var wordInfo : allData) {
								for (var field : wordInfo.getAllFieldString()) {
									if (biFunction.apply(field, key)) {
										resultData.add(wordInfo);
										titleList.add(MyLibrary.DisplayOutput.setStringColored(wordInfo.toString(), key));
										break;
									}
								}
								if (!threadSearchIsRunning) return;
							}
						});
						
					}
					activity.runOnUiThread(() -> {
						binding.textViewKensakuResultCount.setText(resultData.size() + "件");
						setListView(binding.listViewKensakuResult, resultData, titleList, key);
					});
				}
			});
			threadSearchIsRunning = true;
			threadSearch.start();
		} catch (Exception e) {
			MyLibrary.ExceptionManager.showException(context, e);
		}
	}
}