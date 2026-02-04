FROM postgres:16

# 1. 빌드 필수 도구 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    postgresql-server-dev-16 \
    wget \
    ca-certificates \
    libicu-dev \
    && rm -rf /var/lib/apt/lists/*

# 2. pg_bigm 소스 다운로드 및 빌드
# USE_PGXS=1 옵션을 추가하여 PostgreSQL 확장 프로그램 빌드 모드로 실행합니다.
RUN wget https://github.com/pgbigm/pg_bigm/archive/v1.2-20240606.tar.gz \
    && tar xvzf v1.2-20240606.tar.gz \
    && cd pg_bigm-1.2-20240606 \
    && make USE_PGXS=1 install \
    && cd .. \
    && rm -rf pg_bigm-1.2-20240606 v1.2-20240606.tar.gz

# 3. (선택사항) 빌드 도구 제거하여 이미지 최적화
RUN apt-get purge -y --auto-remove build-essential postgresql-server-dev-16 wget