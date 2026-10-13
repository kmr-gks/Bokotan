package com.gukos.bokotan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

final class DictionaryText {
    private DictionaryText() {}

    /** 空行を保持して音声の番号を保つ。行数の不一致は黙って切り捨てない。 */
    static List<String[]> readPairs(Reader english, Reader japanese) throws IOException {
        var result = new ArrayList<String[]>();
        var e = new BufferedReader(english);
        var j = new BufferedReader(japanese);
        while (true) {
            String lineE = e.readLine(), lineJ = j.readLine();
            if (lineE == null && lineJ == null) return result;
            if (lineE == null || lineJ == null) throw new IOException("英日辞書の行数が一致しません。");
            result.add(new String[]{lineE, lineJ});
        }
    }
}
