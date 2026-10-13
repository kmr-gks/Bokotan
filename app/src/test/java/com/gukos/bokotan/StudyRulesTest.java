package com.gukos.bokotan;

import org.junit.Test;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import static org.junit.Assert.*;

public class StudyRulesTest {
    @Test public void tooFewAnswersNeverLoop() {
        for (int size = 0; size < 4; size++) {
            var answers = Arrays.asList("犬", "猫", "牛乳").subList(0, size);
            assertTrue(StudyRules.choices(answers, 0, new Random(0)).isEmpty());
        }
    }
    @Test public void duplicateTranslationsCannotCreateAmbiguousQuiz() {
        assertTrue(StudyRules.choices(Arrays.asList("犬", "犬", "猫", "猫", " ", "null string"), 0, new Random()).isEmpty());
        var answers = Arrays.asList("犬", "犬", "猫", "牛乳", "花", "蝿");
        for (int correct = 0; correct < answers.size(); correct++) {
            for (int seed = 0; seed < 50; seed++) {
                var choices = StudyRules.choices(answers, correct, new Random(seed));
                assertEquals(4, choices.size()); assertTrue(choices.contains(correct));
                var unique = new HashSet<String>();
                for (int index : choices) unique.add(answers.get(index));
                assertEquals(4, unique.size());
            }
        }
    }
    @Test public void fiveWordsIncludingIndexZeroWrapCorrectly() {
        int index = -1;
        for (int expected = 0; expected < 15; expected++) {
            index = StudyRules.nextIndex(5, index, i -> true);
            assertEquals(expected % 5, index);
        }
        assertEquals(-1, StudyRules.nextIndex(5, 4, i -> false));
        assertEquals(-1, StudyRules.nextIndex(0, 0, i -> true));
        assertEquals(0, StudyRules.nextIndex(1, 0, i -> true));
        assertEquals(3, StudyRules.nextIndex(5, 4, i -> i == 3));
    }
    @Test public void bothComparisonsIncludeExactThreshold() {
        for (boolean minimum : new boolean[]{true, false}) {
            assertTrue(StudyRules.matches(2, 2, "seikaisu", minimum, 2));
            assertTrue(StudyRules.matches(2, 2, "huseikai", minimum, 2));
            assertTrue(StudyRules.matches(2, 2, "seikairate", minimum, .5));
        }
        assertFalse(StudyRules.matches(0, 0, "seikairate", true, 0));
        assertTrue(StudyRules.matches(0, 0, "all", true, 999));
    }
    @Test public void pairedReaderPreservesNumberingAndJapanese() throws IOException {
        var pairs = DictionaryText.readPairs(new StringReader("\ndog\ncat\n"), new StringReader("\n犬\n猫\n"));
        assertEquals(3, pairs.size()); assertArrayEquals(new String[]{"", ""}, pairs.get(0));
        assertArrayEquals(new String[]{"cat", "猫"}, pairs.get(2));
        assertTrue(DictionaryText.readPairs(new StringReader(""), new StringReader("")).isEmpty());
    }
    @Test(expected = IOException.class) public void mismatchedEnglishLengthIsRejected() throws IOException {
        DictionaryText.readPairs(new StringReader("dog\ncat"), new StringReader("犬"));
    }
    @Test(expected = IOException.class) public void mismatchedJapaneseLengthIsRejected() throws IOException {
        DictionaryText.readPairs(new StringReader("dog"), new StringReader("犬\n猫"));
    }
}
