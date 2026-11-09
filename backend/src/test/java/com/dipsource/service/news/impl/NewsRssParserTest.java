package com.dipsource.service.news.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class NewsRssParserTest {

    private final NewsRssParser parser = new NewsRssParser();

    @Test
    void parsesRssWithoutCallingNetwork() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                  <channel>
                    <title>GeekNews</title>
                    <item>
                      <title>오래된 React 소식</title>
                      <link>https://news.hada.io/topic?id=1</link>
                      <description><![CDATA[<p>Hello <b>React</b> world &amp; friends</p>]]></description>
                      <pubDate>Mon, 02 Nov 2026 01:00:00 GMT</pubDate>
                    </item>
                    <item>
                      <title></title>
                      <link>https://news.hada.io/topic?id=skip</link>
                      <description>제목 없음</description>
                    </item>
                    <item>
                      <title>최신 Spring Boot 릴리스</title>
                      <link>https://news.hada.io/topic?id=2</link>
                      <description>%s</description>
                      <pubDate>Tue, 03 Nov 2026 01:00:00 GMT</pubDate>
                    </item>
                  </channel>
                </rss>
                """.formatted("가".repeat(130));

        var items = parser.parse(xml);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).title()).isEqualTo("최신 Spring Boot 릴리스");
        assertThat(items.get(0).category()).isEqualTo("backend");
        assertThat(items.get(0).source()).isEqualTo("GeekNews");
        assertThat(items.get(0).summary()).hasSize(NewsRssParser.SUMMARY_LENGTH);
        assertThat(items.get(0).publishedAt()).isEqualTo(OffsetDateTime.parse("2026-11-03T10:00:00+09:00"));
        assertThat(items.get(1).summary()).isEqualTo("Hello React world & friends");
        assertThat(items.get(1).category()).isEqualTo("frontend");
    }

    @Test
    void classifiesJavascriptAsFrontend() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <rss version="2.0">
                  <channel>
                    <item>
                      <title>JavaScript 런타임 비교</title>
                      <link>https://news.hada.io/topic?id=3</link>
                      <description>plain text</description>
                    </item>
                  </channel>
                </rss>
                """;

        assertThat(parser.parse(xml).get(0).category()).isEqualTo("frontend");
    }

    @Test
    void parsesAtom() {
        String xml = """
                <?xml version="1.0" encoding="UTF-8" ?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <title>GeekNews</title>
                  <entry>
                    <title>Unity 6 WebGPU</title>
                    <link href="https://news.hada.io/topic?id=4"/>
                    <updated>2026-11-03T01:00:00Z</updated>
                    <summary>브라우저 게임</summary>
                  </entry>
                </feed>
                """;

        var items = parser.parse(xml);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).url()).isEqualTo("https://news.hada.io/topic?id=4");
        assertThat(items.get(0).category()).isEqualTo("game");
        assertThat(items.get(0).summary()).isEqualTo("브라우저 게임");
    }

    @Test
    void usesHtmlContentWhenGeekNewsAtomHasNoDescription() {
        String xml = """
                <?xml version='1.0' encoding='UTF-8'?>
                <feed xmlns='http://www.w3.org/2005/Atom'>
                <title>GeekNews - 개발/기술/스타트업 뉴스 서비스</title>
                <id>https://news.hada.io/rss/news</id>
                <updated>2026-11-03T15:32:47+09:00</updated>
                <entry>
                  <title><![CDATA[Claude 에이전트로 GPU 클러스터 운영하기]]></title>
                  <link rel='alternate' type='text/html' href='https://news.hada.io/topic?id=5&amp;utm_source=geeknews' />
                  <id>https://news.hada.io/topic?id=5</id>
                  <updated>2026-11-03T15:32:47+09:00</updated>
                  <published>2026-11-03T15:32:47+09:00</published>
                  <author>
                    <name>neo</name>
                  </author>
                  <content type='html' xml:lang='ko'><![CDATA[<div><ul><li><strong>운영 자동화</strong>를 다룬 글 &amp; 사례</li><li>%s</li></ul></div>]]></content>
                </entry>
                </feed>
                """.formatted("나".repeat(130));

        var items = parser.parse(xml);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).url()).isEqualTo("https://news.hada.io/topic?id=5&utm_source=geeknews");
        assertThat(items.get(0).summary()).startsWith("운영 자동화를 다룬 글 & 사례 나");
        assertThat(items.get(0).summary()).hasSize(NewsRssParser.SUMMARY_LENGTH);
        assertThat(items.get(0).summary()).doesNotContain("<", ">");
        assertThat(items.get(0).category()).isEqualTo("ai");
        assertThat(items.get(0).publishedAt()).isEqualTo(OffsetDateTime.parse("2026-11-03T15:32:47+09:00"));
    }

    @Test
    void joinsInlineTagsAndSpacesBlockTags() {
        assertThat(NewsRssParser.clean("<p>Next<code>.js</code> 16 출시</p><p><a href='x'>링크</a>에서 확인</p>"))
                .isEqualTo("Next.js 16 출시 링크에서 확인");
        assertThat(NewsRssParser.clean("첫 줄<br/>둘째 줄<li>항목</li>"))
                .isEqualTo("첫 줄 둘째 줄 항목");
    }

    @Test
    void rejectsBrokenXml() {
        assertThatThrownBy(() -> parser.parse("<rss>"))
                .isInstanceOf(IllegalStateException.class);
    }
}
