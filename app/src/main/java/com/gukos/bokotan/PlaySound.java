package com.gukos.bokotan;

import static com.gukos.bokotan.MainActivity.bEtoJ;
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
import static com.gukos.bokotan.MainActivity.tvDebug;
import static com.gukos.bokotan.MainActivity.tvGenzai;
import static com.gukos.bokotan.MainActivity.tvSeikaisu;
import static com.gukos.bokotan.MainActivity.tvSeikaisuu;
import static com.gukos.bokotan.MainActivity.tvWordEng;
import static com.gukos.bokotan.MainActivity.tvWordJpn;
import static com.gukos.bokotan.MainActivity.tvsubE;
import static com.gukos.bokotan.MainActivity.tvsubJ;
import static com.gukos.bokotan.MainActivity.wordE;
import static com.gukos.bokotan.MainActivity.wordJ;
import static com.gukos.bokotan.Q_sentaku_activity.skipwords;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class PlaySound extends Service {
	MediaPlayer mp = null;
	//static SoundPool sp=new SoundPool.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setMaxStreams(1).build();
	static String tag = "E/";
	String path;
	private char langage;

	public PlaySound() {
	}

	@Override
	public void onDestroy(){
		resetMediaPlayer(mp,"onDestroy");
	}

	public void resetMediaPlayer(MediaPlayer mp,String str) {
		Log.d(tag,"resetMediaPlayer reason:"+str);
		if (mp != null) {
			mp.release();
			mp = null;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(tag, "61");
		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String strTutiChannelName = "再生中", strTutiChannelId = "bokotan_foreground", strTutiChannelShosai = "バックグラウンドで再生中";
		if (nm.getNotificationChannel(strTutiChannelId) == null) {
			NotificationChannel nc = new NotificationChannel(strTutiChannelId, strTutiChannelName, NotificationManager.IMPORTANCE_HIGH);
			nc.setDescription(strTutiChannelShosai);
			nm.createNotificationChannel(nc);
		}
		Notification n = new NotificationCompat.Builder(this, strTutiChannelId).setContentTitle("再生中").setContentText("バックグラウンドで再生中").setSmallIcon(R.drawable.ic_launcher_foreground).build();
		startForeground(1, n);
		Log.d(tag, "startForeground");
		new Thread(() -> Log.d(tag, "run")).start();
		bokotanPlayEnglish();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	void bokotanPlayEnglish() {
		String strQ_WordPhraseKyoutuu=strQ;
		if (strQ.equals("ph1q")) strQ_WordPhraseKyoutuu="1q";
		if (strQ.equals("php1q")) strQ_WordPhraseKyoutuu="p1q";
		if (now % 20 == 0)
			getSharedPreferences("MainActivity" + "now", MODE_PRIVATE).edit().putInt(strQ + "now", now).apply();
		if (bEtoJ) {
			now++;
			if (skipwords) {
				switch (Q_sentaku_activity.skipjoken) {
					case seikai1: {
						while (getSharedPreferences("testActivity" + strQ_WordPhraseKyoutuu + "Test", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0) > 0||kioku_chBox[now]) {
							now++;
							if (now>=lastnum) break;
						}
						break;
					}
					case huseikai2: {
						while (getSharedPreferences("testActivity" + strQ_WordPhraseKyoutuu + "Test", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0) < 2||kioku_chBox[now]) {
							now++;
							if (now>=lastnum) break;
						}
						break;
					}
					case kirokunomi:
					default: {
						Log.d(tag, "onlykioku");
						while (kioku_chBox[now]) {
							now++;
						}
						break;
					}
				}
				tvSeikaisu.setText("kioku:" + kioku_chBox[now] + "正解" + getSharedPreferences("testActivity" + strQ + "Test", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0) + '/' + (getSharedPreferences("testActivity" + strQ + "Test", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0) + getSharedPreferences("testActivity" + strQ + "Test", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0)));
			}
			if (now <= nFrom) now = nFrom;
			if (now >= nTo) now = nFrom;
			if (tvGenzai!=null) tvGenzai.setText("No." + now);
			int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
			if (lastnum == 2400) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			} else if (lastnum == 1850) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			}
			if (tvSeikaisuu!=null) tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);
			if (isPhraseMode) {
				if (bHyojiYakuBeforeRead) {
					tvWordJpn.setText(strPhraseJ[now]);
				} else {
					tvWordJpn.setText("");
				}
				tvWordEng.setText(strPhraseE[now]);
				tvsubE.setText(wordE[now]);
				tvsubJ.setText(wordJ[now]);
			} else {
				if (bHyojiYakuBeforeRead) {
					tvWordJpn.setText(wordJ[now]);
				} else {
					tvWordJpn.setText("");
				}
				tvWordEng.setText(wordE[now]);
			}
		}
		PipActivity.ChangeText(wordE[now], wordJ[now], now);
		if (isPhraseMode)//フレーズならば
		{
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now) + "例.mp3";
		} else {
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now) + "英.mp3";
		}
		try {
			tvDebug.setText("succeed");
			mp = MediaPlayer.create(getApplicationContext(), Uri.parse(path));
			mp.setPlaybackParams(mp.getPlaybackParams().setSpeed((float) dPlaySpeedEng));
			mp.start();
			mp.setOnCompletionListener(mp -> {
				resetMediaPlayer(mp,"bokotanPlayEnglish"+"OnCompletionListener");
				if (isPhraseMode) {
					bokotanPlayJapanese();
				} else {
					JosiCheck(0);
				}
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
			//now--;
			Log.d(tag, "2 E/IO" + e.getMessage());
			//bokotanPlayEnglish();
			bokotanPlayJapanese();
		}
		if (!bEtoJ && !bHyojiYakuBeforeRead) {
			if (isPhraseMode) {
				tvWordEng.setText(strPhraseE[now]);
			} else {
				tvWordEng.setText(wordE[now]);
			}
		}
	}

	void bokotanPlayJapanese() {
		if (!bEtoJ) {
			now++;
			if (skipwords) {
				while (kioku_chBox[now]) {
					now++;
				}
			}
			if (now <= nFrom) now = nFrom;
			if (now >= nTo) now = nFrom;
			if (tvGenzai!=null) tvGenzai.setText("No." + now);
			int nWordSeikaisuu = 0, nWordHuseikaisuu = 0;
			if (lastnum == 2400) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			} else if (lastnum == 1850) {
				nWordSeikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordSeikaisuu" + now, 0);
				nWordHuseikaisuu = getSharedPreferences("testActivity" + "p1qTest", MODE_PRIVATE).getInt("nWordHuseikaisuu" + now, 0);
			}
			tvSeikaisuu.setText(" (" + (int) nWordSeikaisuu * 100 / (nWordSeikaisuu + nWordHuseikaisuu + 1) + "% " + nWordSeikaisuu + '/' + (nWordSeikaisuu + nWordHuseikaisuu) + ')' + nFrom + '-' + nTo);
			if (isPhraseMode) {
				if (bHyojiYakuBeforeRead) {
					tvWordEng.setText(strPhraseE[now]);
				} else {
					tvWordEng.setText("");
				}
				tvWordJpn.setText(strPhraseJ[now]);
				tvsubE.setText(wordE[now]);
				tvsubJ.setText(wordJ[now]);
			} else {
				if (bHyojiYakuBeforeRead) {
					tvWordEng.setText(wordE[now]);
				} else {
					tvWordEng.setText("");
				}
				tvWordJpn.setText(wordJ[now]);
			}
		}
		if (isPhraseMode) {
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now) + "日.mp3";
		} else {
			path = "/storage/emulated/0/Download/data/" + strQ + '/' + String.format("%04d", now) + "訳.mp3";
		}
		try {
			tvDebug.setText("succeed");
			mp = MediaPlayer.create(this, Uri.parse(path));
			mp.setPlaybackParams(mp.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
			mp.start();
			mp.setOnCompletionListener(mp -> {
				resetMediaPlayer(mp,"bokotanPlayJpanese"+"OnCompletionListener");
				bokotanPlayEnglish();
			});
		} catch (IllegalStateException e) {
			e.printStackTrace();
			//now--;
			Log.d(tag, "4 E/State" + e.getMessage());
			//bokotanPlayJapanese();
			bokotanPlayEnglish();
		}
		if (bEtoJ && !bHyojiYakuBeforeRead) {
			if (isPhraseMode) {
				tvWordJpn.setText(strPhraseJ[now]);
			} else {
				tvWordJpn.setText(wordJ[now]);
			}
		}
	}


	public void JosiCheck(int index)//最初は0を指定
	{
		if (langage == '英') return;
		MediaPlayer mpJosi = new MediaPlayer();
		mpJosi.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		String strJosi;
		int nowForJosiCheck = now;
		if (!bEtoJ) nowForJosiCheck++;
		char c = wordJ[nowForJosiCheck].charAt(index);
		tvDebug.setText("now:" + nowForJosiCheck + "最初の文字:" + c);
		switch (c) {
			case 'を': {
				strJosi = "/storage/emulated/0/Download/data/postpositional/wo.mp3";
				break;
			}
			case 'に': {
				strJosi = "/storage/emulated/0/Download/data/postpositional/ni.mp3";
				break;
			}
			case 'の': {
				strJosi = "/storage/emulated/0/Download/data/postpositional/no.mp3";
				break;
			}
			case 'で': {
				strJosi = "/storage/emulated/0/Download/data/postpositional/de.mp3";
				break;
			}
			case '（': {
				for (int i = 0; i < wordJ[nowForJosiCheck].length(); i++) {
					if (wordJ[nowForJosiCheck].charAt(i) == '）') {
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
		tvDebug.setText("succeed");
		mpJosi = MediaPlayer.create(this, Uri.parse(strJosi));
		mpJosi.setPlaybackParams(mpJosi.getPlaybackParams().setSpeed((float) dPlaySpeedJpn));
		mpJosi.start();
		mpJosi.setOnCompletionListener(mpJosi1 -> {
			resetMediaPlayer(mpJosi1,"JosiCheck"+"OnCompletionListener");
			bokotanPlayJapanese();
		});
	}
}