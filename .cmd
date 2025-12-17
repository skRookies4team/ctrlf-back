docker exec -it platform-postgres psql -U postgres -d db
\dn                           -- 스키마 목록
\dt education.*               -- education 스키마 테이블
\dt infra.*                   -- infra 스키마 테이블
SET search_path=education;    -- 필요시 기본 스키마 변경
\dt

# 실행
AWS_PROFILE=sk_4th_team04 ./gradlew :chat-service:bootRun
AWS_PROFILE=sk_4th_team04 ./gradlew :education-service:bootRun
AWS_PROFILE=sk_4th_team04 ./gradlew :infra-service:bootRun
AWS_PROFILE=sk_4th_team04 ./gradlew :chat-service:bootRun
AWS_PROFILE=sk_4th_team04 ./gradlew :api-gateway:bootRun

./gradlew clean build :infra-service:bootRun
 
# DB (Docker Compose)

docker compose up -d postgres
docker compose ps
docker compose logs -f postgres
docker compose down

Chat: http://localhost:9001/swagger-ui.html (OpenAPI: http://localhost:9001/v3/api-docs)
Education: http://localhost:9002/swagger-ui.html (OpenAPI: http://localhost:9002/v3/api-docs)
Infra: http://localhost:9003/swagger-ui.html (OpenAPI: http://localhost:9003/v3/api-docs)
Quiz: http://localhost:9004/swagger-ui.html (OpenAPI: http://localhost:9004/v3/api-docs)

교육 시드 데이터: ./gradlew :education-service:bootRun -Dspring-boot.run.profiles=local,local-seed
인프라 시드 데이터: ./gradlew :infra-service:bootRun --args='--spring.profiles.active=local,local-seed'


# 1) Keycloak 관련 컨테이너 정지
docker compose stop keycloak keycloak-db

# 2) 서비스 컨테이너 삭제(이미지/볼륨은 유지)
docker compose rm -f keycloak keycloak-db
# 또는 컨테이너 이름 기준(compose에 container_name 사용 중)
# docker rm -f platform-keycloak keycloak-postgres

# 3) 볼륨 삭제 (지금 막힌 그 볼륨)
docker volume rm ctrlf-back_kc-db-data

# 4) DB -> Keycloak 순으로 재기동
docker compose up -d keycloak-db
docker compose up -d keycloak

# 5) 기동 로그 확인
docker logs -f platform-keycloak