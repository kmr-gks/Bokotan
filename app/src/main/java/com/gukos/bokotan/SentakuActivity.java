package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.kioku_chBox;
import static com.gukos.bokotan.MainActivity.kioku_file;
import static com.gukos.bokotan.MainActivity.lastnum;
import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MyLibrary.showException;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SentakuActivity extends AppCompatActivity {
	static final CheckBox[] cb = new CheckBox[2500];

	/* LitViewで表示する基データ　*/
	private static class CheckData {
		final String text; //チェックボックスのメッセージ
		final boolean check; // チェック状態
		final int num;

		CheckData(String s, boolean b, int n) {
			text = s;
			check = b;
			num = n;
		}
	}

	// ListViewで表示するListを生成
	private List<CheckData> mList = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			// 今回は使いません
			setContentView(R.layout.activity_sentaku);

			mList = IntStream.range(0, lastnum + 1).mapToObj(n -> new CheckData(n + ":番目", false, n)).collect(Collectors.toList());

			//ここから軽量リストビュー
			ListView lv = findViewById(R.id.listView);
			lv.setDivider(null); // ListViewの境界線を消す
			lv.setAdapter(new ArrayAdapter<CheckData>(this, 0, mList) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					CheckData data = getItem(position);
					int i = data.num;
					cb[i] = (convertView instanceof CheckBox) ? (CheckBox) convertView : new CheckBox(getApplicationContext());
					cb[i].setText(i + ":" + MainActivity.wordE[i] + " " + MainActivity.wordJ[i] + '\n' + MainActivity.nSeikaisuu[i] + '/' + (MainActivity.nSeikaisuu[i] + MainActivity.nHuseikaisuu[i]));
					cb[i].setText(String.format("%d:%2d%%%s%s", i, (int) MainActivity.nSeikaisuu[i] * 100 / (MainActivity.nSeikaisuu[i] + MainActivity.nHuseikaisuu[i] + 1), MainActivity.wordE[i], MainActivity.wordJ[i]));
					cb[i].setChecked(kioku_chBox[i]);
					cb[i].setId(i);
					cb[i].setTag(position); // ViewにIndexを紐づけておく
					cb[i].setOnClickListener(v -> kioku_chBox[v.getId()] = ((CheckBox) v).isChecked());
					int currentNightMode = new Configuration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
					switch (currentNightMode) {
						case Configuration.UI_MODE_NIGHT_NO:
							// Night mode is not active, we're using the light theme
							break;
						case Configuration.UI_MODE_NIGHT_YES:
							// Night mode is active, we're using dark theme
							//cb[i].setTextColor(Color.WHITE);
							break;
					}
					cb[i].setTextColor(getColor(R.color.textcolor));
					return cb[i];
				}
			});
			lv.setSelection(now);
		} catch (Exception e) {
			showException(this, e);
		}
	}

	@Override
	public void onStop() {
		try {
			super.onStop();
			for (int i = 1; i < lastnum; i++) {
				if (kioku_file[i] != kioku_chBox[i]) {
					MyLibrary.putBoolData(this, "settings-" + Q_sentaku_activity.strQenum.getQ, Q_sentaku_activity.strQenum.getQ + i, kioku_chBox[i]);
					kioku_file[i] = kioku_chBox[i];
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}

	public void onResetButtonClicked(View v) {
		try {
			new AlertDialog.Builder(this).setTitle("リセットしますか").setMessage("すべてのチェックを外します")
					.setPositiveButton("yes", (dialog, which) -> {
						// クリックしたときの処理
						for (int i = 1; i <= lastnum; i++) {
							//バグフィックス
							//cb[i].setChecked(false);
							if (cb[i] != null) cb[i].setChecked(false);
							kioku_chBox[i] = false;
						}
					})
					.setNegativeButton("no", (dialog, which) -> {
						// クリックしたときの処理
					})
					.setNeutralButton("cancel", (dialog, which) -> {
						// クリックしたときの処理
					})
					.show();
		} catch (Exception e) {
			showException(this, e);
		}
	}
}