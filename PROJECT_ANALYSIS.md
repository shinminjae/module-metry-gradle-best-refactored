# Module Metry - AWS IoT Core MQTT5 기반 IoT 데이터 수집 시스템

## 📋 프로젝트 개요

**Module Metry**는 AWS IoT Core를 통해 다양한 IoT 디바이스에서 전송되는 실시간 데이터를 수집, 저장, 관리하는 Spring Boot 기반 애플리케이션입니다. MQTT5 프로토콜을 사용하여 다중 토픽을 구독하고, 수신된 메시지를 SQLite 데이터베이스에 저장하며, 웹 인터페이스를 통해 데이터를 검색하고 분석할 수 있습니다.

### 🎯 주요 기능
- **AWS IoT Core MQTT5 연결**: 최신 MQTT5 프로토콜을 사용한 안정적인 IoT 데이터 수집
- **다중 토픽 구독**: INIT, DTC, RO, TRIP, ACC, MAP 등 다양한 IoT 토픽 지원
- **토픽별 메시지 처리**: 각 토픽 유형에 맞는 전용 서비스로 메시지 라우팅
- **SQLite 데이터 저장**: 경량화된 로컬 데이터베이스로 IoT 메시지 영구 저장
- **웹 기반 데이터 검색**: Bootstrap 5를 활용한 현대적인 웹 인터페이스
- **CSV 데이터 내보내기**: 수집된 데이터를 CSV 형식으로 다운로드
- **자동 데이터 정리**: 1000개 이상 메시지 시 자동으로 오래된 데이터 삭제

## 🏗️ 기술 스택

### 백엔드
- **Java 17**: 최신 LTS 버전의 Java 플랫폼
- **Spring Boot 3.1.2**: 현대적인 Spring 프레임워크
- **Gradle**: 빌드 자동화 도구
- **MyBatis 3.0.2**: SQL 매핑 프레임워크
- **SQLite**: 경량화된 임베디드 데이터베이스

### AWS IoT
- **AWS IoT Device SDK (CRT 기반)**: AWS IoT Core 연결을 위한 공식 SDK
- **MQTT5**: 최신 MQTT 프로토콜 버전
- **AWS IoT Core**: 클라우드 기반 IoT 플랫폼

### 프론트엔드
- **Thymeleaf**: 서버사이드 템플릿 엔진
- **Bootstrap 5.3.0**: 현대적인 CSS 프레임워크

### 유틸리티
- **Lombok**: 보일러플레이트 코드 감소
- **Apache Commons Lang3**: 유틸리티 라이브러리

## 📁 프로젝트 구조

```
module-metry-gradle-best/
├── src/main/java/com/daedong/agmtms/
│   ├── ModuleMetryApplication.java          # 메인 애플리케이션 클래스
│   ├── config/                              # 설정 클래스들
│   │   ├── AwsIotCoreConfig.java           # AWS IoT Core MQTT5 설정
│   │   └── DatabaseConfig.java             # 데이터베이스 초기화 설정
│   ├── common/                              # 공통 컴포넌트
│   │   └── AwsIotCoreSubscribe.java        # MQTT5 구독 및 이벤트 처리
│   └── metry/                               # 비즈니스 로직
│       ├── controllers/                     # 웹 컨트롤러
│       │   ├── IotMessageController.java   # 메시지 검색 및 내보내기
│       │   └── RealtimeController.java     # 실시간 데이터 조회
│       ├── dao/                             # 데이터 액세스 객체
│       │   └── IotMessageMapper.java       # MyBatis 매퍼 인터페이스
│       ├── dto/                             # 데이터 전송 객체
│       │   └── IotMessageDto.java          # IoT 메시지 DTO
│       ├── services/                        # 비즈니스 서비스
│       │   └── IotMessageService.java      # 메시지 관리 서비스
│       └── service/                         # 토픽별 메시지 처리 서비스
│           ├── init/                        # INIT 토픽 처리
│           ├── dtc/                         # DTC 토픽 처리
│           ├── ro/                          # RO 토픽 처리
│           ├── trip/                        # TRIP 토픽 처리
│           ├── acc/                         # ACC 토픽 처리
│           └── map/                         # MAP 토픽 처리
├── src/main/resources/
│   ├── application.yml                      # 애플리케이션 설정
│   ├── templates/                           # Thymeleaf 템플릿
│   │   ├── search.html                      # 데이터 검색 페이지
│   │   └── realtime.html                    # 실시간 데이터 페이지
│   └── sql/mapper/                          # MyBatis 매퍼 및 스키마
│       ├── IotMessageMapper.xml             # SQL 매핑 정의
│       └── schema.sql                       # 데이터베이스 스키마
├── data/                                    # SQLite 데이터베이스 파일
├── certs/                                   # AWS IoT 인증서 파일들
├── build.gradle                             # Gradle 빌드 설정
├── settings.gradle                          # 프로젝트 설정
└── README.md                                # 프로젝트 문서
```

