package com.gukos.bokotan;

import org.junit.Test;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.*;

public class PreferenceBackupValidatorTest {
    private static final PreferenceBackupValidator.Limits LIMITS = new PreferenceBackupValidator.Limits(10, 3, 4, 10, 10);
    private static final Set<String> ALLOWED = new HashSet<>(Arrays.asList("qSentakuActivity", "appsettings", "uisetting",
            "spinnerBookQ", "spinnerKuuhaku", "spinnerHyojijun", "SeekBar", "editText",
            "testActivity1qTest", "testActivitysampleTest", "testActivityprivate-my-bookTest"));
    private static final String POSITION = PlayerService.className + Dictionary.Folder.passtan + Dictionary.BookQ.q1 + Dictionary.Datatype.mix;

    private static Map<String, Object> map(Object... pairs) {
        var map = new LinkedHashMap<String, Object>();
        for (int i = 0; i < pairs.length; i += 2) map.put((String) pairs[i], pairs[i + 1]);
        return map;
    }
    private static Map<String, Map<String, Object>> validate(Map<String, Object> files) {
        return PreferenceBackupValidator.validate(files, ALLOWED, LIMITS);
    }
    private static void assertAccepted(String file, String key, Object value) {
        assertEquals(file + "/" + key + "=" + value, PreferenceBackupValidator.Decision.ACCEPT, PreferenceBackupValidator.check(file, key, value, LIMITS));
    }
    private static void assertRejected(String file, String key, Object value) {
        assertEquals(file + "/" + key + "=" + value, PreferenceBackupValidator.Decision.REJECT, PreferenceBackupValidator.check(file, key, value, LIMITS));
        var files = map("uisetting", map("skipThresholdNum", "1"));
        files.put(file, map(key, value));
        try { validate(files); fail(file + "/" + key + "=" + value); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains(file)); }
    }

    @Test public void acceptsEverythingTheCurrentAppWrites() {
        var counts = new int[100001];
        for (int i = 0; i < counts.length; i++) counts[i] = i % 13;
        counts[counts.length - 1] = Integer.MAX_VALUE;
        String seikai = MyLibrary.PreferenceManager.intArrayToString(counts);
        var files = map(
                "appsettings", map("id2131231234", true, "id2131231235", false,
                        PlayerService.className + Dictionary.Folder.sample + Dictionary.BookQ.none + Dictionary.Datatype.word, 4,
                        PlayerService.className + Dictionary.Folder.personal + Dictionary.BookQ.none + Dictionary.Datatype.word + "my-book", 100000,
                        POSITION, 0),
                "uisetting", map("skipCondition", 2131231000, "skipThreshold", 2131231001, "skipThresholdNum", "2.5"),
                "spinnerBookQ", map("selected", 9),
                "spinnerKuuhaku", map("selected", 2),
                "spinnerHyojijun", map("selected", 3),
                "SeekBar", map("english", 10, "japanese", 0),
                "editText", map("editTextPipYoko", 16, "editTextPipTate", 9),
                "testActivityprivate-my-bookTest", map("keySeikai", seikai, "keyHuseikai", "0,1,2", "nGenzaiNanMonme", 12),
                "testActivity1qTest", map("keySeikai", ""),
                "qSentakuActivity", map());
        assertEquals(files, validate(files));
        assertArrayEquals(counts, MyLibrary.PreferenceManager.stringToIntArray(seikai));
    }
    @Test public void dropsKeysTheCurrentCodeNeverReads() {
        var result = validate(map(
                "qSentakuActivity", map("RadioButton", 2131231000),
                "testActivity1qTest", map("nWordSeikaisu12", 3, "keySeikai", "1,2"),
                "appsettings", map("PlayerServiceunknown", "x", "idea", 1.5, "id", false, "id12", true),
                "uisetting", map("legacy", Arrays.asList(1, 2), "skipThresholdNum", "1"),
                "SeekBar", map("speed", null)));
        assertEquals(map(), result.get("qSentakuActivity"));
        assertEquals(map("keySeikai", "1,2"), result.get("testActivity1qTest"));
        assertEquals(map("id12", true), result.get("appsettings"));
        assertEquals(map("skipThresholdNum", "1"), result.get("uisetting"));
        assertEquals(map(), result.get("SeekBar"));
    }
    @Test public void rejectsWrongTypes() {
        assertRejected("spinnerKuuhaku", "selected", "x");
        assertRejected("spinnerKuuhaku", "selected", true);
        assertRejected("spinnerBookQ", "selected", map("value", 1));
        assertRejected("SeekBar", "japanese", null);
        assertRejected("uisetting", "skipThresholdNum", 2);
        assertRejected("uisetting", "skipCondition", "2131231000");
        assertRejected("appsettings", "id2131231234", "true");
        assertRejected("appsettings", "id2131231234", 1);
        assertRejected("appsettings", POSITION, "3");
        assertRejected("editText", "editTextPipYoko", "16");
        assertRejected("testActivity1qTest", "keySeikai", 5);
        assertRejected("testActivity1qTest", "keyHuseikai", Arrays.asList(1, 2));
        assertRejected("testActivitysampleTest", "nGenzaiNanMonme", "1");
    }
    @Test public void rejectsDoubleOrLongWhereIntIsRead() {
        for (Object value : new Object[]{1.5, 1.0, 1L, Integer.MAX_VALUE + 1L}) {
            assertRejected("spinnerBookQ", "selected", value);
            assertRejected("spinnerKuuhaku", "selected", value);
            assertRejected("spinnerHyojijun", "selected", value);
            assertRejected("SeekBar", "english", value);
            assertRejected("editText", "editTextPipTate", value);
            assertRejected("uisetting", "skipThreshold", value);
            assertRejected("appsettings", POSITION, value);
            assertRejected("testActivitysampleTest", "nGenzaiNanMonme", value);
        }
    }
    @Test public void rejectsOutOfRange() {
        assertAccepted("spinnerBookQ", "selected", 0);
        assertAccepted("spinnerBookQ", "selected", 9);
        assertRejected("spinnerBookQ", "selected", 10);
        assertRejected("spinnerBookQ", "selected", -1);
        assertAccepted("spinnerKuuhaku", "selected", 2);
        assertRejected("spinnerKuuhaku", "selected", 3);
        assertAccepted("spinnerHyojijun", "selected", 3);
        assertRejected("spinnerHyojijun", "selected", 4);
        assertAccepted("SeekBar", "english", 10);
        assertRejected("SeekBar", "english", 11);
        assertRejected("SeekBar", "japanese", -1);
    }
    @Test public void rejectsUnparseableNumbers() {
        for (String threshold : new String[]{"0", "0.5", "10"}) assertAccepted("uisetting", "skipThresholdNum", threshold);
        for (String threshold : new String[]{"abc", "", "NaN", "Infinity", "-1", "1e400"}) assertRejected("uisetting", "skipThresholdNum", threshold);
        for (String counters : new String[]{"0", "0,0", "2147483647,1"}) assertAccepted("testActivity1qTest", "keySeikai", counters);
        for (String counters : new String[]{"1,,2", ",1", "1,", "1,-2", "+1", "1 ,2", "a", "1.5", "2147483648", "99999999999999999999"})
            assertRejected("testActivity1qTest", "keyHuseikai", counters);
    }
    @Test public void rejectsFilesOutsideTheAllowList() {
        for (String name : new String[]{"privateImport", "testActivityprivate-otherTest", "unknown"}) {
            try { validate(map("uisetting", map(), name, map())); fail(name); }
            catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains(name)); }
        }
        try { validate(map("uisetting", "x")); fail(); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("uisetting")); }
    }
}
