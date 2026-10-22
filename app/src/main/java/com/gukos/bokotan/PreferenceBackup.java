package com.gukos.bokotan;

import android.content.Context;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** SAFで設定を一つのJSONに入出力。単語本文・音声・取り込み元は含めない。 */
final class PreferenceBackup {
    private static Set<String> names() {
        var names = new HashSet<>(Arrays.asList(MyLibrary.PreferenceManager.getAllFileNames()));
        names.addAll(Arrays.asList("uisetting", "spinnerBookQ", "spinnerKuuhaku", "spinnerHyojijun", "SeekBar", "editText"));
        names.addAll(Dictionary.QuizData.seikai.keySet());
        return names;
    }
    static void exportTo(Context context, Uri uri) throws Exception {
        var files = new JSONObject();
        for (String name : names()) files.put(name, new JSONObject(context.getSharedPreferences(name, Context.MODE_PRIVATE).getAll()));
        var backup = new JSONObject().put("format", 1).put("preferences", files);
        try (OutputStream out = context.getContentResolver().openOutputStream(uri, "wt")) {
            if (out == null) throw new IOException();
            out.write(backup.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
    static void importFrom(Context context, Uri uri) throws Exception {
        var out = new ByteArrayOutputStream();
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) throw new IOException();
            byte[] buffer = new byte[8192]; int n;
            while ((n = in.read(buffer)) != -1) { if (out.size() + n > 16 * 1024 * 1024) throw new IOException(); out.write(buffer, 0, n); }
        }
        var root = new JSONObject(out.toString(StandardCharsets.UTF_8.name()));
        if (root.getInt("format") != 1) throw new IOException();
        // SharedPreferencesに触れる前に全体を検証し、一つでも不正なら何も書き換えない。
        var files = PreferenceBackupValidator.validate(plain(root.getJSONObject("preferences")), names(), limits(context));
        var editors = new ArrayList<android.content.SharedPreferences.Editor>();
        for (var file : files.entrySet()) {
            var editor = context.getSharedPreferences(file.getKey(), Context.MODE_PRIVATE).edit().clear();
            for (var entry : file.getValue().entrySet()) {
                String key = entry.getKey(); Object value = entry.getValue();
                if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else editor.putString(key, (String) value);
            }
            editors.add(editor);
        }
        for (var editor : editors) editor.apply();
    }
    private static PreferenceBackupValidator.Limits limits(Context context) throws Exception {
        var resources = context.getResources();
        return new PreferenceBackupValidator.Limits(resources.getStringArray(R.array.spinner_book_q).length,
                resources.getStringArray(R.array.spinner_kuuhaku).length, resources.getStringArray(R.array.spinner_hyojijun).length,
                seekBarMax(context, R.id.seekBarEng), seekBarMax(context, R.id.seekBarJpn));
    }
    /** 再生画面のレイアウトで宣言したandroid:max。省略時はProgressBarの既定値100。 */
    private static int seekBarMax(Context context, int id) throws Exception {
        String namespace = "http://schemas.android.com/apk/res/android";
        try (var parser = context.getResources().getLayout(R.layout.fragment_player)) {
            for (int event = parser.getEventType(); event != XmlPullParser.END_DOCUMENT; event = parser.next())
                if (event == XmlPullParser.START_TAG && parser.getAttributeResourceValue(namespace, "id", 0) == id)
                    return parser.getAttributeIntValue(namespace, "max", 100);
        }
        throw new IOException();
    }
    /** org.jsonの値をMap・List・nullとJDKの値に置き換える。 */
    private static Map<String, Object> plain(JSONObject object) throws JSONException {
        var map = new LinkedHashMap<String, Object>();
        for (var keys = object.keys(); keys.hasNext();) { String key = keys.next(); map.put(key, plain(object.get(key))); }
        return map;
    }
    private static Object plain(Object value) throws JSONException {
        if (value instanceof JSONObject) return plain((JSONObject) value);
        if (!(value instanceof JSONArray)) return value == JSONObject.NULL ? null : value;
        var array = (JSONArray) value; var list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) list.add(plain(array.get(i)));
        return list;
    }
}
