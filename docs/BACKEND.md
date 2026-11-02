# 찍먹 백엔드 개발 문서

> 백엔드 작업의 기준 문서다.
>
> - **DB 구조:** [테이블정의서](./테이블정의서.md), [database-design.md](./database-design.md)
> - **API 명세:** [기획서](./기획서.md) 5장
> - **Git 규칙:** [AGENTS.md](../AGENTS.md)
> - **용어:** [GLOSSARY.md](../GLOSSARY.md)

---

## 1. 기술 스택

| 기술 | 버전 | 용도 |
|------|------|------|
| Java | 21 | 언어 |
| Spring Boot | 3.4.1 | 프레임워크 |
| Spring Security | - | 인증·인가 |
| Spring Data JPA (Hibernate) | - | ORM |
| Spring Validation | - | 입력 검증 |
| Lombok | - | 보일러플레이트 제거 |
| PostgreSQL | 16 | 운영 DB |
| Gradle | - | 빌드 도구 |
| JUnit 5 | - | 테스트 |

---

## 2. 패키지 구조

3계층을 먼저 나누고, 그 안에 기능(도메인)별 폴더를 둔다. 루트 패키지는 `com.dipsource`다. 기능 목록과 담당 테이블은 [백엔드-청사진.md](./백엔드-청사진.md) 5장을 따른다.

```text
com/dipsource
├─ DipSourceApplication.java
├─ global/                    전역 설정·공통 코드 (도메인 로직 금지)
│  ├─ config/                 Security, Cors, WebSocket, JpaAuditing
│  ├─ auth/                   JWT 필터, OAuth2 핸들러
│  ├─ exception/              BusinessException, ErrorCode, GlobalExceptionHandler
│  └─ common/                 BaseEntity, ApiResponse, PageResponse
│
├─ controller/<기능>/         [1층] 요청 파싱, 응답 포장만
├─ service/<기능>/            [2층] 비즈니스 로직, @Transactional
├─ repository/<기능>/         [3층] 데이터 접근
├─ entity/<기능>/             [3층] JPA 엔티티
└─ dto/<기능>/                요청·응답 record
```

기능 폴더는 5개 층 폴더에 같은 이름으로 만든다. 예를 들어 회원은 `controller/user`, `service/user`, `repository/user`, `entity/user`, `dto/user`다.

| 기능 | 설명 |
|------|------|
| `user` | users, social_accounts, 관심 태그 (+ 인증 컨트롤러) |
| `follow` · `block` | 팔로우, 차단 |
| `feed` | 피드, 좋아요·댓글·리포스트·투표 |
| `tag` | 태그 |
| `demo` | 데모, 피드백, 북마크 |
| `post` | 블로그 글, 댓글·좋아요 |
| `minihome` | 미니홈피, 방명록 |
| `dm` | DM 방·멤버·메시지 |
| `notification` | 알림 |
| `coin` · `subscription` | 칩, 구독 |
| `settlement` | 정산 |
| `report` | 신고 |
| `admin` | 관리자 |
| `search` · `ranking` · `news` | 집계·외부 API. `entity`, `repository` 폴더는 없다 |

### 규칙

- 호출 방향은 `controller → service → repository → entity`이고, `dto`는 controller와 service 사이에서 쓴다.
- 다른 기능의 `entity`나 `repository`를 직접 참조하지 않는다. 필요하면 해당 기능의 `service`를 통해 접근한다.
- `global/`에는 기능 로직을 넣지 않는다.
- 빈 폴더는 `.gitkeep`으로 유지한다. 첫 파일을 만들 때 해당 `.gitkeep`을 지운다.

---

## 3. API 응답 규격

### 성공 응답

```json
{
  "data": { ... }
}
```

목록 조회 (페이지네이션):

