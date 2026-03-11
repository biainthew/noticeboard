# Noticeboard

Spring Boot 기반의 게시판 REST API 서버입니다.

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.11 |
| Database | MySQL |
| Cache | Redis |
| Authentication | JWT (JJWT 0.12.6) |
| ORM | Spring Data JPA + QueryDSL 5.1.0 |
| API 문서 | Swagger (springdoc-openapi 2.8.8) |
| Build | Gradle |

## 프로젝트 구조

```
src/main/java/aib/noticeboard/
├── config/              # 설정 클래스
├── controller/          # REST 컨트롤러
├── domain/
│   ├── entity/          # JPA 엔티티 (Member, Post, Comment, Like, Notification)
│   └── enums/           # 열거형 (MemberRole, PostStatus, CommentStatus, NotificationType)
├── dto/
│   ├── request/         # 요청 DTO
│   └── response/        # 응답 DTO
├── exception/           # 예외 처리 (GlobalExceptionHandler, CustomException, ErrorCode)
├── repository/          # JPA 리포지토리
├── security/            # JWT 인증 필터 및 Security 설정
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
  └── (N) Notification

Comment (1) ─── (N) Comment  (대댓글)
```

### 주요 엔티티

- **Member** — 이메일, 비밀번호(BCrypt), 닉네임, 역할(USER/ADMIN)
- **Post** — 제목, 내용, 조회수, 좋아요 수, 상태(ACTIVE/DELETED)
- **Comment** — 내용, 상태, 대댓글(self-referencing)
- **Like** — 회원-게시글 유니크 제약 (중복 좋아요 방지)
- **Notification** — 발신자, 수신자, 타입(COMMENT/LIKE), 읽음 여부

## API

### 인증 (`/api/auth`)

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 (JWT 토큰 발급) |

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
- Refresh Token 만료: 7일

## 인증/보안

- JWT Bearer 토큰 기반 Stateless 인증
- BCryptPasswordEncoder (strength 10) 비밀번호 암호화
- `/api/auth/**`, `/swagger-ui/**`, `/v3/api-docs/**` 는 인증 없이 접근 가능
- 그 외 모든 엔드포인트는 JWT 인증 필요

## 예외 처리

`GlobalExceptionHandler`를 통한 일관된 에러 응답:

| ErrorCode | HTTP Status | 설명 |
|-----------|-------------|------|
| MEMBER_NOT_FOUND | 404 | 회원 없음 |
| EMAIL_ALREADY_EXISTS | 409 | 이메일 중복 |
| NICKNAME_ALREADY_EXISTS | 409 | 닉네임 중복 |
| INVALID_PASSWORD | 401 | 비밀번호 불일치 |
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

### 실행

```bash
# DB 생성
mysql -u root -p -e "CREATE DATABASE noticeboard;"

# 빌드 및 실행
./gradlew bootRun
```

서버는 `http://localhost:8000`에서 실행됩니다.

### API 문서

서버 실행 후 `http://localhost:8000/swagger-ui/index.html`에서 Swagger UI를 확인할 수 있습니다.