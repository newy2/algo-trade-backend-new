# 후속 개선 계획

## 1. 예외 처리 기본 정책 개선

- 문제 요약:
  예상하지 못한 예외까지 `400 Bad Request` 로 응답하는 기본 정책이 있다.
  관련 파일은 `task/src/main/kotlin/com/newy/task/spring/error/GlobalExceptionHandler.kt` 다.
- 왜 위험한지:
  서버 내부 버그와 잘못된 클라이언트 요청이 같은 범주로 보이게 되어 장애 원인 파악이 어려워진다.
  운영 중 실제 서버 오류를 클라이언트 책임처럼 보이게 만들어 모니터링과 대응이 늦어질 수 있다.
- 권장 수정 방향:
  알 수 없는 예외는 기본적으로 `500 Internal Server Error` 로 처리하고 서버 로그를 남긴다.
  `400`, `404`, `409` 처럼 의도적으로 분류한 예외만 명시적으로 유지한다.

## 2. CurrentUserArgumentResolver 중복 등록 제거

- 문제 요약:
  `CurrentUserArgumentResolver` 가 설정 클래스 두 곳에서 중복 등록되고 있다.
  관련 파일은 `task/src/main/kotlin/com/newy/task/spring/auth/config/WebConfig.kt` 와 `task/src/main/kotlin/com/newy/task/spring/auth/config/ArgumentResolverConfig.kt` 다.
- 왜 위험한지:
  지금은 큰 장애가 없더라도 설정 책임이 분산되어 유지보수 시 혼란을 만든다.
  이후 인증 관련 설정을 변경할 때 한쪽만 수정하고 다른 쪽을 놓칠 가능성이 있다.
- 권장 수정 방향:
  argument resolver 등록 책임을 한 설정 클래스로만 모은다.
  `CurrentUserArgumentResolver` 빈 생성 방식도 함께 통일해 설정 경로를 단일화한다.

## 3. 검색 API page size 검증 정합성 맞추기

- 문제 요약:
  검색 요청 모델은 `size=0` 을 허용하지만 실제 검색 어댑터는 `PageRequest.of(page, size)` 를 사용한다.
  관련 파일은 `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt` 와 `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/SearchTaskAdapter.kt` 다.
- 왜 위험한지:
  API 입력 검증 단계에서는 통과한 요청이 런타임에서 예외를 일으킬 수 있다.
  클라이언트 입장에서는 허용된 계약처럼 보이는 값이 실제로는 실패하기 때문에 API 신뢰도가 떨어진다.
- 권장 수정 방향:
  입력 검증에서 `size >= 1` 을 강제해 런타임 제약과 맞춘다.
  필요하면 기본 페이지 크기 정책도 함께 문서화하고 테스트 케이스를 보강한다.

## 4. 마감 알림 조회 시 N+1 가능성 점검 및 개선

- 문제 요약:
  마감 임박 Task 조회 후 assignee 목록을 접근하는 과정에서 추가 쿼리가 반복될 가능성이 있다.
  관련 파일은 `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/querydsl/TaskNotificationQuerydslRepository.kt`,
  `task/src/main/kotlin/com/newy/task/notification/adapter/out/persistence/NotificationAdapter.kt`,
  `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/jpa/model/TaskJpaEntity.kt` 다.
- 왜 위험한지:
  데이터가 적을 때는 드러나지 않지만, 마감 대상 Task 수가 늘면 쿼리 수가 급격히 증가할 수 있다.
  스케줄러 기반 배치 로직에서 성능 저하가 누적되면 알림 생성 지연으로 이어질 수 있다.
- 권장 수정 방향:
  마감 알림 조회 시 assignee 정보까지 한 번에 가져오는 조회 전략을 사용한다.
  fetch join, 별도 projection, 전용 조회 모델 중 현재 구조에 맞는 방식을 선택하고 통합 테스트에서 쿼리 패턴을 함께 점검한다.