```json
{
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 에러 응답

```json
{
  "error": {
    "code": "NOT_FOUND",
    "message": "데모를 찾을 수 없습니다"
  }
}
```

### HTTP 상태 코드

| 코드 | 사용 |
|------|------|
| 200 | 조회·수정 성공 |
| 201 | 생성 성공 |
| 204 | 삭제 성공 (본문 없음) |
| 400 | 입력 검증 실패, 잘못된 요청 |
| 401 | 인증 필요 (JWT 없음·만료) |
| 403 | 권한 없음 (본인 아님, 관리자 아님) |
| 404 | 대상 없음 |
| 409 | 중복 (이미 팔로우, 이미 좋아요 등) |
| 500 | 서버 오류 |

### 에러 코드

| 코드 | 설명 |
|------|------|
| `VALIDATION_ERROR` | 입력 검증 실패 (400) |
| `UNAUTHORIZED` | 인증 필요 (401) |
| `FORBIDDEN` | 권한 없음 (403) |
| `NOT_FOUND` | 대상 없음 (404) |
| `DUPLICATE` | 중복 리소스 (409) |
| `INSUFFICIENT_BALANCE` | 도토리 잔액 부족 (400) |
| `ALREADY_SUBSCRIBED` | 이미 구독 중 (409) |
| `SELF_ACTION` | 자기 자신 대상 불가 — 팔로우, 차단 등 (400) |
| `INTERNAL_ERROR` | 서버 오류 (500) |

---

## 4. 엔티티 컨벤션

### BaseEntity

모든 엔티티가 상속하는 공통 클래스.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

- `created_at`만 필요한 엔티티(likes, follows, bookmarks 등)는 BaseEntity를 상속하지 않고 `@CreatedDate`만 직접 선언한다.
- `updatedAt`이 없는 테이블은 상속 대신 직접 필드를 선언한다.

### 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| 엔티티 클래스 | 단수형 PascalCase | `User`, `Demo`, `MiniHome` |
| 테이블 | 복수형 snake_case | `users`, `demos`, `mini_homes` |
| 컬럼 | snake_case | `user_id`, `like_count`, `is_paid` |
| Repository | `엔티티명 + Repository` | `UserRepository`, `DemoRepository` |
| Service | `엔티티명 + Service` | `UserService`, `DemoService` |
| Controller | `엔티티명 + Controller` | `UserController`, `DemoController` |
| DTO | `동작 + 엔티티명 + Request/Response` | `CreateDemoRequest`, `DemoResponse` |

### FK 관계

```java
// 단방향 ManyToOne을 기본으로 한다
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;
```

- `FetchType.LAZY`를 기본으로 한다. EAGER는 사용하지 않는다.
- 양방향 매핑은 꼭 필요한 경우에만 사용한다 (부모에서 자식 목록이 필수인 경우).
- cascade, orphanRemoval은 부모-자식 생명주기가 동일한 경우에만 사용한다.

### 다형성 테이블

comments, likes, reports는 FK 대신 `targetType` + `targetId`를 사용한다.

```java
@Column(nullable = false, length = 20)
private String targetType;  // "demo", "post", "feed"

@Column(nullable = false)
private Long targetId;
```

- JPA FK를 걸지 않는다. 서비스 레이어에서 대상 존재 여부를 검증한다.
- `targetType`은 문자열 상수 또는 enum으로 관리한다.

---

## 5. 인증·인가

### JWT 흐름

```
소셜 로그인 요청 → OAuth2 제공자 (GitHub/카카오/구글)
    → 백엔드 콜백에서 사용자 확인/생성
    → Access Token + Refresh Token 발급
    → httpOnly 쿠키로 응답
    → 이후 요청마다 쿠키 → JwtAuthenticationFilter → SecurityContext
