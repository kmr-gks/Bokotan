package com.gukos.bokotan;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.*;
import static org.junit.Assert.*;

public class PrivatePackageTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private byte[] zip(String[] names, String[] contents) throws Exception {
        var out = new ByteArrayOutputStream();
        try (var zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            for (int i = 0; i < names.length; i++) { zip.putNextEntry(new ZipEntry(names[i])); zip.write(contents[i].getBytes(StandardCharsets.UTF_8)); zip.closeEntry(); }
        }
        return out.toByteArray();
    }
    private String words(String rows) { return "id\tenglish\tjapanese\tenglish_audio\tjapanese_audio\n" + rows; }
    private File extract(String table) throws Exception {
        File root = new File(temp.getRoot(), "stage");
        PrivatePackage.extract(new ByteArrayInputStream(zip(new String[]{"package.properties", "words.tsv", "audio/0001英.btv", "audio/0001訳.btv"},
                new String[]{"format=1\nid=my-book\n", table, "audio-en", "audio-ja"})), root, bytes -> {});
        return root;
    }
    @Test public void preservesOriginalNumbersJapanesePathsAndUnicode() throws Exception {
        File root = extract(words("1\tdog\t犬\taudio/0001英.btv\taudio/0001訳.btv\n5000\tcat\t猫\taudio/0001英.btv\taudio/0001訳.btv"));
        assertEquals("my-book", PrivatePackage.readId(root));
        var entries = PrivatePackage.readWords(root);
        assertEquals(2, entries.size()); assertEquals(5000, entries.get(1).id);
        assertEquals("犬", entries.get(0).japanese);
        assertEquals("audio/0001英.btv", entries.get(0).enAudio);
    }
    @Test(expected = IOException.class) public void rejectsDuplicateIds() throws Exception {
        extract(words("1\tdog\t犬\taudio/0001英.btv\taudio/0001訳.btv\n1\tcat\t猫\taudio/0001英.btv\taudio/0001訳.btv"));
    }
    @Test(expected = IOException.class) public void rejectsMissingAudio() throws Exception {
        extract(words("1\tdog\t犬\taudio/missing.mp3\taudio/0001訳.btv"));
    }
    @Test public void rejectsZipSlipWithoutWritingOutsideStage() throws Exception {
        File root = new File(temp.getRoot(), "stage");
        try { PrivatePackage.extract(new ByteArrayInputStream(zip(new String[]{"../outside"}, new String[]{"secret"})), root, bytes -> {}); fail(); }
        catch (IOException expected) { assertFalse(new File(temp.getRoot(), "outside").exists()); }
    }
    @Test(expected = IOException.class) public void rejectsAudioTraversal() throws Exception {
        extract(words("1\tdog\t犬\taudio/../0001英.btv\taudio/0001訳.btv"));
    }
    @Test(expected = IOException.class) public void cancellationStopsExtraction() throws Exception {
        PrivatePackage.extract(new ByteArrayInputStream(zip(new String[]{"package.properties"}, new String[]{"format=1\nid=test"})),
                new File(temp.getRoot(), "stage"), bytes -> { throw new IOException("cancel"); });
    }
    @Test(expected = IOException.class) public void rejectsWrongHeader() throws Exception { extract("id,english,japanese"); }
    @Test public void streamsOneGiBWithoutAllocatingTheWholeAudio() throws Exception {
        org.junit.Assume.assumeTrue(temp.getRoot().getUsableSpace() > 1200L * 1024 * 1024);
        var out = new ByteArrayOutputStream();
        long size = 1024L * 1024 * 1024;
        try (var zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry("package.properties")); zip.write("format=1\nid=large-test\n".getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("words.tsv")); zip.write(words("0\tdog\t犬\taudio/large.btv\taudio/large.btv\n").getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
            zip.putNextEntry(new ZipEntry("audio/large.btv"));
            byte[] block = new byte[128 * 1024];
            for (long written = 0; written < size; written += block.length) zip.write(block);
            zip.closeEntry();
        }
        File root = new File(temp.getRoot(), "large"); long[] progress = {0};
        PrivatePackage.extract(new ByteArrayInputStream(out.toByteArray()), root, bytes -> progress[0] = bytes);
        assertEquals(size, new File(root, "audio/large.btv").length());
        assertTrue(progress[0] >= size);
        // This synthetic payload tests size/streaming, not audio decoding.
    }

}
