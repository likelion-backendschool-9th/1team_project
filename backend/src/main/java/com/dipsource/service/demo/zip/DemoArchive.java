package com.dipsource.service.demo.zip;

import java.nio.file.Path;
import java.util.List;

/**
 * 검증을 통과하고 임시 폴더에 풀린 ZIP.
 *
 * @param files      실제 파일 목록
 * @param totalBytes 실제로 푼 크기의 합
 * @param entryPath  진입 파일 경로
 */
public record DemoArchive(List<File> files, long totalBytes, String entryPath) {

    /**
     * @param path        ZIP 안의 경로 (검증·정규화 완료)
     * @param tempFile    임시 파일. ZIP 안의 이름과 무관한 일련번호 이름이다
     * @param size        실제 크기
     * @param contentType 확장자로 정한 MIME 타입
     */
    public record File(String path, Path tempFile, long size, String contentType) {
    }
}
