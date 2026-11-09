package com.dipsource.service.demo.zip;

import com.dipsource.global.config.DemoProperties;
import com.dipsource.global.exception.BusinessException;
import com.dipsource.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 업로드된 ZIP을 검증하며 임시 폴더에 푼다.
 *
 * <p>java.util.zip은 심볼릭 링크 여부(외부 속성)를 알려 주지 않으므로 중앙 디렉터리를 직접 읽어 먼저 검사하고,
 * 압축 해제만 ZipFile에 맡긴다. 파일은 ZIP 안의 이름이 아니라 일련번호 이름으로 저장해 경로 조작이 끼어들 틈을 없앤다.
 * 헤더의 크기는 믿지 않고 실제로 푼 바이트 수를 세어 한도를 넘는 순간 멈춘다.
 */
@Component
@RequiredArgsConstructor
public class DemoZipValidator {

    public static final String ENTRY_PATH = "index.html";
    /** Mock API 경로와 겹치지 않도록 최상위에서 쓰지 못하게 막는 이름 */
    private static final String RESERVED_ROOT = "__mock";

    private static final int LOCAL_HEADER_SIGNATURE = 0x04034b50;
    private static final int CENTRAL_HEADER_SIGNATURE = 0x02014b50;
    private static final int END_OF_CENTRAL_SIGNATURE = 0x06054b50;
    private static final int END_OF_CENTRAL_SIZE = 22;
    private static final int CENTRAL_HEADER_SIZE = 46;
    private static final int MAX_COMMENT_LENGTH = 0xFFFF;

    private static final int UNIX_CREATOR = 3;
    private static final int MAC_CREATOR = 19;
    private static final int FILE_TYPE_MASK = 0170000;
    private static final int REGULAR_FILE = 0100000;
    private static final int DIRECTORY = 0040000;

    private static final int METHOD_STORED = 0;
    private static final int METHOD_DEFLATED = 8;
    private static final int FLAG_ENCRYPTED = 1;

    private static final int MAX_PATH_LENGTH = 255;
    private static final int MAX_SEGMENT_LENGTH = 120;
    /** 폴더 항목까지 포함한 ZIP 항목 수 상한 (파일 수 제한과 별개로 헤더 파싱량을 묶는다) */
    private static final int MAX_ENTRIES = 2000;
    /** 항목별 압축률 상한. 정상 빌드 결과물은 여기에 닿지 않는다 */
    private static final long MAX_COMPRESSION_RATIO = 200;
    private static final long RATIO_CHECK_MIN_BYTES = 1024 * 1024;
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long MAX_ENTRY_HTML_BYTES = 2 * 1024 * 1024;

    /** 경로·URL 해석을 흔드는 문자 */
    private static final Pattern FORBIDDEN_PATH_CHARS = Pattern.compile("[\\\\:*?\"<>|%#\\p{Cntrl}]");
    /** 도메인 루트 기준 경로로 스크립트·스타일을 부르는 태그 */
    private static final Pattern ROOT_ABSOLUTE_ASSET = Pattern.compile(
            "<(?:script|link)\\b[^>]*?\\b(?:src|href)\\s*=\\s*[\"']?/(?!/)", Pattern.CASE_INSENSITIVE);

    private final DemoProperties properties;

    /**
     * @param zip       업로드된 ZIP
     * @param targetDir 파일을 풀 빈 임시 폴더
     * @throws BusinessException 규격을 어긴 ZIP (400/413)
     */
    public DemoArchive extract(Path zip, Path targetDir) {
        DemoProperties.Limits limits = properties.limits();
        try {
            if (Files.size(zip) > limits.maxZipBytes()) {
                throw tooLarge("ZIP 파일은 " + toMb(limits.maxZipBytes()) + "MB 이하만 올릴 수 있습니다");
            }
            List<CentralEntry> entries = checkEntries(readCentralDirectory(zip), limits);
            DemoArchive archive = unpack(zip, targetDir, entries, limits);
            checkEntryHtml(archive);
            return archive;
        } catch (IOException | IllegalArgumentException e) {
            // ZipFile은 깨진 헤더에 ZipException/IllegalArgumentException을 던진다
            throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
        }
    }

