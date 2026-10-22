package com.gukos.bokotan;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SearchHighlightInstrumentedTest {
    private static ForegroundColorSpan onlySpan(Spanned text) {
        var spans = text.getSpans(0, text.length(), ForegroundColorSpan.class);
        assertEquals(1, spans.length);
        return spans[0];
    }
    private static void assertNoSpans(Spanned text) {
        assertEquals(0, text.getSpans(0, text.length(), Object.class).length);
    }
    @Test public void markupIsShownLiterallyAndOnlyMatchIsRed() {
        String source = "<b>x</b> &amp; Apple\napple";
        SpannableString text = SearchHighlight.highlight(source, "apple");
        assertEquals(source, text.toString());
        var span = onlySpan(text);
        assertEquals(15, text.getSpanStart(span));
        assertEquals(20, text.getSpanEnd(span));
        assertEquals(Color.RED, span.getForegroundColor());
    }
    @Test public void uppercaseSourceIsHighlighted() {
        String source = "12 : ABANDON";
        SpannableString text = SearchHighlight.highlight(source, "abandon");
        assertEquals(source, text.toString());
        var span = onlySpan(text);
        assertEquals(5, text.getSpanStart(span));
        assertEquals(12, text.getSpanEnd(span));
    }
    @Test public void missingOrEmptyKeyLeavesPlainText() {
        String source = "<i>犬</i>\n&lt;";
        for (String key : new String[]{null, "", "cat"}) {
            SpannableString text = SearchHighlight.highlight(source, key);
            assertEquals(source, text.toString());
            assertNoSpans(text);
        }
    }
}
