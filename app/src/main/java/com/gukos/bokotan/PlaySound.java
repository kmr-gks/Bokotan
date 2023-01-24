package com.gukos.bokotan;

import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;
import static com.gukos.bokotan.CommonVariables.bEnglishToJapaneseOrder;
import static com.gukos.bokotan.CommonVariables.bHyojiYakuBeforeRead;
import static com.gukos.bokotan.CommonVariables.bSkipOboe;
import static com.gukos.bokotan.CommonVariables.dPlaySpeedEng;
import static com.gukos.bokotan.CommonVariables.dPlaySpeedJpn;
import static com.gukos.bokotan.CommonVariables.from;
import static com.gukos.bokotan.CommonVariables.hashMapKishutu;
import static com.gukos.bokotan.CommonVariables.isPhraseMode;
import static com.gukos.bokotan.CommonVariables.isWordAndPhraseMode;
import static com.gukos.bokotan.CommonVariables.lastnum;
import static com.gukos.bokotan.CommonVariables.nFrom;
import static com.gukos.bokotan.CommonVariables.nTo;
import static com.gukos.bokotan.CommonVariables.now;
import static com.gukos.bokotan.CommonVariables.strPhraseE;
import static com.gukos.bokotan.CommonVariables.strPhraseJ;
import static com.gukos.bokotan.CommonVariables.strQ;
import static com.gukos.bokotan.CommonVariables.swOnlyFirst;
import static com.gukos.bokotan.CommonVariables.textViewHatsuonKigou;
import static com.gukos.bokotan.CommonVariables.textViewPath;
import static com.gukos.bokotan.CommonVariables.tvGenzai;
import static com.gukos.bokotan.CommonVariables.tvGogen;
import static com.gukos.bokotan.CommonVariables.tvNumSeikaisuu;
import static com.gukos.bokotan.CommonVariables.tvSeikaisu;
import static com.gukos.bokotan.CommonVariables.tvWordEng;
import static com.gukos.bokotan.CommonVariables.tvWordJpn;
import static com.gukos.bokotan.CommonVariables.tvsubE;
import static com.gukos.bokotan.CommonVariables.tvsubJ;
import static com.gukos.bokotan.CommonVariables.wordE;
import static com.gukos.bokotan.CommonVariables.wordJ;
import static com.gukos.bokotan.GogenYomuFactory.getGogenString;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.getClassName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.getMethodName;
import static com.gukos.bokotan.MyLibrary.DisplayOutput.puts;
import static com.gukos.bokotan.MyLibrary.ExceptionManager.showException;
import static com.gukos.bokotan.MyLibrary.FileDirectoryManager.getPath;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.DataName;
import static com.gukos.bokotan.MyLibrary.PreferenceManager.getIntData;
import static com.gukos.bokotan.WordPhraseData.DataBook.passTan;
import static com.gukos.bokotan.WordPhraseData.DataBook.tanjukugoEX;
import static com.gukos.bokotan.WordPhraseData.DataBook.yumetan;
import static com.gukos.bokotan.WordPhraseData.DataLang.english;
import static com.gukos.bokotan.WordPhraseData.DataLang.japanese;
import static com.gukos.bokotan.WordPhraseData.DataType.phrase;
import static com.gukos.bokotan.WordPhraseData.DataType.word;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.SetHatsuonKigou;
import static com.gukos.bokotan.WordPhraseData.HatsuonKigou.getHatsuon;

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
	private static MediaPlayer mediaPlayerClassStatic;
	private static int nInstance = 0;
	private String path;
	private Context context;
	
	private void Sleep() {
		Sleep(500);
	}
	
	private void Sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (Exception e) {
			showException(this, e);
		}
	}

	@Override
	public void onDestroy() {
		try {
			super.onDestroy();
			resetMediaPlayer(mediaPlayerClassStatic);
			mediaPlayerClassStatic = null;
			nInstance--;
		} catch (Exception e) {
			showException(this, e);
		}
	}

	private void resetMediaPlayer(MediaPlayer mediaPlayer) {
		try {
			if (mediaPlayer != null) {
				try {
					if (mediaPlayer.isPlaying()) mediaPlayer.stop();
				} catch (Exception e) {
					showException(this, e, ".stop()");
					return;
				}
				try {
					mediaPlayer.reset();
				} catch (Exception e) {
					showException(this, e, ".reset()");
				}
				try {
					mediaPlayer.release();
				} catch (Exception e) {
					showException(this, e, ".release()");
				}
			}
		} catch (Exception e) {
			showException(this, e);
		}
	}

	private void playStart(MediaPlayer mediaPlayer) {
		try {
			mediaPlayer.start();
		} catch (Exception e) {
			showException(this, e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			puts(getClassName() + getMethodName() + " start");
			context = getApplicationContext();
			resetMediaPlayer(mediaPlayerClassStatic);
			mediaPlayerClassStatic = null;
			nInstance++;
			NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			String strTutiChannelName = "再生中", strTutiChannelId = "bokotan_foreground", strTutiChannelShosai = "バックグラウンドで再生中";
			if (nm.getNotificationChannel(strTutiChannelId) == null) {
				NotificationChannel nc = new NotificationChannel(strTutiChannelId, strTutiChannelName, NotificationManager.IMPORTANCE_HIGH);
				nc.setDescription(strTutiChannelShosai);
				nm.createNotificationChannel(nc);
			}
			
			Intent sendStopIntent = new Intent(this, StopPlayBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			Intent sendPipIntent = new Intent(this, StartPipBroadcastReceiver.class).setAction(Intent.ACTION_SEND);
			PendingIntent sendStopPendingIntent = PendingIntent.getBroadcast(this, 0, sendStopIntent, FLAG_IMMUTABLE);
			PendingIntent sendPipPendingIntent = PendingIntent.getBroadcast(this, 0, sendPipIntent, FLAG_IMMUTABLE);
			//通知を押したときに表示する画面
			Intent intent1 = new Intent(context, TabActivity.class).setFlags(FLAG_ACTIVITY_REORDER_TO_FRONT);
			/*
			Intent intent2 = new Intent(context, TabActivity.class).setFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
			puts("FLAG_ACTIVITY_CLEAR_TOP|FLAG_ACTIVITY_SINGLE_TOP,FLAG_MUTABLE");//13o,8x,hwx,g
			Intent intent3 = new Intent(context, TabActivity.class).setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
			puts("FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY,FLAG_MUTABLE");//13o,8x,hwx,gx
			 */
			PendingIntent pendingIntentOpenActivity = PendingIntent.getActivity(context, 110, intent1, FLAG_IMMUTABLE);
			
			Notification notification = new NotificationCompat.Builder(this, strTutiChannelId)
					.setContentTitle("再生中").setContentText("バックグラウンドで再生中")
					.setSmallIcon(R.mipmap.ic_launcher)
					.addAction(R.drawable.ic_launcher_foreground, "停止", sendStopPendingIntent)
					.addAction(R.mipmap.launcher_new_icon, "小窓で表示", sendPipPendingIntent)
					.setContentIntent(pendingIntentOpenActivity)
					.build();
			//Notification.FLAG_NO_CLEARだと消える(Android13)
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			startForeground(1, notification);
			SetHatsuonKigou(this);
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
			if (QSentakuFragment.checkBoxHatsuonKigou.isChecked())
				textViewHatsuonKigou.setText(getHatsuon(wordE[now]));
		} catch (Exception e) {
			showException(this, e);
		}
	}
	
	private void bokotanPlayEnglish() {
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
				if (!isWordAndPhraseMode || isPhraseMode){
					now++;
				}
				if (bSkipOboe) {
					switch (CommonVariables.skipjoken) {
						case seikai1: {
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null) || getIntData(this, "testActivity" + strQ_WordPhraseKyoutuu + "Test", "nWordSeikaisuu" + now, 0) > 0) {
								now++;
								if (now >= lastnum) break;
							}
							break;
						}
						case huseikai2: {
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null)
									|| getIntData(this, "testActivity" + strQ_WordPhraseKyoutuu + "Test", "nWordHuseikaisuu" + now, 0) < 2) {
								now++;
								if (now >= lastnum) break;
							}
							break;
						}
						case onlyHugoukaku: {
							int seikaisu = getIntData(this, DataName.dnTestActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語正解数 + now, 0);
							int huseikaisu = getIntData(this, DataName.dnTestActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語不正解数 + now, 0);
							while ((swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null)
									|| TestActivity.isGokaku(seikaisu, huseikaisu)) {
								now++;
								seikaisu = getIntData(this, DataName.dnTestActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語正解数 + now, 0);
								huseikaisu = getIntData(this, DataName.dnTestActivity + strQ_WordPhraseKyoutuu + "Test", DataName.単語不正解数 + now, 0);
							}
							break;
						}
						case kirokunomi:
						default: {
							while (swOnlyFirst.isChecked() && hashMapKishutu.get(wordE[now]) != null) {
								now++;
							}
							break;
						}
					}
					//puts("now="+now+",word="+wordE[now]+",hash="+hashMapKishutu.get(wordE[now]));
					tvSeikaisu.setText("正解" + getIntData(this, "testActivity" + strQ + "Test", "nWordSeikaisuu" + now, 0) + '/' + (getIntData(this, "testActivity" + strQ + "Test", "nWordSeikaisuu" + now, 0) + getIntData(this, "testActivity" + strQ + "Test", "nWordHuseikaisuu" + now, 0)));
				}

				if (nFrom!=0&&nTo!=0) {
					if (now <= nFrom) now = nFrom;
					if (now >= nTo) now = nFrom;
				}
				
				if (tvGenzai != null) tvGenzai.setText("No." + now);
				int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
				if (lastnum == 2400) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "1qTest", "nWordHuseikaisuu" + now, 0);
				} else if (lastnum == 1850) {
					nWordSeikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordSeikaisuu" + now, 0);
					nWordHuseikaisuu = getIntData(this, "testActivity" + "p1qTest", "nWordHuseikaisuu" + now, 0);
				}
				if (tvNumSeikaisuu != null)
					tvNumSeikaisuu.setText(
							" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu > 0 ? nWordSeikaisuu + nWordHuseikaisuu : 1) +
									"% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);
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
			if ((strQPath.startsWith("ph")) || strQPath.startsWith(
					"phy")) {
				//フォルダ統合
				strQPath = strQ.substring(2);
			}
			//単語のみ
			if (isPhraseMode)//フレーズならば
				if (strQPath.startsWith("y"))
					path = getPath(yumetan, strQPath, phrase, english, now);
				else if (strQPath.startsWith("tanjukugo")) {
					path = getPath(tanjukugoEX, strQPath, phrase, english, now);
				} else
					path = getPath(passTan, strQPath, phrase, english, now);
			else if (strQPath.startsWith("y"))
				path = getPath(yumetan, strQPath, word, english, now);
			else if (strQPath.startsWith("tanjukugo")) {
				path = getPath(tanjukugoEX, strQPath, word, english, now);
			} else path = getPath(passTan, strQPath, word, english, now);
			textViewPath.setText(path);
			try {
				mediaPlayerClassStatic = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
				if (mediaPlayerClassStatic==null){
					puts("null"+",from="+nFrom+"to"+nTo+"now"+now+"uri:"+path);
					return;
				}
				mediaPlayerClassStatic.setPlaybackParams(mediaPlayerClassStatic.getPlaybackParams().setSpeed((float) dPlaySpeedEng));
				playStart(mediaPlayerClassStatic);
				mediaPlayerClassStatic.setOnCompletionListener(mediaPlayerLamda -> {
					resetMediaPlayer(mediaPlayerLamda);
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
	
	private void bokotanPlayJapanese() {
		try {
			if (!bEnglishToJapaneseOrder) {
				now++;
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
				tvNumSeikaisuu.setText(
						" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) +
								"% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);

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
			if ((strQPath.startsWith("ph")) || strQPath.startsWith("phy")) {
				//フォルダ統合
				strQPath = strQ.substring(2);
			}
			if (isPhraseMode)
				if (strQPath.startsWith("y"))
					path = getPath(yumetan, strQPath, phrase, japanese, now);
				else if (strQPath.startsWith("tanjukugo")) {
					path = getPath(tanjukugoEX, strQPath, phrase, japanese, now);
				} else
					path = getPath(passTan, strQPath, phrase, japanese, now);
			else if (strQPath.startsWith("y"))
				path = getPath(yumetan, strQPath, word, japanese, now);
			else if (strQPath.startsWith("tanjukugo")) {
				path = getPath(tanjukugoEX, strQPath, word, japanese, now);
			} else path = getPath(passTan, strQPath, word, japanese, now);
			textViewPath.setText(path);
			try {
				mediaPlayerClassStatic = MediaPlayer.create(this, Uri.parse(path));
				mediaPlayerClassStatic.setPlaybackParams(mediaPlayerClassStatic.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
				playStart(mediaPlayerClassStatic);
				mediaPlayerClassStatic.setOnCompletionListener(mediaPlayerLamda -> {
					resetMediaPlayer(mediaPlayerLamda);
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
			MediaPlayer mediaPlayerJoshi = new MediaPlayer();
			mediaPlayerJoshi.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
			String strJosi;
			int nowForJosiCheck = now;
			if (!bEnglishToJapaneseOrder) nowForJosiCheck++;
			char c = wordJ[nowForJosiCheck].charAt(index);
			switch (c) {
				case 'を':
				case 'に':
				case 'の':
				case 'で': {
					strJosi = MyLibrary.FileDirectoryManager.getJosiPath(c);
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
			mediaPlayerJoshi = MediaPlayer.create(this, Uri.parse(strJosi));
			mediaPlayerJoshi.setPlaybackParams(mediaPlayerJoshi.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
			playStart(mediaPlayerJoshi);
			mediaPlayerJoshi.setOnCompletionListener(mediaPlayerJosiLamda -> {
				resetMediaPlayer(mediaPlayerJosiLamda);
				bokotanPlayJapanese();
			});
		} catch (Exception e) {
			showException(this, e);
		}
	}
}