# Sionic AI — VIP Onboarding 과제

> 잠재 고객사 시연용 **챗봇 + 피드백 + 분석/보고 API**.
> 단순 데모가 아닌, 이후 정식 기능 확장의 토대가 될 수 있는 구조를 우선으로 설계했습니다.

---

## ⚠️ 언어 선택에 대한 사전 안내 — 자바로 구현한 이유

> **본 과제는 원래 Kotlin 과제이지만, 본 제출물은 Java 17로 구현되었습니다.**

마감 3시간 안에 구현을 하기 위해 **현재 가장 익숙한 언어인 Java로 견고한 설계와 동작 검증에 집중**하는 쪽을 택했습니다.

입사하게 된다면 빠르게 Kotlin과 Spring + Kotlin 관용구를 빠르게 학습하여 팀 코드 표준에 맞춰 작성할 자신이 있습니다.

---

## 📝 과제 수행 회고

### 1. 과제 분석 방법

3시간이라는 제약 안에서 "무엇이 반드시 동작해야 하는가"를 먼저 정의했습니다.

고객사가 API spec에 익숙하지 않다는 점에 주목해, **코드 품질보다 시연 가능성**을 최우선으로 두었습니다. 구체적으로는 curl 명령 없이도 브라우저에서 바로 챗봇을 체험할 수 있는 `test.html` UI를 먼저 구성하고, 그 위에서 실제로 동작함을 확인하는 방식으로 진행했습니다.

동시에 "단발성 데모로 끝나선 안 된다"는 요건을 고려해, `AiProvider` 추상화·레이어드 아키텍처·도메인 이벤트 기반 analytics 같은 확장 지점을 처음부터 구조에 포함시켰습니다. 당장 시연에 필요하지 않더라도 나중에 실제 provider 교체나 RAG 컨텍스트 추가가 최소한의 변경으로 가능하도록 설계했습니다.

### 2. AI 활용 방법 및 어려움

Claude Code를 사용해 반복적인 보일러플레이트(엔티티·DTO·Repository·Controller 뼈대)와 테스트 시나리오 작성을 AI에 위임했습니다. 덕분에 아키텍처 설계와 동작 검증에 집중할 수 있었습니다.

어려웠던 점은 **컨텍스트 토큰이 한정되어 있어 토큰 관리가 까다로웠다는 것**입니다. 대화가 길어질수록 앞선 설계 맥락이 잘려나가면서 AI가 이미 결정한 아키텍처 원칙(예: 컨텍스트 간 의존 방향, JWT userId 신뢰 원칙)을 잊고 다른 방향으로 코드를 생성하는 경우가 생겼습니다. 이를 방지하기 위해 핵심 설계 결정을 CLAUDE.md에 명시적으로 기록해두고, 새 대화를 시작할 때마다 해당 맥락을 다시 주입하는 방식으로 관리했습니다.

### 3. 가장 어려웠던 기능 — 고객사가 직접 테스트할 수 있는 UI 설계

코드 구현 자체는 큰 어려움이 없었습니다. 레이어드 아키텍처, `AiProvider` 추상화, 도메인 이벤트 기반 analytics 등은 평소에 익숙한 패턴이라 비교적 빠르게 잡을 수 있었습니다.

오히려 시간을 많이 쓴 부분은 **고객사 담당자가 API spec 없이도 직접 체험하고 이해할 수 있는 `test.html` UI**였습니다. 기능 구현보다 "어떤 흐름으로 화면을 구성해야 기술에 낯선 사람도 API가 동작한다는 걸 납득할 수 있을까"를 고민하는 과정이 더 어려웠습니다.

회원가입·로그인·채팅·피드백·관리자 분석 보고서까지 한 페이지에서 순서대로 체험할 수 있도록 UX 흐름을 잡고, JWT 토큰 자동 저장·헤더 자동 주입, 스트리밍 응답의 실시간 렌더링 등 사용자가 내부 동작을 의식하지 않아도 되는 경험을 만드는 데 집중했습니다.

---

## 🚀 빌드 & 실행

```bash
./gradlew build       # 컴파일 + 테스트
./gradlew bootRun     # 애플리케이션 실행 (http://localhost:8080)
```

### 시연 UI (test.html)

서버 실행 후 브라우저에서 아래 URL을 엽니다.

```
http://localhost:8080/test.html
```


## 🎯 시나리오 / 목표

