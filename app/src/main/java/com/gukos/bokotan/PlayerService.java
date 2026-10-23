package com.gukos.bokotan;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.media.AudioManager;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/** MediaPlayerと再生状態をメインスレッドで管理し、終了時に必ず解放する。 */
public class PlayerService extends Service {
    private static boolean running;
    private static String lastError;
    public static final String PLAYER_STATE_CHANGED = "player_state_changed";
    public static boolean isRunning() { return running; }
    public static String statusMessage(android.content.Context context) {
        return lastError != null ? lastError : context.getString(R.string.player_not_started);
    }
    private void notifyState() {
        sendBroadcast(new Intent(PlayerFragment.PLAYER_ACTION_UI_CHANGE).setPackage(getPackageName())
                .putExtra(PLAYER_STATE_CHANGED, true));
    }
    public static final String className = "PlayerService",
            PLAYERSERVICE_EXTRA_MODE = "ps_em", PLAYERSERVICE_EXTRA_BOOK = "ps_eb",
            PLAYERSERVICE_EXTRA_DATA_Q = "ps_edq", PLAYERSERVICE_EXTRA_SKIP_COND = "psesc",
            PLAYERSERVICE_EXTRA_SKIP_THRES_NUM = "psestn", PLAYERSERVICE_EXTRA_SKIP_THRES_COMP = "psestc",
            PLAYERSERVICE_EXTRA_NOW = "ps_en", PLAYERSERVICE_EXTRA_SHOW_APPEARED = "ps_esa",
            PLAYERSERVICE_ACTION = "playerservice_action", PLAYERSERVICE_MESSAGE_TYPE = "playerservice_message_type",
            PLAYERSERVICE_MESSAGE_STOP = "playerservice_message_stop", PLAYERSERVICE_MESSAGE_NOW = "ps_mn",
            PLAYERSERVICE_MESSAGE_REFRESH = "ps_refresh", PLAYERSERVICE_MESSAGE_NOTIFICATION = "ps_notification";
    public enum SkipContidion { all, seikaisu, huseikai, seikairate }
    public enum SkipThreshold { eqormore, eqorless }
    public static float dPlaySpeedEng = 1.5f, dPlaySpeedJpn = 2f;
    static ArrayList<Dictionary.Entry> wordDataList = new ArrayList<>(), phraseDataList = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<Dictionary.Entry> words = new ArrayList<>(), phrases = new ArrayList<>();
    private final HashSet<String> appeared = new HashSet<>();
    private final HashSet<String> firstOccurrences = new HashSet<>();
    private final HashSet<Integer> duplicateIndices = new HashSet<>();
    private MediaPlayer player;
    private final java.util.EnumMap<PlayerFragment.PlayerViewName, String> displayed = new java.util.EnumMap<>(PlayerFragment.PlayerViewName.class);
    private final java.util.EnumMap<PipActivity.PipViewName, String> pipDisplayed = new java.util.EnumMap<>(PipActivity.PipViewName.class);
    private Dictionary.Folder book;
    private Dictionary.BookQ grade;
    private Dictionary.Datatype mode, currentMode;
    private Dictionary.DataLang language = Dictionary.DataLang.english;
    private SkipContidion condition;
    private SkipThreshold comparison;
    private double threshold;
    private int now, count, generation;
    private boolean active, receiverRegistered, particlePlayed, showBeforeReading;
    private final BroadcastReceiver noisyReceiver = new AudioDisconnectReceiver(
            () -> active && MyLibrary.PreferenceManager.getSetting(this, "id" + R.id.checkBoxAutoStop, false),
            this::stopPlayback);
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(PLAYERSERVICE_MESSAGE_TYPE);
            if (PLAYERSERVICE_MESSAGE_REFRESH.equals(type)) {
                for (var item : new ArrayList<>(displayed.entrySet())) sendText(item.getKey(), item.getValue());
                for (var item : new ArrayList<>(pipDisplayed.entrySet())) sendPip(item.getKey(), item.getValue());
            } else if (PLAYERSERVICE_MESSAGE_STOP.equals(type)) stopPlayback();
            else if (PLAYERSERVICE_MESSAGE_NOTIFICATION.equals(type) && active)
                getSystemService(NotificationManager.class).notify(1, buildNotification());
            else if (PLAYERSERVICE_MESSAGE_NOW.equals(type) && active) {
                int requested = intent.getIntExtra(PLAYERSERVICE_MESSAGE_NOW, -1);
                if (requested < 0 || requested >= words.size()) { report("範囲内の番号を指定してください。"); return; }
                now = requested;
                language = Dictionary.DataLang.english;
                currentMode = mode == Dictionary.Datatype.mix ? Dictionary.Datatype.word : mode;
                particlePlayed = false;
                releasePlayer();
                onPlay();
            }
        }
    };

    @Override public void onCreate() {
        super.onCreate();
        ContextCompat.registerReceiver(this, receiver, new IntentFilter(PLAYERSERVICE_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(this, noisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY), ContextCompat.RECEIVER_EXPORTED);
        receiverRegistered = true;
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        releasePlayer();
        active = true;
        running = true;
        lastError = null;
        notifyState();
        int token = ++generation;
        startNotification();
        if (intent == null) { stopPlayback(); return START_NOT_STICKY; }
        mode = IntentCompat.getSerializableExtra(intent, PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.class);
        book = IntentCompat.getSerializableExtra(intent, PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.class);
        grade = IntentCompat.getSerializableExtra(intent, PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.class);
        condition = IntentCompat.getSerializableExtra(intent, PLAYERSERVICE_EXTRA_SKIP_COND, SkipContidion.class);
        comparison = IntentCompat.getSerializableExtra(intent, PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, SkipThreshold.class);
        showBeforeReading = MyLibrary.PreferenceManager.getSetting(this, "id" + R.id.switchHyojiYakuBeforeRead, true);
        threshold = intent.getDoubleExtra(PLAYERSERVICE_EXTRA_SKIP_THRES_NUM, 0);
        if (mode == null || book == null || grade == null || condition == null || comparison == null
                || !Double.isFinite(threshold) || threshold < 0) {
            fail("再生の設定が不正です。"); return START_NOT_STICKY;
        }
        Dictionary.whenReady(this, () -> {
            if (!active || token != generation) return;
            words.clear(); phrases.clear(); appeared.clear(); duplicateIndices.clear(); firstOccurrences.clear();
            if (book == Dictionary.Folder.all) {
                for (var folder : new Dictionary.Folder[]{Dictionary.Folder.yumetan, Dictionary.Folder.passtan, Dictionary.Folder.tanjukugo}) {
                    for (var q : folder == Dictionary.Folder.yumetan
                            ? new Dictionary.BookQ[]{Dictionary.BookQ.y1, Dictionary.BookQ.y2, Dictionary.BookQ.y3}
                            : new Dictionary.BookQ[]{Dictionary.BookQ.qp1, Dictionary.BookQ.q1}) addBook(folder, q);
                }
                addBook(Dictionary.Folder.sample, Dictionary.BookQ.none);
                addBook(Dictionary.Folder.personal, Dictionary.BookQ.none);
            } else addBook(book, grade);
            if (intent.getBooleanExtra(PLAYERSERVICE_EXTRA_SHOW_APPEARED, false) && book != Dictionary.Folder.all) {
                if (book == Dictionary.Folder.passtan || book == Dictionary.Folder.tanjukugo) {
                    for (var q : new Dictionary.BookQ[]{Dictionary.BookQ.y08, Dictionary.BookQ.y1, Dictionary.BookQ.y2, Dictionary.BookQ.y3})
                        addAppeared(Dictionary.BookName.yumetanWord, q);
                }
                if ((book == Dictionary.Folder.passtan && grade == Dictionary.BookQ.q1) || book == Dictionary.Folder.tanjukugo)
                    addAppeared(Dictionary.BookName.PasstanWordData, Dictionary.BookQ.qp1);
                if (book == Dictionary.Folder.tanjukugo) addAppeared(Dictionary.BookName.PasstanWordData, Dictionary.BookQ.q1);
                if (book == Dictionary.Folder.tanjukugo && grade == Dictionary.BookQ.q1)
                    addAppeared(Dictionary.BookName.tanjukugoWord, Dictionary.BookQ.qp1);
            }
            if (MyLibrary.PreferenceManager.getSetting(this, "id" + R.id.switchSortHanten, false)) {
                java.util.Collections.reverse(words);
                java.util.Collections.reverse(phrases);
            }
            for (int i = 0; i < words.size(); i++) if (!firstOccurrences.add(words.get(i).e)) duplicateIndices.add(i);
            wordDataList = new ArrayList<>(words);
            phraseDataList = new ArrayList<>(phrases);
            if (words.isEmpty()) { fail("この範囲に再生できるデータがありません。"); return; }
            int requested = intent.getIntExtra(PLAYERSERVICE_EXTRA_NOW, -1);
            boolean explicit = requested != -1;
            if (!explicit) requested = MyLibrary.PreferenceManager.getIntData(this,
                    MyLibrary.PreferenceManager.fnAppSettings, positionKey(), -1);
            now = -1;
            if (book == Dictionary.Folder.all) {
                if (requested >= 0 && requested < words.size()) now = requested;
            } else {
                for (int i = 0; i < words.size(); i++) if (words.get(i).numberInBook == requested) { now = i; break; }
            }
            if (now == -1 && explicit) { fail("開始位置が辞書の範囲外です。"); return; }
            if (now == -1 || !eligible(now)) now = StudyRules.nextIndex(words.size(), -1, this::eligible);
            if (now == -1) { fail("条件に合う単語がありません。"); return; }
            language = Dictionary.DataLang.english;
            currentMode = mode == Dictionary.Datatype.mix ? Dictionary.Datatype.word : mode;
            particlePlayed = false; count = 1;
            awaitView(token, 0);
        });
        return START_NOT_STICKY;
    }

    private void addBook(Dictionary.Folder folder, Dictionary.BookQ q) {
        var wordBook = folder == Dictionary.Folder.personal ? Dictionary.BookName.personalWord
                : folder == Dictionary.Folder.sample ? Dictionary.BookName.sampleWord
                : folder == Dictionary.Folder.yumetan ? Dictionary.BookName.yumetanWord
                : folder == Dictionary.Folder.tanjukugo ? Dictionary.BookName.tanjukugoWord : Dictionary.BookName.PasstanWordData;
        var phraseBook = folder == Dictionary.Folder.yumetan ? Dictionary.BookName.yumetanPhrase
                : folder == Dictionary.Folder.tanjukugo ? Dictionary.BookName.tanjukugoPhrase : Dictionary.BookName.PasstanPhrase;
        var phraseMap = new HashMap<Integer, Dictionary.Entry>();
        for (var entry : (folder == Dictionary.Folder.sample || folder == Dictionary.Folder.personal) ? new ArrayList<Dictionary.Entry>() : Dictionary.getList(phraseBook, q)) if (entry.isUsable()) phraseMap.put(entry.numberInBook, entry);
        if (mode == Dictionary.Datatype.phrase) {
            for (var entry : (folder == Dictionary.Folder.sample || folder == Dictionary.Folder.personal) ? new ArrayList<Dictionary.Entry>() : Dictionary.getList(phraseBook, q)) if (entry.isUsable()) { words.add(entry); phrases.add(entry); }
        } else {
            for (var entry : Dictionary.getList(wordBook, q)) if (entry.isUsable()) {
                var phrase = phraseMap.get(entry.numberInBook);
                if (mode == Dictionary.Datatype.mix && phrase == null) continue;
                words.add(entry); phrases.add(phrase);
            }
        }
    }

    private void addAppeared(Dictionary.BookName name, Dictionary.BookQ q) {
        for (var entry : Dictionary.getList(name, q)) if (entry.isUsable()) appeared.add(entry.e);
    }

    private boolean eligible(int index) {
        var entry = words.get(index);
        int[] scores = Dictionary.QuizData.scores(entry);
        return (book != Dictionary.Folder.all || !duplicateIndices.contains(index)) && !appeared.contains(entry.e)
                && StudyRules.matches(scores[0], scores[1], condition.name(), comparison == SkipThreshold.eqormore, threshold);
    }

    private void awaitView(int token, int attempt) {
        if (!active || generation != token) return;
        if (PlayerFragment.isInitialized) onPlay();
        else if (attempt < 50) handler.postDelayed(() -> awaitView(token, attempt + 1), 100);
        else fail("再生画面を開けませんでした。もう一度開始してください。");
    }

    private void onPlay() {
        releasePlayer();
        if (!active || now < 0 || now >= words.size()) return;
        var entry = currentMode == Dictionary.Datatype.phrase ? phrases.get(now) : words.get(now);
        if (entry == null || !entry.isUsable()) { fail("再生できるデータがありません。"); return; }
        if (language == Dictionary.DataLang.english) {
            int[] scores = Dictionary.QuizData.scores(words.get(now));
            sendText(PlayerFragment.PlayerViewName.tvcount, scores[0] + "/" + (scores[0] + scores[1]) + " 再生回数:" + count + "回");
            sendText(PlayerFragment.PlayerViewName.genzai, "No." + entry.numberInBook);
            sendText(PlayerFragment.PlayerViewName.jpn, showBeforeReading ? entry.j : "");
            sendText(PlayerFragment.PlayerViewName.eng, entry.e);
            sendText(PlayerFragment.PlayerViewName.hatsuon, currentMode == Dictionary.Datatype.word
                    && MyLibrary.PreferenceManager.getSetting(this, "id" + R.id.checkBoxHatsuonkigou, false)
                    ? Dictionary.HatsuonKigou.getHatsuon(entry.e) : "");
            sendText(PlayerFragment.PlayerViewName.subE, mode == Dictionary.Datatype.mix && currentMode == Dictionary.Datatype.phrase ? words.get(now).e : "");
            sendText(PlayerFragment.PlayerViewName.subJ, mode == Dictionary.Datatype.mix && currentMode == Dictionary.Datatype.phrase ? words.get(now).j : "");
            sendPip(PipActivity.PipViewName.num, "No." + entry.numberInBook);
            sendPip(PipActivity.PipViewName.eng, entry.e);
            sendPip(PipActivity.PipViewName.jpn, showBeforeReading ? entry.j : "");
        }
        if (language == Dictionary.DataLang.japanese) {
            sendText(PlayerFragment.PlayerViewName.jpn, entry.j);
            sendPip(PipActivity.PipViewName.jpn, entry.j);
        }
        String path = entry.toPath(language);
        // Optional legacy Japanese particles: safely ignore a missing particle recording.
        if (book == Dictionary.Folder.passtan && currentMode == Dictionary.Datatype.word
                && language == Dictionary.DataLang.japanese && !particlePlayed) {
            particlePlayed = true;
            String word = entry.j.replaceFirst("^～?\\([^)]*\\)", "").replaceFirst("^～", "");
            if (!word.isEmpty() && "をにので".indexOf(word.charAt(0)) >= 0) {
                String particlePath = MyLibrary.FileDirectoryManager.getJosiPath(word.charAt(0));
                if (new java.io.File(particlePath).isFile()) {
                    playFile(particlePath, dPlaySpeedJpn, this::onPlay);
                    return;
                }
            }
        }
        float speed = language == Dictionary.DataLang.english ? dPlaySpeedEng : dPlaySpeedJpn;
        playFile(path, speed, () -> {
            if (language == Dictionary.DataLang.english) language = Dictionary.DataLang.japanese;
            else {
                language = Dictionary.DataLang.english;
                particlePlayed = false;
                if (mode == Dictionary.Datatype.mix && currentMode == Dictionary.Datatype.word) currentMode = Dictionary.Datatype.phrase;
                else {
                    currentMode = mode == Dictionary.Datatype.mix ? Dictionary.Datatype.word : mode;
                    now = StudyRules.nextIndex(words.size(), now, this::eligible);
                    if (now == -1) { fail("条件に合う単語がありません。"); return; }
                    count++;
                }
            }
            onPlay();
        });
    }

    private void playFile(String path, float speed, Runnable completion) {
        int token = generation;
        try {
            if (path == null) { fail("このデータの音声パスを取得できません。"); return; }
            player = AudioSource.create(this, path);
            if (player == null) { fail("音声ファイルが見つかりません: " + path); return; }
            player.setOnCompletionListener(mp -> {
                if (active && token == generation && mp == player) completion.run();
            });
            player.setOnErrorListener((mp, what, extra) -> { fail("音声を再生できません: " + path); return true; });
            player.setPlaybackParams(new PlaybackParams().setSpeed(speed));
            player.start();
        } catch (Exception error) { fail("音声を再生できません: " + path); }
    }

    private void releasePlayer() {
        MediaPlayer previous = player;
        player = null;
        if (previous != null) {
            previous.setOnCompletionListener(null);
            previous.setOnErrorListener(null);
            previous.release();
        }
    }

    private String positionKey() { return className + book + grade + mode + (book == Dictionary.Folder.personal ? PrivateData.id : ""); }

    private void report(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
    private void fail(String message) {
        stopPlayback();
        lastError = message;
        notifyState();
        sendText(PlayerFragment.PlayerViewName.eng, message);
        report(message);
    }

    private void stopPlayback() {
        if (active && now >= 0 && now < words.size()) MyLibrary.PreferenceManager.putIntData(this,
                MyLibrary.PreferenceManager.fnAppSettings, positionKey(),
                book == Dictionary.Folder.all ? now : words.get(now).numberInBook);
        active = false;
        running = false;
        lastError = null;
        notifyState();
        generation++;
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        for (var name : PlayerFragment.PlayerViewName.values()) sendText(name, "");
        for (var name : PipActivity.PipViewName.values()) sendPip(name, "");
        TabActivity.setTabPageNum(0);
        stopForeground(STOP_FOREGROUND_REMOVE);
        stopSelf();
    }

    @Override public void onDestroy() {
        active = false;
        running = false;
        notifyState();
        generation++;
        handler.removeCallbacksAndMessages(null);
        releasePlayer();
        if (receiverRegistered) { unregisterReceiver(receiver); unregisterReceiver(noisyReceiver); receiverRegistered = false; }
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    private void startNotification() {
        var manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel("default", getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW));
        var notification = buildNotification();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        else startForeground(1, notification);
    }

    private Notification buildNotification() {
        var open = PendingIntent.getActivity(this, 101, new Intent(this, TabActivity.class), PendingIntent.FLAG_IMMUTABLE);
        var stop = PendingIntent.getBroadcast(this, 0, new Intent(PLAYERSERVICE_ACTION).setPackage(getPackageName())
                .putExtra(PLAYERSERVICE_MESSAGE_TYPE, PLAYERSERVICE_MESSAGE_STOP), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        var builder = new Notification.Builder(this, "default").setContentTitle(getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_media_play).setContentText("英単語を再生中")
                .setOngoing(true).setContentIntent(open)
                .addAction(new Notification.Action.Builder(Icon.createWithResource(this, android.R.drawable.ic_media_pause), "停止", stop).build());
        // 小窓の表示中に開き直すとシステムが全画面へ戻すため、表示中は出さない。
        if (!PipActivity.isPipShown()) {
            // Android 12以降は通知から起動したレシーバー経由のActivity起動が阻止されるため、直接PIP画面を開く。
            var pip = PendingIntent.getActivity(this, 10, new Intent(this, PipActivity.class), PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(new Notification.Action.Builder(Icon.createWithResource(this, R.mipmap.launcher_new_icon), "小窓で表示", pip).build());
        }
        return builder.build();
    }

    private void sendText(PlayerFragment.PlayerViewName name, String text) {
        displayed.put(name, text);
        sendBroadcast(new Intent(PlayerFragment.PLAYER_ACTION_UI_CHANGE).setPackage(getPackageName())
                .putExtra(PlayerFragment.PLAYER_VIEW_NAME, name).putExtra(PlayerFragment.PLAYER_VIEW_TEXT, text)
                .putExtra(PlayerFragment.PLAYER_VIEW_SINGLE_LINE, currentMode != Dictionary.Datatype.phrase));
    }

    private void sendPip(PipActivity.PipViewName name, String text) {
        pipDisplayed.put(name, text);
        sendBroadcast(new Intent(PipActivity.PIP_ACTION_UI).setPackage(getPackageName())
                .putExtra(PipActivity.PIP_VIEW_NAME, name).putExtra(PipActivity.PIP_VIEW_TEXT, text)
                .putExtra(PipActivity.PIP_VIEW_SINGLE_LINE, currentMode != Dictionary.Datatype.phrase));
    }
}
