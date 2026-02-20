FROM postgres:16 AS builder

RUN apt-get update && apt-get install -y \
    build-essential \
    postgresql-server-dev-16 \
    wget \
    libicu-dev

RUN wget https://github.com/pgbigm/pg_bigm/archive/v1.2-20240606.tar.gz \
    && tar xvzf v1.2-20240606.tar.gz \
    && cd pg_bigm-1.2-20240606 \
    && make USE_PGXS=1 install

FROM postgres:16
COPY --from=builder /usr/lib/postgresql/16/lib/pg_bigm.so /usr/lib/postgresql/16/lib/
COPY --from=builder /usr/share/postgresql/16/extension/pg_bigm* /usr/share/postgresql/16/extension/

RUN apt-get update && apt-get install -y \
    libicu-dev \
    && rm -rf /var/lib/apt/lists/*