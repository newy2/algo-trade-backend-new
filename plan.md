# 후속 개선 계획

## 1. 검색 API page size 검증 정합성 맞추기

- 문제 요약:
  검색 요청 모델은 `size=0` 을 허용하지만 실제 검색 어댑터는 `PageRequest.of(page, size)` 를 사용한다.
  관련 파일은 `task/src/main/kotlin/com/newy/task/task/port/in/model/SearchTaskQuery.kt` 와 `task/src/main/kotlin/com/newy/task/task/adapter/out/persistence/SearchTaskAdapter.kt` 다.
- 왜 위험한지:
  API 입력 검증 단계에서는 통과한 요청이 런타임에서 예외를 일으킬 수 있다.
  클라이언트 입장에서는 허용된 계약처럼 보이는 값이 실제로는 실패하기 때문에 API 신뢰도가 떨어진다.
- 권장 수정 방향:
  입력 검증에서 `size >= 1` 을 강제해 런타임 제약과 맞춘다.
  필요하면 기본 페이지 크기 정책도 함께 문서화하고 테스트 케이스를 보강한다.

## 2. 마감 알림 조회 시 N+1 가능성 점검 및 개선

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
