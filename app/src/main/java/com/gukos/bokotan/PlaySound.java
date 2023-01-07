package com.gukos.bokotan;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static com.gukos.bokotan.MainActivity.hashMapKishutu;
import static com.gukos.bokotan.MainActivity.textViewHatsuonKigou;
import static com.gukos.bokotan.MainActivity.textViewPath;
import static com.gukos.bokotan.MyLibrary.DataBook.passTan;
import static com.gukos.bokotan.MyLibrary.DataBook.tanjukugoEX;
import static com.gukos.bokotan.MyLibrary.DataBook.yumetan;
import static com.gukos.bokotan.MyLibrary.DataLang.english;
import static com.gukos.bokotan.MyLibrary.DataLang.japanese;
import static com.gukos.bokotan.MyLibrary.DataType.phrase;
import static com.gukos.bokotan.MyLibrary.DataType.word;
import static com.gukos.bokotan.MyLibrary.DataName;
import static com.gukos.bokotan.MyLibrary.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.MyLibrary.HatsuonKigou.getHatsuon;
import static com.gukos.bokotan.MyLibrary.getIntData;
import static com.gukos.bokotan.MyLibrary.showException;
import static com.gukos.bokotan.GogenYomuFactory.getGogenString;
import static com.gukos.bokotan.MainActivity.bEnglishToJapaneseOrder;
import static com.gukos.bokotan.MainActivity.bHyojiYakuBeforeRead;
import static com.gukos.bokotan.MainActivity.dPlaySpeedEng;
import static com.gukos.bokotan.MainActivity.dPlaySpeedJpn;
import static com.gukos.bokotan.MainActivity.isPhraseMode;
import static com.gukos.bokotan.MainActivity.kioku_chBox;
import static com.gukos.bokotan.MainActivity.lastnum;
import static com.gukos.bokotan.MainActivity.nFrom;
import static com.gukos.bokotan.MainActivity.nTo;
import static com.gukos.bokotan.MainActivity.now;
import static com.gukos.bokotan.MainActivity.strPhraseE;
import static com.gukos.bokotan.MainActivity.strPhraseJ;
import static com.gukos.bokotan.MainActivity.strQ;
import static com.gukos.bokotan.MainActivity.tvGenzai;
import static com.gukos.bokotan.MainActivity.tvSeikaisu;
import static com.gukos.bokotan.MainActivity.tvSeikaisuu;
import static com.gukos.bokotan.MainActivity.tvWordEng;
import static com.gukos.bokotan.MainActivity.tvWordJpn;
import static com.gukos.bokotan.MainActivity.tvsubE;
import static com.gukos.bokotan.MainActivity.tvsubJ;
import static com.gukos.bokotan.MainActivity.tvGogen;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;
import static com.gukos.bokotan.MyLibrary.getPath;
import static com.gukos.bokotan.MyLibrary.puts;
import static com.gukos.bokotan.Q_sentaku_activity.cbDirTOugou;
import static com.gukos.bokotan.Q_sentaku_activity.checkBoxHatsuonKigou;
import static com.gukos.bokotan.Q_sentaku_activity.isWordAndPhraseMode;
import static com.gukos.bokotan.Q_sentaku_activity.bSkipOboe;
import static com.gukos.bokotan.Q_sentaku_activity.swOnlyFirst;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.core.app.NotificationCompat;

public class PlaySound extends Service {
	static MediaPlayer mp;
	static int nInstance = 0;
	String path;

	public PlaySound() {
	}

	void Sleep() {
		Sleep(500);
	}

