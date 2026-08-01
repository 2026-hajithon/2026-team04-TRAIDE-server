# 🏃 TRAIDE

> 서로의 운동을 가르쳐주고 배우며 새로운 운동 메이트를 만나는 서비스

TRAIDE는 함께 운동할 친구를 찾고, 서로 잘하는 운동을 공유할 수 있는 운동 메이트 매칭 서비스입니다.

같은 지역의 사용자를 우선 추천받고, 관심 있는 운동과 지역을 기준으로 메이트를 탐색할 수 있습니다. 친구가 된 사용자와 채팅으로 소통하고, 운동 약속을 등록하거나 함께한 운동에 대한 후기와 기록을 남길 수 있습니다.

## ✨ 주요 기능

### 🔐 회원 및 프로필

- 아이디와 비밀번호를 이용한 회원가입·로그인
- JWT 기반 사용자 인증
- 이름, 나이, 성별, 운동 종목, 운동 수준, 활동 지역 등록
- 선택한 운동 종목에 따른 기본 프로필 이미지 제공
- 프로필 조회 및 부분 수정

### 🏠 운동 메이트 추천

- 같은 지역의 사용자를 우선 추천
- 운동 종목 및 지역 다중 필터
- 자기 자신, 현재 친구, 친구 요청 대기 중인 사용자 제외
- 추천 순서 랜덤 제공

### 🤝 친구

- 친구 요청 보내기
- 받은 요청 및 보낸 요청 조회
- 친구 요청 수락·거절·취소
- 친구 목록 조회 및 친구 삭제
- 두 사용자 ID를 이용한 고정 채팅방 ID 제공

### 💬 채팅

- Firebase Authentication 커스텀 토큰 발급
- Firebase Firestore 기반 실시간 채팅
- 친구 관계인 사용자끼리 채팅 가능
- Spring 서버는 채팅 사용자 인증과 채팅방 정보를 제공

### 📅 운동 약속

- 친구와 운동 날짜, 시간, 장소 등록
- 두 참여자 중 한 명을 코치로 지정
- 예정된 약속 조회
- 약속 수정 및 삭제
- 두 참여자 모두 약속 수정·삭제 가능

### 🔥 운동 후기

- 친구와 약속이 한 번 이상 있으면 후기 작성 가능
- 1점부터 5점까지의 별점
- 후기 글과 사진 한 장 선택 등록
- 받은 후기 및 작성자 정보 조회
- 평균 별점과 후기 수를 프로필에 반영

## 🛠 기술 스택

### Backend

- **Language**: Java 17
- **Framework**: Spring Boot 4.1.0
- **Database**: MySQL
- **ORM**: Spring Data JPA, Hibernate
- **Security**: Spring Security, JWT, BCrypt
- **Chat Authentication**: Firebase Admin SDK
- **API Documentation**: Swagger / OpenAPI
- **Build Tool**: Gradle
- **Test**: JUnit 5, H2, Spring Boot Test
- **Additional**: Lombok

### iOS

- Swift
- Firebase Authentication
- Firebase Firestore

## 📁 프로젝트 구조

```text
src/main/java/com/gdghajithon/
├── appointment/       # 운동 약속 생성, 조회, 수정, 삭제
├── auth/              # 회원가입 및 로그인
├── friend/            # 친구 목록 및 친구 삭제
├── friendrequest/     # 친구 요청, 수락, 거절, 취소
├── global/
│   ├── config/        # Swagger 설정
│   ├── exception/     # 공통 예외 처리
│   ├── firebase/      # Firebase Admin SDK 및 토큰 발급
│   └── security/      # JWT 인증 및 Spring Security
├── image/             # 후기 이미지 업로드 및 이미지 URL 처리
├── profile/           # 프로필과 사용자 추천
├── region/            # 서울시 지역 정보
├── review/            # 운동 후기와 평점
├── sport/             # 운동 종목과 기본 이미지
├── user/              # 사용자 엔티티 및 저장소
└── GdgHajithonApplication.java
```

## 🔄 주요 사용자 흐름

```text
회원가입 및 로그인
        ↓
프로필 생성
        ↓
운동 메이트 추천
        ↓
친구 요청 및 수락
        ↓
친구 목록과 Firebase 채팅
        ↓
운동 약속 등록
        ↓
운동 후기 작성
        ↓
프로필 평점 및 운동 기록 반영
```

## 🔐 인증 방식

회원가입 또는 로그인에 성공하면 다음 두 가지 토큰을 반환합니다.

- `accessToken`: Spring REST API 인증에 사용
- `firebaseToken`: Firebase Authentication 로그인에 사용

인증이 필요한 API에는 다음 헤더를 전달합니다.

```http
Authorization: Bearer {accessToken}
```

## 🚀 로컬 실행 방법

### 1. 실행 환경

- Java 17 이상
- MySQL
- Firebase 서비스 계정 키

### 2. 데이터베이스 생성

```sql
CREATE DATABASE gdg_hajithon;
```

### 3. 환경변수 설정

```text
DB_USERNAME=MySQL 사용자 이름
DB_PASSWORD=MySQL 비밀번호
JWT_SECRET=JWT 서명용 비밀키
GOOGLE_APPLICATION_CREDENTIALS=Firebase 서비스 계정 JSON 절대 경로
```

Firebase 서비스 계정 JSON 파일은 GitHub에 커밋하지 않습니다.

### 4. 서버 실행

```bash
./gradlew bootRun
```

### 5. Swagger

```text
http://localhost:8080/swagger-ui.html
```

실제 iPhone에서 연동할 때는 `localhost` 대신 서버를 실행하는 Mac의 로컬 IP를 사용해야 합니다.

```text
http://{Mac의 로컬 IP}:8080
```

## 🧪 테스트

```bash
./gradlew test
```

- 서비스 및 저장소 단위 테스트
- Spring MVC API 테스트
- JWT 및 Firebase 토큰 테스트
- 친구 요청 동시성 테스트
- 전체 핵심 사용자 흐름 검증

## 📌 프로젝트 정책

- 채팅 메시지는 Spring 서버가 아닌 Firebase Firestore에서 관리합니다.
- 사용자는 하나의 운동 종목과 하나의 활동 지역을 등록합니다.
- 운동 종목은 9개, 활동 지역은 서울시 25개 구를 제공합니다.
- 약속은 별도의 상대방 승인 없이 생성 즉시 확정됩니다.
- 후기 수정과 삭제는 해커톤 MVP 범위에서 제외했습니다.
- 이미지 파일은 로컬 서버에 저장합니다.

## 👥 팀

GDG Hajithon Team 04에서 개발했습니다.

| 이름 | 포지션 | 담당 역할 | GitHub |
| --- | --- | --- | --- |
| 김태한 | Backend | 약속, 후기, 이미지, Firebase 연동 등 | [taehan0](https://github.com/taehan0) |
| 이정훈 | Backend | 사용자, 인증, 친구 관련 기능 | [wjdgns313131] (https://github.com/wjdgns313131) |
| 김지우 | iOS | iOS 애플리케이션 및 Firebase 채팅 | [jiwookim1202] (https://github.com/jiwookim1202) |
| 윤유빈 | Design | UI/UX 디자인 |  |
| 성유정 | Design | UI/UX 디자인 |  |

## 📖 API 문서

- Swagger: 서버 실행 후 `/swagger-ui.html`
- 세부 API 명세: 팀 Notion의 `GDG-Hajithon API 명세 v2`
