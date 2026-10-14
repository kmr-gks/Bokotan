package com.gukos.bokotan;

import android.app.*;
import android.content.*;
import android.net.Uri;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PrivateImportInstrumentedTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private void main(Runnable task) { InstrumentationRegistry.getInstrumentation().runOnMainSync(task); }
    private void await(java.util.function.BooleanSupplier ready) throws Exception {
        long deadline = System.currentTimeMillis() + 15000;
        while (!ready.getAsBoolean() && System.currentTimeMillis() < deadline) Thread.sleep(50);
        assertTrue("Timed out: " + ImportService.status, ready.getAsBoolean());
    }
    private File fixture(boolean valid) throws Exception {
        File file = new File(context.getCacheDir(), "import-test.zip");
        try (var zip = new ZipOutputStream(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            zip.putNextEntry(new ZipEntry("package.properties")); zip.write("format=1\nid=instrumented-import\n".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            var rows = new StringBuilder("id\tenglish\tjapanese\tenglish_audio\tjapanese_audio\n");
            var english = new BufferedReader(new InputStreamReader(context.getAssets().open("dictionaries/Sample/Words.e.txt"), StandardCharsets.UTF_8));
            var japanese = new BufferedReader(new InputStreamReader(context.getAssets().open("dictionaries/Sample/Words.j.txt"), StandardCharsets.UTF_8));
            for (int i = 0; i < 5; i++) {
                rows.append(i).append('\t').append(english.readLine()).append('\t').append(japanese.readLine()).append('\t')
                        .append("audio/").append(i).append("-en.btv\taudio/").append(i).append("-ja.btv\n");
                if (valid) for (String lang : new String[]{"en", "ja"}) {
                    zip.putNextEntry(new ZipEntry("audio/" + i + "-" + lang + ".btv"));
                    try (InputStream in = context.getAssets().open(String.format(java.util.Locale.ROOT, "sample-audio/%04d-%s.btv", i, lang))) { byte[] buffer = new byte[8192]; int n; while ((n = in.read(buffer)) != -1) zip.write(buffer, 0, n); }
                    zip.closeEntry();
                }
            }
            english.close(); japanese.close();
            zip.putNextEntry(new ZipEntry("words.tsv")); zip.write(rows.toString().getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
        }
        return file;
    }
    @Test public void pickerImportLoadsSearchQuizAudioAndBadReplacementKeepsOriginal() throws Exception {
        File old = PrivateData.root(context);
        var instrumentation = InstrumentationRegistry.getInstrumentation();
        try (var scenario = ActivityScenario.launch(TabActivity.class)) {
            await(Dictionary::isReady);
            File good = fixture(true);
            // The picker itself belongs to Android. Intercept its result with the public fixture.
            var pickerFilter = new IntentFilter(Intent.ACTION_OPEN_DOCUMENT);
            pickerFilter.addCategory(Intent.CATEGORY_OPENABLE); pickerFilter.addDataType("*/*");
            var monitor = instrumentation.addMonitor(pickerFilter,
                    new Instrumentation.ActivityResult(Activity.RESULT_OK, new Intent().setData(Uri.fromFile(good))), true);
            scenario.onActivity(activity -> activity.findViewById(R.id.buttonImportZip).performClick());
            {
                androidx.test.espresso.Espresso.onView(
                        androidx.test.espresso.matcher.ViewMatchers.withText("ZIPを選択")).perform(
                        androidx.test.espresso.action.ViewActions.click());
            }
            instrumentation.removeMonitor(monitor);
            await(() -> PrivateData.root(context) != old && !ImportService.busy);
            assertEquals(5, Dictionary.getList(Dictionary.BookName.personalWord, Dictionary.BookQ.none).size());
            var entry = Dictionary.getList(Dictionary.BookName.personalWord, Dictionary.BookQ.none).get(0);
            assertTrue(Dictionary.allData.contains(entry));
            assertTrue(new File(entry.toPath(Dictionary.DataLang.english)).getCanonicalPath().startsWith(context.getNoBackupFilesDir().getCanonicalPath()));
            main(() -> {
                try {
                    var player = AudioSource.create(context, entry.toPath(Dictionary.DataLang.english));
                    assertNotNull(player); player.start(); assertTrue(player.isPlaying()); player.release();
                    TabActivity.setTabPageNum(2);
                    QuizCreator.build(context, Dictionary.Folder.personal, Dictionary.BookQ.none,
                            PlayerService.SkipContidion.all, 0, PlayerService.SkipThreshold.eqormore);
                } catch (IOException error) { throw new AssertionError(error); }
            });
            Thread.sleep(500);
            scenario.onActivity(activity -> {
                android.widget.TextView question = activity.findViewById(R.id.textViewMondai);
                assertTrue(Dictionary.getList(Dictionary.BookName.personalWord, Dictionary.BookQ.none).stream().anyMatch(e -> e.e.contentEquals(question.getText())));
            });
            main(() -> {
                QuizCreator.stopCurrent();
                TabActivity.setTabPageNum(1);
                context.startForegroundService(new Intent(context, PlayerService.class)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_MODE, Dictionary.Datatype.word)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_BOOK, Dictionary.Folder.personal)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_DATA_Q, Dictionary.BookQ.none)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_COND, PlayerService.SkipContidion.all)
                        .putExtra(PlayerService.PLAYERSERVICE_EXTRA_SKIP_THRES_COMP, PlayerService.SkipThreshold.eqormore));
            });
            await(() -> PlayerService.wordDataList.size() == 5 && PlayerService.wordDataList.get(0).folder == Dictionary.Folder.personal);
            main(() -> {
                context.stopService(new Intent(context, PlayerService.class));
                TabActivity.setTabPageNum(3);
            });
            var found = new java.util.concurrent.atomic.AtomicBoolean();
            await(() -> {
                scenario.onActivity(activity -> {
                    android.widget.ListView list = activity.findViewById(R.id.listViewKensakuResult);
                    if (list == null || list.getAdapter() == null) return;
                    androidx.appcompat.widget.SearchView search = activity.findViewById(R.id.searchView);
                    search.setQuery("dog", false);
                    for (int i = 0; i < list.getAdapter().getCount(); i++) {
                        var item = (Dictionary.Entry) list.getAdapter().getItem(i);
                        if (item.folder == Dictionary.Folder.personal && item.e.equals("dog")) found.set(true);
                    }
                });
                return found.get();
            });
            assertNotEquals(Dictionary.QuizData.keyFor(entry), Dictionary.QuizData.keyFor(Dictionary.allData.get(0)));
            String[] permissions = context.getPackageManager().getPackageInfo(context.getPackageName(), android.content.pm.PackageManager.GET_PERMISSIONS).requestedPermissions;
            assertFalse(java.util.Arrays.asList(permissions).contains("android.permission.MANAGE_EXTERNAL_STORAGE"));
            File active = PrivateData.root(context); File bad = fixture(false);
            main(() -> context.startForegroundService(new Intent(context, ImportService.class).setData(Uri.fromFile(bad))));
            await(() -> !ImportService.busy && ImportService.status.contains("失敗"));
            assertEquals(active, PrivateData.root(context)); assertTrue(active.isDirectory());
            assertEquals(5, Dictionary.getList(Dictionary.BookName.personalWord, Dictionary.BookQ.none).size());
        } finally {
            main(() -> { QuizCreator.stopCurrent(); ImportService.completed = false; });
            File imported = PrivateData.root(context);
            PrivateData.activate(context, old);
            if (imported != null && !imported.equals(old)) PrivatePackage.delete(imported);
            main(() -> Dictionary.reload(context, () -> {}));
            await(Dictionary::isReady);
        }
    }
    @Test public void preferenceBackupRoundTripsMixedTypesWithoutPrivateFiles() throws Exception {
        var prefs = context.getSharedPreferences("SeekBar", Context.MODE_PRIVATE);
        int original = prefs.getInt("english", 5);
        var setting = context.getSharedPreferences("uisetting", Context.MODE_PRIVATE);
        String threshold = setting.getString("skipThresholdNum", "1");
        boolean pronunciation = MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchQuizHatsuon, true);
        File file = new File(context.getCacheDir(), "settings-test.json");
        try {
            prefs.edit().putInt("english", 9).apply();
            setting.edit().putString("skipThresholdNum", "2.5").apply();
            MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchQuizHatsuon, false);
            PreferenceBackup.exportTo(context, Uri.fromFile(file));
            String backup = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            assertFalse(backup.contains("privateImport"));
            assertFalse(backup.contains("no_backup"));
            prefs.edit().putInt("english", 1).apply();
            setting.edit().putString("skipThresholdNum", "100").apply();
            MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchQuizHatsuon, true);
            PreferenceBackup.importFrom(context, Uri.fromFile(file));
            assertEquals(9, prefs.getInt("english", 0));
            assertEquals("2.5", setting.getString("skipThresholdNum", ""));
            assertFalse(MyLibrary.PreferenceManager.getSetting(context, "id" + R.id.switchQuizHatsuon, true));
        } finally {
            prefs.edit().putInt("english", original).apply();
            setting.edit().putString("skipThresholdNum", threshold).apply();
            MyLibrary.PreferenceManager.putSetting(context, "id" + R.id.switchQuizHatsuon, pronunciation);
            file.delete();
        }
    }

}
