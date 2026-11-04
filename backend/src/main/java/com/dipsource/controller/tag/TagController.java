package com.dipsource.controller.tag;

import com.dipsource.entity.tag.Tag;
import com.dipsource.global.common.ApiResponse;
import com.dipsource.repository.tag.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 태그 조회 API */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagRepository tagRepository;

    /** 전체 태그 목록 조회 */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> getAll() {
        List<Tag> tags = tagRepository.findAll();
        return ResponseEntity.ok(ApiResponse.of(tags));
    }
}