## 🔧 핵심 컴포넌트 상세 분석

### 1. AWS IoT Core 설정 (`AwsIotCoreConfig.java`)

AWS IoT Core와의 MQTT5 연결을 관리하는 핵심 설정 클래스입니다.

#### 주요 기능:
- **MQTT5 클라이언트 빌더**: `AwsIotMqtt5ClientBuilder`를 사용한 클라이언트 생성
- **연결 설정**: 클라이언트 ID, 세션 만료, Keep-Alive 간격 설정
- **라이프사이클 이벤트**: 연결 시도, 성공, 실패, 해제 이벤트 처리
- **토픽 구독**: 애플리케이션 시작 시 자동으로 모든 토픽 구독

#### 설정 파라미터:
```yaml
aws:
  iot:
    endpoint: a1vopgopgy3eh3-ats.iot.ap-northeast-2.amazonaws.com
    clientId: metry-local-daedong-agmtms
    certificateFile: D:/aws/tms_kr_local_setting_key/MZC-Test-Thing-01.cert.pem
    privateKeyFile: D:/aws/tms_kr_local_setting_key/MZC-Test-Thing-01.private.key
    topics: DDTM/TRIP/+,DDTM/INIT/+,DDTM/DTC/+,DDTM/RO/+,DDTM/ACC/+,DDTM/MAP/+
```

### 2. MQTT5 이벤트 처리 (`AwsIotCoreSubscribe.java`)

수신된 MQTT 메시지를 토픽별로 라우팅하는 이벤트 핸들러입니다.

#### 주요 기능:
- **메시지 라우팅**: 토픽 패턴에 따라 적절한 서비스로 메시지 전달
- **토픽별 처리**: INIT, DTC, RO, TRIP, ACC, MAP 각각의 전용 서비스 호출
- **에러 처리**: 메시지 처리 중 발생하는 예외 상황 처리

#### 토픽 라우팅 로직:
```java
if (topic.contains("/INIT/")) {
    initMetryService.processInitMessage(topic, payload);
} else if (topic.contains("/DTC/")) {
    dtcMetryService.processDtcMessage(topic, payload);
} else if (topic.contains("/RO/")) {
    roMetryService.processRoMessage(topic, payload);
} else if (topic.contains("/TRIP/")) {
    tripMetryService.processTripMessage(topic, payload);
} else if (topic.contains("/ACC/")) {
    accMetryService.processAccMessage(topic, payload);
} else if (topic.contains("/MAP/")) {
    mapMetryService.processMapMessage(topic, payload);
}
```

### 3. 데이터베이스 관리

#### SQLite 스키마 (`schema.sql`):
```sql
CREATE TABLE IF NOT EXISTS iot_messages (
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

#### MyBatis 매퍼 (`IotMessageMapper.xml`):
- **insertMessage**: 새로운 IoT 메시지 삽입
- **searchMessages**: 다양한 조건으로 메시지 검색 (페이징 지원)
- **countMessages**: 전체 메시지 수 조회
- **deleteOldestMessages**: 오래된 메시지 자동 삭제

### 4. 토픽별 메시지 처리 서비스

각 IoT 토픽 유형에 맞는 전용 서비스 클래스들이 구현되어 있습니다:

#### 공통 처리 패턴:
1. **토픽 파싱**: 디바이스 ID와 데이터 타입 추출
2. **DTO 생성**: `IotMessageDto` 객체 생성
3. **데이터 저장**: `IotMessageService`를 통한 데이터베이스 저장
4. **로깅**: 처리 과정 및 결과 로깅

#### 예시: `TripMetryService.java`
```java
@Service("tripMetryService")
@RequiredArgsConstructor
@Slf4j
public class TripMetryService {
    private final IotMessageService messageService;

