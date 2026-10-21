package com.gukos.bokotan;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@RunWith(AndroidJUnit4.class)
public class PipInstrumentedTest {
    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getTargetContext();

    private void main(Runnable action) {
        instrumentation.runOnMainSync(action);
    }

    private <T> T onMain(Supplier<T> query) {
        var value = new AtomicReference<T>();
        main(() -> value.set(query.get()));
        return value.get();
    }

    private void await(String description, BooleanSupplier ready) {
        long deadline = SystemClock.elapsedRealtime() + 10_000;
        while (!ready.getAsBoolean() && SystemClock.elapsedRealtime() < deadline) {
            SystemClock.sleep(50);
        }
        assertTrue(description, ready.getAsBoolean());
    }

    private String text(Activity activity, int id) {
        return onMain(() -> {
            TextView view = activity.findViewById(id);
            return view == null ? "" : view.getText().toString();
        });
    }

    @Test public void samplePlaybackEntersPipUpdatesWordsAndReturnsToApp() {
        assumeTrue(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));
        boolean reverse = MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchSortHanten, false);
        boolean showTranslation = MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchHyojiYakuBeforeRead, true);
        var tab = new AtomicReference<TabActivity>();
        var pip = new AtomicReference<Activity>();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(PipActivity.class.getName(), null, false);

        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            try {
                scenario.onActivity(tab::set);
                await("Dictionary is ready", Dictionary::isReady);
                main(() -> {
                    MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchSortHanten, false);
                    MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchHyojiYakuBeforeRead, true);
                    TabActivity.setTabPageNum(1);
                    context.startForegroundService(new Intent(context, PlayerService.class)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.word)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.sample)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.none)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND, PlayerService.SkipContidion.all)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, PlayerService.SkipThreshold.eqormore)
                            .putExtra(PlayerService.PLAYERSERVICE_EXTRA_NOW, 0));
                });
                await("Sample playback has a word", () -> !text(tab.get(), R.id.textViewEng).isEmpty());
                var entries = onMain(() -> new ArrayList<>(PlayerService.wordDataList));
                assertEquals(5, entries.size());
                assertTrue(onMain(() -> tab.get().findViewById(R.id.buttonPip).performClick()));

                // ActivityScenario.onActivity waits for RESUMED and would block once PiP is paused.
                // Observe the normally launched activity, then query its views on the main thread.
                Activity started = monitor.waitForActivityWithTimeout(10_000);
                pip.set(started);
                assertNotNull("PIP button launches PipActivity", started);
                await("Activity enters the small PIP window", () -> onMain(started::isInPictureInPictureMode));

                jumpToWord(1);
                await("PIP receives the selected word", () -> entries.get(1).e.equals(text(started, R.id.textViewPipEng)));
                jumpToWord(3);
                await("PIP continues updating during playback", () -> entries.get(3).e.equals(text(started, R.id.textViewPipEng)));

                main(() -> started.startActivity(new Intent(started, TabActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)));
                await("Main app returns to the foreground", () -> onMain(() -> tab.get().hasWindowFocus()));
                await("Playback is still visible after returning", () -> !text(tab.get(), R.id.textViewEng).isEmpty());
                assertTrue(onMain(PlayerService::isRunning));
            } finally {
                main(() -> {
                    Activity activity = pip.get() != null ? pip.get() : monitor.getLastActivity();
                    if (activity != null && !activity.isDestroyed()) activity.finish();
                    context.stopService(new Intent(context, PlayerService.class));
                    MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchSortHanten, reverse);
                    MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchHyojiYakuBeforeRead, showTranslation);
                });
                await("Playback service stops during cleanup", () -> onMain(() -> !PlayerService.isRunning()));
                Activity activity = pip.get() != null ? pip.get() : monitor.getLastActivity();
                if (activity != null) await("PIP activity is destroyed during cleanup", () -> onMain(activity::isDestroyed));
            }
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    private void jumpToWord(int index) {
        main(() -> context.sendBroadcast(new Intent(PlayerService.PLAYERSERVICE_ACTION)
                .setPackage(context.getPackageName())
                .putExtra(PlayerService.PLAYERSERVICE_MESSAGE_TYPE, PlayerService.PLAYERSERVICE_MESSAGE_NOW)
                .putExtra(PlayerService.PLAYERSERVICE_MESSAGE_NOW, index)));
    }
}
