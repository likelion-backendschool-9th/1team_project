package com.dipsource.global.config;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 통합 검색 설정을 한곳에서 관리한다. 가중치와 랭킹 정책은 코드에 흩어 놓지 않고 여기서 바꾼다.
 * 기본값은 평가셋(docs/search-eval)으로 비교해 고른 초기 실험값이며 확정값이 아니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "search")
public class SearchProperties {

    /** 읽기 경로 */
    private Engine engine = Engine.POSTGRES;
    /** Elasticsearch 검색이 실패하면 PostgreSQL 검색으로 대체할지 */
    private boolean fallbackToPostgres = true;
    /** 실패 후 이 시간 동안은 Elasticsearch를 호출하지 않고 바로 대체한다. 장애 중 타임아웃 대기가 쌓이는 것을 막는다 */
    private Duration circuitOpenDuration = Duration.ofSeconds(30);

    private Elasticsearch elasticsearch = new Elasticsearch();
    private Analysis analysis = new Analysis();
    private Ranking ranking = new Ranking();
    private Personalization personalization = new Personalization();

    public enum Engine {
        POSTGRES, ELASTICSEARCH
    }

    @Getter
    @Setter
    public static class Elasticsearch {
        /** 색인·동기화 구성요소를 켠다. 읽기 경로(engine)와 별개라 전환 전에 미리 색인할 수 있다 */
        private boolean enabled = false;
        private String indexPrefix = "dipsource";
        private int shards = 1;
        private int replicas = 0;
        private int bulkSize = 500;
        /** 서버가 뜰 때 인덱스가 비어 있으면 자동으로 초기 색인한다 */
        private boolean bootstrapOnEmpty = true;
    }

    @Getter
    @Setter
    public static class Analysis {
        /** ko/en/ja/zh 언어별 서브필드를 검색에 쓴다 */
        private boolean languageFields = true;
        /** C++, Node.js 같은 기술 용어를 분석 전에 보존한다 */
        private boolean techTerms = true;
        /** 사전의 동의어로 검색어를 넓힌다(원문 일치보다 낮은 점수) */
        private boolean synonyms = true;
    }

    @Getter
    @Setter
    public static class Ranking {
        /** true면 필드 가중치·구문·정확 일치·재정렬을 끄고 순수 BM25로만 정렬한다(비교 실험용) */
        private boolean flat = false;
        /** 2차 재정렬 후보 수. 페이지를 넘겨도 이 범위 안에서는 순위가 일관된다 */
        private int window = 200;
        /** 재정렬 결과를 재사용하는 시간. 페이지를 넘기는 동안 순서가 흔들리지 않게 한다 */
        private Duration windowCacheTtl = Duration.ofSeconds(60);
        private int windowCacheSize = 10_000;
        /** 검색어 안의 단어끼리 다른 필드 점수를 얼마나 더할지(dis_max tie_breaker) */
        private float tieBreaker = 0.3f;
        /** 긴 문장 검색(auto)에서 필요한 단어 일치 비율 */
        private String naturalMinimumShouldMatch = "75%";
        /** auto 모드에서 이 단어 수 이상이면 문장으로 본다 */
        private int naturalMinKeywords = 3;
        /** 동의어 일치의 상대 가중치. 원문 일치(1.0)보다 낮아야 한다 */
        private float synonymBoost = 0.3f;
        private float maxSpamPenalty = 0.7f;

        /** 재정렬로 올릴 수 있는 최소 관련도(1위 대비 비율). 이보다 낮은 문서는 인기도를 받지 못한다 */
        private double minRelevanceForBoost = 0.25;

        private TypePolicy post = TypePolicy.of(fields("title", 5f, "content", 1f, "tags_text", 1f),
                "tags", 1.5f, "title_norm", "title", new Popularity(0.45, 0.25, 0.2, 0.1, 0.15));
        private TypePolicy demo = TypePolicy.of(fields("title", 5f, "description", 1f, "tags_text", 1.5f),
                "tags", 4f, "title_norm", "title", new Popularity(0.45, 0.25, 0.2, 0.1, 0.15));
        private TypePolicy feed = TypePolicy.of(fields("content", 3f, "tags_text", 1f, "demo_text", 0.5f),
                "tags", 2f, null, "content", new Popularity(0.5, 0, 0.3, 0.2, 0.15));
        private TypePolicy user = TypePolicy.of(
                fields("nickname", 8f, "nickname.ngram", 3f, "bio", 1f, "tech_text", 2f, "github_username", 2f),
                "tech_stacks", 3f, "nickname_norm", "nickname", new Popularity(0, 0, 1, 0, 0.15));

