package com.dipsource.service.search.index;

import org.springframework.stereotype.Component;

/** 색인 시점의 콘텐츠 품질 계수(0~1). 1이면 감점 없음. 2단계에서 스팸 감지를 넣는다. */
@Component
public class ContentQualityScorer {

    public float score(String text) {
        return 1.0f;
    }
}
