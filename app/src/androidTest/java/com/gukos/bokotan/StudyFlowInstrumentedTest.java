package com.gukos.bokotan;

import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class StudyFlowInstrumentedTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private void await(BooleanSupplier ready) throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (!ready.getAsBoolean() && System.currentTimeMillis() < deadline) Thread.sleep(50);
        assertTrue("Timed out waiting for study state", ready.getAsBoolean());
    }
    private String text(ActivityScenario<TabActivity> scenario, int id) {
        var result = new AtomicReference<>("");
        scenario.onActivity(activity -> {
            TextView view = activity.findViewById(id);
            if (view != null) result.set(view.getText().toString());
        });
        return result.get();
    }

    @Test public void quizSurvivesRecreationAndStopsWithInsufficientData() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            assertEquals(5, Dictionary.allData.size());
            main(() -> {
                MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchQuizHatsuon, false);
                TabActivity.setTabPageNum(2);
                QuizCreator.build(context, Dictionary.Folder.all, Dictionary.BookQ.all,
                        PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
            });
            await(() -> !text(scenario, R.id.textViewMondai).isEmpty());
            String question = text(scenario, R.id.textViewMondai);
            assertTrue(Dictionary.allData.stream().anyMatch(e -> e.e.equals(question)));
            scenario.recreate();
            await(() -> text(scenario, R.id.textViewMondai).equals(question));
            main(() -> QuizCreator.stopCurrent());
            await(() -> text(scenario, R.id.textViewMondai).isEmpty());
            String key = Dictionary.BookName.sampleWord.toString() + Dictionary.BookQ.none;
            var original = Dictionary.listsPerBook.get(key);
            try {
                main(() -> {
                    Dictionary.listsPerBook.put(key, new ArrayList<>(original.subList(0, 3)));
                    QuizCreator.build(context, Dictionary.Folder.sample, Dictionary.BookQ.none,
                            PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
                });
                await(() -> text(scenario, R.id.textViewMondai).contains("4語以上"));
                main(() -> QuizCreator.stopCurrent());
                main(() -> {
                    Dictionary.listsPerBook.put(key, new ArrayList<>());
                    QuizCreator.build(context, Dictionary.Folder.sample, Dictionary.BookQ.none,
                            PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
                });
                await(() -> text(scenario, R.id.textViewMondai).contains("出題できる単語がありません"));
            } finally { main(() -> { QuizCreator.stopCurrent(); Dictionary.listsPerBook.put(key, original); }); }
        }
    }

    @Test public void fullScopeRemainsStableAfterAnswersAndScoresUseOriginalBookNumber() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            String key = Dictionary.BookName.PasstanWordData.toString() + Dictionary.BookQ.qp1;
            var original = Dictionary.listsPerBook.get(key);
            var extra = new ArrayList<Dictionary.Entry>();
            main(() -> {
                for (int i = 0; i < 5; i++) extra.add(new Dictionary.Entry("other" + i, "別の訳" + i,
                        Dictionary.Folder.passtan, Dictionary.BookName.PasstanWordData, Dictionary.BookQ.qp1, 5000 + i, Dictionary.Datatype.word));
                Dictionary.listsPerBook.put(key, extra);
                MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchQuizHatsuon, false);
                TabActivity.setTabPageNum(2);
                QuizCreator.build(context, Dictionary.Folder.all, Dictionary.BookQ.all,
                        PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
            });
            try {
                await(() -> !text(scenario, R.id.textViewMondai).isEmpty());
                // Inspect the actual candidate list so this regression is deterministic across random draws.
                var instanceField = QuizCreator.class.getDeclaredField("instance"); instanceField.setAccessible(true);
                var candidatesField = QuizCreator.class.getDeclaredField("candidates"); candidatesField.setAccessible(true);
                for (int i = 0; i < 12; i++) {
                    main(() -> {
                        try { assertEquals(10, ((ArrayList<?>) candidatesField.get(instanceField.get(null))).size()); }
                        catch (IllegalAccessException e) { throw new AssertionError(e); }
                        context.sendBroadcast(new Intent(QuizCreator.QTHREAD_ACTION_CLICKED).setPackage(context.getPackageName())
                                .putExtra(QuizCreator.QTHREAD_EXTRA_CHOICE, -1));
                    });
                    Thread.sleep(250);
                }
                main(() -> {
                    var entry = extra.get(0);
                    int old = Dictionary.QuizData.scores(entry)[0];
                    Dictionary.QuizData.record(entry, true);
                    assertEquals(old + 1, Dictionary.QuizData.scores(entry)[0]);
                    assertNotEquals(Dictionary.QuizData.keyFor(entry), Dictionary.QuizData.keyFor(Dictionary.allData.get(0)));
                });
            } finally { main(() -> { QuizCreator.stopCurrent(); Dictionary.listsPerBook.put(key, original); }); }
        }
    }

    @Test public void sharedAudioPathAndInvalidPlaybackNeverCrash() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            main(() -> {
                var entry = new Dictionary.Entry("word", "訳", Dictionary.Folder.yumetan,
                        Dictionary.BookName.yumetanWord, Dictionary.BookQ.y1, 1, Dictionary.Datatype.word);
                assertEquals(MyLibrary.FileDirectoryManager.getPathPs(entry.folder, entry.bookQ, entry.datatype,
                        Dictionary.DataLang.english, 1), entry.toPath(Dictionary.DataLang.english));
                assertTrue(entry.toPath(Dictionary.DataLang.english).contains("/y1/"));
                TabActivity.setTabPageNum(1);
                context.startForegroundService(new Intent(context, PlayerService.class)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.word)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.sample)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.none)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND, PlayerService.SkipContidion.all)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, PlayerService.SkipThreshold.eqormore)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, 999));
            });
            Thread.sleep(500);
            scenario.onActivity(activity -> assertFalse(activity.isFinishing()));
            main(() -> context.stopService(new Intent(context, PlayerService.class)));
        }
    }
    @Test public void playbackLoopsAllFiveRecordingsAndStops() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            var seen = java.util.Collections.synchronizedSet(new java.util.HashSet<String>());
            var receiver = new android.content.BroadcastReceiver() {
                @Override public void onReceive(Context ignored, Intent intent) {
                    if (intent.getSerializableExtra(PlayerFragment.PLAYER_VIEW_NAME) == PlayerFragment.PlayerViewName.eng) {
                        String value = intent.getStringExtra(PlayerFragment.PLAYER_VIEW_TEXT);
                        if (Dictionary.allData.stream().anyMatch(entry -> entry.e.equals(value))) seen.add(value);
                    }
                }
            };
            main(() -> androidx.core.content.ContextCompat.registerReceiver(context, receiver,
                    new android.content.IntentFilter(PlayerFragment.PLAYER_ACTION_UI_CHANGE),
                    androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED));
            try {
                main(() -> {
                    TabActivity.setTabPageNum(1);
                    context.startForegroundService(new Intent(context, PlayerService.class)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.word)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.sample)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.none)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND, PlayerService.SkipContidion.all)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, PlayerService.SkipThreshold.eqormore)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, 0));
                });
                await(() -> seen.size() == 5);
                scenario.recreate();
                await(() -> !text(scenario, R.id.textViewEng).isEmpty());
                main(() -> context.sendBroadcast(new Intent(PlayerService.PLAYERSERVICE_ACTION).setPackage(context.getPackageName())
                        .putExtra(PlayerService.PLAYERSERVICE_MESSAGE_TYPE, PlayerService.PLAYERSERVICE_MESSAGE_STOP)));
                var manager = (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                await(() -> manager.getRunningServices(Integer.MAX_VALUE).stream()
                        .noneMatch(service -> service.service.getClassName().equals(PlayerService.class.getName())));
            } finally {
                main(() -> { context.stopService(new Intent(context, PlayerService.class)); context.unregisterReceiver(receiver); });
            }
        }
    }

    @Test public void freshLaunchSelectsSampleAndSearchAudioWorksWithoutStoragePermission() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            assertFalse(android.os.Environment.isExternalStorageManager());
            scenario.onActivity(activity -> {
                android.widget.Spinner spinner = activity.findViewById(R.id.spinnerBookQ);
                assertEquals("サンプル", spinner.getSelectedItem().toString());
                assertEquals(8, spinner.getSelectedItemPosition());
                activity.findViewById(R.id.buttonQuiz).performClick();
            });
            await(() -> !text(scenario, R.id.textViewMondai).isEmpty());
            main(QuizCreator::stopCurrent);
            main(() -> TabActivity.setTabPageNum(3));
            var found = new java.util.concurrent.atomic.AtomicBoolean();
            await(() -> {
                scenario.onActivity(activity -> {
                    android.widget.ListView list = activity.findViewById(R.id.listViewKensakuResult);
                    if (list != null && list.getAdapter() != null) {
                        androidx.appcompat.widget.SearchView search = activity.findViewById(R.id.searchView);
                        search.setQuery("dog", false);
                        found.set(list.getAdapter().getCount() == 1);
                    }
                });
                return found.get();
            });
            scenario.onActivity(activity -> {
                android.widget.ListView list = activity.findViewById(R.id.listViewKensakuResult);
                Dictionary.Entry entry = (Dictionary.Entry) list.getAdapter().getItem(0);
                assertEquals("dog", entry.e);
                assertEquals(Dictionary.Folder.sample, entry.folder);
                assertTrue(entry.toPath(Dictionary.DataLang.english).startsWith(AudioSource.ASSET_PREFIX));
                var fragment = activity.getSupportFragmentManager().getFragments().stream()
                        .filter(f -> f instanceof KensakuFragment && f.getView() != null).findFirst().get();
                ((KensakuFragment) fragment).playEnglishAndJapanese(entry);
                try {
                    var field = KensakuFragment.class.getDeclaredField("player"); field.setAccessible(true);
                    assertTrue(((android.media.MediaPlayer) field.get(fragment)).isPlaying());
                } catch (ReflectiveOperationException error) { throw new AssertionError(error); }
            });
            for (var entry : Dictionary.getList(Dictionary.BookName.sampleWord, Dictionary.BookQ.none)) {
                for (var lang : new Dictionary.DataLang[]{Dictionary.DataLang.english, Dictionary.DataLang.japanese}) {
                    try (var fd = context.getAssets().openFd(entry.toPath(lang).substring(AudioSource.ASSET_PREFIX.length()))) {
                        assertTrue(fd.getLength() > 0);
                    }
                }
            }
        }
    }

}
