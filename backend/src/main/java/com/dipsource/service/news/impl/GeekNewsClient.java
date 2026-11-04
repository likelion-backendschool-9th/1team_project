package com.dipsource.service.news.impl;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** GeekNews RSS를 5초 안에 받아 파서에 넘긴다. */
@Component
public class GeekNewsClient {

    static final String FEED_URL = "https://news.hada.io/rss/news";

    private final RestClient restClient;
    private final NewsRssParser parser;

    public GeekNewsClient(NewsRssParser parser) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
        this.parser = parser;
    }

    public List<NewsItemDraft> fetch() {
        byte[] body = restClient.get()
                .uri(FEED_URL)
                .retrieve()
                .body(byte[].class);
        if (body == null || body.length == 0) {
            throw new IllegalStateException("RSS 본문이 비어 있습니다");
        }
        return parser.parse(new ByteArrayInputStream(body));
    }
}
