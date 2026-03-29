# 아키텍처 가이드

## 목적과 적용 범위

이 문서는 이 저장소에서 기능을 추가하거나 변경하는 AI 에이전트와 개발자가 따라야 할 단일 기준 아키텍처 문서다.

- 이 문서는 저장소 구조, 레이어 경계, 기존 동작 보존 규칙, 테스트 배치 기준을 정의한다.
- 구현 방식, TDD 사이클, Tidy First, 커밋 규칙은 `AGENTS.md`를 따른다.
- 루트의 `plan.md`가 존재하면 작업 순서와 우선순위는 `plan.md`를 따른다.
- 새 기능이 작아 보여도 현재 프로젝트의 패키지 구조와 테스트 분리 원칙은 유지한다.

## AI 에이전트 사용 원칙

- 먼저 `AGENTS.md`, `ARCHITECTURE.md`, `plan.md`를 함께 읽고 역할을 나눠 이해한다.
- `AGENTS.md`는 개발 절차를, 이 문서는 구현 형태를 정의한다.
- 기능을 추가할 때는 기존 도메인과 테스트 패턴을 최대한 재사용한다.
- 사용자가 구조 변경을 명시적으로 요청하지 않았다면 패키지 구조를 평탄화하거나 단순화하지 않는다.
- 문서와 코드가 어긋난 것을 발견하면, 현재 작업에 직접 영향을 주는 경우에만 함께 정리한다.

## 프로젝트 구조 요약

- 실제 애플리케이션 코드는 `task` 모듈에 있다.
- 루트의 `local-infra`는 로컬 DB 및 컨테이너 실행 구성을 담는다.
- 루트의 `doc`는 다이어그램과 보조 문서를 담는다.
- 주요 비즈니스 도메인은 `com.newy.task.<domain>` 아래에 배치한다.
- 공통 검증과 공통 예외는 `com.newy.task.common`에 둔다.
- 인증, MVC 설정, DB 감지, 예외 처리 같은 프레임워크 코드는 `com.newy.task.spring`에 둔다.

## 도메인 패키지 기본 구조

기존 도메인을 확장하거나 새 도메인을 만들 때는 아래 구조를 기본으로 유지한다.

- `adapter.in`
- `adapter.in.web`
- `adapter.in.web.model`
- `adapter.out`
- `adapter.out.internal_system`
- `adapter.out.persistence`
- `adapter.out.persistence.jpa`
- `adapter.out.persistence.jpa.model`
- `adapter.out.persistence.querydsl`
- `adapter.out.persistence.querydsl.model`
- `domain`
- `port.in`
- `port.in.model`
- `port.out`
- `service`

작은 기능이라는 이유로 여러 책임을 한 패키지로 합치지 않는다.

## 신규 기능 구현 순서

### 1. 도메인 모델부터 정의한다

- 기능의 핵심 상태와 규칙을 먼저 `domain`에 만든다.
- HTTP 요청 형태나 DB 저장 형태보다 유스케이스가 필요로 하는 비즈니스 의미를 우선한다.
- 가능하면 Spring, JPA, Web 의존성을 도메인에 넣지 않는다.

예시:
- `task/src/main/kotlin/com/newy/task/task/domain/CreateTask.kt`
- `task/src/main/kotlin/com/newy/task/task/domain/UpdateTask.kt`
- `task/src/main/kotlin/com/newy/task/notification/domain/Notification.kt`

### 2. InPort 와 InPortModel 을 만든다

- 외부에서 호출할 유스케이스 계약을 `port.in`에 선언한다.
- 입력 검증, 문자열 enum 변환, 날짜 파싱, trim, 기본값 정리, 도메인 모델 변환은 `port.in.model`에 둔다.
- `port.in.model`은 검증이 끝난 입력을 도메인 모델로 바꿀 수 있어야 한다.

예시:
- `task/src/main/kotlin/com/newy/task/task/port/in/CreateTaskInPort.kt`
- `task/src/main/kotlin/com/newy/task/task/port/in/model/CreateTaskCommand.kt`
- `task/src/main/kotlin/com/newy/task/task/port/in/model/UpdateTaskCommand.kt`
- `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt`

