| 분류 | 구분 | 테스트 수 | 실패 |
|---|---|---|---|
| 단위 | 기존 | 126 | 0 |
| 통합: Spring 컨텍스트 | 기존 | 1 | 0 |
| 통합: 실제 PostgreSQL | 기존 | 161 | 0 |
| 통합: 실제 PostgreSQL + ES 접속 불가(장애 시뮬레이션) | 기존 | 1 | 0 |
| 통합: 실제 PostgreSQL + ES 접속 불가(장애 시뮬레이션) | 신규 | 4 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | 78 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 신규 | 52 | 0 |

기존 367건, 신규 56건, 합계 423건

| 분류 | 구분 | 클래스 | 테스트 수 | 실패 |
|---|---|---|---|---|
| 단위 | 기존 | dto.search.AutocompletePrefixTest | 17 | 0 |
| 단위 | 기존 | entity.user.UserTagProfileTest | 1 | 0 |
| 단위 | 기존 | global.config.CacheConfigTest | 1 | 0 |
| 단위 | 기존 | service.feed.FeedCommentLikeTest$LikeComment | 4 | 0 |
| 단위 | 기존 | service.feed.FeedCommentLikeTest$UnlikeComment | 3 | 0 |
| 단위 | 기존 | service.feed.FeedEngagementServiceTest$Bookmark | 3 | 0 |
| 단위 | 기존 | service.feed.FeedEngagementServiceTest$Like | 4 | 0 |
| 단위 | 기존 | service.feed.FeedEngagementServiceTest$Unbookmark | 2 | 0 |
| 단위 | 기존 | service.feed.FeedEngagementServiceTest$Unlike | 2 | 0 |
| 단위 | 기존 | service.follow.FollowServiceImplTest$Follow | 4 | 0 |
| 단위 | 기존 | service.follow.FollowServiceImplTest$Lists | 9 | 0 |
| 단위 | 기존 | service.follow.FollowServiceImplTest$MutualFollow | 4 | 0 |
| 단위 | 기존 | service.follow.FollowServiceImplTest$Unfollow | 3 | 0 |
| 단위 | 기존 | service.minihome.impl.GuestbookServiceImplTest | 3 | 0 |
| 단위 | 기존 | service.minihome.impl.MiniHomeCommentServiceImplTest | 6 | 0 |
| 단위 | 기존 | service.minihome.impl.MiniHomeServiceImplTest | 12 | 0 |
| 단위 | 기존 | service.minihome.impl.MiniHomeVisitServiceTest | 4 | 0 |
| 단위 | 기존 | service.news.impl.CachingNewsFeedTest | 2 | 0 |
| 단위 | 기존 | service.news.impl.NewsCategoriesTest | 1 | 0 |
| 단위 | 기존 | service.news.impl.NewsRssParserTest | 5 | 0 |
| 단위 | 기존 | service.news.impl.NewsServiceTest | 4 | 0 |
| 단위 | 기존 | service.search.ContentQualityScorerTest | 5 | 0 |
| 단위 | 기존 | service.search.KeyboardLayoutConverterTest | 15 | 0 |
| 단위 | 기존 | service.search.SearchQueryParserTest | 5 | 0 |
| 단위 | 기존 | service.search.SearchRankerTest | 7 | 0 |
| 통합: Spring 컨텍스트 | 기존 | DipSourceApplicationTests | 1 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.feed.FeedBookmarkApiTest | 5 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.feed.FeedLikeApiTest | 5 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest | 0 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest$Boundary | 10 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest$Hangul | 11 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest$Limitations | 4 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest$Normal | 9 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchAutocompleteControllerTest$Security | 3 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest | 0 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$All | 3 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$BadRequest | 18 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$DemoSearch | 2 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$FeedSearch | 2 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$Limitations | 8 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$MultiKeyword | 9 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$Pagination | 4 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$PostNormal | 12 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$PostVisibility | 4 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$QuerySuggestion | 9 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$Relevance | 10 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$SpecialCharacters | 6 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$TypoCorrection | 19 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchControllerTest$UserSearch | 2 | 0 |
| 통합: 실제 PostgreSQL | 기존 | controller.search.SearchMetricsTest | 6 | 0 |
| 통합: 실제 PostgreSQL + ES 접속 불가(장애 시뮬레이션) | 기존 | search.SearchFallbackTest | 1 | 0 |
| 통합: 실제 PostgreSQL + ES 접속 불가(장애 시뮬레이션) | 신규 | search.SearchOutageVerificationTest | 4 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | search.ElasticsearchSearchTest | 21 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | search.ElasticsearchSearchTest$Analysis | 16 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | search.ElasticsearchSearchTest$Personalization | 10 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | search.ElasticsearchSearchTest$Suggestions | 22 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 기존 | search.SearchSyncTest | 9 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 신규 | search.SearchPaginationVerificationTest | 11 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 신규 | search.SearchPolicyVerificationTest | 12 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 신규 | search.SearchRecoveryVerificationTest | 4 | 0 |
| 통합: 실제 PostgreSQL + 실제 Elasticsearch | 신규 | search.SearchSingleCharTest | 25 | 0 |
