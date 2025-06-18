# Edu've: RAG 기반 AI 챗봇 서비스

Edu've는 RAG(Retrieval-Augmented Generation) 기반의 AI 챗봇 학습 지원 서비스로, 강사와 수강생 문서 기반 질문 응답을 지원합니다. 학습자료 아카이빙, 개인 맞춤형 챗봇, 실시간 질의응답 기능을 포함하며, Spring Boot와 Flask로 백엔드를 구성하고 React 기반 웹 인터페이스를 제공합니다. 학습 환경에서의 커뮤니케이션과 정보 접근성을 향상시키는 것을 목표로 합니다.

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

# 2. application.yml 설정 -> 아래 Database 사용 정보 참고
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
프로젝트를 클론받은 뒤, 아래 방법으로 Test 실행
- 주의 : flask 서버가 로컬에서 돌아가는 상태에서 Test 가능
```bash
./gradlew test
```
IntelliJ에서 src/test/java/.../controller/ 내 테스트 클래스 실행

#### 테스트 구조
| 디렉토리 경로                         | 설명            |
| ------------------------------- | ------------- |
| `src/test/java/.../controller/` | API 컨트롤러 테스트  |


### 1. 채팅 시작 테스트

**대상 API: POST /chat/start/{userId}**
**설명: 사용자의 질문을 입력하면, 설정된 캐릭터 말투 및 설명 난이도에 맞춰 챗봇이 응답합니다.**

**입력 예시:**

```json
{
  "question": "안녕하세요."
}
```

**검증 포인트:**
- 상태 코드: 200 OK
- userMessage.question 필드가 입력값과 동일한지 확인
- botMessage.answer 필드가 비어 있지 않은지 확인 (GPT 응답 존재 여부)

**테스트 코드 (일부)**
```java
mockMvc.perform(post("/chat/start/2")
        .param("graph", "0")
        .param("url", "0")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"question\": \"안녕하세요.\"}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.userMessage.question").value("안녕하세요."))
    .andExpect(jsonPath("$.botMessage.answer").isNotEmpty());
```

**위치 기반 / 내용 기반 응답 테스트**
이 프로젝트는 질문 입력 시, 다음 두 방식으로 챗봇 응답의 정확도를 검증할 수 있습니다:
| 유형        | 설명                                                     |
| --------- | ------------------------------------------------------ |
| **위치 기반** | 질문에 대해 응답이 올바른 문서의 `파일명`과 `페이지 번호`에서 추출되었는지를 테스트       |
| **내용 기반** | 질문에 대해 응답 문장 자체가 예상한 의미를 포함하는지를 비교 (정답 메시지 일부 포함 여부 등) |

<br>

### 2. 캐릭터 설정 반영 테스트

**대상 API: PATCH /userCharacter/{userId}**
**설명: 사용자별 캐릭터 말투(tone)와 설명 난이도(descriptionLevel)를 변경하고, 해당 설정이 챗봇 응답에 반영되는지 확인합니다.**

**입력 예시:**

```json
{
  "userCharacterName": "공감형 조언자",
  "tone": "FRIENDLY",
  "descriptionLevel": "HIGH"
}
```
- 사용자 캐릭터 설정 요청 시 tone과 descriptionLevel 필드는 아래 값 중 하나를 사용할 수 있습니다.
- **Tone (말투)**

  | 값          | 설명                     |
  | ---------- | ---------------------- |
  | `FORMAL`   | 격식 있는 말투               |
  | `KINDLY`   | 친절하고 공손한 말투            |
  | `FRIENDLY` | 캐주얼하고 다정한 말투           |
  | `TSUNDERE` | 퉁명스럽지만 챙겨주는 말투 (*츤데레*) |

- **DescriptionLevel (설명 난이도)**

  | 값            | 설명                |
  | ------------ | ----------------- |
  | `ELEMENTARY` | 초등학생도 이해할 수 있는 수준 |
  | `MIDDLE`     | 중학생 수준의 설명        |
  | `HIGH`       | 고등학생 수준의 설명       |
  | `UNIVERSITY` | 대학생 수준의 상세 설명     |
  | `EXPERT`     | 전문가 수준의 심화 설명     |


**검증 포인트:**
- 상태 코드 200 OK
- 응답 본문에 변경된 속성 반영 여부

**연계 테스트 흐름**
1. 캐릭터 설정 변경 요청
2. 변경된 캐릭터 상태 확인 (userCharacterName, tone, descriptionLevel)
3. 채팅 요청 → 응답이 해당 캐릭터 설정을 반영하고 있는지 확인


**테스트 코드 (일부)**
```java
mockMvc.perform(patch("/user-character/2")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{"
            + "\"userCharacterName\": \"공감형 조언자\","
            + "\"tone\": \"FRIENDLY\","
            + "\"descriptionLevel\": \"EASY\""
            + "}"))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.userCharacterName").value("공감형 조언자"));
```


<br>
<br>

## 📊 샘플 데이터 설명
프로젝트에는 API 테스트용 샘플 데이터가 포함되어 있습니다.


#### 1. 캐릭터 설정 (character_sample.json)
- 위치: src/main/resources/sample/character_sample.json
- 형식: JSON

```json
[
  {
    "userCharacterName": "공감형 조언자",
    "tone": "FRIENDLY",
    "descriptionLevel": "EASY"
  },

  {
    "userCharacterName": "논리적 안내자",
    "tone": "FORMAL",
    "descriptionLevel": "EXPERT"
  }
]
```
- 다양한 캐릭터 설정(tone, 설명 난이도 등)을 테스트할 수 있는 샘플 데이터입니다.
- 테스트 코드에서는 이 JSON을 참고해 직접 설정 값을 지정해 PATCH /user-character/{userId} API로 캐릭터를 변경하고,
이후 POST /chat/start/{userId}를 통해 챗봇의 응답에 캐릭터 말투가 잘 반영되는지를 확인할 수 있습니다.
- 이 파일을 기반으로 다양한 캐릭터 설정을 반복적으로 바꿔가며 테스트하는 데 유용합니다.

<br>
<br>


## 🗄 Database 사용 정보
- DBMS: MySQL
- application.yml:

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



