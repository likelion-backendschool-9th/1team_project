package com.dipsource.dto.demo;

import java.time.LocalDateTime;

/** 임시 미리보기. url은 expiresAt까지만 열린다. */
public record DemoPreviewResponse(String url, LocalDateTime expiresAt) {
}
