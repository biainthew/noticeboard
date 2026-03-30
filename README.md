# Noticeboard

Spring Boot 기반의 게시판 REST API 서버입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.11 |
| Database | MySQL 8.x |
| Cache | Redis |
| Authentication | JWT (JJWT 0.12.6) |
| ORM | Spring Data JPA + QueryDSL 5.1.0 |
| Storage | AWS S3 |
| API 문서 | Swagger (springdoc-openapi 2.8.8) |
| Build | Gradle |

## 프로젝트 구조

```
src/main/java/aib/noticeboard/
├── config/              # 설정 클래스 (CORS, Security, S3, Swagger)
├── controller/          # REST 컨트롤러
├── domain/
│   ├── entity/          # JPA 엔티티 (Member, Post, Comment, Like, Notification, BaseEntity)
│   └── enums/           # 열거형 (MemberRole, PostStatus, CommentStatus, NotificationType)
├── dto/
│   ├── request/         # 요청 DTO
│   └── response/        # 응답 DTO
├── exception/           # 예외 처리 (GlobalExceptionHandler, CustomException, ErrorCode)
├── repository/          # JPA 리포지토리
├── security/            # JWT 인증 필터 및 토큰 프로바이더
├── service/             # 비즈니스 로직
└── util/                # 유틸리티
```

## ERD

```
Member (1) ─── (N) Post
  │                  │
  ├── (N) Comment    ├── (N) Comment
  │                  │
  ├── (N) Like       └── (N) Like
  │
  └── (N) Notification (receiver/sender)

Comment (1) ─── (N) Comment  (대댓글)
```

### 주요 엔티티

- **Member** — 이메일, 비밀번호(BCrypt), 닉네임, 역할(USER/ADMIN)
- **Post** — 제목, 내용, 조회수, 좋아요 수, 상태(ACTIVE/DELETED)
- **Comment** — 내용, 상태, 대댓글(self-referencing parent-child)
- **Like** — 회원-게시글 유니크 제약 (중복 좋아요 방지)
- **Notification** — 발신자, 수신자, 타입(COMMENT/LIKE), 읽음 여부

## API

### 인증 (`/api/auth`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | X |
| POST | `/api/auth/login` | 로그인 (JWT 토큰 발급) | X |
| POST | `/api/auth/refresh` | Access Token 갱신 | X |

**회원가입 요청**
```json
{
  "email": "user@example.com",
  "password": "12345678",
  "nickname": "홍길동"
}
```

**로그인 응답**
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG..."
}
```

- Access Token 만료: 30분
- Refresh Token 만료: 7일 (Redis 저장)

### 게시글 (`/api/posts`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts` | 게시글 작성 | O |
| GET | `/api/posts` | 게시글 목록 (페이징, 10건/페이지) | O |
| GET | `/api/posts/{postId}` | 게시글 상세 조회 | O |
| PUT | `/api/posts/{postId}` | 게시글 수정 | O |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 (소프트 삭제) | O |

### 댓글 (`/api/posts/{postId}/comments`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts/{postId}/comments` | 댓글/대댓글 작성 | O |
| GET | `/api/posts/{postId}/comments` | 댓글 목록 조회 | O |
| PUT | `/api/posts/{postId}/comments/{commentId}` | 댓글 수정 | O |
| DELETE | `/api/posts/{postId}/comments/{commentId}` | 댓글 삭제 (소프트 삭제) | O |

### 좋아요 (`/api/posts/{postId}/likes`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/posts/{postId}/likes` | 좋아요 | O |
| DELETE | `/api/posts/{postId}/likes` | 좋아요 취소 | O |

### 알림 (`/api/notifications`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/notifications/subscribe` | SSE 알림 구독 (text/event-stream) | O |
| GET | `/api/notifications` | 알림 목록 조회 | O |
| GET | `/api/notifications/unread-count` | 읽지 않은 알림 수 | O |
| PATCH | `/api/notifications/read-all` | 전체 읽음 처리 | O |

### 이미지 (`/api/images`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/images` | 이미지 업로드 (S3) | O |
| DELETE | `/api/images` | 이미지 삭제 (S3) | O |

- 파일 업로드 최대 크기: 10MB

## 주요 기능

### 인증/보안

- JWT Bearer 토큰 기반 Stateless 인증
- BCryptPasswordEncoder (strength 10) 비밀번호 암호화
- Refresh Token은 Redis에 저장 (TTL: 7일)
- `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**`는 인증 없이 접근 가능

### 실시간 알림 (SSE)

- Server-Sent Events 기반 실시간 알림
- 댓글 작성 시 게시글 작성자에게 알림
- 좋아요 시 게시글 작성자에게 알림
- 자기 자신에 대한 알림은 발송하지 않음

### 조회수 Redis 캐싱

- 게시글 조회 시 Redis에 조회수 증가분 저장
- 60초 간격 스케줄러로 DB에 일괄 반영 (`@Scheduled`)
- DB 부하 최소화

### AWS S3 이미지 업로드

- UUID 기반 파일명 생성
- Content-Type 자동 설정
- 업로드/삭제 지원

## 예외 처리

`GlobalExceptionHandler`를 통한 일관된 에러 응답:

| ErrorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| MEMBER_NOT_FOUND | 404 | 회원 없음 |
| EMAIL_ALREADY_EXISTS | 409 | 이메일 중복 |
| NICKNAME_ALREADY_EXISTS | 409 | 닉네임 중복 |
| INVALID_PASSWORD | 401 | 비밀번호 불일치 |
| INVALID_TOKEN | 401 | 토큰 유효하지 않음 |
| POST_NOT_FOUND | 404 | 게시글 없음 |
| POST_UNAUTHORIZED | 403 | 게시글 권한 없음 |
| COMMENT_NOT_FOUND | 404 | 댓글 없음 |
| COMMENT_UNAUTHORIZED | 403 | 댓글 권한 없음 |
| LIKE_ALREADY_EXISTS | 409 | 좋아요 중복 |
| LIKE_NOT_FOUND | 404 | 좋아요 없음 |
| INVALID_INPUT | 400 | 입력값 오류 |

## 실행 방법

### 사전 요구사항

- Java 17
- MySQL 8.x (`localhost:3306`, DB명: `noticeboard`)
- Redis (`localhost:6379`)
- AWS S3 버킷 및 자격 증명

### 환경 변수

프로젝트 루트에 `.env` 파일 생성:

```
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
```

프로덕션 환경에서는 추가로:

```
DB_URL=jdbc:mysql://host:3306/noticeboard
DB_USERNAME=username
DB_PASSWORD=password
JWT_SECRET=your-jwt-secret
```

### 실행

```bash
# DB 생성
mysql -u root -p -e "CREATE DATABASE noticeboard;"

# 빌드 및 실행
./gradlew bootRun
```

- 개발 서버: `http://localhost:8000`
- 프로덕션 서버: `https://communeio.site` (포트: 8080)

### API 문서

서버 실행 후 `http://localhost:8000/swagger-ui/index.html`에서 Swagger UI를 확인할 수 있습니다.

### CORS 허용 Origin

- `http://localhost:5173` (개발)
- `https://communeio.site` (프로덕션)

## 프론트엔드

- GitHub: https://github.com/biainthew/noticeboard-frontend

## 테스트 계정

| 이메일 | 비밀번호 |
|--------|----------|
| user1@test.com | user1234 |
| user2@test.com | user1234 |