- **조직**: Sionic AI VIP Onboarding 팀(영업 1명 + 개발자 1명).
- **상황**: 영업 발 긴급 시연 요청. 마감 **3시간**, 매뉴얼 동봉. 요건 조율 불가.
- **고객사 요구**:
  1. 챗봇 AI를 호출할 수 있는 API
  2. 향후 자사 대외비 문서 학습 → **RAG 확장 여지**
  3. 고객사는 OpenAI/Anthropic은 알지만 **API spec 이해도가 낮음** → 인터페이스는 단순/직관적이어야 함
- **시연 목표**: "API를 통해 AI를 활용할 수 있다"의 증명 + **이후 정식 기능의 토대**가 되는 구조.

---

## 🛠 기술 스택

| 영역 | 선택 |
|---|---|
| 언어/런타임 | **Java 17** |
| 빌드 | **Gradle** (Spring Boot Plugin 3.5.13) |
| 프레임워크 | **Spring Boot 3.5.13** — Web / Data JPA / Security / Validation |
| DB | **H2 in-memory** (PostgreSQL 호환 모드, `MODE=PostgreSQL`) |
| 인증 | **JWT (HS256)** — `jjwt 0.12.6` (api/impl/jackson) |
| 비동기 응답 | **Server-Sent Events (SSE)** — Spring `SseEmitter` + 별도 `ThreadPoolTaskExecutor` |
| 이벤트 | **Spring `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)`** |

---

## 🏛 아키텍처 — 레이어드 + 도메인 이벤트

각 바운디드 컨텍스트(`auth`, `user`, `chat`, `feedback`, `analytics`)는 **3계층**으로 분리합니다.

| 계층 | 역할 | 포함 |
|---|---|---|
| `domain` | 순수 비즈니스 모델/규칙 | 엔티티, Enum, Repository 인터페이스, 도메인 예외, **도메인 이벤트** |
| `application` | 유스케이스 오케스트레이션 | Service, DTO, 외부 어댑터(JwtTokenProvider, AiProvider impl), **이벤트 리스너** |
| `resource` | HTTP/외부 경계 | Controller, Filter |

**의존 방향**: `resource → application → domain` (역방향 금지). 컨텍스트 간 결합은 최소화하며, 분석 같은 다운스트림 컨텍스트는 **도메인 이벤트로만 상위 컨텍스트에서 정보를 받습니다**(단방향).

```
auth ──publishEvent──▶ (Spring AppContext) ──@TransactionalEventListener──▶ analytics
chat ──publishEvent──▶ (Spring AppContext) ──@TransactionalEventListener──▶ analytics

feedback ──read─only──▶ chat.domain (소유권 검증)
analytics ──read─only──▶ chat.domain / user.domain (CSV 보고서)
```

`analytics`의 이벤트 리스너는 `@TransactionalEventListener(AFTER_COMMIT) + @Transactional(REQUIRES_NEW)`로 동작하기 때문에, **활동 로그 적재가 실패해도 원본 도메인 트랜잭션(가입/로그인/대화 생성)은 절대 롤백되지 않습니다.**

설계 결정의 더 자세한 배경은 [`CLAUDE.md`](./CLAUDE.md)를 참고해주세요.

---


**기본 흐름:**

1. **로그인** — 기본 admin 계정(`admin@example.com` / `admin1234`)이 자동 입력되어 있습니다. *로그인* 버튼을 누르세요.
2. **채팅** — 하단 입력창에 질문을 입력하고 `Enter` 또는 `↑` 버튼으로 전송합니다.
3. **빠른 질문 칩** — 빈 화면에 표시되는 `📦 배송 조회`, `📋 주문 확인`, `🔄 반품/교환 안내`, `🛍️ 상품 문의` 버튼을 클릭하면 관련 질문이 즉시 전송됩니다.
4. **대화 세션** — 좌측 사이드바에서 이전 세션을 클릭하면 해당 대화 내역을 볼 수 있습니다. 세션은 마지막 대화 기준 30분 단위로 자동 관리됩니다.
5. **관리자 패널** — 우측 상단 `⚙️ 관리자` 버튼으로 피드백 현황 조회 및 24시간 활동 통계/CSV 보고서를 확인할 수 있습니다.
6. **회원가입** — 로그인 화면 상단 *회원가입* 탭에서 일반 사용자 계정을 새로 만들 수 있습니다.

> SSE 스트리밍이 기본으로 활성화되어 있어 AI 응답이 실시간으로 출력됩니다.

---

### 시연용 기본값

- **Admin 계정** (기동 시 `AdminSeeder`가 자동 생성, 이미 존재하면 skip):
  - email: `admin@example.com`
  - password: `admin1234`
  - 오버라이드: `app.admin.email`, `app.admin.password`, `app.admin.name`
