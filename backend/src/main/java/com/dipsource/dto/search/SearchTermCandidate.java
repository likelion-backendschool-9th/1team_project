package com.dipsource.dto.search;

import java.util.Comparator;

/**
 * 오타 교정 후보. 공개된 제목·태그에서 뽑은 단어 하나와, 검색 단어와의 거리.
 * native 쿼리 결과를 받는 인터페이스 projection이다.
 */
public interface SearchTermCandidate {

    /** 가장 좋은 후보가 앞에 온다: 검색 단어를 포함 → 편집 거리 짧음 → 등장 횟수 많음 → trigram 유사도 높음 → 사전순 */
    Comparator<SearchTermCandidate> BEST_FIRST = Comparator
            .comparing(SearchTermCandidate::getContained).reversed()
            .thenComparing(SearchTermCandidate::getDistance)
            .thenComparing(SearchTermCandidate::getFrequency, Comparator.reverseOrder())
            .thenComparing(SearchTermCandidate::getSimilarity, Comparator.reverseOrder())
            .thenComparing(SearchTermCandidate::getTerm);

    /** 사전 단어(소문자) */
    String getTerm();

    /** 사전 단어가 검색 단어를 그대로 포함하는지. true면 오타가 아니다(LIKE로 이미 찾힌다) */
    Boolean getContained();

    /** 편집 거리(levenshtein). 글자를 몇 번 넣고·빼고·바꿔야 같아지는지 */
    Integer getDistance();

    /** 사전 출처(제목·태그)에 이 단어가 나온 횟수. 거리가 같을 때 바른 철자를 고르는 데 쓴다 */
    Long getFrequency();

    /** trigram 유사도(0~1). 거리와 등장 횟수가 같을 때 순서를 정한다 */
    Double getSimilarity();
}
