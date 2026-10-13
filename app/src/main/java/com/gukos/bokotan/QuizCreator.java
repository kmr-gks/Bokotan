package com.gukos.bokotan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** 出題状態・受信・終了処理をメインスレッドで直列化する。 */
public final class QuizCreator {
    private static QuizCreator instance;
    public static final String QTHREAD_ACTION_CLICKED = "qthread_action_clicked",
            QTHREAD_EXTRA_CHOICE = "qthread_extra_choice", QTHREAD_EXTRA_STOP = "qes";
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private final ArrayList<Dictionary.Entry> candidates = new ArrayList<>();
    private final SoundPool sounds = new SoundPool.Builder().setMaxStreams(2)
            .setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()).build();
    private final PlayerService.SkipContidion condition;
    private final PlayerService.SkipThreshold comparison;
    private final double threshold;
    private final boolean pronunciation, effects;
    private boolean active = true, waitingForAnswer;
    private final java.util.EnumMap<TestFragment.ViewName, CharSequence> displayed = new java.util.EnumMap<>(TestFragment.ViewName.class);
    private int problem, questionCount;
    private List<Integer> choices = new ArrayList<>();
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(QTHREAD_EXTRA_STOP)) stop();
            else if (intent.hasExtra(QTHREAD_EXTRA_CHOICE)) answer(intent.getIntExtra(QTHREAD_EXTRA_CHOICE, -1));
        }
    };

    private QuizCreator(Context context, Dictionary.Folder book, Dictionary.BookQ q,
                        PlayerService.SkipContidion condition, double threshold, PlayerService.SkipThreshold comparison) {
        this.context = context.getApplicationContext();
        this.condition = condition;
        this.comparison = comparison;
        this.threshold = threshold;
        pronunciation = MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchQuizHatsuon, true);
        effects = MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchQuizOxKoukaon, true);
        ContextCompat.registerReceiver(this.context, receiver, new IntentFilter(QTHREAD_ACTION_CLICKED), ContextCompat.RECEIVER_NOT_EXPORTED);
        sounds.setOnLoadCompleteListener((pool, id, status) -> {
            if (active && status == 0) pool.play(id, 1, 1, 1, 0, 1);
        });
        Dictionary.whenReady(context, () -> {
            if (!active) return;
            if (book == Dictionary.Folder.all) {
                for (var folder : new Dictionary.Folder[]{Dictionary.Folder.passtan, Dictionary.Folder.tanjukugo, Dictionary.Folder.yumetan}) {
                    for (var grade : folder == Dictionary.Folder.yumetan
                            ? new Dictionary.BookQ[]{Dictionary.BookQ.y1, Dictionary.BookQ.y2, Dictionary.BookQ.y3}
                            : new Dictionary.BookQ[]{Dictionary.BookQ.q1, Dictionary.BookQ.qp1}) addBook(folder, grade);
                }
            } else addBook(book, q);
            awaitView(0);
        });
    }

    private void addBook(Dictionary.Folder folder, Dictionary.BookQ q) {
        var name = folder == Dictionary.Folder.yumetan ? Dictionary.BookName.yumetanWord
                : folder == Dictionary.Folder.tanjukugo ? Dictionary.BookName.tanjukugoWord : Dictionary.BookName.PasstanWordData;
        for (var entry : Dictionary.getList(name, q)) if (entry.isUsable()) candidates.add(entry);
    }

    private void awaitView(int attempt) {
        if (!active) return;
        if (TestFragment.isInitialized) {
            sendText(TestFragment.ViewName.Idontknow, "わかりません");
            setMondai();
        } else if (attempt < 50) handler.postDelayed(() -> awaitView(attempt + 1), 100);
        else fail("テスト画面を開けませんでした。もう一度開始してください。");
    }

    public static QuizCreator build(Context context, Dictionary.Folder book, Dictionary.BookQ q,
                                    PlayerService.SkipContidion condition, double threshold, PlayerService.SkipThreshold comparison) {
        stopCurrent();
        instance = new QuizCreator(context, book, q, condition, threshold, comparison);
        return instance;
    }

    public static void stopCurrent() {
        if (instance != null) instance.stop();
    }

    private void setMondai() {
        if (!active) return;
        var eligible = new ArrayList<Integer>();
        var translations = new ArrayList<String>();
        for (int i = 0; i < candidates.size(); i++) {
            var entry = candidates.get(i);
            translations.add(entry.j);
            int[] scores = Dictionary.QuizData.scores(entry);
            if (StudyRules.matches(scores[0], scores[1], condition.name(),
                    comparison == PlayerService.SkipThreshold.eqormore, threshold)) eligible.add(i);
        }
        if (eligible.isEmpty()) { fail("出題できる単語がありません。範囲や条件を変更してください。"); return; }
        problem = eligible.get(random.nextInt(eligible.size()));
        choices = StudyRules.choices(translations, problem, random);
        if (choices.isEmpty()) { fail("4択には異なる日本語訳を持つ単語が4語以上必要です。"); return; }
        var entry = candidates.get(problem);
        int[] scores = Dictionary.QuizData.scores(entry);
        sendText(TestFragment.ViewName.No, ++questionCount + "問目 No." + entry.numberInBook);
        sendText(TestFragment.ViewName.monme, Dictionary.QuizData.questionNumber(entry) + "問目 正解率"
                + scores[0] + "/" + (scores[0] + scores[1]));
        sendText(TestFragment.ViewName.Mondaibun, entry.e);
        var names = new TestFragment.ViewName[]{TestFragment.ViewName.Select1, TestFragment.ViewName.Select2,
                TestFragment.ViewName.Select3, TestFragment.ViewName.Select4};
        for (int i = 0; i < names.length; i++) sendText(names[i], candidates.get(choices.get(i)).j);
        waitingForAnswer = true;
        if (pronunciation) sounds.load(entry.toPath(Dictionary.DataLang.english), 1);
    }

    private void answer(int choice) {
        if (!active || !waitingForAnswer || choice < -1 || choice > 4 || choice == 0) return;
        waitingForAnswer = false;
        boolean correct = choice > 0 && choices.get(choice - 1) == problem;
        Dictionary.QuizData.record(candidates.get(problem), correct);
        // SharedPreferences.apply() is asynchronous; serialize the snapshot on this thread.
        Dictionary.QuizData.saveQuizData(context);
        sendText(TestFragment.ViewName.Marubatsu, correct ? "○" : "×");
        context.sendBroadcast(new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE).setPackage(context.getPackageName())
                .putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.TextColor)
                .putExtra(TestFragment.QUIZ_VIEW_NAME, TestFragment.ViewName.Marubatsu)
                .putExtra(TestFragment.QUIZ_VIEW_COLOR, correct ? Color.RED : Color.BLUE));
        if (effects) sounds.load(context, correct ? R.raw.seikai : R.raw.huseikai, 1);
        var explanation = new StringBuilder();
        for (int index : choices) {
            var entry = candidates.get(index);
            if (index == problem) explanation.append("<font color=\"red\">");
            explanation.append(Html.escapeHtml(entry.e)).append(" ").append(Html.escapeHtml(entry.j));
            if (index == problem) explanation.append("</font>");
            explanation.append("<br>");
        }
        sendText(TestFragment.ViewName.Editorial, Html.fromHtml(explanation.toString(), Html.FROM_HTML_MODE_COMPACT));
        // Let already queued clicks expire before accepting an answer to the next question.
        handler.postDelayed(this::setMondai, 150);
    }

    private void fail(String message) {
        stop();
        sendText(TestFragment.ViewName.Mondaibun, message);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void stop() {
        if (!active) return;
        active = false;
        waitingForAnswer = false;
        handler.removeCallbacksAndMessages(null);
        context.unregisterReceiver(receiver);
        sounds.release();
        for (var name : TestFragment.ViewName.values()) sendText(name, "");
        Dictionary.QuizData.saveQuizData(context);
        if (instance == this) instance = null;
    }

    public void cancel() { stop(); }

    public static void refreshCurrent() {
        if (instance != null && instance.active) {
            for (var item : new java.util.ArrayList<>(instance.displayed.entrySet())) instance.sendText(item.getKey(), item.getValue());
        }
    }

    private void sendText(TestFragment.ViewName name, CharSequence text) {
        displayed.put(name, text);
        context.sendBroadcast(new Intent(TestFragment.QUIZ_ACTION_UI_CHANGE).setPackage(context.getPackageName())
                .putExtra(TestFragment.QUIZ_VIEW_PROPERTIES, TestFragment.ViewProperties.Text)
                .putExtra(TestFragment.QUIZ_VIEW_NAME, name).putExtra(TestFragment.QUIZ_VIEW_TEXT_CHARSEQ, text));
    }
}
