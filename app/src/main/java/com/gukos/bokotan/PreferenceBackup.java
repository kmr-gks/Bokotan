package com.gukos.bokotan;

import android.content.Context;
import android.net.Uri;
import org.json.JSONObject;
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
        var files = root.getJSONObject("preferences"); var allowed = names();
        var editors = new ArrayList<android.content.SharedPreferences.Editor>();
        for (var iterator = files.keys(); iterator.hasNext();) {
            String name = iterator.next(); if (!allowed.contains(name)) throw new IOException();
            var values = files.getJSONObject(name);
            var editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear();
            for (var keys = values.keys(); keys.hasNext();) {
                String key = keys.next(); Object value = values.get(key);
                if (value instanceof Boolean) editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Integer) editor.putInt(key, (Integer) value);
                else if (value instanceof Long) editor.putLong(key, (Long) value);
                else if (value instanceof Double) editor.putFloat(key, ((Double) value).floatValue());
                else if (value instanceof String) editor.putString(key, (String) value);
                else throw new IOException();
            }
            editors.add(editor);
        }
        for (var editor : editors) editor.apply();
    }
}
