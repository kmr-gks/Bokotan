package com.gukos.bokotan;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class EmptyStudyTabInstrumentedTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private void main(Runnable action) { InstrumentationRegistry.getInstrumentation().runOnMainSync(action); }
    private void await(BooleanSupplier ready) throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (!ready.getAsBoolean() && System.currentTimeMillis() < deadline) Thread.sleep(50);
        assertTrue("Timed out waiting for visible study UI", ready.getAsBoolean());
    }
    private boolean shown(ActivityScenario<TabActivity> scenario, int id) {
        var result = new AtomicBoolean();
        scenario.onActivity(activity -> {
            View view = activity.findViewById(id);
            result.set(view != null && view.isShown());
        });
        return result.get();
    }
    private String text(ActivityScenario<TabActivity> scenario, int id) {
        var result = new AtomicReference<>("");
        scenario.onActivity(activity -> {
            TextView view = activity.findViewById(id);
            if (view != null) result.set(view.getText().toString());
        });
        return result.get();
    }
    private void click(ActivityScenario<TabActivity> scenario, int id) {
        scenario.onActivity(activity -> {
            View view = activity.findViewById(id);
            assertTrue("Control must be visible before clicking", view.isShown());
            assertTrue(view.performClick());
        });
    }
    @After public void cleanup() {
        main(() -> { QuizCreator.stopCurrent(); context.stopService(new Intent(context, PlayerService.class)); });
    }

    @Test public void errorReportingDoesNotRecurseWithoutStoragePermission() {
        assertFalse(android.os.Environment.isExternalStorageManager());
        main(() -> MyLibrary.ExceptionManager.showException(context,
                new IllegalStateException("Sample error for a storage-permission regression test")));
    }

    @Test public void directTestTabOffersSampleAndCanAnswerStopAndRestart() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            main(QuizCreator::stopCurrent);
            main(() -> TabActivity.setTabPageNum(2));
            await(() -> shown(scenario, R.id.buttonStartSampleQuiz));
            assertTrue(text(scenario, R.id.quizStatus).contains("まだ開始"));
            assertFalse(shown(scenario, R.id.buttonSelect1));
            scenario.recreate();
            await(() -> shown(scenario, R.id.buttonStartSampleQuiz));
            click(scenario, R.id.buttonStartSampleQuiz);
            await(() -> shown(scenario, R.id.buttonSelect1));
            assertTrue(Dictionary.allData.stream().anyMatch(e -> e.e.equals(text(scenario, R.id.textViewMondai))));
            click(scenario, R.id.buttonIdontKnow);
            await(() -> !text(scenario, R.id.textViewEditorial).isEmpty());
            scenario.recreate();
            await(() -> shown(scenario, R.id.buttonEndQuiz));
            click(scenario, R.id.buttonEndQuiz);
            await(() -> shown(scenario, R.id.buttonStartSampleQuiz));
            assertFalse(shown(scenario, R.id.buttonSelect1));
            click(scenario, R.id.buttonStartSampleQuiz);
            await(() -> shown(scenario, R.id.buttonSelect1));
        }
    }

    @Test public void unavailableBookShowsPersistentErrorAndSampleRecovery() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            main(() -> {
                TabActivity.setTabPageNum(2);
                QuizCreator.build(context, Dictionary.Folder.passtan, Dictionary.BookQ.q1,
                        PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
            });
            await(() -> text(scenario, R.id.quizStatus).contains("出題できる単語がありません"));
            assertTrue(shown(scenario, R.id.buttonStartSampleQuiz));
            scenario.recreate();
            await(() -> text(scenario, R.id.quizStatus).contains("出題できる単語がありません"));
            click(scenario, R.id.buttonStartSampleQuiz);
            await(() -> shown(scenario, R.id.buttonSelect1));
        }
    }

    @Test public void directPlayerTabOffersPlaybackAndRestoresIdleAfterStop() throws Exception {
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            main(() -> { context.stopService(new Intent(context, PlayerService.class)); TabActivity.setTabPageNum(1); });
            await(() -> shown(scenario, R.id.buttonStartSamplePlayback));
            assertFalse(shown(scenario, R.id.buttonPip));
            click(scenario, R.id.buttonStartSamplePlayback);
            await(() -> shown(scenario, R.id.buttonStopService));
            await(() -> !text(scenario, R.id.textViewEng).isEmpty());
            scenario.recreate();
            await(() -> shown(scenario, R.id.buttonStopService));
            click(scenario, R.id.buttonStopService);
            await(() -> !PlayerService.isRunning());
            main(() -> TabActivity.setTabPageNum(1));
            await(() -> shown(scenario, R.id.buttonStartSamplePlayback));
            assertTrue(text(scenario, R.id.playerStatus).contains("再生していません"));
        }
    }
}
