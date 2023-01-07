package com.gukos.bokotan;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gukos.bokotan.MainActivity.kioku_chBox;
import static com.gukos.bokotan.MainActivity.kioku_file;
import static com.gukos.bokotan.MainActivity.lastnum;
import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MainActivity.tag;

//@RequiresApi(api = Build.VERSION_CODES.N)
public class SentakuActivity extends AppCompatActivity {
	static final CheckBox[] cb =new CheckBox[2500];
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
	private List<CheckData>mList=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag,getLocalClassName()+"--onCreate");
		super.onCreate(savedInstanceState);
		// 今回は使いません
		setContentView(R.layout.activity_sentaku);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mList = IntStream.range(0, lastnum + 1).mapToObj(n -> new CheckData(n + ":番目", false, n)).collect(Collectors.toList());
		}

		//ここから軽量リストビュー
		ListView lv = findViewById(R.id.listView);
		lv.setDivider(null); // ListViewの境界線を消す
		lv.setAdapter(new ArrayAdapter<CheckData>(this, 0, mList) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				CheckData data = getItem(position);
				int i=data.num;
				cb[i] = (convertView instanceof CheckBox) ? (CheckBox)convertView : new CheckBox(getApplicationContext());
				cb[i].setText(i + ":" + MainActivity.wordE[i] + " " + MainActivity.wordJ[i]+'\n'+MainActivity.nSeikaisuu[i]+'/'+(MainActivity.nSeikaisuu[i]+MainActivity.nHuseikaisuu[i]));
				cb[i].setText(String.format("%d:%2d%%%s%s",i,(int)MainActivity.nSeikaisuu[i]*100/(MainActivity.nSeikaisuu[i]+MainActivity.nHuseikaisuu[i]+1),MainActivity.wordE[i],MainActivity.wordJ[i]));
				cb[i].setChecked(kioku_chBox[i]);
				cb[i].setId(i);
				if (kioku_chBox[i]) Log.d(tag,"chbox_checked:"+i);
				cb[i].setTag(position); // ViewにIndexを紐づけておく
				cb[i].setOnClickListener(v -> kioku_chBox[v.getId()]=((CheckBox)v).isChecked());
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
				cb[i].setTextColor(Color.GRAY);
				return cb[i];
			}
		});
		lv.setSelection(now);
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.d(tag,getLocalClassName()+"--onStop");
		if (lastnum == 2400) {
			for (int i = 1; i < lastnum; i++) {
				//バグフィックス
				//if (cb[i]!=null&&cb[i].isChecked()!= kioku_file[i]){
				if (kioku_file[i] != kioku_chBox[i]) {
					Log.d(tag, "modified:" + kioku_chBox[i]);
					getSharedPreferences("settings-1q",MODE_PRIVATE).edit().putBoolean("1q" + i, kioku_chBox[i]).apply();
					kioku_file[i]=kioku_chBox[i];
				}
			}
		}
		if (lastnum == 1850) {
			for (int i = 1; i < lastnum; i++) {
				//バグフィックス
				if (kioku_file[i] != kioku_chBox[i]) {
					Log.d(tag, "modified:" + i + cb[i].isChecked());
					getSharedPreferences("settings-p1q",MODE_PRIVATE).edit().putBoolean("p1q" + i, cb[i].isChecked()).apply();
					kioku_file[i]=kioku_chBox[i];
				}
			}
		}
	}

	public void onResetButtonClicked(View v){
		new AlertDialog.Builder(this).setTitle( "リセットしますか" ).setMessage( "すべてのチェックを外します" )
				.setPositiveButton( "yes", (dialog, which) -> {
					// クリックしたときの処理
					for (int i=1;i<=lastnum;i++)
					{
						//バグフィックス
						//cb[i].setChecked(false);
						if (cb[i]!=null) cb[i].setChecked(false);
						kioku_chBox[i]=false;
					}
				})
				.setNegativeButton("no", (dialog, which) -> {
					// クリックしたときの処理
				})
				.setNeutralButton( "cancel", (dialog, which) -> {
					// クリックしたときの処理
				})
				.show();

	}
}
