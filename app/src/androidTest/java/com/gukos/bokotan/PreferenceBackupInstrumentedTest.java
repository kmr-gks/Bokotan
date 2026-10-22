package com.gukos.bokotan;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PreferenceBackupInstrumentedTest {
    private static final String[] FILES = {"spinnerKuuhaku", "uisetting", "SeekBar", "testActivity1qTest", "privateImport"};
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private final File backup = new File(context.getCacheDir(), "settings-validation-test.json");
    private final Map<String, Map<String, Object>> saved = new HashMap<>();

    private SharedPreferences prefs(String name) { return context.getSharedPreferences(name, Context.MODE_PRIVATE); }
    private void importJson(String preferences) throws Exception {
        Files.write(backup.toPath(), ("{\"format\":1,\"preferences\":" + preferences + "}").getBytes(StandardCharsets.UTF_8));
        PreferenceBackup.importFrom(context, Uri.fromFile(backup));
    }
    private void assertImportFails(String preferences) {
        try { importJson(preferences); fail("Imported " + preferences); }
        catch (Exception expected) { }
    }

    @Before public void save() {
        for (String name : FILES) saved.put(name, new HashMap<String, Object>(prefs(name).getAll()));
    }
    @After public void restore() {
        for (var file : saved.entrySet()) {
            var editor = prefs(file.getKey()).edit().clear();
            for (var entry : file.getValue().entrySet()) {
                String key = entry.getKey(); Object value = entry.getValue();
                if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else if (value instanceof Long) editor.putLong(key, (Long) value);
                else if (value instanceof Float) editor.putFloat(key, (Float) value);
                else if (value instanceof String) editor.putString(key, (String) value);
            }
            assertTrue(editor.commit());
        }
        backup.delete();
    }

    @Test public void wrongTypedValueThrowsAndKeepsPreferences() {
        assertTrue(prefs("spinnerKuuhaku").edit().clear().putInt("selected", 1).commit());
        assertTrue(prefs("uisetting").edit().clear().putString("skipThresholdNum", "3").putInt("skipCondition", 7).commit());
        // uisettingを先に置き、後のファイルが不正なら前のファイルも書き換えないことを確かめる。
        for (String invalid : new String[]{"{\"selected\":\"x\"}", "{\"selected\":1.5}", "{\"selected\":3}", "{\"selected\":4294967296}", "{\"selected\":null}"})
            assertImportFails("{\"uisetting\":{\"skipThresholdNum\":\"9\"},\"spinnerKuuhaku\":" + invalid + "}");
        assertImportFails("{\"uisetting\":{\"skipThresholdNum\":\"abc\"}}");
        assertImportFails("{\"uisetting\":{\"skipThresholdNum\":2}}");
        assertImportFails("{\"SeekBar\":{\"english\":11}}");
        assertImportFails("{\"privateImport\":{\"directory\":\"x\"}}");
        assertEquals(1, prefs("spinnerKuuhaku").getInt("selected", -1));
        assertEquals("3", prefs("uisetting").getString("skipThresholdNum", null));
        assertEquals(7, prefs("uisetting").getInt("skipCondition", -1));
        assertEquals(saved.get("privateImport"), prefs("privateImport").getAll());
    }
    @Test public void validBackupImportsAndDropsUnknownKeys() throws Exception {
        assertTrue(prefs("SeekBar").edit().clear().putInt("english", 5).putFloat("legacy", 1.5f).commit());
        importJson("{\"spinnerKuuhaku\":{\"selected\":2,\"legacy\":1.5},"
                + "\"uisetting\":{\"skipThresholdNum\":\"2.5\",\"skipCondition\":123},"
                + "\"SeekBar\":{\"english\":10,\"japanese\":0}}");
        assertEquals(2, prefs("spinnerKuuhaku").getInt("selected", -1));
        assertFalse(prefs("spinnerKuuhaku").contains("legacy"));
        assertEquals("2.5", prefs("uisetting").getString("skipThresholdNum", null));
        assertEquals(123, prefs("uisetting").getInt("skipCondition", -1));
        assertEquals(10, prefs("SeekBar").getInt("english", -1));
        assertEquals(0, prefs("SeekBar").getInt("japanese", -1));
        assertFalse(prefs("SeekBar").contains("legacy"));
    }
    @Test public void exportedBackupWithLargeScoresImportsAgain() throws Exception {
        var counts = new int[100001];
        for (int i = 0; i < counts.length; i++) counts[i] = i % 13;
        String scores = MyLibrary.PreferenceManager.intArrayToString(counts);
        assertTrue(prefs("testActivity1qTest").edit().clear().putString("keySeikai", scores)
                .putString("keyHuseikai", "0,1").putInt(MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME, 42).commit());
        PreferenceBackup.exportTo(context, Uri.fromFile(backup));
        assertTrue(prefs("testActivity1qTest").edit().clear().commit());
        PreferenceBackup.importFrom(context, Uri.fromFile(backup));
        assertEquals(scores, prefs("testActivity1qTest").getString("keySeikai", null));
        assertEquals("0,1", prefs("testActivity1qTest").getString("keyHuseikai", null));
        assertEquals(42, prefs("testActivity1qTest").getInt(MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME, -1));
    }
}