    /** 중앙 디렉터리를 읽는다. 끝에 다른 데이터가 붙었거나 ZIP64·분할 압축이면 거부한다. */
    private List<RawEntry> readCentralDirectory(Path zip) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(zip.toFile(), "r")) {
            long length = file.length();
            if (length < END_OF_CENTRAL_SIZE || readSignature(file) != LOCAL_HEADER_SIGNATURE) {
                throw invalid("ZIP 파일이 아니거나 내용이 비어 있습니다");
            }
            ByteBuffer end = findEndOfCentralDirectory(file, length);
            int disk = u16(end, 4);
            int centralDisk = u16(end, 6);
            int entriesOnDisk = u16(end, 8);
            int entryCount = u16(end, 10);
            long centralSize = u32(end, 12);
            long centralOffset = u32(end, 16);
            if (disk != 0 || centralDisk != 0 || entriesOnDisk != entryCount) {
                throw invalid("분할 압축 ZIP은 지원하지 않습니다");
            }
            if (entryCount == 0xFFFF || centralOffset == 0xFFFFFFFFL || centralSize == 0xFFFFFFFFL) {
                throw invalid("ZIP64 형식은 지원하지 않습니다");
            }
            if (entryCount > MAX_ENTRIES) {
                throw tooLarge("ZIP 항목이 너무 많습니다");
            }
            if (centralOffset + centralSize > length - END_OF_CENTRAL_SIZE) {
                throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
            }
            byte[] central = new byte[(int) centralSize];
            file.seek(centralOffset);
            file.readFully(central);
            return parseEntries(ByteBuffer.wrap(central).order(ByteOrder.LITTLE_ENDIAN), entryCount, centralOffset);
        }
    }

    private int readSignature(RandomAccessFile file) throws IOException {
        byte[] head = new byte[4];
        file.seek(0);
        file.readFully(head);
        return ByteBuffer.wrap(head).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private ByteBuffer findEndOfCentralDirectory(RandomAccessFile file, long length) throws IOException {
        int tailSize = (int) Math.min(length, END_OF_CENTRAL_SIZE + MAX_COMMENT_LENGTH);
        byte[] tail = new byte[tailSize];
        file.seek(length - tailSize);
        file.readFully(tail);
        ByteBuffer buffer = ByteBuffer.wrap(tail).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = tailSize - END_OF_CENTRAL_SIZE; i >= 0; i--) {
            // 주석 길이까지 맞아야 진짜 끝 레코드다. 뒤에 데이터를 덧붙인 파일은 여기서 걸러진다
            if (buffer.getInt(i) == END_OF_CENTRAL_SIGNATURE && i + END_OF_CENTRAL_SIZE + u16(buffer, i + 20) == tailSize) {
                return buffer.slice(i, END_OF_CENTRAL_SIZE).order(ByteOrder.LITTLE_ENDIAN);
            }
        }
        throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
    }

    private List<RawEntry> parseEntries(ByteBuffer central, int entryCount, long centralOffset) {
        List<RawEntry> entries = new ArrayList<>(entryCount);
        int position = 0;
        for (int i = 0; i < entryCount; i++) {
            if (position + CENTRAL_HEADER_SIZE > central.limit() || central.getInt(position) != CENTRAL_HEADER_SIGNATURE) {
                throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
            }
            int nameLength = u16(central, position + 28);
            int extraLength = u16(central, position + 30);
            int commentLength = u16(central, position + 32);
            int next = position + CENTRAL_HEADER_SIZE + nameLength + extraLength + commentLength;
            if (next > central.limit() || u32(central, position + 42) >= centralOffset) {
                throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
            }
            entries.add(new RawEntry(
                    decodeName(central, position + CENTRAL_HEADER_SIZE, nameLength),
                    u16(central, position + 4) >>> 8,
                    u16(central, position + 8),
                    u16(central, position + 10),
                    u32(central, position + 20),
                    u32(central, position + 24),
                    (int) (u32(central, position + 38) >>> 16)));
            position = next;
        }
        if (position != central.limit()) {
            throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
        }
        return entries;
    }

    private String decodeName(ByteBuffer central, int offset, int length) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(central.slice(offset, length))
                    .toString();
        } catch (CharacterCodingException e) {
            throw invalid("파일 이름은 UTF-8이어야 합니다");
        }
    }

    /** 내용을 풀기 전에 끝낼 수 있는 검사. 경로·형식·개수·선언 크기를 본다. */
    private List<CentralEntry> checkEntries(List<RawEntry> rawEntries, DemoProperties.Limits limits) {
        List<CentralEntry> files = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        long declaredTotal = 0;

        for (RawEntry entry : rawEntries) {
            String path = normalizePath(entry.name());
            checkFileType(entry);
            if ((entry.flags() & FLAG_ENCRYPTED) != 0) {
                throw invalid("암호가 걸린 ZIP은 올릴 수 없습니다");
            }
            if (entry.method() != METHOD_STORED && entry.method() != METHOD_DEFLATED) {
                throw invalid("지원하지 않는 압축 방식입니다: " + path);
            }
            if (isJunk(path) || entry.name().endsWith("/")) {
                continue;
            }
            // macOS·Windows에서 같은 파일이 되는 이름도 중복으로 본다
            if (!seen.add(path.toLowerCase(Locale.ROOT))) {
                throw invalid("중복된 파일 이름이 있습니다: " + path);
            }
            if (DemoFileTypes.contentTypeOf(path).isEmpty()) {
                throw invalid("허용하지 않는 파일 형식입니다: " + path);
            }
            if (entry.uncompressedSize() > limits.maxFileBytes()) {
                throw tooLarge("파일 하나는 " + toMb(limits.maxFileBytes()) + "MB 이하여야 합니다: " + path);
            }
            checkCompressionRatio(entry, path);
            declaredTotal += entry.uncompressedSize();
            files.add(new CentralEntry(entry, path));
        }

        if (files.size() > limits.maxFiles()) {
            throw tooLarge("파일은 " + limits.maxFiles() + "개까지 올릴 수 있습니다");
        }
        if (declaredTotal > limits.maxTotalBytes()) {
            throw tooLarge("압축을 푼 크기는 " + toMb(limits.maxTotalBytes()) + "MB 이하여야 합니다");
        }
        if (files.stream().noneMatch(file -> file.path().equals(ENTRY_PATH))) {
            throw invalid("ZIP 최상위에 index.html이 있어야 합니다");
        }
        return files;
    }

    /** 경로 조작을 거부하고 NFC로 맞춘다. macOS ZIP은 한글 파일명이 NFD로 들어온다. */
    private String normalizePath(String rawName) {
        String name = Normalizer.normalize(rawName, Normalizer.Form.NFC);
        String trimmed = name.endsWith("/") ? name.substring(0, name.length() - 1) : name;
        if (trimmed.isEmpty() || trimmed.startsWith("/") || trimmed.length() > MAX_PATH_LENGTH
                || FORBIDDEN_PATH_CHARS.matcher(trimmed).find()) {
            throw invalid("허용하지 않는 파일 경로입니다: " + printable(rawName));
        }
        String[] segments = trimmed.split("/", -1);
        for (String segment : segments) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")
                    || segment.length() > MAX_SEGMENT_LENGTH || !segment.equals(segment.strip())) {
                throw invalid("허용하지 않는 파일 경로입니다: " + printable(rawName));
            }
        }
        if (isJunk(trimmed)) {
            return trimmed;
        }
        for (String segment : segments) {
            if (segment.startsWith(".")) {
                throw invalid("숨김 파일은 올릴 수 없습니다: " + printable(rawName));
            }
        }
        if (segments[0].equals(RESERVED_ROOT)) {
            throw invalid("__mock 폴더 이름은 예약되어 있습니다");
        }
        return trimmed;
    }

    /** OS가 압축할 때 끼워 넣는 파일. 오류로 돌려보내지 않고 건너뛴다. */
    private boolean isJunk(String path) {
        if (path.equals("__MACOSX") || path.startsWith("__MACOSX/")) {
            return true;
        }
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.equals(".DS_Store") || name.equals("Thumbs.db") || name.equals("desktop.ini");
    }

    /** 심볼릭 링크, 장치 파일 같은 특수 파일을 거부한다. */
    private void checkFileType(RawEntry entry) {
        if (entry.creator() != UNIX_CREATOR && entry.creator() != MAC_CREATOR) {
            return;
        }
        int fileType = entry.unixMode() & FILE_TYPE_MASK;
        if (fileType != 0 && fileType != REGULAR_FILE && fileType != DIRECTORY) {
            throw invalid("심볼릭 링크나 특수 파일은 올릴 수 없습니다: " + printable(entry.name()));
        }
    }

    private void checkCompressionRatio(RawEntry entry, String path) {
        if (entry.uncompressedSize() >= RATIO_CHECK_MIN_BYTES
                && entry.uncompressedSize() > entry.compressedSize() * MAX_COMPRESSION_RATIO) {
            throw invalid("압축률이 비정상적인 파일이 있습니다: " + path);
        }
    }

    private DemoArchive unpack(Path zip, Path targetDir, List<CentralEntry> entries, DemoProperties.Limits limits)
            throws IOException {
        List<DemoArchive.File> files = new ArrayList<>(entries.size());
        long total = 0;
        try (ZipFile zipFile = new ZipFile(zip.toFile(), StandardCharsets.UTF_8)) {
            int index = 0;
            for (CentralEntry entry : entries) {
                ZipEntry zipEntry = zipFile.getEntry(entry.raw().name());
                if (zipEntry == null) {
                    throw invalid("손상되었거나 지원하지 않는 ZIP 파일입니다");
                }
                Path target = targetDir.resolve("f" + index++);
                long size = copyWithLimit(zipFile, zipEntry, target, entry.path(), limits.maxFileBytes(),
                        limits.maxTotalBytes() - total, limits);
                if (size != entry.raw().uncompressedSize()) {
                    throw invalid("선언된 크기와 실제 크기가 다른 파일이 있습니다: " + entry.path());
                }
                total += size;
                files.add(new DemoArchive.File(entry.path(), target, size,
                        DemoFileTypes.contentTypeOf(entry.path()).orElseThrow()));
            }
        }
        return new DemoArchive(files, total, ENTRY_PATH);
    }

    /** 실제로 읽은 바이트를 세며 복사한다. ZIP 헤더가 거짓이어도 한도 이상은 쓰지 않는다. */
    private long copyWithLimit(ZipFile zipFile, ZipEntry entry, Path target, String path, long fileLimit,
                               long remainingTotal, DemoProperties.Limits limits) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long written = 0;
        try (InputStream in = zipFile.getInputStream(entry); OutputStream out = Files.newOutputStream(target)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                written += read;
                if (written > fileLimit) {
                    throw tooLarge("파일 하나는 " + toMb(limits.maxFileBytes()) + "MB 이하여야 합니다: " + path);
                }
                if (written > remainingTotal) {
                    throw tooLarge("압축을 푼 크기는 " + toMb(limits.maxTotalBytes()) + "MB 이하여야 합니다");
                }
                out.write(buffer, 0, read);
            }
        }
        return written;
    }

    /** Vite·CRA 기본 빌드는 /assets/... 로 시작해서 하위 경로에서 실행하면 파일을 못 찾는다. 공개 전에 알려 준다. */
    private void checkEntryHtml(DemoArchive archive) throws IOException {
        DemoArchive.File entry = archive.files().stream()
                .filter(file -> file.path().equals(archive.entryPath()))
                .findFirst().orElseThrow();
        if (entry.size() > MAX_ENTRY_HTML_BYTES) {
            return;
        }
        if (ROOT_ABSOLUTE_ASSET.matcher(Files.readString(entry.tempFile(), StandardCharsets.ISO_8859_1)).find()) {
            throw invalid("index.html이 /로 시작하는 절대 경로로 파일을 불러옵니다. "
                    + "Vite는 base: './', Create React App은 homepage: '.'로 다시 빌드해 주세요");
        }
    }

    private static int u16(ByteBuffer buffer, int offset) {
        return Short.toUnsignedInt(buffer.getShort(offset));
    }

    private static long u32(ByteBuffer buffer, int offset) {
        return Integer.toUnsignedLong(buffer.getInt(offset));
    }

    private static long toMb(long bytes) {
        return bytes / (1024 * 1024);
    }

    /** 오류 메시지에 넣기 전에 제어 문자를 지운다 */
    private static String printable(String name) {
        String cleaned = name.replaceAll("\\p{Cntrl}", "?");
        return cleaned.length() > 80 ? cleaned.substring(0, 80) + "…" : cleaned;
    }

    private static BusinessException invalid(String message) {
        return new BusinessException(ErrorCode.BAD_REQUEST, message);
    }

    private static BusinessException tooLarge(String message) {
        return new BusinessException(ErrorCode.PAYLOAD_TOO_LARGE, message);
    }

    private record RawEntry(String name, int creator, int flags, int method, long compressedSize,
                            long uncompressedSize, int unixMode) {
    }

    private record CentralEntry(RawEntry raw, String path) {
    }
}
