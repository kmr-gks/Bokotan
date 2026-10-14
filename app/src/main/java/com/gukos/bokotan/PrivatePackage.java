package com.gukos.bokotan;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipInputStream;

/** ZIPの展開・検証。Androidにも実データにも依存しない。 */
final class PrivatePackage {
    static final long MAX_BYTES = 8L * 1024 * 1024 * 1024;
    static final int MAX_FILES = 100000;
    interface Progress { void update(long bytes) throws IOException; }
    static final class Word {
        final int id; final String english, japanese, enAudio, jaAudio;
        Word(int id, String english, String japanese, String enAudio, String jaAudio) {
            this.id = id; this.english = english; this.japanese = japanese;
            this.enAudio = enAudio; this.jaAudio = jaAudio;
        }
    }
    static File path(File root, String name) throws IOException {
        if (name.isEmpty() || name.startsWith("/") || name.contains("\\") || name.contains(":")) throw new IOException("ZIP内のパスが不正です。");
        for (String part : name.split("/")) if (part.equals("..") || part.equals(".")) throw new IOException("ZIP内のパスが不正です。");
        File result = new File(root, name);
        if (!result.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator)) throw new IOException("ZIP内のパスが不正です。");
        return result;
    }
    static void extract(InputStream source, File root, Progress progress) throws IOException {
        if (!root.mkdirs()) throw new IOException("取り込み用領域を作成できません。");
        long total = 0; int files = 0;
        Set<String> names = new HashSet<>(); byte[] buffer = new byte[128 * 1024];
        try (var zip = new ZipInputStream(source, StandardCharsets.UTF_8)) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("取り込みを中止しました。");
                String name = entry.getName(); File target = path(root, name);
                if (++files > MAX_FILES) throw new IOException("ZIPのファイル数が上限を超えました。");
                if (name.startsWith("__MACOSX/") || name.endsWith(".DS_Store")) {
                    int n;
                    while ((n = zip.read(buffer)) != -1) {
                        total += n; if (total > MAX_BYTES) throw new IOException("ZIPの展開サイズが上限を超えました。");
                        progress.update(total);
                    }
                    zip.closeEntry(); continue;
                }
                if (!names.add(target.getCanonicalPath())) throw new IOException("ZIPのファイル数または重複が不正です。");
                if (!(name.equals("package.properties") || name.equals("words.tsv") || name.startsWith("audio/"))) throw new IOException("ZIPの構成が形式に合いません。");
                if (entry.isDirectory()) { if (!target.isDirectory() && !target.mkdirs()) throw new IOException("フォルダを作成できません。"); continue; }
                if (!target.getParentFile().isDirectory() && !target.getParentFile().mkdirs()) throw new IOException("フォルダを作成できません。");
                long fileBytes = 0;
                try (var output = new FileOutputStream(target)) {
                    int count;
                    while ((count = zip.read(buffer)) != -1) {
                        total += count; fileBytes += count;
                        if (total > MAX_BYTES || (!name.startsWith("audio/") && fileBytes > 16 * 1024 * 1024)) throw new IOException("ZIPの展開サイズが上限を超えました。");
                        if (root.getUsableSpace() < count + 16L * 1024 * 1024) throw new IOException("端末の空き容量が不足しています。");
                        progress.update(total); output.write(buffer, 0, count);
                    }
                }
                zip.closeEntry();
            }
        }
        readId(root); readWords(root);
    }
    static String readId(File root) throws IOException {
        var properties = new Properties();
        try (var reader = new InputStreamReader(new FileInputStream(new File(root, "package.properties")), StandardCharsets.UTF_8)) { properties.load(reader); }
        String id = properties.getProperty("id", "");
        if (!"1".equals(properties.getProperty("format")) || !id.matches("[A-Za-z0-9_-]{1,64}")) throw new IOException("package.propertiesのformatまたはidが不正です。");
        return id;
    }
    static List<Word> readWords(File root) throws IOException {
        var result = new ArrayList<Word>(); var ids = new HashSet<Integer>();
        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(root, "words.tsv")), StandardCharsets.UTF_8))) {
            if (!"id\tenglish\tjapanese\tenglish_audio\tjapanese_audio".equals(reader.readLine())) throw new IOException("words.tsvのヘッダーが不正です。");
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] fields = line.split("\t", -1); int id;
                if (fields.length != 5) throw new IOException("words.tsvは5列必要です。");
                try { id = Integer.parseInt(fields[0]); } catch (NumberFormatException error) { throw new IOException("単語番号が不正です。"); }
                if (id < 0 || id > 100000 || !ids.add(id) || !StudyRules.hasText(fields[1]) || !StudyRules.hasText(fields[2])) throw new IOException("単語番号・英語・日本語が不正です。");
                for (int i = 3; i <= 4; i++) {
                    if (!fields[i].startsWith("audio/") || !fields[i].matches("(?i).*\\.(mp3|ogg|wav|m4a|btv)$")) throw new IOException("音声のパスまたは拡張子が不正です。");
                    File audio = path(root, fields[i]);
                    if (!audio.isFile() || audio.length() == 0) throw new IOException("単語に対応する音声がありません。");
                }
                result.add(new Word(id, fields[1], fields[2], fields[3], fields[4]));
            }
        }
        if (result.isEmpty()) throw new IOException("単語がありません。");
        return result;
    }
    static void delete(File file) {
        File[] children = file.listFiles();
        if (children != null) for (File child : children) delete(child);
        file.delete();
    }
}
