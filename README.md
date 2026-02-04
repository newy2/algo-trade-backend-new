# 프로젝트 설명

## 목적

Spring Boot WebMVC 와 JPA 학습을 위한 PoC 프로젝트로 Task 관리 기능을 구현합니다.

## 개발 환경

- JDK Version: 21
- Spring Boot Version: 4.0.2
- Local Machine: MacBook M1

## 사전 설치 프로그램

- Docker Desktop

해당 프로젝트는 Docker 실행 환경이 설치되어 있어야 합니다. 개발/테스트 DB를 Docker 컨테이너로 관리합니다.

## 실행 명령어

아래 명령어로 테스트 또는 애플리케이션을 실행할 수 있습니다.

### 테스트 실행 명령어

```bash
# 전체 테스트 실행 명령어
cd task && ./gradlew test

# 유닛 테스트 실행 명령어
cd task && ./gradlew test --tests "com.newy.task.unit.*"
```

### 애플리케이션 실행 명령어

```bash
cd task && ./gradlew bootRun
```

## DB 설정 (개발용, 테스트용)

DB는 PostgreSQL 과 MySQL 을 지원하고 설정값으로 DB를 선택할 수 있습니다.  
개발용 DB의 데이터 Docker Volume 에 저장되고, Docker Volume 경로는 `local-infra/db` 입니다.

```bash
docker-compose -f task-app/local-infra/docker-compose.yml up -d
```

Spring profile 로 개발/테스트용 DB를 선택할 수 있습니다.

```
## main/resources/application.properties 파일 (개발용 DB 선택)
spring.profiles.active=postgresql ## 개발 DB로 PostgreSQL 사용
# spring.profiles.active=mysql ## 개발 DB로 MySQL 사용

## test/resources/application.properties 파일 (테스트용 DB 선택)
spring.profiles.active=test,postgresql ## 테스트 DB로 PostgreSQL 사용
# spring.profiles.active=test,mysql ## 테스트 DB로 MySQL 사용
```

개발용 DB 데이터 생성은 `main/resources/application.properties` 파일의 `spring.liquibase.contexts` 값으로 결정합니다.
`spring.liquibase.contexts` 에 설정할 수 있는 값은 아래와 같습니다.

- `prod,use_feature_test_data`: Task 5건 및 User 등 생성
- `prod,use_performance_test_data`: Task 100,000건 및 User 등 생성

```
## main/resources/application.properties 파일 (데이터 생성 방법 선택)
spring.liquibase.contexts=prod,use_feature_test_data ## => Task 5건 및 User 등 생성
# spring.liquibase.contexts=prod,use_performance_test_data ## => Task 100,000건 및 User 등 생성
```

데이터 생성 스크립트는 `task-app/src/main/resources/local-infra/ddl/liquibase/020-init-data` 폴더에서 확인할 수 있습니다.

## test 폴더의 패키지 설명

- `com.newy.task.integration`: Spring Container, RDB 등을 사용하는 통합 테스트 코드를 작성합니다
- `com.newy.task.unit`: 외부 프로세스와 통신이 필요 없는 유닛 테스트 코드를 작성합니다

## main 폴더의 패키지 설명

해당 프로젝트는 헥사고날(Ports and Adapters) 아키텍처를 적용했습니다.

- `com.newy.task.common`: 모듈에서 공통으로 사용하는 코드 구현
- `com.newy.task.spring`: Spring 프레임워크에서 사용하는 코드 구현
- `도메인 이름 패키지` (`com.newy.task.task`, `com.newy.task.notification`)
    - `adapter.in`: 인커밍 어댑터 구현
    - `adapter.in.web`: 웹 어댑터 구현
    - `adapter.in.web.model`: 웹 요청/응답 DTO 구현
    - `adapter.out`: 아웃고잉 어댑터 구현
    - `adapter.out.internal_system`: 백그라운드 작업 어댑터 구현
    - `adapter.out.persistence`: 영속성 어댑터 구현
    - `adapter.out.persistence.jpa`: JPA Repository 구현
    - `adapter.out.persistence.jpa.model`: JPA Entity 구현
    - `adapter.out.persistence.querydsl`: Querydsl Repository 구현
    - `adapter.out.persistence.querydsl.model`: Querydsl DTO 구현
    - `domain`: 유스케이스(Service)에서 사용하는 데이터 모델 구현
    - `port.in`: 인커밍 포트 선언
    - `port.in.model`: 인커밍 포트 입력 모델 구현. 입력 데이터의 유효성 검증 담당
    - `port.out`: 아웃고잉 포트 선언
    - `service`: 유스케이스(Service) 구현
