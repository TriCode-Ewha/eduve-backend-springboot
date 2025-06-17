# Eduve: RAG 기반 AI 챗봇 서비스

Eduve는 음성 인식(STT), OCR 문자 추출, 채팅 메시지 저장 등 기능을 제공하는 Spring Boot 기반의 교육 지원 백엔드 서버입니다. 이 프로젝트는 JWT 인증과 RESTful API를 기반으로 하며, 학생과 교사 간 커뮤니케이션을 지원합니다.

<br>
<br>

## 📦 프로젝트 구성 레포지토리

본 프로젝트는 다음 세 개의 레포지토리로 구성되어 있습니다:

| 이름         | 설명                           | GitHub 주소 |
|--------------|--------------------------------|--------------|
| **springboot** | 백엔드 주요 로직 (API, DB, STT, JWT 등)  | [Eduve Spring Boot](https://github.com/TriCode-Ewha/eduve-backend-springboot) |
| **flask**     | AI 모델 기반 RAG  서버 (LangChain, 임베딩, 유사도 검색)     | [Eduve Flask AI](https://github.com/TriCode-Ewha/eduve-backend-flask) |
| **front**     | 프론트엔드 클라이언트 (웹 인터페이스, React) | [Eduve Front](https://github.com/TriCode-Ewha/eduve-frontend) |


<br>
<br>


## Spring Boot (eduve-springboot)

Springboot 서버는 RESTful API, 사용자 인증, DB 관리, STT, OCR 등 eduve의 핵심 백엔드 기능을 담당합니다.

<br>

## 📁 주요 디렉토리 구조

```
eduve/
├── src/
│   └── main/
│       ├── java/tricode/eduve/
│       │   ├── controller/
│       │   ├── service/
│       │   ├── repository/
│       │   ├── domain/
│       │   ├── dto/
│       │   ├── jwt/
│       │   └── EduveApplication.java
│       └── resources/
│           └── application.yml
├── scripts/
├── appspec.yml
├── build.gradle
└── README.md
```

<br>


## 🧾 Source Code 설명

### controller/

| 클래스명                                 | 설명                       |
| ---------------------------------------- | ------------------------- |
| `AllCharacterController`         | 전체 캐릭터 리스트 제공   |
| `AuthController`                 | 로그인 및 JWT 토큰 발급 |
| `ChatController`                 | 채팅 데이터 송수신 처리   |
| `ConversationController`         | 대화 세션 관리        |
| `FileController`                 | 파일 업로드/관리     |
| `FolderController`               | 사용자 폴더 생성/관리    |
| `JoinController`                 | 회원가입 (강사/수강생)    |
| `MessageLikeController`          | 메시지 좋아요 기능 처리   |
| `PreferenceController`           | 챗봇설정/선호도 저장 API   |
| `UserCharacterController`        | 사용자가 선택한 캐릭터 제어 |
| `UserController`                 | 사용자 정보 조회/수정    |

### domain/
- Entity 클래스 모음. 예: User, Message, Conversation, File, Folder, Tone 등

### dto/
- request/ : 클라이언트 → 서버 요청 형식
- response/ : 서버 → 클라이언트 응답 형식

### service/
- 비즈니스 로직 계층. 각 컨트롤러가 주입 받아 사용.

### jwt/
- JWTFilter, JWTUtil, LoginFilter: 인증 필터 및 토큰 처리

### repository/
- Spring Data JPA 기반 DB 접근 계층


<br>

## 📦 How to Install

#### 1. 환경 요구사항

- Java 17
- Gradle 7.5+
- MySQL 8.0
- Git

#### 2. 설치 절차

```bash
# 1. Git 저장소 클론
git clone https://github.com/TriCode-Ewha/eduve-backend-springboot.git
cd eduve-backend-springboot

# 2. application.yml 설정
cp src/main/resources/application-example.yml src/main/resources/application.yml
# 설정 후 DB URL, 사용자 정보, JWT 시크릿 등을 입력

```
<br>

## 🛠 How to Build

프로젝트를 설치 한 후 Gradle을 사용하여 프로젝트를 빌드합니다.

```bash
./gradlew clean build
```

<br>

## 🚀 How to Run

빌드 후 다음 명령으로 실행할 수 있습니다:

```bash
java -jar build/libs/eduve-0.0.1-SNAPSHOT.jar
```
또는 스크립트를 통해 실행:

```bash
sh scripts/start.sh
```
- 기본 포트는 8080입니다. 실행 후 http://localhost:8080 에서 서버가 동작합니다.



## 

<br>
<br>


## ✅ How to Test
이 프로젝트는 JUnit 5 및 Spring Boot Test를 기반으로 테스트를 수행합니다.

#### 테스트 실행
```bash
./gradlew test
```
또는 IntelliJ에서 src/test/java/.../controller/ 또는 /service/ 내 테스트 클래스 실행

#### 테스트 구조
| 디렉토리 경로                         | 설명            |
| ------------------------------- | ------------- |
| `src/test/java/.../controller/` | API 컨트롤러 테스트  |
| `src/test/java/.../service/`    | 서비스 로직 단위 테스트 |


#### ✨ 테스트 예시: 채팅 시작 API

**대상 API: POST /chat/start/{userId}**

**입력 예시:**

```json
{
  "question": "안녕하세요"
}
```

**검증 포인트:**
- Bot 응답 메시지 존재
- 상태 코드 200 OK

**테스트 코드 (일부)**
```java
mockMvc.perform(post("/chat/start/1")
    .param("graph", "1")
    .param("url", "123")
    .contentType(MediaType.APPLICATION_JSON)
    .content("{\"question\": \"안녕하세요\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.botMessage.message").exists());
```


<br>
<br>

## 📊 샘플 데이터 설명
프로젝트에는 API 테스트용 샘플 데이터가 포함되어 있습니다.

#### 1. 사용자 데이터 (users.csv)
- 위치: src/main/resources/sample/users.csv
- 형식: CSV
```csv
id,username,password,role
1,teacher01,password123,ROLE_TEACHER
2,student01,password456,ROLE_STUDENT
```

#### 2. 채팅 메시지 샘플 (messages.json)
- 위치: src/main/resources/sample/messages.json
- 형식: JSON

```json
[
  {
    "sender": "student01",
    "receiver": "teacher01",
    "message": "안녕하세요 선생님!",
    "timestamp": "2024-06-01T10:00:00"
  },
  {
    "sender": "teacher01",
    "receiver": "student01",
    "message": "네, 어떤 도움이 필요하신가요?",
    "timestamp": "2024-06-01T10:01:00"
  }
]
```

<br>
<br>


## 🗄 Database 사용 정보
- DBMS: MySQL
- 설정 예시 (application.yml):

``` yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/eduvedb
    username: {your_username}
    password: {your_password}
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

cloud:
  aws:
    s3:
      bucket: {bucket_name}
    credentials:
      accessKey: {accessKey}
      secretKey: {secretKey}

chatgpt:
    api-key: {api}

daglo:
  stt:
    api-key: {api}
    api-url: {url}
```
- JPA 기반으로 자동 테이블 생성 (ddl-auto: update)
- 테스트/운영 환경별 DB 분리 가능



<br>
<br>

## 📚 사용된 오픈소스 목록
| 라이브러리        | 설명                   | 라이선스     | 
|------------------|------------------------|--------------|
| Spring Boot      | 백엔드 프레임워크       | Apache 2.0   |
| Spring Security  | 인증/인가 처리         | Apache 2.0    | 
| jjwt             | JWT 토큰 처리           | Apache 2.0   |
| Lombok           | 보일러플레이트 코드 제거 | MIT          |
| Gradle           | 빌드 도구               | Apache 2.0   |
| AWS CodeDeploy   | 배포 자동화             | -            | 



