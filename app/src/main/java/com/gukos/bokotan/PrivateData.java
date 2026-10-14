package com.gukos.bokotan;

import android.content.Context;
import java.io.File;

final class PrivateData {
    private static File current;
    static String id = "";
    static synchronized File root(Context context) {
        if (current == null) {
            String directory = context.getSharedPreferences("privateImport", Context.MODE_PRIVATE).getString("directory", "");
            if (!directory.isEmpty()) current = new File(context.getNoBackupFilesDir(), directory);
        }
        return current;
    }
    static synchronized void activate(Context context, File directory) throws java.io.IOException {
        String relative = directory == null ? "" : directory.getName();
        if (!context.getSharedPreferences("privateImport", Context.MODE_PRIVATE).edit().putString("directory", relative).commit())
            throw new java.io.IOException("取り込み結果を保存できません。");
        current = directory;
    }
    static void load(Context context, java.util.List<Dictionary.Entry> entries) {
        id = "";
        File root = root(context);
        if (root == null) return;
        try {
            id = PrivatePackage.readId(root);
            var words = new java.util.ArrayList<Dictionary.Entry>();
            for (var word : PrivatePackage.readWords(root)) {
                var entry = new Dictionary.Entry(word.english, word.japanese, Dictionary.Folder.personal,
                        Dictionary.BookName.personalWord, Dictionary.BookQ.none, word.id, Dictionary.Datatype.word);
                entry.privatePackageId = id;
                entry.privateEnglishAudio = new File(root, word.enAudio).getAbsolutePath();
                entry.privateJapaneseAudio = new File(root, word.jaAudio).getAbsolutePath();
                words.add(entry);
            }
            Dictionary.listsPerBook.put(Dictionary.BookName.personalWord.toString() + Dictionary.BookQ.none, words);
            entries.addAll(words);
        } catch (java.io.IOException error) {
            Dictionary.loadErrors.add("個人データを読み込めません。");
        }
    }
}