### 3. Service 에 유스케이스를 구현한다

- 서비스는 `port.in` 구현체다.
- 서비스는 비즈니스 규칙과 흐름 제어를 담당하고 필요한 `port.out`을 조합한다.
- 트랜잭션 경계는 서비스 메서드에 둔다.
- 서비스는 Controller DTO, JPA Entity, SQL 세부 구현에 직접 의존하지 않는다.

예시:
- `task/src/main/kotlin/com/newy/task/task/service/TaskService.kt`
- `task/src/main/kotlin/com/newy/task/notification/service/NotificationService.kt`

### 4. Web 어댑터를 추가한다

- HTTP 진입점이 필요한 기능은 `adapter.in.web`에 Controller 를 둔다.
- Controller 는 웹 요청 DTO 를 `port.in.model`로 변환하고 InPort 를 호출한 뒤 웹 응답 DTO 로 매핑한다.
- Spring MVC 어노테이션과 인증 관련 어노테이션은 이 레이어에 둔다.
- 공통 인증, argument resolver, 예외 처리, 설정은 `com.newy.task.spring`에 둔다.

예시:
- `task/src/main/kotlin/com/newy/task/task/adapter/in/web/TaskController.kt`
- `task/src/main/kotlin/com/newy/task/task/adapter/in/web/model/CreateTaskWebRequest.kt`
- `task/src/main/kotlin/com/newy/task/spring/error/GlobalExceptionHandler.kt`

### 5. OutPort 와 Outgoing Adapter 를 추가한다

- 저장, 조회, 검색 인덱싱, 외부 발송처럼 서비스 바깥 책임이 필요하면 먼저 `port.out`을 정의한다.
- 실제 구현은 `adapter.out` 아래에서 담당한다.
- 단순 CRUD 와 복잡 조회를 억지로 한 클래스에 합치지 말고 역할이 다르면 어댑터를 분리한다.
- 영속성 어댑터는 도메인 모델을 반환하고, JPA Entity 는 영속성 레이어 밖으로 내보내지 않는다.

예시:
- `task/src/main/kotlin/com/newy/task/task/port/out/CreateTaskOutPort.kt`
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/TaskAdapter.kt`
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/SearchTaskAdapter.kt`
- `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/NotificationAdapter.kt`

### 6. DB 스키마와 영속성 모델을 반영한다

- 테이블 변경이 필요하면 Liquibase 변경 로그를 먼저 추가한다.
- JPA Entity, JPA Repository, Querydsl 조회 모델은 기능 복잡도에 맞게 추가한다.
- 검색 전용 테이블이나 복잡 조회 최적화가 필요하면 별도 엔티티와 Querydsl Repository 를 둔다.

