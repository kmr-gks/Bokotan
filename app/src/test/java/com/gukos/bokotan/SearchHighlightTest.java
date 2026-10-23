package com.gukos.bokotan;

import org.junit.Test;
import static org.junit.Assert.*;

public class SearchHighlightTest {
    @Test public void matchIgnoresCase() {
        assertEquals(4, SearchHighlight.indexOfIgnoreCase("big apple pie", "apple"));
        assertEquals(4, SearchHighlight.indexOfIgnoreCase("big apple pie", "APPLE"));
    }
    @Test public void uppercaseSourceIsMatchedAtOriginalIndex() {
        assertEquals(0, SearchHighlight.indexOfIgnoreCase("Apple", "apple"));
        assertEquals(6, SearchHighlight.indexOfIgnoreCase("1 : I APPLE", "apple"));
    }
    @Test public void indexIsNotShiftedByCaseFoldingThatChangesLength() {
        // "İ".toLowerCase(Locale.ROOT) has two chars, so lower-casing the source would return 2.
        assertEquals(1, SearchHighlight.indexOfIgnoreCase("İab", "ab"));
    }
    @Test public void returnsFirstOccurrence() {
        assertEquals(2, SearchHighlight.indexOfIgnoreCase("a Cat cat", "cat"));
    }
    @Test public void noMatchReturnsMinusOne() {
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("dog", "cat"));
    }
    @Test public void nullOrEmptyKeyReturnsMinusOne() {
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("dog", null));
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("dog", ""));
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase(null, "dog"));
    }
    @Test public void htmlLikeCharactersAreOrdinaryText() {
        String source = "<b>x</b> &amp;";
        assertEquals(0, SearchHighlight.indexOfIgnoreCase(source, "<b>"));
        assertEquals(3, SearchHighlight.indexOfIgnoreCase(source, "x"));
        assertEquals(9, SearchHighlight.indexOfIgnoreCase(source, "&AMP;"));
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase(source, "&lt;"));
    }
    @Test public void matchAtEnd() {
        assertEquals(6, SearchHighlight.indexOfIgnoreCase("hello WORLD", "world"));
        assertEquals(10, SearchHighlight.indexOfIgnoreCase("hello WORLD", "d"));
    }
    @Test public void japaneseText() {
        assertEquals(4, SearchHighlight.indexOfIgnoreCase("日本語:犬と猫", "犬"));
        assertEquals(6, SearchHighlight.indexOfIgnoreCase("日本語:犬と猫", "猫"));
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("日本語:犬と猫", "牛"));
    }
    @Test public void keyLongerThanSourceReturnsMinusOne() {
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("cat", "cats"));
        assertEquals(-1, SearchHighlight.indexOfIgnoreCase("", "a"));
    }
}