- **JWT secret**: `JWT_SECRET` env로 오버라이드 가능 (개발용 더미 기본값 존재)
- **JWT 만료**: 1시간 (`app.jwt.expiration-ms=3600000`)
- **H2 콘솔**: <http://localhost:8080/h2-console> · JDBC URL `jdbc:h2:mem:assignment`

### AI Provider

현재는 외부 API 키 없이 동작하도록 `StubAiProvider`(`@Primary`)가 활성화되어 있고, `OpenAiProvider` / `AnthropicProvider`는 placeholder입니다.
실제 provider로 교체하려면 해당 클래스의 `complete`/`stream` 본문을 채우고 `@Primary`만 옮기면 됩니다. `AiProvider` 인터페이스 자체는 그대로 유지됩니다.

**Stub 정적 응답 — 키워드 분기표**

| 카테고리 | 인식 키워드 |
|---|---|
| 배송 | 배송, 택배, 도착, 운송장, 배달 |
| 주문 | 주문, 결제, 구매, 영수증, 구입 |
| 반품 | 반품, 교환, 환불, 취소, 돌려 |
| 상품 | 상품, 제품, 재고, 가격, 할인, 스펙, 사양 |
| 인사 | 안녕, hello, hi, 도움, 뭐, 무엇 |
| 기타 | (위 키워드 미포함 시) |

질문에 해당 키워드가 포함되면 카테고리에 맞는 고정 텍스트를 반환합니다. 실제 AI 호출은 일어나지 않습니다.

---

## 🔐 인증

모든 보호된 엔드포인트는 다음 헤더가 필요합니다.

```
Authorization: Bearer <JWT>
```

JWT는 `subject=email`, `uid=userId`, `role=MEMBER|ADMIN` claim을 포함하며, `JwtAuthenticationFilter`가 파싱해 `AuthenticatedUser(id, email, role)` principal을 SecurityContext에 세팅합니다. 컨트롤러는 `@AuthenticationPrincipal AuthenticatedUser user`로 받아 `user.id()` / `user.isAdmin()`로 접근합니다.

→ 결과적으로 `chat`, `feedback` 등 다운스트림 모듈은 **JWT가 전달한 `userId`만 신뢰**하고 `UserRepository`를 다시 조회하지 않습니다.

---

## 📡 API 명세

### 0. 응답 코드 일반 규칙

| 코드 | 의미 |
|---|---|
| `200 OK` | 정상 처리 |
| `201 Created` | 리소스 생성 |
| `204 No Content` | 삭제 성공 |
| `400 Bad Request` | 검증 실패 |
| `401 Unauthorized` | JWT 없음/만료/위변조 |
| `403 Forbidden` | 권한 부족(본인 리소스 아님, 관리자 전용 등) |
| `404 Not Found` | 리소스 없음 |
| `409 Conflict` | 중복(이메일, 1유저-1대화-1피드백 등) |

### 1. 인증 (`/auth`)

#### `POST /auth/signup` — 회원가입

```bash
curl -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@example.com","password":"demo1234","name":"데모"}'
```

응답:
```json
{ "tokenType": "Bearer", "accessToken": "eyJhbGciOi..." }
```