예시:
- `task/src/main/resources/ddl/liquibase/master_change_log.xml`
- `task/src/main/resources/ddl/liquibase/010-table/020_task.xml`
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/jpa/model/TaskJpaEntity.kt`
- `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/querydsl/TaskFullTextSearchQuerydslRepository.kt`

### 7. 테스트를 레이어별로 함께 추가한다

- 기능 구현이 끝난 뒤 한꺼번에 테스트를 붙이지 말고, 레이어를 추가할 때 해당 레이어 테스트를 같이 추가한다.
- 최소 기준은 Controller 단위 테스트, Service 단위 테스트, InPortModel 단위 테스트, Persistence Adapter 통합 테스트다.
- 인증, 글로벌 예외 처리, 작은 인프라 로직도 현재 저장소의 테스트 분리 기준에 맞춰 검증한다.

## 레이어별 책임 규칙

### domain

- 비즈니스 의미가 드러나는 이름을 사용한다.
- 가능하면 프레임워크 어노테이션을 넣지 않는다.
- 저장 구조보다 유스케이스 중심 모델을 우선한다.

### port.in / port.in.model

- `port.in`은 유스케이스 계약만 정의한다.
- `port.in.model`은 요청값 검증과 도메인 변환 책임을 가진다.
- 문자열 enum 파싱, 날짜 파싱, 정규화, 중복 제거 전 검증 같은 입력 정리는 여기서 처리한다.

### service

- 서비스는 `port.in` 구현체로 두고 유스케이스의 순서와 규칙을 표현한다.
- 트랜잭션 경계는 서비스에 둔다.
- 서비스는 웹 DTO, JPA Entity, SQL 세부 구현에 의존하지 않는다.

### adapter.in.web

- Controller 는 HTTP 어댑터다.
- 비즈니스 규칙을 넣지 말고 InPort 호출과 요청/응답 매핑에 집중한다.
- 상태코드, 요청/응답 포맷, 인증 어노테이션을 명확히 드러낸다.

### adapter.out

- 저장, 조회, 외부 시스템 연동은 모두 `port.out` 뒤에 둔다.
- 도메인 모델과 저장 모델 간 매핑 책임은 어댑터가 가진다.
- Querydsl 기반 복잡 조회는 `adapter.out.persistence.querydsl`로 분리한다.

### spring / common

- 인증, 예외 처리, 설정, 스케줄링, DB 감지 같은 프레임워크성 코드는 `com.newy.task.spring`에 둔다.
- 여러 도메인이 함께 쓰는 검증기와 공통 예외는 `com.newy.task.common`에 둔다.
- 특정 도메인에만 속한 규칙을 성급하게 공통 패키지로 올리지 않는다.

## 반드시 유지해야 하는 기존 동작

아래 동작은 현재 시스템의 중요한 결합점이므로 기능 변경 시 함께 보존 여부를 확인한다.

- Task 생성/수정 흐름은 검색 인덱스 유지와 연결되어 있다.
- Task 생성/수정 흐름은 담당자 알림 생성과 연결될 수 있다.
- Task 검색은 별도 `task_full_text_search` 테이블을 사용한다.
- Notification 발송은 `event_key` 기준 중복 방지가 있는 outbox 흐름을 사용한다.
- 보호된 API 인증은 `X-User-Id` 요청 헤더를 기준으로 한다.
- 전역 요청/검증 에러는 글로벌 예외 처리기를 통해 정규화된다.

이 결합점을 변경하면 코드와 테스트를 함께 갱신한다.

## 테스트 전략과 실행 기준

현재 저장소는 단위 테스트와 통합 테스트를 분리하므로, 새 기능도 같은 기준을 따른다.

### 단위 테스트

대상:
- Controller 와 웹 어댑터
- Service / Use case
- InPortModel
- Domain 규칙
- Scheduler 와 작은 인프라 로직

검증할 것:
- 요청 파라미터 매핑
- 응답 매핑
- 입력 검증
- 비즈니스 규칙
- OutPort 호출 인자
- 순서가 중요한 호출의 실행 순서
- 공개 진입점의 Spring 어노테이션

예시:
- `task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt`
- `task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt`
- `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt`
- `task/src/test/kotlin/com/newy/task/unit/notification/adapter/in/internal_system/SendNotificationSchedulerTest.kt`

### 통합 테스트

대상:
- Persistence Adapter
- Querydsl 조회
- Liquibase / JPA 매핑
- 인증 인터셉터와 argument resolver
- 글로벌 예외 처리

예시:
- `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/TaskAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/SearchTaskAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/notification/out/persistence/NotificationAdapterTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/spring/auth/FakeLoginTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/spring/auth/CurrentUserArgumentResolverTest.kt`
- `task/src/test/kotlin/com/newy/task/integration/spring/error/GlobalExceptionHandlerTest.kt`

### 테스트 배치 규칙

- `src/test/kotlin` 아래에서 운영 코드 패키지 구조를 최대한 따라간다.
- `unit`과 `integration`을 섞지 않는다.
- 운영 코드가 `task/.../service` 아래에 있으면 같은 의미의 테스트 패키지를 먼저 만든다.

### 테스트 실행 기준

- 전체 비장기 테스트 실행:
  `cd task && ./gradlew test`
- 개발 중에는 더 좁은 테스트 실행을 사용할 수 있다.
- 작업 마무리 전에는 저장소의 기본 전체 테스트 명령을 다시 실행한다.

## DB/Search 관련 주의사항

- 개발 기본 프로필은 PostgreSQL 기준이다.
- 테스트 기본 프로필은 MySQL 기준이다.
- 스키마 생성과 변경은 Liquibase 가 관리한다.
- 전문 검색 동작은 DB 구현 차이를 탈 수 있으므로 PostgreSQL 과 MySQL 을 함께 의식한다.
- 통합 테스트의 기본 경로는 Testcontainers 다.

관련 파일:
- `task/src/main/resources/application.properties`
- `task/src/test/resources/application.properties`
- `task/src/test/kotlin/com/newy/task/integration/config/TestcontainersConfiguration.kt`

## 변경 금지 또는 주의 가드레일

사용자가 명시적으로 요청하지 않았다면 아래 변경은 피한다.

- 현재 패키지 구조를 기술 레이어 중심 단일 구조로 바꾸는 일
- 입력 검증을 `port.in.model`에서 Controller 로 옮기는 일
- JPA Entity 를 영속성 레이어 밖으로 노출하는 일
- Controller 가 포트를 건너뛰고 어댑터를 직접 호출하는 일
- 영속성 통합 테스트를 모두 목 기반 테스트로 대체하는 일
- Controller / Service 의 어노테이션 검증 테스트를 제거하는 일
- 데이터베이스 로직을 서비스로 합쳐 넣는 일

## 구현 체크리스트

- 도메인 모델이 HTTP 요청 구조나 JPA 저장 구조에 끌려가지 않았는가
- `port.in`, `port.out`이 유스케이스 중심 이름으로 정의되었는가
- 입력 검증이 `port.in.model`에 모여 있는가
- 서비스가 도메인 규칙과 흐름 제어만 담당하는가
- Controller 가 비즈니스 로직 없이 InPort 호출만 수행하는가
- 필요한 Liquibase 변경 로그가 추가되었는가
- 필요한 JPA Entity, Repository, Querydsl 조회 로직이 추가되었는가
- 필요한 단위 테스트와 통합 테스트가 레이어 기준에 맞게 추가되었는가
- 인증, 공통 예외 처리, 스케줄링에 영향이 있으면 `com.newy.task.spring`도 함께 점검했는가
- 기존 동작 결합점과 DB 별 차이를 함께 확인했는가

## 참고 예시

신규 기능 설계 시 아래 파일들을 우선 참고한다.

- 유스케이스 흐름 예시:
  `task/src/main/kotlin/com/newy/task/task/service/TaskService.kt`
- 웹 어댑터 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/in/web/TaskController.kt`
- 입력 모델 예시:
  `task/src/main/kotlin/com/newy/task/task/port/in/model/CreateTaskCommand.kt`
  `task/src/main/kotlin/com/newy/task/task/port/in/model/UpdateTaskCommand.kt`
  `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt`
- 영속성 어댑터 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/TaskAdapter.kt`
  `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/NotificationAdapter.kt`
- Querydsl 조회 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/querydsl/TaskFullTextSearchQuerydslRepository.kt`
- 공통 인증/예외 처리 예시:
  `task/src/main/kotlin/com/newy/task/spring/auth/interceptor/AuthenticationInterceptor.kt`
  `task/src/main/kotlin/com/newy/task/spring/error/GlobalExceptionHandler.kt`

## 미래 에이전트를 위한 실무 메모

- 가능한 한 작은 행위 변경부터 진행한다.
- 주변 코드가 한국어 테스트 이름을 쓰고 있으면 같은 스타일을 유지한다.
- 기존 테스트 헬퍼를 재사용할 수 있으면 새 패턴을 만들기보다 먼저 확장한다.
- `CurrentUserArgumentResolver` 등록 중복, `DatabaseTypeProvider` 초기화 시 리소스 처리, 글로벌 예외 처리의 넓은 400 매핑은 관찰 포인트지만, 관련 작업이 아닌데 선제적으로 고치지는 않는다.
