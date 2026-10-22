package com.gukos.bokotan;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

/** 検索語の強調表示。辞書の文字列はHTMLとして解釈せず、そのまま表示する。 */
final class SearchHighlight {
    private SearchHighlight() {}

    /** 大文字小文字を区別せず最初に一致した元の文字列上の位置。キーが空か一致しなければ-1。 */
    static int indexOfIgnoreCase(String source, String key) {
        if (source == null || key == null || key.isEmpty()) return -1;
        for (int index = 0, last = source.length() - key.length(); index <= last; index++) {
            if (source.regionMatches(true, index, key, 0, key.length())) return index;
        }
        return -1;
    }

    static SpannableString highlight(String source, String key) {
        var text = new SpannableString(source == null ? "" : source);
        int index = indexOfIgnoreCase(source, key);
        if (index >= 0) {
            text.setSpan(new ForegroundColorSpan(Color.RED), index, index + key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return text;
    }
}
