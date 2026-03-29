# 후속 개선 계획

## 1. 마감 알림 조회 시 N+1 가능성 점검 및 개선

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
