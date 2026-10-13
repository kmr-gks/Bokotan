package com.gukos.bokotan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.IntPredicate;

/** Androidから独立した出題・再生の境界条件。元の辞書内番号は変更しない。 */
final class StudyRules {
    private StudyRules() {}

    static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty() && !value.equals("null string");
    }

    static boolean matches(int correct, int incorrect, String condition,
                           boolean minimum, double threshold) {
        double value;
        switch (condition) {
            case "all": return true;
            case "seikaisu": value = correct; break;
            case "huseikai": value = incorrect; break;
            case "seikairate":
                value = correct + incorrect == 0 ? -1 : (double) correct / (correct + incorrect);
                break;
            default: throw new IllegalArgumentException("Unknown condition: " + condition);
        }
        return minimum ? value >= threshold : value <= threshold;
    }

    /** 全件を一周だけ調べる。条件に合う語がなければ-1。 */
    static int nextIndex(int size, int current, IntPredicate eligible) {
        if (size <= 0) return -1;
        for (int offset = 1; offset <= size; offset++) {
            int index = Math.floorMod(current + offset, size);
            if (eligible.test(index)) return index;
        }
        return -1;
    }

    /** 同じ日本語訳を複数の選択肢に入れない。正解の位置は最後にシャッフルする。 */
    static List<Integer> choices(List<String> answers, int correct, Random random) {
        if (correct < 0 || correct >= answers.size() || !hasText(answers.get(correct))) {
            return Collections.emptyList();
        }
        var candidates = new ArrayList<Integer>();
        for (int i = 0; i < answers.size(); i++) candidates.add(i);
        Collections.shuffle(candidates, random);
        var seen = new HashSet<String>();
        var result = new ArrayList<Integer>();
        result.add(correct);
        seen.add(answers.get(correct).trim());
        for (int i : candidates) {
            String answer = answers.get(i);
            if (hasText(answer) && seen.add(answer.trim())) result.add(i);
            if (result.size() == 4) break;
        }
        if (result.size() != 4) return Collections.emptyList();
        Collections.shuffle(result, random);
        return result;
    }
}
