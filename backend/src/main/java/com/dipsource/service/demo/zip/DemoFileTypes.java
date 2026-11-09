package com.dipsource.service.demo.zip;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** 데모에 넣을 수 있는 파일 형식과 MIME 타입. Worker의 MIME_TYPES와 같은 목록을 유지한다. */
public final class DemoFileTypes {

    private static final Map<String, String> MIME_TYPES = Map.ofEntries(
            Map.entry("html", "text/html; charset=utf-8"),
            Map.entry("htm", "text/html; charset=utf-8"),
            Map.entry("css", "text/css; charset=utf-8"),
            Map.entry("js", "text/javascript; charset=utf-8"),
            Map.entry("mjs", "text/javascript; charset=utf-8"),
            Map.entry("json", "application/json; charset=utf-8"),
            Map.entry("map", "application/json; charset=utf-8"),
            Map.entry("txt", "text/plain; charset=utf-8"),
            Map.entry("png", "image/png"),
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("gif", "image/gif"),
            Map.entry("svg", "image/svg+xml"),
            Map.entry("webp", "image/webp"),
            Map.entry("avif", "image/avif"),
            Map.entry("ico", "image/x-icon"),
            Map.entry("woff", "font/woff"),
            Map.entry("woff2", "font/woff2"),
            Map.entry("ttf", "font/ttf"),
            Map.entry("otf", "font/otf"));

    private DemoFileTypes() {
    }

    public static Optional<String> contentTypeOf(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) {
            return Optional.empty();
        }
        return Optional.ofNullable(MIME_TYPES.get(path.substring(dot + 1).toLowerCase(Locale.ROOT)));
    }
}
