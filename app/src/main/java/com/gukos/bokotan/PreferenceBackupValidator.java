package com.gukos.bokotan;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/** 設定バックアップの値を、現在のコードが読む型と範囲で検証する。Androidに依存しない。 */
final class PreferenceBackupValidator {
    /** ACCEPT:そのまま保存 DROP:現在のコードが読まないキー REJECT:読むキーなのに型か値が不正 */
    enum Decision { ACCEPT, DROP, REJECT }

    /** リソースで決まるスピナーの項目数。 */
    static final class Limits {
        final int bookQ, kuuhaku, hyojijun;
        Limits(int bookQ, int kuuhaku, int hyojijun) {
            this.bookQ = bookQ; this.kuuhaku = kuuhaku; this.hyojijun = hyojijun;
        }
    }

    private PreferenceBackupValidator() {}

    /** 一つでも不正ならIllegalArgumentException。返す値は読まないキーを除いたBoolean・Integer・Stringのみ。 */
    static Map<String, Map<String, Object>> validate(Map<String, ?> files, Set<String> allowed, Limits limits) {
        var result = new LinkedHashMap<String, Map<String, Object>>();
        for (var file : files.entrySet()) {
            String name = file.getKey();
            if (!allowed.contains(name) || !(file.getValue() instanceof Map)) throw new IllegalArgumentException("設定ファイルが不正です: " + name);
            var values = new LinkedHashMap<String, Object>();
            for (var entry : ((Map<?, ?>) file.getValue()).entrySet()) {
                if (!(entry.getKey() instanceof String)) throw new IllegalArgumentException("設定のキーが不正です: " + name);
                String key = (String) entry.getKey();
                Decision decision = check(name, key, entry.getValue(), limits);
                if (decision == Decision.REJECT) throw new IllegalArgumentException("設定の値が不正です: " + name + "/" + key);
                if (decision == Decision.ACCEPT) values.put(key, entry.getValue());
            }
            result.put(name, values);
        }
        return result;
    }

    static Decision check(String file, String key, Object value, Limits limits) {
        Predicate<Object> rule = rule(file, key, limits);
        if (rule == null) return Decision.DROP;
        return rule.test(value) ? Decision.ACCEPT : Decision.REJECT;
    }

    /** 現在のコードがそのキーを読むときに必要な条件。読まないキーはnull。 */
    private static Predicate<Object> rule(String file, String key, Limits limits) {
        switch (file) {
            case MyLibrary.PreferenceManager.fnAppSettings:
                if (isViewIdKey(key)) return Boolean.class::isInstance;
                return isPositionKey(key) ? Integer.class::isInstance : null;
            case "uisetting":
                if (key.equals("skipCondition") || key.equals("skipThreshold")) return Integer.class::isInstance;
                return key.equals("skipThresholdNum") ? PreferenceBackupValidator::isThreshold : null;
            case "spinnerBookQ":
                return key.equals("selected") ? value -> inRange(value, limits.bookQ - 1) : null;
            case "spinnerKuuhaku":
                return key.equals("selected") ? value -> inRange(value, limits.kuuhaku - 1) : null;
            case "spinnerHyojijun":
                return key.equals("selected") ? value -> inRange(value, limits.hyojijun - 1) : null;
            case "SeekBar":
                // SeekBar.setProgressが範囲内に丸めるため、型だけを確認する。
                return key.equals("english") || key.equals("japanese") ? Integer.class::isInstance : null;
            case "editText":
                // 画面から0以下も保存できるが、PipActivityが縦横比を確認してから使う。
                return key.equals("editTextPipYoko") || key.equals("editTextPipTate") ? Integer.class::isInstance : null;
            default:
                if (!file.startsWith(MyLibrary.PreferenceManager.DataName.dnTestActivity) || !file.endsWith("Test")) return null;
                if (key.equals("keySeikai") || key.equals("keyHuseikai")) return PreferenceBackupValidator::isCounters;
                return key.equals(MyLibrary.PreferenceManager.DataName.N_GENZAI_NAN_MONME) ? Integer.class::isInstance : null;
        }
    }

    private static boolean inRange(Object value, int max) {
        return value instanceof Integer && (Integer) value >= 0 && (Integer) value <= max;
    }

    /** MyLibrary.PreferenceManager.getSettingが読む"id"+ビューID。 */
    private static boolean isViewIdKey(String key) {
        if (!key.startsWith("id")) return false;
        try { return key.equals("id" + Integer.parseInt(key.substring(2))); }
        catch (NumberFormatException error) { return false; }
    }

    /** PlayerService.positionKey()と同じ組み立て。個人データは末尾に取り込みIDが付く。 */
    private static boolean isPositionKey(String key) {
        for (var book : Dictionary.Folder.values())
            for (var grade : Dictionary.BookQ.values())
                for (var mode : Dictionary.Datatype.values()) {
                    String prefix = PlayerService.className + book + grade + mode;
                    if (book == Dictionary.Folder.personal ? key.startsWith(prefix) : key.equals(prefix)) return true;
                }
        return false;
    }

    /** QSentakuFragmentがDouble.parseDoubleで読む値。開始時と同じく有限の0以上に限る。 */
    private static boolean isThreshold(Object value) {
        if (!(value instanceof String)) return false;
        try {
            double number = Double.parseDouble((String) value);
            return Double.isFinite(number) && number >= 0;
        } catch (NumberFormatException error) { return false; }
    }

    /** MyLibrary.PreferenceManager.intArrayToStringが書く0以上の整数のカンマ区切り。空文字は記録なしとして読まれる。 */
    private static boolean isCounters(Object value) {
        if (!(value instanceof String)) return false;
        String text = (String) value;
        long number = 0; boolean digits = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ',' && digits) { number = 0; digits = false; continue; }
            if (c < '0' || c > '9') return false;
            number = number * 10 + (c - '0');
            if (number > Integer.MAX_VALUE) return false;
            digits = true;
        }
        return digits || text.isEmpty();
    }
}
