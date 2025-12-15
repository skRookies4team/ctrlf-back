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