	void Sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (Exception e) {
			showException(this, e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		resetMediaPlayer(mp);
		mp = null;
		nInstance--;
		puts("onDestroy:" + nInstance);
	}

	private void resetMediaPlayer(MediaPlayer mp) {
		try {
			if (mp != null) mp.stop();
		} catch (Exception e) {
			showException(this, e);
		}
		try {
			if (mp != null) mp.reset();
		} catch (Exception e) {
			showException(this, e);
		}
		try {
			if (mp != null) mp.release();
		} catch (Exception e) {
			showException(this, e);
		}
	}

	private void playStart(MediaPlayer mediaPlayer) {
		mediaPlayer.start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			resetMediaPlayer(mp);
			mp = null;
			nInstance++;
			puts("onStartCommand:" + nInstance);
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String strTutiChannelName = "再生中", strTutiChannelId = "bokotan_foreground", strTutiChannelShosai = "バックグラウンドで再生中";
			if (nm.getNotificationChannel(strTutiChannelId) == null) {
				NotificationChannel nc = new NotificationChannel(strTutiChannelId, strTutiChannelName, NotificationManager.IMPORTANCE_HIGH);
				nc.setDescription(strTutiChannelShosai);
				nm.createNotificationChannel(nc);
			}

			Intent pipctrlintent = new Intent(this, PipControlBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			Intent sendStopIntent = new Intent(this, StopPlayBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			Intent sendPipIntent = new Intent(this, StartPipBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			PendingIntent sendStopPendingIntent = PendingIntent.getBroadcast(this, 0, sendStopIntent, FLAG_IMMUTABLE);
			PendingIntent sendPipPendingIntent = PendingIntent.getBroadcast(this, 0, sendPipIntent, FLAG_IMMUTABLE);

			Notification n = new NotificationCompat.Builder(this, strTutiChannelId)
					.setContentTitle("再生中").setContentText("バックグラウンドで再生中")
					.setSmallIcon(R.mipmap.ic_launcher)
					.addAction(R.drawable.ic_launcher_foreground, "停止", sendStopPendingIntent)
					.addAction(R.mipmap.launcher_new_icon, "小窓で表示", sendPipPendingIntent)
					.build();
			startForeground(1, n);

			SetHatsuonKigou(this);
		/*
		//発音記号のためにSVL読み込み
		if (checkBoxHatsuonKigou.isChecked()&&hashMapHatsuonKigou.size()==0){
			textViewHatsuonKigou.setVisibility(View.VISIBLE);
			WordPhraseData wordPhraseDataSVL=new WordPhraseData(Svl,this);
			for (int i=1;i<Math.min(wordPhraseDataSVL.e.length, wordPhraseDataSVL.j.length);i++)
				if (wordPhraseDataSVL.e[i]!=null&&wordPhraseDataSVL.j[i]!=null)
					hashMapHatsuonKigou.put(wordPhraseDataSVL.e[i],wordPhraseDataSVL.j[i]);
		}else if(!checkBoxHatsuonKigou.isChecked()) {
			textViewHatsuonKigou.setText("");
			textViewHatsuonKigou.setVisibility(GONE);
		}
		*/

			bokotanPlayEnglish();
		} catch (Exception e) {
			showException(this, e);
		}
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	private void setEngText(int num) {
		try {
			tvWordEng.setMaxLines(1);
			tvWordEng.setText(wordE[num]);
			if (checkBoxHatsuonKigou.isChecked())
				textViewHatsuonKigou.setText(getHatsuon(wordE[now]));
		} catch (Exception e) {
			showException(this, e);
		}
	}

	void bokotanPlayEnglish() {
		try {
			String strQ_WordPhraseKyoutuu = strQ;
			try {
				if (strQ.equals("ph1q")) strQ_WordPhraseKyoutuu = "1q";
				if (strQ.equals("php1q")) strQ_WordPhraseKyoutuu = "p1q";
			} catch (Exception e) {
				showException(this, e);
				strQ = "p1q";
			}
			if (bEnglishToJapaneseOrder) {
				if (!isWordAndPhraseMode || isPhraseMode) now++;
				if (bSkipOboe) {
					switch (Q_sentaku_activity.skipjoken) {
						case seikai1: {
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null) || getIntData(this, "testActivity" + strQ_WordPhraseKyoutuu + "Test", "nWordSeikaisuu" + now, 0) > 0 || kioku_chBox[now]) {
								now++;
								if (now >= lastnum) break;
							}
							break;
						}
						case huseikai2: {
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null)
									|| getIntData(this, "testActivity" + strQ_WordPhraseKyoutuu + "Test", "nWordHuseikaisuu" + now, 0) < 2
									|| kioku_chBox[now]) {
								now++;
								if (now >= lastnum) break;
							}
							break;
						}
						case onlyHugoukaku: {
							int seikaisu = getIntData(this, DataName.testActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語正解数 + now, 0);
							int huseikaisu = getIntData(this, DataName.testActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語不正解数 + now, 0);
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null)
									|| TestActivity.isGokaku(seikaisu, huseikaisu)) {
								now++;
								seikaisu = getIntData(this, DataName.testActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語正解数 + now, 0);
								huseikaisu = getIntData(this, DataName.testActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語不正解数 + now, 0);
							}
							break;
						}
						case kirokunomi:
						default: {
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null) || kioku_chBox[now]) {
								now++;
							}
							break;
						}
					}
					tvSeikaisu.setText("kioku:" + kioku_chBox[now] + "正解" + getIntData(this, "testActivity" + strQ + "Test", "nWordSeikaisuu" + now, 0) + '/' + (getIntData(this, "testActivity" + strQ + "Test", "nWordSeikaisuu" + now, 0) + getIntData(this, "testActivity" + strQ + "Test", "nWordHuseikaisuu" + now, 0)));
				}

				if (now <= nFrom) now = nFrom;
				if (now >= nTo) now = nFrom;
				if (tvGenzai != null) tvGenzai.setText("No." + now);
				int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
				if (lastnum == 2400) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordHuseikaisuu" + now, 0);
				} else if (lastnum == 1850) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordHuseikaisuu" + now, 0);
				}
				if (tvSeikaisuu != null)
					tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);
				if (isWordAndPhraseMode) {
					switch (strQ) {
						case "1q": {
							strQ = "ph1q";
							break;
						}
						case "p1q": {
							strQ = "php1q";
							break;
						}
						case "ph1q": {
							strQ = "1q";
							break;
						}
						case "php1q": {
							strQ = "p1q";
							break;
						}
					}
					isPhraseMode = !isPhraseMode;
				}

				if (isPhraseMode) {
					if (bHyojiYakuBeforeRead) {
						tvWordJpn.setText(strPhraseJ[now]);
					} else {
						tvWordJpn.setText("");
					}
					tvWordEng.setMaxLines(10);
					tvWordEng.setText(strPhraseE[now]);
					tvsubE.setText(wordE[now]);
					tvsubJ.setText(wordJ[now]);
				} else {
					if (bHyojiYakuBeforeRead) {
						tvWordJpn.setText(wordJ[now]);
					} else {
						tvWordJpn.setText("");
					}
					setEngText(now);
				}
				if (isPhraseMode || isWordAndPhraseMode) {
					tvsubE.setText(wordE[now]);
					tvsubJ.setText(wordJ[now]);
				}
				tvGogen.setText(getGogenString(now, false, false));
			}
			PipActivity.ChangeText(wordE[now], wordJ[now], now);
			String strQPath = strQ;
			if ((cbDirTOugou.isChecked() && strQPath.startsWith("ph")) || strQPath.startsWith("phy")) {
				//フォルダ統合
				strQPath = strQ.substring(2);
			}
			//単語のみ
			if (isPhraseMode)//フレーズならば
				if (strQPath.startsWith("y"))
					path = getPath(yumetan, strQPath, phrase, english, now, cbDirTOugou.isChecked());
				else if (strQPath.startsWith("tanjukugo")) {
					path = getPath(tanjukugoEX, strQPath, phrase, english, now, cbDirTOugou.isChecked());
				} else
					path = getPath(passTan, strQPath, phrase, english, now, cbDirTOugou.isChecked());
			else if (strQPath.startsWith("y"))
				path = getPath(yumetan, strQPath, word, english, now, cbDirTOugou.isChecked());
			else if (strQPath.startsWith("tanjukugo")) {
				path = getPath(tanjukugoEX, strQPath, word, english, now, cbDirTOugou.isChecked());
			} else path = getPath(passTan, strQPath, word, english, now, cbDirTOugou.isChecked());
			textViewPath.setText(path);
			try {
				mp = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
				mp.setPlaybackParams(mp.getPlaybackParams().setSpeed((float) dPlaySpeedEng));
				playStart(mp);
				mp.setOnCompletionListener(mp -> {
					//puts("再生終了しました。"+getNowTime());
					resetMediaPlayer(mp);
					mp=null;
					if (isPhraseMode || strQ.startsWith("y") || strQ.startsWith("tanjukugo")) {
						bokotanPlayJapanese();
					} else {
						JosiCheck(0);
					}
				});
			} catch (Exception e) {
				showException(this, e);
				Sleep();
				bokotanPlayJapanese();
			}
			if (!bEnglishToJapaneseOrder && !bHyojiYakuBeforeRead) {
				if (isPhraseMode) {
					tvWordEng.setMaxLines(10);
					tvWordEng.setText(strPhraseE[now]);
				} else {
					setEngText(now);
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}

	void bokotanPlayJapanese() {
		try {
			if (!bEnglishToJapaneseOrder) {
				now++;
				if (bSkipOboe) {
					while (kioku_chBox[now]) {
						now++;
					}
				}
				try {
					if (swOnlyFirst.isChecked())
						while (hashMapKishutu.get(wordE[now]) != null)
							now++;
				} catch (Exception e) {
					showException(this, e);
				}
				if (now <= nFrom) now = nFrom;
				if (now >= nTo) now = nFrom;
				if (tvGenzai != null) tvGenzai.setText("No." + now);
				int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
				if (lastnum == 2400) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordHuseikaisuu" + now, 0);
				} else if (lastnum == 1850) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordHuseikaisuu" + now, 0);
				}
				tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);

				if (isPhraseMode) {
					if (bHyojiYakuBeforeRead) {
						tvWordEng.setMaxLines(10);
						tvWordEng.setText(strPhraseE[now]);
					} else {
						tvWordEng.setText("");
					}
					tvWordJpn.setText(strPhraseJ[now]);
				} else {
					if (bHyojiYakuBeforeRead) {
						setEngText(now);
					} else {
						tvWordEng.setText("");
					}
					tvWordJpn.setText(wordJ[now]);
				}
				if (isPhraseMode || isWordAndPhraseMode) {
					tvsubE.setText(wordE[now]);
					tvsubJ.setText(wordJ[now]);
				}
				PipActivity.ChangeText(wordE[now], wordJ[now], now);
				tvGogen.setText(getGogenString(now, false, false));
			}

			String strQPath = strQ;
			if ((cbDirTOugou.isChecked() && strQPath.startsWith("ph")) || strQPath.startsWith("phy")) {
				//フォルダ統合
				strQPath = strQ.substring(2);
			}
			if (isPhraseMode)
				if (strQPath.startsWith("y"))
					path = getPath(yumetan, strQPath, phrase, japanese, now, cbDirTOugou.isChecked());
				else if (strQPath.startsWith("tanjukugo")) {
					path = getPath(tanjukugoEX, strQPath, phrase, japanese, now, cbDirTOugou.isChecked());
				} else
					path = getPath(passTan, strQPath, phrase, japanese, now, cbDirTOugou.isChecked());
			else if (strQPath.startsWith("y"))
				path = getPath(yumetan, strQPath, word, japanese, now, cbDirTOugou.isChecked());
			else if (strQPath.startsWith("tanjukugo")) {
				path = getPath(tanjukugoEX, strQPath, word, japanese, now, cbDirTOugou.isChecked());
			} else path = getPath(passTan, strQPath, word, japanese, now, cbDirTOugou.isChecked());
			textViewPath.setText(path);
			try {
				mp = MediaPlayer.create(this, Uri.parse(path));
				mp.setPlaybackParams(mp.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
				playStart(mp);
				mp.setOnCompletionListener(mp -> {
					//puts("再生終了しました。"+getNowTime());
					resetMediaPlayer(mp);
					mp=null;
					bokotanPlayEnglish();
				});
			} catch (Exception e) {
				showException(this, e);
				Sleep();
				bokotanPlayEnglish();
			}
			if (bEnglishToJapaneseOrder && !bHyojiYakuBeforeRead) {
				if (isPhraseMode) {
					tvWordJpn.setText(strPhraseJ[now]);
				} else {
					tvWordJpn.setText(wordJ[now]);
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}


	public void JosiCheck(int index)//最初は0を指定
	{
		try {
			MediaPlayer mpJosi = new MediaPlayer();
			mpJosi.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
			String strJosi;
			int nowForJosiCheck = now;
			if (!bEnglishToJapaneseOrder) nowForJosiCheck++;
			char c = wordJ[nowForJosiCheck].charAt(index);
			switch (c) {
				case 'を':
				case 'に':
				case 'の':
				case 'で': {
					strJosi = MyLibrary.getJosiPath(c);
					break;
				}
				case '(':
				case '（': {
					for (int i = 0; i < wordJ[nowForJosiCheck].length(); i++) {
						if (wordJ[nowForJosiCheck].charAt(i) == ')' || wordJ[nowForJosiCheck].charAt(i) == '）') {
							JosiCheck(i + 1);
							return;
						}
					}
				}
				case '～': {
					JosiCheck(1);
					return;
				}
				default://助詞がない場合
				{
					bokotanPlayJapanese();
					return;
				}
			}
			//助詞がある場合
			mpJosi = MediaPlayer.create(this, Uri.parse(strJosi));
			mpJosi.setPlaybackParams(mpJosi.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
			playStart(mpJosi);
			mpJosi.setOnCompletionListener(mpJosi1 -> {
				//puts("再生終了しました。"+getNowTime());
				resetMediaPlayer(mpJosi1);
				mpJosi1=null;
				bokotanPlayJapanese();
			});
		} catch (Exception e) {
			showException(this, e);
		}
	}
}