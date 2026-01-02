#!/bin/bash

# Keycloak 데이터베이스 초기화 스크립트
# 이 스크립트는 Keycloak 데이터베이스 볼륨을 삭제하고 다시 시작하여
# ctrlf-realm.json 파일로 realm을 초기화합니다.

set -e

echo "=========================================="
echo "Keycloak 데이터베이스 초기화 시작"
echo "=========================================="

# 현재 디렉토리 확인
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Keycloak 서비스 중지
echo ""
echo "1. Keycloak 서비스 중지 중..."
docker-compose stop keycloak keycloak-init

# Keycloak 데이터베이스 볼륨 삭제
echo ""
echo "2. Keycloak 데이터베이스 볼륨 삭제 중..."
docker-compose rm -f keycloak keycloak-init 2>/dev/null || true
docker volume rm ctrlf-back_kc-db-data 2>/dev/null || true

echo ""
echo "3. Keycloak 데이터베이스 컨테이너 중지 및 삭제 중..."
docker-compose stop keycloak-db 2>/dev/null || true
docker-compose rm -f keycloak-db 2>/dev/null || true

# 데이터베이스 볼륨이 완전히 삭제되었는지 확인
echo ""
echo "4. 볼륨 삭제 확인 중..."
if docker volume ls | grep -q "ctrlf-back_kc-db-data"; then
    echo "경고: 볼륨이 아직 존재합니다. 강제로 삭제합니다..."
    docker volume rm -f ctrlf-back_kc-db-data 2>/dev/null || true
fi

# Keycloak realm JSON 파일 확인
echo ""
echo "5. Realm JSON 파일 확인 중..."
if [ ! -f "./keycloak-realms/ctrlf-realm.json" ]; then
    echo "오류: keycloak-realms/ctrlf-realm.json 파일을 찾을 수 없습니다!"
    exit 1
fi
echo "✓ ctrlf-realm.json 파일 발견"

# Keycloak 서비스 시작 (데이터베이스부터)
echo ""
echo "6. Keycloak 데이터베이스 시작 중..."
docker-compose up -d keycloak-db

# 데이터베이스가 준비될 때까지 대기
echo ""
echo "7. 데이터베이스 준비 대기 중..."
timeout=60
counter=0
while ! docker-compose exec -T keycloak-db pg_isready -U keycloak -d keycloak > /dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "오류: 데이터베이스가 준비되지 않았습니다!"
        exit 1
    fi
    sleep 1
    counter=$((counter + 1))
    echo -n "."
done
echo ""
echo "✓ 데이터베이스 준비 완료"

# Keycloak 서비스 시작 (--import-realm 옵션이 이미 docker-compose.yml에 설정되어 있음)
echo ""
echo "8. Keycloak 서비스 시작 중 (realm 자동 import)..."
docker-compose up -d keycloak

# Keycloak이 준비될 때까지 대기
echo ""
echo "9. Keycloak 준비 대기 중..."
timeout=120
counter=0
while ! curl -f http://localhost:8090/health/ready > /dev/null 2>&1; do
    if [ $counter -ge $timeout ]; then
        echo "경고: Keycloak이 준비되지 않았습니다 (타임아웃)"
        echo "      하지만 백그라운드에서 계속 시작 중일 수 있습니다."
        break
    fi
    sleep 2
    counter=$((counter + 2))
    echo -n "."
done

if curl -f http://localhost:8090/health/ready > /dev/null 2>&1; then
    echo ""
    echo "✓ Keycloak 준비 완료"
else
    echo ""
    echo "경고: Keycloak 헬스 체크 실패, 로그를 확인하세요"
fi

echo ""
echo "=========================================="
echo "Keycloak 초기화 완료!"
echo "=========================================="
echo ""
echo "다음 명령어로 상태를 확인할 수 있습니다:"
echo "  docker-compose ps"
echo "  docker-compose logs keycloak"
echo ""
echo "Keycloak Admin Console: http://localhost:8090"
echo "Admin 계정: admin / admin"
echo ""

