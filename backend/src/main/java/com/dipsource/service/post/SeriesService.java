package com.dipsource.service.post;

import com.dipsource.dto.post.MySeriesPurchaseResponse;
import com.dipsource.dto.post.SeriesDetailResponse;
import com.dipsource.dto.post.SeriesIdResponse;
import com.dipsource.dto.post.SeriesSummaryResponse;
import com.dipsource.dto.post.SeriesWriteRequest;
import java.util.List;

/**
 * 블로그 연재 시리즈. 시리즈는 현금으로 한 번 사면 이후 회차까지 계속 읽는다 (인프런 방식).
 * 회차는 글쓰기에서 시리즈를 고르면 붙는다 ({@link PostService}).
 */
public interface SeriesService {

    /** 시리즈 만들기. 활동 중인 회원만 */
    SeriesIdResponse create(Long userId, SeriesWriteRequest request);

    /** 시리즈 수정. 내 시리즈가 아니면 403. 유료 회차가 있으면 0원으로 바꿀 수 없다 (400) */
    void update(Long userId, Long seriesId, SeriesWriteRequest request);

    /** 시리즈 삭제. 회차가 남아 있거나 산 사람이 있으면 400 */
    void delete(Long userId, Long seriesId);

    /** 시리즈 화면. 없거나 차단 관계면 404. viewerId는 비로그인이면 null */
    SeriesDetailResponse getSeries(Long seriesId, Long viewerId);

    /** 작가의 시리즈 목록. 최근에 만든 순. 차단 관계면 빈 목록 */
    List<SeriesSummaryResponse> getUserSeries(Long userId, Long viewerId);

    /** 인기 시리즈 (구매 많은 순) */
    List<SeriesSummaryResponse> getPopular(Long viewerId, int size);

    /**
     * 시리즈 구매. 현금 결제 기록을 남기고 바로 열어 준다. 이미 샀으면 아무것도 하지 않는다 (멱등).
     * 내 시리즈·무료 시리즈는 400, 없거나 차단 관계면 404
     */
    void purchase(Long userId, Long seriesId);

    /** 내가 산 시리즈. 최근에 산 순. 차단 관계인 작가의 시리즈는 숨긴다 */
    List<MySeriesPurchaseResponse> getMyPurchases(Long userId);
}
