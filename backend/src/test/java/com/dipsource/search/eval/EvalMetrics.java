package com.dipsource.search.eval;

import java.util.List;
import java.util.Map;

/** 검색 품질 지표. 평가 등급은 0~3이고, 2 이상을 "관련 있음"으로 본다. */
final class EvalMetrics {

    static final int RELEVANT_GRADE = 2;

    private EvalMetrics() {
    }

    /** 상위 k건 중 관련 문서 비율. 정답이 k건보다 적어도 분모는 k다 */
    static double precisionAt(List<String> ranked, Map<String, Integer> grades, int k) {
        long hits = ranked.stream().limit(k).filter(key -> grades.getOrDefault(key, 0) >= RELEVANT_GRADE).count();
        return (double) hits / k;
    }

    /** 첫 관련 문서 순위의 역수. 없으면 0 */
    static double reciprocalRank(List<String> ranked, Map<String, Integer> grades) {
        for (int i = 0; i < ranked.size(); i++) {
            if (grades.getOrDefault(ranked.get(i), 0) >= RELEVANT_GRADE) {
                return 1.0 / (i + 1);
            }
        }
        return 0;
    }

    /** 이득 2^등급 - 1, 할인 log2(순위 + 1)인 NDCG. 정답이 없으면 0 */
    static double ndcgAt(List<String> ranked, Map<String, Integer> grades, int k) {
        double dcg = 0;
        for (int i = 0; i < Math.min(k, ranked.size()); i++) {
            dcg += gain(grades.getOrDefault(ranked.get(i), 0), i);
        }
        List<Integer> ideal = grades.values().stream().sorted((a, b) -> b - a).limit(k).toList();
        double idcg = 0;
        for (int i = 0; i < ideal.size(); i++) {
            idcg += gain(ideal.get(i), i);
        }
        return idcg == 0 ? 0 : dcg / idcg;
    }

    private static double gain(int grade, int index) {
        return (Math.pow(2, grade) - 1) / (Math.log(index + 2) / Math.log(2));
    }
}
