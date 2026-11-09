package com.dipsource.service.demo;

import com.dipsource.dto.demo.DemoEditorSaveRequest;
import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import com.dipsource.service.demo.zip.DemoArchive;
import com.dipsource.service.demo.zip.DemoFileTypes;
import com.dipsource.service.demo.zip.DemoZipValidator;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 코드 편집기의 HTML·CSS·JS를 정적 파일 묶음으로 만든다. 이후는 ZIP 업로드와 같은 저장 경로를 탄다. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoEditorArchiveBuilder {

    private static final Pattern HAS_DOCUMENT = Pattern.compile("<html[\\s>]", Pattern.CASE_INSENSITIVE);
    private static final String STYLE_TAG = "<link rel=\"stylesheet\" href=\"./style.css\">";
    private static final String SCRIPT_TAG = "<script src=\"./script.js\"></script>";

    private final DemoZipValidator zipValidator;
    private final DemoProperties properties;

    public <T> T withArchive(DemoEditorSaveRequest request, Function<DemoArchive, T> action) {
        Path dir = null;
        try {
            dir = Files.createTempDirectory("demo-editor-");
            List<DemoArchive.File> files = new ArrayList<>();
            files.add(write(dir, "index.html", composeHtml(request.html())));
            files.add(write(dir, "style.css", request.css() == null ? "" : request.css()));
            files.add(write(dir, "script.js", request.js() == null ? "" : request.js()));
            long total = files.stream().mapToLong(DemoArchive.File::size).sum();
            if (total > properties.limits().maxTotalBytes()) {
                throw new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE, "코드 전체 크기가 제한을 넘었습니다");
            }
            DemoArchive archive = new DemoArchive(files, total, DemoZipValidator.ENTRY_PATH);
            zipValidator.checkEntryHtml(archive);
            return action.apply(archive);
        } catch (IOException e) {
            log.error("편집기 파일 생성 실패", e);
            throw new BusinessException(ErrorCode.BAD_REQUEST, "코드를 저장하지 못했습니다");
        } finally {
            deleteQuietly(dir);
        }
    }

    /** HTML 조각이면 문서로 감싸고 CSS·JS를 연결한다. */
    static String composeHtml(String html) {
        String document = HAS_DOCUMENT.matcher(html).find()
                ? html
                : "<!doctype html>\n<html lang=\"ko\">\n<head>\n<meta charset=\"utf-8\">\n"
                        + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n<title>데모</title>\n</head>\n<body>\n"
                        + html + "\n</body>\n</html>\n";
        return insertBeforeClosing(insertBeforeClosing(document, "</head>", STYLE_TAG, false), "</body>", SCRIPT_TAG, true);
    }

    private static String insertBeforeClosing(String document, String tag, String content, boolean last) {
        String lower = document.toLowerCase(Locale.ROOT);
        int at = last ? lower.lastIndexOf(tag) : lower.indexOf(tag);
        return at < 0 ? document + "\n" + content : document.substring(0, at) + content + "\n" + document.substring(at);
    }

    private DemoArchive.File write(Path dir, String name, String content) throws IOException {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Path file = Files.write(dir.resolve(name), bytes);
        return new DemoArchive.File(name, file, bytes.length, DemoFileTypes.contentTypeOf(name).orElseThrow());
    }

    private void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> path.toFile().delete());
        } catch (IOException e) {
            log.warn("편집기 임시 폴더 삭제 실패: {}", dir, e);
        }
    }
}