    public void processTripMessage(String topic, String payload) {
        try {
            log.info("Processing TRIP message: topic={}, payload={}", topic, payload);
            
            String[] parts = topic.split("/");
            String deviceId = parts.length > 2 ? parts[2] : "UNKNOWN";
            
            IotMessageDto dto = IotMessageDto.builder()
                    .deviceId(deviceId)
                    .topic(topic)
                    .payload(payload)
                    .dataType("TRIP")
                    .value(null)
                    .receivedAt(LocalDateTime.now())
                    .rawMessage(payload)
                    .build();
            
            messageService.saveMessage(dto);
        } catch (Exception e) {
            log.error("Error processing TRIP message", e);
        }
    }
}
```

### 5. 웹 인터페이스

#### 데이터 검색 페이지 (`search.html`):
- **검색 조건**: 시작/종료 일시, 디바이스 ID, 토픽, 데이터 타입, 값 범위
- **정렬 옵션**: 최신순/오래된순 정렬
- **페이징**: 페이지 단위 데이터 조회
- **CSV 내보내기**: 검색 결과를 CSV 파일로 다운로드

#### 실시간 데이터 페이지 (`realtime.html`):
- **실시간 데이터 조회**: 최신 수신 메시지 표시
- **자동 새로고침**: 주기적인 데이터 업데이트

## 🚀 실행 방법

### 1. 사전 요구사항
- Java 17 이상
- Gradle 8.x
- AWS IoT Core 인증서 파일들

### 2. 설정 파일 준비
`application.yml`에서 AWS IoT 설정을 환경에 맞게 수정:
```yaml
aws:
  iot:
    endpoint: [AWS IoT Core 엔드포인트]
    clientId: [클라이언트 ID]
    certificateFile: [인증서 파일 경로]
    privateKeyFile: [개인키 파일 경로]
    topics: [구독할 토픽 목록]
```

### 3. 애플리케이션 실행
```bash
# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
./gradlew bootRun
```

### 4. 웹 접속
- **데이터 검색**: http://localhost:8080/search
- **실시간 데이터**: http://localhost:8080/realtime

## 📊 데이터 흐름

### 1. IoT 디바이스 → AWS IoT Core
```
IoT Device → MQTT5 → AWS IoT Core
```

### 2. AWS IoT Core → 애플리케이션
```
AWS IoT Core → MQTT5 → AwsIotCoreConfig → AwsIotCoreSubscribe
```

### 3. 메시지 처리 및 저장
```
AwsIotCoreSubscribe → 토픽별 서비스 → IotMessageService → MyBatis → SQLite
```

### 4. 데이터 조회
```
웹 요청 → IotMessageController → IotMessageService → MyBatis → SQLite → Thymeleaf → 웹 페이지
```

## 🔍 주요 특징

### 1. MQTT5 기반 최신 프로토콜
- **향상된 성능**: MQTT4 대비 더 나은 성능과 안정성
- **세션 관리**: 자동 재연결 및 세션 복구
- **메시지 우선순위**: QoS 레벨을 통한 메시지 전송 보장

### 2. 모듈화된 토픽 처리
- **단일 책임 원칙**: 각 서비스가 특정 토픽만 처리
- **확장성**: 새로운 토픽 추가 시 새로운 서비스만 구현
- **유지보수성**: 토픽별 독립적인 로직 수정 가능

### 3. 자동 데이터 관리
- **자동 정리**: 1000개 이상 메시지 시 오래된 데이터 자동 삭제
- **데이터 무결성**: 트랜잭션 기반 데이터 저장
- **성능 최적화**: 인덱스 및 쿼리 최적화

### 4. 사용자 친화적 웹 인터페이스
- **반응형 디자인**: Bootstrap 5 기반 모바일 친화적 UI
- **다양한 검색 조건**: 복합 조건을 통한 정확한 데이터 검색
- **데이터 내보내기**: CSV 형식으로 데이터 다운로드

## 🛠️ 개발 환경 설정

### 1. IDE 설정
- **IntelliJ IDEA** 또는 **Eclipse** 권장
- **Lombok 플러그인** 설치
- **Spring Boot DevTools** 활성화 (개발 시)

### 2. 데이터베이스 관리
- **SQLite 브라우저**: 데이터베이스 내용 확인
- **MyBatis 로깅**: SQL 쿼리 디버깅

### 3. 로깅 설정
```yaml
logging:
  level:
    com.daedong.agmtms: DEBUG
    org.apache.ibatis: DEBUG
