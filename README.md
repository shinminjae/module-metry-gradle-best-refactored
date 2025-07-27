# Module Metry - AWS IoT 데이터 수집 및 모니터링 시스템

## 📋 프로젝트 개요

Module Metry는 AWS IoT Core를 통해 실시간 IoT 디바이스 데이터를 수집하고, 웹 인터페이스를 통해 데이터를 검색하고 모니터링할 수 있는 Spring Boot 기반 애플리케이션입니다.

## 🏗️ 기술 스택

- **Backend**: Spring Boot 3.1.2, Java 17
- **Database**: SQLite
- **ORM**: MyBatis
- **Frontend**: Thymeleaf, Bootstrap 5.3.0
- **AWS IoT**: AWS IoT Device SDK (CRT 기반)
- **Build Tool**: Gradle
- **기타**: Lombok

## 🚀 주요 기능

### 1. AWS IoT Core 구독
- MQTT 프로토콜을 통한 실시간 데이터 수신
- 다중 토픽 구독 지원
- 인증서 기반 보안 연결

### 2. 데이터 수집 및 저장
- SQLite 데이터베이스에 IoT 메시지 저장
- 자동 데이터 정리 (최대 1000개 메시지 유지)
- 메시지 파싱 및 구조화

### 3. 웹 인터페이스
- **검색 페이지**: 다양한 조건으로 IoT 데이터 검색
- **실시간 모니터링**: 최신 50개 메시지 실시간 표시
- **CSV 내보내기**: 검색 결과를 CSV 파일로 다운로드

## 📁 프로젝트 구조

```
module-metry-gradle-best/
├── build.gradle                 # Gradle 빌드 설정
├── settings.gradle              # 프로젝트 설정
├── src/
│   └── main/
│       ├── java/
│       │   └── com/daedong/agmtms/
│       │       ├── common/
│       │       │   └── AwsIotCoreSubscribe.java    # AWS IoT 구독 처리
│       │       ├── metry/
│       │       │   ├── controllers/
│       │       │   │   ├── IotMessageController.java    # 메인 컨트롤러
│       │       │   │   └── RealtimeController.java      # 실시간 API
│       │       │   ├── dao/
│       │       │   │   └── IotMessageMapper.java        # MyBatis 매퍼
│       │       │   ├── dto/
│       │       │   │   └── IotMessageDto.java           # 데이터 전송 객체
│       │       │   └── services/
│       │       │       └── IotMessageService.java       # 비즈니스 로직
│       │       └── ModuleMetryApplication.java          # 메인 애플리케이션
│       └── resources/
│           ├── application.yml                           # 애플리케이션 설정
│           ├── sql/
│           │   └── mapper/
│           │       ├── IotMessageMapper.xml             # MyBatis SQL 매핑
│           │       └── schema.sql                       # 데이터베이스 스키마
│           └── templates/
│               ├── search.html                          # 검색 페이지
│               └── realtime.html                        # 실시간 모니터링 페이지
└── certs/                                                # AWS IoT 인증서 디렉토리
```

## ⚙️ 설정

### 1. AWS IoT 설정

`application.yml` 파일에서 AWS IoT 설정을 구성해야 합니다:

```yaml
aws:
  iot:
    endpoint: your-aws-iot-endpoint.amazonaws.com
    clientId: your-client-id
    certificateFile: path/to/certificate.pem
    privateKeyFile: path/to/private-key.pem
    rootCAFile: path/to/root-ca.pem
    topics: DDTM/TRIP/+, DDTM/STATUS/+   # 구독할 토픽들
```

### 2. 인증서 파일

`certs/` 디렉토리에 다음 파일들을 배치해야 합니다:
- `MZC-Test-Thing-01.cert.pem` - 디바이스 인증서
- `MZC-Test-Thing-01.private.key` - 개인 키
- `AmazonRootCA1.pem` - 루트 CA 인증서

## 🚀 실행 방법

### 1. 프로젝트 빌드

```bash
./gradlew build
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
java -jar build/libs/module-metry-final-1.0.0.jar
```

### 3. 웹 접속

- **검색 페이지**: http://localhost:8080/search
- **실시간 모니터링**: http://localhost:8080/realtime
- **실시간 API**: http://localhost:8080/realtime (JSON)

## 📊 API 엔드포인트

### 웹 페이지
- `GET /search` - IoT 데이터 검색 페이지
- `POST /search` - 검색 조건으로 데이터 조회
- `GET /export` - CSV 파일 다운로드

### REST API
- `GET /realtime` - 최신 50개 메시지 조회 (JSON)

## 🔍 검색 기능

다음 조건으로 IoT 데이터를 검색할 수 있습니다:

- **시간 범위**: 시작일시 ~ 종료일시
- **디바이스 ID**: 특정 디바이스 필터링
- **토픽**: 특정 MQTT 토픽 필터링
- **데이터 타입**: 메시지 타입 필터링
- **값 범위**: 최소값 ~ 최대값
- **정렬**: 최신순/오래된순

## 💾 데이터베이스

### 테이블 구조

```sql
CREATE TABLE iot_messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    topic TEXT NOT NULL,
    payload TEXT NOT NULL,
    data_type TEXT,
    value REAL,
    received_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    raw_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 데이터 관리
- 최대 1000개 메시지 유지
- 자동으로 오래된 메시지 삭제
- SQLite 파일 위치: `data/iot_messages.db`

## 🔧 개발 환경 설정

### 필수 요구사항
- Java 17 이상
- Gradle 7.x 이상
- AWS IoT Core 계정 및 인증서

### IDE 설정
- IntelliJ IDEA 또는 Eclipse 권장
- Lombok 플러그인 설치 필요

## 📝 로그

애플리케이션은 다음 로그를 제공합니다:
- AWS IoT 연결 상태
- 메시지 수신 로그
- 에러 및 예외 처리

## 🤝 기여

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 생성해 주세요. 