        public TypePolicy policy(String type) {
            return switch (type) {
                case "post" -> post;
                case "demo" -> demo;
                case "feed" -> feed;
                case "user" -> user;
                default -> throw new IllegalArgumentException("알 수 없는 검색 대상: " + type);
            };
        }

        private static Map<String, Float> fields(Object... pairs) {
            Map<String, Float> fields = new LinkedHashMap<>();
            for (int i = 0; i < pairs.length; i += 2) {
                fields.put((String) pairs[i], (Float) pairs[i + 1]);
            }
            return fields;
        }
    }

    @Getter
    @Setter
    public static class Personalization {
        private boolean enabled = true;
        /** 개인화 가산을 받을 수 있는 최소 상대 관련도. 관련 없는 문서가 개인화로 올라오지 않게 한다 */
        private double minRelevance = 0.5;
        /** 내 팔로잉이 그 사람을 팔로우한 수에 따른 가산의 상한 */
        private double maxMutualBoost = 0.25;
        private double mutualSaturation = 2;
        /** 방문 기록 가산의 상한 */
        private double maxVisitBoost = 0.1;
        private int minVisits = 3;
        /** 이 기간 안의 방문만 쓴다 */
        private Duration visitRecency = Duration.ofDays(90);
        private double visitHalfLifeDays = 30;
        /** 지금 검색어와 방문 때 검색어의 단어 겹침(Jaccard)이 이 값 이상이어야 한다 */
        private double minQuerySimilarity = 0.5;
        /** 이 시간 안의 재방문은 세지 않는다 */
        private Duration visitDedupWindow = Duration.ofMinutes(30);
        /** 이 기간이 지난 방문 기록은 지운다 */
        private Duration visitRetention = Duration.ofDays(180);
        /** 검색 제외에 쓰는 숨긴 항목 수. 쿼리 크기를 제한한다 */
        private int maxHiddenFilter = 1000;
    }

    @Getter
    @Setter
    public static class TypePolicy {
        /** 분석 필드와 BM25 가중치 */
        private Map<String, Float> fields = new LinkedHashMap<>();
        /** 정확 일치용 keyword 필드 */
        private String keywordField;
        private float keywordBoost = 1;
        /** 제목·닉네임 전체 일치용 정규화 필드. 없으면 null */
        private String exactField;
        private float exactBoost = 50;
        /** 구문 일치를 볼 필드 */
        private String phraseField;
        private float phraseBoost = 3;
        private Popularity popularity;

        static TypePolicy of(Map<String, Float> fields, String keywordField, float keywordBoost,
                             String exactField, String phraseField, Popularity popularity) {
            TypePolicy policy = new TypePolicy();
            policy.fields = new LinkedHashMap<>(fields);
            policy.keywordField = keywordField;
            policy.keywordBoost = keywordBoost;
            policy.exactField = exactField;
            policy.phraseField = phraseField;
            policy.popularity = popularity;
            return policy;
        }
    }

    /**
     * 2차 재정렬의 인기도 정책. 각 신호는 포화 함수로 0~1이 되고, 가중 합에 maxBoost를 곱한 만큼만 점수를 올린다.
     * 그래서 상대 점수 차이가 maxBoost보다 큰 문서는 인기도로 뒤집히지 않는다.
     */
    @Getter
    @Setter
    public static class Popularity {
        private double likeWeight;
        private double viewWeight;
        private double followerWeight;
        private double recencyWeight;
        private double maxBoost;
        /** 좋아요가 이 값이면 신호가 0.5다. 아무리 커져도 1을 넘지 않는다 */
        private double likeSaturation = 30;
        private double viewSaturation = 1000;
        private double followerSaturation = 100;
        private double recencyHalfLifeDays = 30;

        public Popularity() {
        }

        public Popularity(double likeWeight, double viewWeight, double followerWeight, double recencyWeight,
                          double maxBoost) {
            this.likeWeight = likeWeight;
            this.viewWeight = viewWeight;
            this.followerWeight = followerWeight;
            this.recencyWeight = recencyWeight;
            this.maxBoost = maxBoost;
        }
    }
}
