# 신규 기능 구현 가이드

## 목적

이 문서는 이 프로젝트에 신규 기능을 추가할 때 따라야 할 기본 구현 규칙과 테스트 전략을 정리한 가이드다.
새 기능은 현재 코드베이스가 따르고 있는 헥사고날 아키텍처와 테스트 분리 원칙을 그대로 유지하는 것을 기본으로 한다.

## 기본 원칙

- 신규 기능은 기존 `task`, `notification` 도메인과 같은 방식으로 도메인 중심으로 설계한다.
- 웹, 서비스, 영속성 관심사를 섞지 않고 각 레이어의 책임을 분리한다.
- 입력 검증은 가능한 한 `port.in.model` 에서 수행하고, 비즈니스 규칙은 `service` 와 `domain` 에 둔다.
- 데이터 저장 방식이나 외부 시스템 연동 방식은 `port.out` 과 `adapter.out` 뒤로 숨긴다.
- 기능 추가 시 테스트도 같은 레이어 구조로 함께 추가한다.

## 프로젝트 구조 요약

`com.newy.task.<도메인명>` 패키지 아래에 기능을 배치하는 것을 기본으로 한다.

- `domain`
  핵심 비즈니스 모델과 규칙을 둔다. Spring, JPA, Web 의존성을 넣지 않는다.
- `port.in`
  유스케이스 진입점을 정의한다. 서비스가 구현해야 하는 인터페이스다.
- `port.in.model`
  유스케이스 입력 모델을 둔다. 입력값 검증과 도메인 모델 변환 책임을 가진다.
- `port.out`
  서비스가 외부에 기대하는 동작을 정의한다. 저장소, 검색 인덱스, 외부 알림 등이 여기에 해당한다.
- `service`
  유스케이스를 구현한다. 트랜잭션 경계와 비즈니스 흐름 제어를 담당한다.
- `adapter.in`
  유스케이스를 호출하는 진입 어댑터를 둔다.
- `adapter.in.web`
  Controller 를 둔다. HTTP 요청/응답 변환과 InPort 호출만 담당한다.
- `adapter.in.web.model`
  웹 요청/응답 DTO 를 둔다.
- `adapter.out`
  `port.out` 구현체를 둔다.
- `adapter.out.persistence`
  DB 저장/조회 로직과 도메인-엔티티 매핑을 둔다.
- `adapter.out.persistence.jpa`
  Spring Data JPA Repository 를 둔다.
- `adapter.out.persistence.jpa.model`
  JPA Entity 를 둔다.
- `adapter.out.persistence.querydsl`
  Querydsl 기반 조회 로직을 둔다.
- `adapter.out.internal_system`
  스케줄러, 내부 발행기 등 백그라운드 실행 어댑터를 둔다.

## 신규 기능 구현 순서

### 1. 도메인 모델부터 정의한다

- 기능의 핵심 상태와 규칙을 `domain` 에 먼저 만든다.
- 저장 방식이나 HTTP 스펙보다, 유스케이스가 어떤 데이터와 규칙을 필요로 하는지 먼저 정리한다.
- 예시:
  `task/src/main/kotlin/com/newy/task/task/domain/CreateTask.kt`
  `task/src/main/kotlin/com/newy/task/task/domain/UpdateTask.kt`
  `task/src/main/kotlin/com/newy/task/notification/domain/Notification.kt`

### 2. InPort 와 InPortModel 을 만든다

- 외부에서 어떤 유스케이스를 호출할지 `port.in` 에 선언한다.
- 입력값 검증, 문자열 enum 변환, 날짜 파싱, 기본값 정리는 `port.in.model` 에 둔다.
- `port.in.model` 은 검증 완료 후 도메인 모델로 변환할 수 있어야 한다.
- 예시:
  `task/src/main/kotlin/com/newy/task/task/port/in/CreateTaskInPort.kt`
  `task/src/main/kotlin/com/newy/task/task/port/in/model/CreateTaskCommand.kt`
  `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt`

### 3. Service 에 유스케이스를 구현한다

- 서비스는 `port.in` 을 구현하고 `port.out` 을 조합해 흐름을 완성한다.
- 트랜잭션 경계는 서비스 메서드에 둔다.
- 서비스는 웹 DTO 나 JPA Entity 를 직접 다루지 않고 도메인 모델과 포트만 사용한다.
- 예시:
  `task/src/main/kotlin/com/newy/task/task/service/TaskService.kt`
  `task/src/main/kotlin/com/newy/task/notification/service/NotificationService.kt`

