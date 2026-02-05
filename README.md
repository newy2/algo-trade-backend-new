# 프로젝트 설명

## 목적

Spring Boot WebMVC 와 JPA 학습을 위한 PoC 프로젝트로 Task 관리 기능을 구현한다.

## 개발 환경

- JDK Version: 21
- Spring Boot Version: 4.0.2
- Local Machine: MacBook M1

## 사전 설치 프로그램

- [Docker Desktop](https://www.docker.com/)

해당 프로젝트는 개발/테스트 DB를 Docker 컨테이너로 실행하므로, Docker 실행 환경이 설치되어 있어야 한다.

## 실행 명령어

### 테스트 실행 명령어

```bash
# 전체 테스트 실행 명령어
cd task && ./gradlew test

# or 유닛 테스트 실행 명령어
cd task && ./gradlew test --tests "com.newy.task.unit.*"
```

### 애플리케이션 실행 명령어

```bash
# 1. 개발용 DB 실행
docker-compose -f task-app/local-infra/docker-compose.yml up -d

# 2. 애플리케이션 실행
cd task && ./gradlew bootRun
```

## 구현 전략

해당 프로젝트는 헥사고날 아키텍처(또는 포트 어댑터 아키텍처)를 적용한다.

### main 폴더의 패키지 설명

- `com.newy.task.common`: 공통으로 사용하는 코드 구현
- `com.newy.task.spring`: Spring 프레임워크에서 사용하는 코드 구현
- `도메인 이름 패키지` (예: `com.newy.task.task` or `com.newy.task.notification`)
    - `adapter.in`: 인커밍 어댑터 구현
    - `adapter.in.web`: 웹 어댑터 구현 (계층형 아키텍처의 Controller 컴포넌트)
    - `adapter.in.web.model`: 웹 요청/응답 DTO 구현
    - `adapter.out`: 아웃고잉 어댑터 구현
    - `adapter.out.internal_system`: 백그라운드 작업 어댑터 구현
    - `adapter.out.persistence`: 영속성 어댑터 구현 (계층형 아키텍처의 Repository 컴포넌트)
    - `adapter.out.persistence.jpa`: JPA Repository 구현
    - `adapter.out.persistence.jpa.model`: JPA Entity 구현
    - `adapter.out.persistence.querydsl`: Querydsl Repository 구현
    - `adapter.out.persistence.querydsl.model`: Querydsl DTO 구현
    - `domain`: 유스케이스에서 사용하는 데이터 모델 구현
    - `port.in`: 인커밍 포트 선언. `adapter.in`에서 호출하고, `service`에서 구현한다.
    - `port.in.model`: 인커밍 포트 입력 모델 구현. 입력 데이터의 유효성 검증 담당
    - `port.out`: 아웃고잉 포트 선언. `service`에서 호출하고, `adapter.out`에서 구현한다
    - `service`: 유스케이스 구현 (계층형 아키텍처의 Service 컴포넌트)
    -

## 테스트 전략

헥사고날 아키텍처의 컴포넌트는 아래와 같은 테스트 전략을 사용한다.

### test 폴더의 패키지 설명

- `com.newy.task.integration`: Spring Container, RDB 등을 사용하는 통합 테스트 코드를 작성한다
- `com.newy.task.unit`: 외부 프로세스와 통신이 필요 없는 유닛 테스트 코드를 작성한다

### 인커밍 어댑터 (계층형 아키텍처의 Controller)

- 단위 테스트로 기능을 검증한다
- 테스트는 InPort 로 전달하는 파라미터를 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt#L100-L105
- 테스트는 인커밍 어댑터의 전달받은 응답 데이터를 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt#L107-L110
- 테스트는 인커밍 어댑터의 Spring Annotation 사용 여부를 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/adapter/in/web/TaskControllerTest.kt#L229-L244

### 유스케이스 (계층형 아키텍처의 Service)

- 단위 테스트로 기능을 검증한다
- 테스트는 비즈니스 규칙을 검증해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt#L127-L139
- 테스트는 OutPort 로 전달하는 파라미터를 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt#L79-L84
- (필요한 경우) 테스트는 OutPort 호출 순서를 확인한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt#L91-L99
- 테스트는 유스케이스의 Spring Annotation 사용 여부를 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/service/TaskServiceTest.kt#L315-L325

### 유스케이스의 입력 파라미터 (InPortModel 로 표기)

- 단위 테스트로 기능을 검증한다
- 테스트는 InPortModel 을 DomainModel 로 변환을 확인해야 한다  
  https://github.com/0newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt#L35-L44
- 테스트는 입력 데이터의 유효성 검증을 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/unit/task/port/in/model/CreateTaskCommandTest.kt#L47-L53

### 아웃고잉 어댑터 (계층형 아키텍처의 Repository)

- 통합 테스트로 기능을 검증한다
- 테스트는 DB에 데이터가 저장/조회 되는지 확인해야 한다  
  https://github.com/newy2/algo-trade-backend-new/blob/803a0960612f66874355c1b29dba1edea007498b/task/src/test/kotlin/com/newy/task/integration/task/out/persistence/TaskAdapterTest.kt#L109-L128

---

## DB 관련 설명

### 개발/테스트용 DB 설정

해당 프로젝트는 PostgreSQL 과 MySQL 지원하고 Spring Profile 로 DB를 선택한다.  
개발용 DB의 데이터는 `local-infra/db` 폴더에 저장된다.

```
[main/resources/application.properties 파일]
spring.profiles.active=dev,postgresql #개발용 DB로 PostgreSQL 사용
```

```
[test/resources/application.properties 파일]
spring.profiles.active=test,mysql #테스트용 DB로 MySQL 사용
```

### 개발용 DB의 테스트 데이터 생성 방법

개발 테스트 데이터 생성은 `spring.liquibase.contexts` 설정 값으로 선택한다.  
개발 테스트 데이터 생성 스크립트는 `task/src/main/resources/local-infra/ddl/liquibase/020-init-data` 폴더에서 확인한다.

```
[main/resources/application.properties 파일]
spring.liquibase.contexts=prod,use_feature_test_data ## => Task 5건 및 User 등 생성
```

```
[main/resources/application.properties 파일]
spring.liquibase.contexts=prod,use_performance_test_data ## => Task 100,000건 및 User 등 생성
```