#### `POST /auth/login` — 로그인

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com","password":"admin1234"}'
```

---

### 2. 챗봇 (`/chats`, `/threads`)

#### `POST /chats` — 대화 생성 (동기 또는 SSE)

`isStreaming=false`(또는 생략) — JSON 단건 응답:
```bash
curl -X POST http://localhost:8080/chats \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"안녕?","model":"gpt-4o-mini"}'
```
```json
{
  "chatId": 1, "threadId": 1,
  "question": "안녕?", "answer": "...",
  "model": "gpt-4o-mini",
  "createdAt": "2026-04-07T13:00:00Z"
}
```

`isStreaming=true` — SSE 응답:
```bash
curl -N -X POST http://localhost:8080/chats \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"question":"긴 답변 부탁","isStreaming":true}'
```
이벤트 흐름: `event: thread` → `event: chunk` × N → `event: done`

> **30분 스레드 규칙**: 동일 사용자의 마지막 채팅 이후 30분 이내면 같은 `ChatThread`에 누적, 30분이 넘으면 새 스레드를 자동 생성합니다(`ChatService.resolveThread`).

#### `GET /chats` — 대화 목록 (스레드 단위 그룹핑)

```bash
curl "http://localhost:8080/chats?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer $TOKEN"
```
- **member**: 본인 스레드만
- **admin**: 전체 스레드

#### `DELETE /threads/{threadId}` — 스레드 삭제

본인 스레드만 삭제 가능 (요건상 "각 유저는 자신이 생성한 스레드만 삭제"). admin이라도 타인의 스레드는 삭제 불가.

---

### 3. 피드백 (`/feedbacks`)

#### `POST /feedbacks` — 피드백 생성

```bash
curl -X POST http://localhost:8080/feedbacks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"chatId":1,"positive":true}'
```
- **member**: 본인이 생성한 대화에만 피드백 가능
- **admin**: 모든 대화에 피드백 가능
- DB 유니크 제약 `(user_id, chat_id)`로 **한 사용자는 한 대화에 단 하나의 피드백만** — 중복 시 `409`
- 동일 대화에 **서로 다른 사용자들의 N개 피드백**은 허용

응답:
```json
{
  "id": 10, "userId": 2, "chatId": 1,
  "positive": true, "status": "PENDING",
  "createdAt": "2026-04-07T13:05:00Z"
}
```

#### `GET /feedbacks` — 피드백 목록

```bash
curl "http://localhost:8080/feedbacks?positive=true&page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer $TOKEN"
```
- **member**: 본인이 생성한 피드백만
- **admin**: 전체
- 쿼리 파라미터:
  - `positive=true|false` — 긍정/부정 필터 (생략 시 전체)
  - `page`, `size` — 페이지네이션
  - `sort=createdAt,desc` 또는 `createdAt,asc` — 생성일시 정렬

#### `PATCH /feedbacks/{feedbackId}/status` — 피드백 상태 변경 (**admin 전용**)

```bash
curl -X PATCH http://localhost:8080/feedbacks/10/status \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"RESOLVED"}'
```
일반 사용자가 호출하면 `403`. 상태값은 `PENDING` 또는 `RESOLVED`.

---

### 4. 분석/보고 (`/analytics`) — **admin 전용**

#### `GET /analytics/activity` — 지난 24시간 활동 카운트

```bash
curl http://localhost:8080/analytics/activity \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```
응답:
```json
{
  "since": "2026-04-06T13:00:00Z",
  "until": "2026-04-07T13:00:00Z",
  "signupCount": 5,
  "loginCount": 12,
  "chatCreatedCount": 37
}
```

> **구현 노트**: 회원가입/로그인/대화 생성은 각각 도메인 이벤트(`UserSignedUpEvent`, `UserLoggedInEvent`, `ChatCreatedEvent`)를 발행하고, `analytics` 컨텍스트의 리스너가 `@TransactionalEventListener(AFTER_COMMIT) + REQUIRES_NEW`로 `user_activity_logs`에 적재합니다. 적재 실패는 원본 트랜잭션을 롤백하지 않습니다.
>
> `AdminSeeder`는 `userRepository.save()` 직접 호출이라 이벤트가 발행되지 않으며, 따라서 시드 관리자는 카운트에 포함되지 않습니다.

#### `GET /analytics/report` — 지난 24시간 대화 CSV 보고서

```bash
curl http://localhost:8080/analytics/report \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -o chat-report.csv
```
응답 헤더:
```
Content-Type: text/csv;charset=UTF-8
Content-Disposition: attachment; filename="chat-report.csv"
```
CSV 컬럼:
```
chatId,threadId,userId,userEmail,userName,question,answer,createdAt
```
- 모든 사용자의 대화 + 작성자 정보 포함
- 콤마/따옴표/개행은 RFC 4180 기본 규칙으로 이스케이프(필드를 `"`로 감싸고 내부 `"`는 `""`로 이중화)

---

## 📁 프로젝트 구조

```
src/main/java/com/sdcodebase/assignment/
├── AssignmentApplication.java
├── config/                       # SecurityConfig, AdminSeeder
├── user/domain/                  # User, Role, UserRepository
├── auth/                         # 회원가입/로그인, JWT, 인증 필터
│   ├── domain/event/             # UserSignedUpEvent, UserLoggedInEvent
│   ├── application/              # AuthService, JwtTokenProvider
│   └── resource/                 # AuthController, JwtAuthenticationFilter
├── chat/                         # 챗봇, 30분 스레드, SSE
│   ├── domain/event/             # ChatCreatedEvent
│   ├── application/ai/           # AiProvider 추상화 + Stub/OpenAI/Anthropic
│   └── resource/                 # ChatController
├── feedback/                     # 피드백 CRUD + 권한 분기
└── analytics/                    # 활동 카운트 + CSV 보고서 (이벤트 구독)
    ├── application/UserActivityEventListener.java
    └── application/AnalyticsService.java
```