### 4. Web 어댑터를 추가한다

- HTTP 요청이 필요한 기능이면 `adapter.in.web` 에 Controller 를 추가한다.
- 웹 레이어는 요청 DTO 를 `port.in.model` 로 바꾸고 InPort 를 호출한 뒤, 응답 DTO 로 변환하는 역할만 맡는다.
- 인증, 공통 예외 처리, argument resolver 같은 프레임워크성 로직은 `com.newy.task.spring` 에 둔다.
- 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/in/web/TaskController.kt`
  `task/src/main/kotlin/com/newy/task/task/adapter/in/web/model/CreateTaskWebRequest.kt`
  `task/src/main/kotlin/com/newy/task/spring/error/GlobalExceptionHandler.kt`

### 5. OutPort 와 Outgoing Adapter 를 추가한다

- 저장, 조회, 검색 인덱싱, 외부 발송처럼 서비스 밖 책임이 필요하면 먼저 `port.out` 을 정의한다.
- 그 다음 `adapter.out` 에서 구현한다.
- 단순 CRUD 와 복잡 조회를 한 클래스에 억지로 합치지 말고, 역할이 다르면 어댑터를 분리한다.
- 예시:
  `task/src/main/kotlin/com/newy/task/task/port/out/CreateTaskOutPort.kt`
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/TaskAdapter.kt`
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/SearchTaskAdapter.kt`
  `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/NotificationAdapter.kt`

### 6. DB 스키마와 영속성 모델을 반영한다

- 테이블 변경이 필요하면 Liquibase 변경 로그를 먼저 추가한다.
- JPA Entity, JPA Repository, Querydsl 조회 모델을 기능 복잡도에 맞게 추가한다.
- 검색 전용 테이블이나 조인 최적화가 필요하면 별도 엔티티와 Querydsl Repository 를 둔다.
- 예시:
  `task/src/main/resources/ddl/liquibase/master_change_log.xml`
  `task/src/main/resources/ddl/liquibase/010-table/020_task.xml`
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/jpa/model/TaskJpaEntity.kt`
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/querydsl/TaskFullTextSearchQuerydslRepository.kt`

### 7. 테스트를 레이어별로 추가한다

- 기능 구현이 끝난 뒤 테스트를 붙이는 방식보다, 레이어를 추가할 때 해당 레이어 테스트를 같이 추가한다.
- 최소 기준은 Controller 단위 테스트, Service 단위 테스트, InPortModel 단위 테스트, Persistence Adapter 통합 테스트다.

## 레이어별 규칙

### domain

- 도메인은 비즈니스 의미가 드러나는 이름을 사용한다.
- 가능하면 프레임워크 어노테이션을 넣지 않는다.
- 영속성 저장 형태보다 유스케이스 중심의 모델을 우선한다.

### port.in / port.in.model

- `port.in` 은 유스케이스 계약만 정의한다.
- `port.in.model` 은 요청값 검증과 도메인 모델 변환 책임을 가진다.
- 문자열 enum 파싱, 날짜 파싱, trim, 중복 제거 전 검증 같은 입력 정리는 여기서 처리한다.

### service

- 서비스는 `port.in` 구현체로 두고, 유스케이스의 순서와 규칙을 표현한다.
- 트랜잭션 경계는 서비스에 둔다.
- 서비스는 Controller DTO, JPA Entity, SQL 세부 구현에 의존하지 않는다.

### adapter.in.web

- Controller 는 HTTP 어댑터다.
- 비즈니스 규칙을 넣지 말고 InPort 호출에 집중한다.
- 요청/응답 포맷, 상태코드, 인증 어노테이션 적용 여부를 명확히 드러낸다.

### adapter.out

- 저장/조회/외부 연동 구현은 모두 `port.out` 뒤에 둔다.
- 도메인 모델과 저장 모델 간 매핑 책임은 어댑터가 가진다.
- 복잡 조회는 Querydsl Repository 나 별도 조회 어댑터로 분리한다.

### spring / common

- 공통 인증, 예외 처리, 설정, 커스텀 함수 등록 같은 프레임워크성 코드는 `com.newy.task.spring` 에 둔다.
- 여러 도메인이 함께 쓰는 검증기나 공통 예외는 `com.newy.task.common` 에 둔다.
- 특정 도메인에서만 쓰는 규칙을 공통 패키지로 올리지 않는다.

## 테스트 전략

### 1. Controller 단위 테스트

- 목적:
  Controller 가 InPort 에 올바른 파라미터를 넘기는지 검증한다.
- 확인할 것:
  요청 DTO 가 InPortModel 로 정확히 변환되는지
  응답 DTO 와 상태코드가 올바른지
  Spring MVC 어노테이션과 인증 관련 어노테이션이 의도대로 붙어 있는지
- 예시:
  `task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt`

### 2. Service 단위 테스트

- 목적:
  유스케이스의 비즈니스 규칙과 OutPort 호출 흐름을 검증한다.
- 확인할 것:
  도메인 규칙 검증
  OutPort 전달값 검증
  여러 OutPort 호출 순서 검증
  `@Transactional` 사용 여부 검증
- 예시:
  `task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt`
  `task/src/test/kotlin/com/newy/task/unit/notification/service/NotificationServiceTest.kt`

### 3. InPortModel 단위 테스트

- 목적:
  입력 검증과 도메인 모델 변환을 빠르게 검증한다.
- 확인할 것:
  필수값, 형식, 범위, 중복값 검증
  문자열 정규화와 도메인 모델 변환 결과
- 예시:
  `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt`
  `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/UpdateTaskCommandTest.kt`
  `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/SearchTaskQueryTest.kt`

### 4. Persistence Adapter 통합 테스트

- 목적:
  실제 DB 저장/조회/검색 동작을 검증한다.
- 확인할 것:
  엔티티 저장과 조회 결과
  조인/검색/인덱싱 동작
  DB 별 차이가 있는 경우 프로필 기반 동작 확인
- 예시:
  `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/TaskAdapterTest.kt`
  `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/SearchTaskAdapterTest.kt`
  `task/src/test/kotlin/com/newy/task/integration/notification/out/persistence/NotificationAdapterTest.kt`

## 구현 체크리스트

- 도메인 모델이 HTTP 요청 구조나 JPA 저장 구조에 끌려가지 않았는가
- `port.in` 과 `port.out` 이 유스케이스 중심 이름으로 정의되었는가
- 입력 검증이 `port.in.model` 에 모여 있는가
- 서비스가 도메인 규칙과 흐름 제어만 담당하고 있는가
- Controller 가 비즈니스 로직 없이 InPort 호출만 수행하는가
- 필요한 Liquibase 변경 로그가 추가되었는가
- 필요한 JPA Entity, Repository, Querydsl 조회 로직이 추가되었는가
- Controller 단위 테스트가 추가되었는가
- Service 단위 테스트가 추가되었는가
- InPortModel 단위 테스트가 추가되었는가
- Persistence Adapter 통합 테스트가 추가되었는가
- 신규 기능이 공통 예외 처리, 인증, 스케줄링에 영향을 주면 `com.newy.task.spring` 도 함께 점검했는가

## 기존 코드 예시 참조

신규 기능 설계 시 아래 파일들을 우선 참고한다.

- 전체 유스케이스 흐름 예시:
  `task/src/main/kotlin/com/newy/task/task/service/TaskService.kt`
- 웹 어댑터 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/in/web/TaskController.kt`
- 입력 모델 예시:
  `task/src/main/kotlin/com/newy/task/task/port/in/model/CreateTaskCommand.kt`
  `task/src/main/kotlin/com/newy/task/task/port/in/model/UpdateTaskCommand.kt`
- 영속성 어댑터 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/TaskAdapter.kt`
  `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/NotificationAdapter.kt`
- Querydsl 조회 예시:
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/querydsl/TaskFullTextSearchQuerydslRepository.kt`
- 공통 인증/예외 처리 예시:
  `task/src/main/kotlin/com/newy/task/spring/auth/interceptor/AuthenticationInterceptor.kt`
  `task/src/main/kotlin/com/newy/task/spring/error/GlobalExceptionHandler.kt`
- 테스트 예시:
  `task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt`
  `task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt`
  `task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt`
  `task/src/test/kotlin/com/newy/task/integration/task/out/persistence/TaskAdapterTest.kt`

## 테스트 실행 기준

- 전체 테스트:
  `cd task && ./gradlew test`
- 유닛 테스트만 빠르게 확인:
  `cd task && ./gradlew test --tests "com.newy.task.unit.*"`

신규 기능은 구현 코드와 같은 수준으로 테스트를 갖추는 것을 완료 기준으로 본다.