```

## 🔧 커스터마이징

### 1. 새로운 토픽 추가
1. `application.yml`에 새 토픽 추가
2. 새로운 서비스 클래스 생성
3. `AwsIotCoreSubscribe`에 라우팅 로직 추가

### 2. 데이터베이스 변경
1. `schema.sql` 수정
2. `IotMessageMapper.xml` 업데이트
3. DTO 클래스 수정

### 3. 웹 인터페이스 확장
1. 새로운 컨트롤러 메서드 추가
2. Thymeleaf 템플릿 수정
3. CSS/JavaScript 추가

## 📈 성능 최적화

### 1. 데이터베이스 최적화
- **인덱스 추가**: 자주 검색되는 컬럼에 인덱스 생성
- **파티셔닝**: 날짜별 테이블 파티셔닝
- **정규화**: 데이터 중복 제거

### 2. 메모리 최적화
- **연결 풀링**: HikariCP 설정 최적화
- **캐싱**: Redis를 통한 자주 조회되는 데이터 캐싱
- **가비지 컬렉션**: JVM 힙 크기 및 GC 설정

### 3. 네트워크 최적화
- **메시지 압축**: MQTT5 메시지 압축 활성화
- **배치 처리**: 여러 메시지를 배치로 처리
- **비동기 처리**: 메시지 처리를 비동기로 수행

## 🔒 보안 고려사항

### 1. AWS IoT 인증서 관리
- **인증서 로테이션**: 정기적인 인증서 갱신
- **권한 최소화**: 필요한 권한만 부여
- **보안 그룹**: 네트워크 접근 제한

### 2. 데이터 보안
- **암호화**: 민감한 데이터 암호화 저장
- **접근 제어**: 사용자 인증 및 권한 관리
- **감사 로그**: 데이터 접근 이력 기록

## 🚨 문제 해결

### 1. 연결 문제
- **인증서 확인**: AWS IoT 인증서 파일 경로 및 권한 확인
- **네트워크 연결**: 방화벽 및 보안 그룹 설정 확인
- **로그 확인**: 애플리케이션 로그에서 오류 메시지 확인

### 2. 데이터베이스 문제
- **파일 권한**: SQLite 데이터베이스 파일 쓰기 권한 확인
- **디스크 공간**: 충분한 디스크 공간 확보
- **스키마 확인**: 데이터베이스 스키마 정상 생성 확인

### 3. 성능 문제
- **메모리 사용량**: JVM 힙 크기 조정
- **데이터베이스 최적화**: 쿼리 성능 분석 및 최적화
- **로깅 레벨**: 불필요한 로깅 비활성화

## 📝 향후 개선 계획

### 1. 기능 확장
- **실시간 알림**: 특정 조건 시 이메일/SMS 알림
- **데이터 분석**: 통계 및 차트 기능 추가
- **API 제공**: RESTful API를 통한 외부 시스템 연동

### 2. 아키텍처 개선
- **마이크로서비스**: 서비스 분리를 통한 확장성 향상
- **클라우드 배포**: AWS ECS/EKS를 통한 클라우드 배포
- **모니터링**: Prometheus/Grafana를 통한 시스템 모니터링

### 3. 데이터 관리
- **백업 전략**: 자동 데이터 백업 및 복구
- **데이터 보관**: 장기 보관을 위한 아카이브 시스템
- **데이터 마이그레이션**: 스키마 변경 시 데이터 마이그레이션 도구

---

## 📞 지원 및 문의

프로젝트 관련 문의사항이나 개선 제안이 있으시면 언제든지 연락주세요.

**개발팀**: Daedong AGMTMS Team  
**버전**: 1.0.0  
**최종 업데이트**: 2025년 7월 27일 