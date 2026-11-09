package com.dipsource.service.demo.zip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class DemoZipValidatorTest {

    private static final String HTML = "<!doctype html><html><body>hi</body></html>";
    private static final int CENTRAL_SIGNATURE = 0x02014b50;

    @TempDir
    Path temp;

    private Path target;
    private DemoZipValidator validator;

    @BeforeEach
    void setUp() throws IOException {
        target = Files.createDirectory(temp.resolve("out"));
        validator = validatorWith(new DemoProperties.Limits(10 * 1024 * 1024, 30 * 1024 * 1024, 10 * 1024 * 1024, 200));
    }

    private DemoZipValidator validatorWith(DemoProperties.Limits limits) {
        return new DemoZipValidator(new DemoProperties("bucket", "http://worker", limits));
    }

    private static byte[] zip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, byte[]> file : files.entrySet()) {
                out.putNextEntry(new ZipEntry(file.getKey()));
                out.write(file.getValue());
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static Map<String, byte[]> withIndex(String name, String content) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("index.html", HTML.getBytes(StandardCharsets.UTF_8));
        files.put(name, content.getBytes(StandardCharsets.UTF_8));
        return files;
    }

    private Path write(byte[] bytes) throws IOException {
        return Files.write(temp.resolve("upload.zip"), bytes);
    }

    private DemoArchive extract(byte[] bytes) throws IOException {
        return validator.extract(write(bytes), target);
    }

    private void assertRejected(byte[] bytes, ErrorCode code) {
        assertThatThrownBy(() -> extract(bytes))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(code);
    }

    /** n번째 중앙 디렉터리 헤더를 고친다. */
    private static byte[] patchCentral(byte[] zip, int entryIndex, java.util.function.BiConsumer<ByteBuffer, Integer> patch) {
        byte[] copy = Arrays.copyOf(zip, zip.length);
        ByteBuffer buffer = ByteBuffer.wrap(copy).order(ByteOrder.LITTLE_ENDIAN);
        int seen = 0;
        for (int i = 0; i + 4 <= copy.length; i++) {
            if (buffer.getInt(i) == CENTRAL_SIGNATURE && seen++ == entryIndex) {
                patch.accept(buffer, i);
                return copy;
            }
        }
        throw new IllegalArgumentException("헤더를 찾지 못함");
    }

    @Test
    @DisplayName("정상 ZIP을 풀고 파일 정보를 돌려준다")
    void extractsValidZip() throws IOException {
        Map<String, byte[]> files = withIndex("assets/app.js", "console.log(1)");
        files.put("assets/logo.png", new byte[] {1, 2, 3});

        DemoArchive archive = extract(zip(files));

        assertThat(archive.entryPath()).isEqualTo("index.html");
        assertThat(archive.files()).extracting(DemoArchive.File::path)
                .containsExactly("index.html", "assets/app.js", "assets/logo.png");
        assertThat(archive.totalBytes()).isEqualTo(HTML.length() + 14 + 3);
        assertThat(archive.files().get(1).contentType()).startsWith("text/javascript");
    }

    @Test
    @DisplayName("임시 파일은 ZIP 안의 이름이 아니라 일련번호로 저장한다")
    void tempFilesUseSequentialNames() throws IOException {
        DemoArchive archive = extract(zip(withIndex("deep/app.js", "1")));

        assertThat(archive.files()).allSatisfy(file -> assertThat(file.tempFile().getParent()).isEqualTo(target));
        try (Stream<Path> listed = Files.list(target)) {
            assertThat(listed.map(p -> p.getFileName().toString())).containsExactlyInAnyOrder("f0", "f1");
        }
    }

    @Test
    @DisplayName("macOS 압축 잔여물은 오류 없이 건너뛴다")
    void skipsOsJunk() throws IOException {
        Map<String, byte[]> files = withIndex("__MACOSX/._index.html", "x");
        files.put(".DS_Store", new byte[] {0});
        files.put("assets/.DS_Store", new byte[] {0});

        assertThat(extract(zip(files)).files()).extracting(DemoArchive.File::path).containsExactly("index.html");
    }

    @Test
    @DisplayName("폴더 항목은 파일로 치지 않는다")
    void directoryEntriesAreSkipped() throws IOException {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("assets/", new byte[0]);
        files.put("index.html", HTML.getBytes(StandardCharsets.UTF_8));

        assertThat(extract(zip(files)).files()).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../evil.js", "a/../../evil.js", "/etc/passwd.js", "C:/evil.js", "a\\..\\evil.js",
            "a//b.js", "./a.js", "a/./b.js", "a%2e.js", "a?.js", "a#b.js"
    })
    @DisplayName("ZIP Slip과 경로 조작을 거부한다")
    void rejectsPathTraversal(String name) throws IOException {
        assertRejected(zip(withIndex(name, "1")), ErrorCode.BAD_REQUEST);
        try (Stream<Path> listed = Files.list(temp.getParent() == null ? temp : temp)) {
            assertThat(listed.map(p -> p.getFileName().toString())).doesNotContain("evil.js");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"run.exe", "server.php", "a.sh", "lib.jar", "noext", "inner.zip", ".env", "assets/.htaccess"})
    @DisplayName("허용하지 않는 파일 형식과 숨김 파일을 거부한다")
    void rejectsDisallowedTypes(String name) throws IOException {
        assertRejected(zip(withIndex(name, "1")), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("__mock 최상위 폴더는 예약되어 있다")
    void rejectsReservedRoot() throws IOException {
        assertRejected(zip(withIndex("__mock/a.json", "{}")), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("최상위 index.html이 없으면 거부한다")
    void requiresRootIndex() throws IOException {
        assertRejected(zip(Map.of("dist/index.html", HTML.getBytes(StandardCharsets.UTF_8))), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("대소문자만 다른 중복 파일을 거부한다")
    void rejectsCaseInsensitiveDuplicates() throws IOException {
        Map<String, byte[]> files = withIndex("App.js", "1");
        files.put("app.js", new byte[] {1});
        assertRejected(zip(files), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("심볼릭 링크를 거부한다")
    void rejectsSymlink() throws IOException {
        byte[] symlink = patchCentral(zip(withIndex("link.js", "/etc/passwd")), 1, (buffer, at) -> {
            buffer.putShort(at + 4, (short) (3 << 8));
            buffer.putInt(at + 38, 0120777 << 16);
        });
        assertRejected(symlink, ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("장치 파일 같은 특수 파일을 거부한다")
    void rejectsSpecialFile() throws IOException {
        byte[] device = patchCentral(zip(withIndex("dev.js", "1")), 1, (buffer, at) -> {
            buffer.putShort(at + 4, (short) (3 << 8));
            buffer.putInt(at + 38, 0020666 << 16);
        });
        assertRejected(device, ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("일반 파일 권한이 있는 유닉스 ZIP은 통과한다")
    void acceptsUnixRegularFile() throws IOException {
        byte[] regular = patchCentral(zip(withIndex("a.js", "1")), 1, (buffer, at) -> {
            buffer.putShort(at + 4, (short) (3 << 8));
            buffer.putInt(at + 38, 0100644 << 16);
        });
        assertThat(extract(regular).files()).hasSize(2);
    }

    @Test
    @DisplayName("암호화 플래그가 있으면 거부한다")
    void rejectsEncrypted() throws IOException {
        byte[] encrypted = patchCentral(zip(withIndex("a.js", "1")), 1,
                (buffer, at) -> buffer.putShort(at + 8, (short) (buffer.getShort(at + 8) | 1)));
        assertRejected(encrypted, ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("ZIP Bomb: 비정상 압축률을 내용을 풀기 전에 거부한다")
    void rejectsHighRatio() throws IOException {
        Map<String, byte[]> files = withIndex("a.js", "1");
        files.put("bomb.js", new byte[9 * 1024 * 1024]);
        assertRejected(zip(files), ErrorCode.BAD_REQUEST);
        try (Stream<Path> listed = Files.list(target)) {
            assertThat(listed).isEmpty();
        }
    }

    @Test
    @DisplayName("ZIP Bomb: 헤더 크기를 속여도 실제로 푼 크기로 거부한다")
    void rejectsLyingHeader() throws IOException {
        byte[] content = new byte[5000];
        new Random(1).nextBytes(content);
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("index.html", HTML.getBytes(StandardCharsets.UTF_8));
        files.put("big.js", content);
        byte[] lying = patchCentral(zip(files), 1, (buffer, at) -> buffer.putInt(at + 24, 10));

        validator = validatorWith(new DemoProperties.Limits(1024 * 1024, 2000, 1000, 10));
        assertThatThrownBy(() -> extract(lying)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("선언 크기와 실제 크기가 다르면 거부한다")
    void rejectsSizeMismatch() throws IOException {
        byte[] content = new byte[500];
        new Random(2).nextBytes(content);
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("index.html", HTML.getBytes(StandardCharsets.UTF_8));
        files.put("a.js", content);
        byte[] lying = patchCentral(zip(files), 1, (buffer, at) -> buffer.putInt(at + 24, 100));
        assertThatThrownBy(() -> extract(lying)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("파일 수 한도를 넘으면 413")
    void rejectsTooManyFiles() throws IOException {
        validator = validatorWith(new DemoProperties.Limits(1024 * 1024, 1024 * 1024, 1024 * 1024, 2));
        Map<String, byte[]> files = withIndex("a.js", "1");
        files.put("b.js", new byte[] {1});
        assertRejected(zip(files), ErrorCode.PAYLOAD_TOO_LARGE);
    }

    @Test
    @DisplayName("단일 파일 한도를 넘으면 413")
    void rejectsLargeFile() throws IOException {
        validator = validatorWith(new DemoProperties.Limits(1024 * 1024, 1024 * 1024, 100, 10));
        byte[] content = new byte[101];
        new Random(3).nextBytes(content);
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("index.html", "<p>x</p>".getBytes(StandardCharsets.UTF_8));
        files.put("a.js", content);
        assertRejected(zip(files), ErrorCode.PAYLOAD_TOO_LARGE);
    }

    @Test
    @DisplayName("총 크기 한도를 넘으면 413")
    void rejectsTotalSize() throws IOException {
        validator = validatorWith(new DemoProperties.Limits(1024 * 1024, 150, 100, 10));
        byte[] content = new byte[90];
        new Random(4).nextBytes(content);
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("index.html", "<p>x</p>".getBytes(StandardCharsets.UTF_8));
        files.put("a.js", content);
        files.put("b.js", content);
        assertRejected(zip(files), ErrorCode.PAYLOAD_TOO_LARGE);
    }

    @Test
    @DisplayName("ZIP 자체 크기 한도를 넘으면 413")
    void rejectsLargeZip() throws IOException {
        validator = validatorWith(new DemoProperties.Limits(50, 1024 * 1024, 1024 * 1024, 10));
        assertRejected(zip(withIndex("a.js", "1")), ErrorCode.PAYLOAD_TOO_LARGE);
    }

    @Test
    @DisplayName("ZIP이 아니거나 손상된 파일을 거부한다")
    void rejectsCorruptFile() throws IOException {
        assertRejected("not a zip file at all, just some text bytes".getBytes(StandardCharsets.UTF_8), ErrorCode.BAD_REQUEST);
        byte[] valid = zip(withIndex("a.js", "1"));
        assertRejected(Arrays.copyOf(valid, valid.length - 10), ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("끝에 데이터를 덧붙인 ZIP을 거부한다")
    void rejectsTrailingData() throws IOException {
        byte[] valid = zip(withIndex("a.js", "1"));
        byte[] extended = Arrays.copyOf(valid, valid.length + 16);
        assertRejected(extended, ErrorCode.BAD_REQUEST);
    }

    @Test
    @DisplayName("Stored(무압축) 항목도 CRC까지 맞으면 받는다")
    void acceptsStoredEntries() throws IOException {
        byte[] html = HTML.getBytes(StandardCharsets.UTF_8);
        CRC32 crc = new CRC32();
        crc.update(html);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            ZipEntry entry = new ZipEntry("index.html");
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(html.length);
            entry.setCompressedSize(html.length);
            entry.setCrc(crc.getValue());
            out.putNextEntry(entry);
            out.write(html);
            out.closeEntry();
        }
        assertThat(extract(bytes.toByteArray()).files()).hasSize(1);
    }

    @Test
    @DisplayName("한글 파일명은 NFC로 맞춘다")
    void normalizesKoreanNames() throws IOException {
        String nfd = java.text.Normalizer.normalize("이미지.png", java.text.Normalizer.Form.NFD);
        assertThat(extract(zip(withIndex(nfd, "1"))).files().get(1).path()).isEqualTo("이미지.png");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<script type=\"module\" crossorigin src=\"/assets/index-abc.js\"></script>",
            "<link rel=\"stylesheet\" href=\"/static/css/main.css\">",
            "<SCRIPT SRC='/app.js'></SCRIPT>"
    })
    @DisplayName("루트 절대 경로로 자산을 부르는 빌드는 고치는 방법과 함께 거부한다")
    void rejectsRootAbsoluteAssets(String tag) throws IOException {
        byte[] bytes = zip(Map.of("index.html", ("<html><head>" + tag + "</head></html>").getBytes(StandardCharsets.UTF_8)));
        assertThatThrownBy(() -> extract(bytes))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("base: './'");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "<script type=\"module\" crossorigin src=\"./assets/index-abc.js\"></script>",
            "<script src=\"assets/app.js\"></script>",
            "<script src=\"//cdn.example.com/x.js\"></script>",
            "<a href=\"/about\">링크</a>"
    })
    @DisplayName("상대 경로 빌드와 일반 링크는 통과한다")
    void acceptsRelativeAssets(String tag) throws IOException {
        byte[] bytes = zip(Map.of("index.html", ("<html><head>" + tag + "</head></html>").getBytes(StandardCharsets.UTF_8)));
        assertThat(extract(bytes).files()).hasSize(1);
    }
}