```

### 구현 규칙

- Access Token은 짧은 만료 (15~30분), Refresh Token은 긴 만료 (7~14일).
- 토큰은 httpOnly + Secure + SameSite=Lax 쿠키에 저장한다.
- `JwtAuthenticationFilter`는 `OncePerRequestFilter`를 상속해 Security Filter Chain에 등록한다.
- 권한 검사는 `@PreAuthorize` 또는 SecurityConfig에서 URL 패턴으로 설정한다.
- 관리자 API(`/api/v1/admin/**`)는 `ADMIN` 역할만 접근 가능하게 설정한다.
- 본인 확인이 필요한 API(프로필 수정, 글 수정/삭제 등)는 서비스 레이어에서 `userId` 일치를 검증한다.

---

## 6. 서비스 레이어 규칙

- **트랜잭션:** 변경(생성·수정·삭제)은 `@Transactional`, 조회는 `@Transactional(readOnly = true)`.
- **비정규화 갱신:** `like_count`, `coin_balance` 등 비정규화 필드는 원본 변경과 같은 트랜잭션에서 갱신한다.
- **동시성:** 도토리 차감 등 경쟁 조건이 있는 작업은 `SELECT ... FOR UPDATE` 또는 비관적 잠금을 사용한다.
- **검증 순서:** 인증 확인 → 입력 검증 → 권한 확인 → 비즈니스 로직 → 저장.
- **예외:** 비즈니스 예외는 커스텀 예외 클래스로 던지고, `GlobalExceptionHandler`에서 에러 응답으로 변환한다.

---

## 7. 컨트롤러 규칙

```java
@RestController
@RequestMapping("/api/v1/demos")
@RequiredArgsConstructor
public class DemoController {

    private final DemoService demoService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DemoResponse>>> getDemos(
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "latest") String sort,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(demoService.getDemos(tag, sort, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DemoResponse>> createDemo(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute CreateDemoRequest request) {
        DemoResponse response = demoService.createDemo(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
```

### 규칙

- 모든 API 경로는 `/api/v1/`로 시작한다.
- 컨트롤러는 요청 파싱·응답 포장만 한다. 비즈니스 로직을 넣지 않는다.
- 입력 검증은 `@Valid`로 DTO에서 처리한다.
- 인증된 사용자 정보는 `@AuthenticationPrincipal`로 받는다.
- 파일 업로드(데모 zip)는 `@ModelAttribute` + `MultipartFile`을 사용한다.

---

## 8. DTO 규칙

```java
// 요청
public record CreateDemoRequest(
    @NotBlank @Size(max = 100) String title,
    String description,
    @NotNull MultipartFile file,
    boolean isPaid,
    @Min(0) int priceCoins,
    List<String> tags
) {}

// 응답
public record DemoResponse(
    Long id,
    Long userId,
    String userNickname,
    String title,
    String description,
    String thumbnail,
    boolean isPaid,
    int priceCoins,
    int playCount,
    int likeCount,
    LocalDateTime createdAt
) {
    public static DemoResponse from(Demo demo) {
        return new DemoResponse(
            demo.getId(),
            demo.getUser().getId(),
            demo.getUser().getNickname(),
            // ...
        );
    }
}
```

- Java `record`를 사용한다.
- 요청 DTO에 검증 어노테이션(`@NotBlank`, `@Size`, `@Min` 등)을 붙인다.
- 응답 DTO에 `from(Entity)` 정적 팩토리 메서드를 둔다. 엔티티를 직접 반환하지 않는다.
- 엔티티의 모든 필드를 노출하지 않는다. 화면에 필요한 필드만 포함한다.

---

## 9. 예외 처리

```java
// 커스텀 예외
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

// 에러 코드
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "대상을 찾을 수 없습니다"),
    DUPLICATE(HttpStatus.CONFLICT, "DUPLICATE", "이미 존재합니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "권한이 없습니다"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "도토리 잔액이 부족합니다"),
    SELF_ACTION(HttpStatus.BAD_REQUEST, "SELF_ACTION", "자기 자신에게는 할 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

// 전역 핸들러
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
            .body(ApiResponse.error(code.getCode(), code.getMessage()));
    }
}
```

---

## 10. 구현 순서

[database-design.md](./database-design.md) 6장의 Phase별 순서를 따른다. 각 도메인에서:

1. 엔티티 + Repository 생성
2. Service 구현 (비즈니스 로직, 트랜잭션)
3. Controller + DTO 구현
4. 테스트 (단위 + 통합)
5. 프론트엔드와 API 연결 확인

작은 PR 단위로 만들고, 각 PR에서 엔티티-서비스-컨트롤러-테스트를 한 세트로 포함한다.
