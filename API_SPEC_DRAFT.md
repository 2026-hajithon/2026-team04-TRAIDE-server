# 운동 메이트 앱 MVP 및 API 명세 초안

> **안내:** iOS·백엔드 협업 시에는 [GDG-Hajithon API 명세](https://app.notion.com/p/3ad908c4cbe28093a3f2c6e14215f188)를 최신 기준으로 사용한다. 이 파일은 초기 확장 초안으로 보관한다.
>
> 상태: 논의용 초안  
> 대상: Spring Boot 백엔드, MySQL, iOS  
> API 스타일: REST, JSON, `/api/v1`  
> 목적: 8월 1일 해커톤에서 구현할 최소 기능과 iOS-백엔드 계약을 먼저 맞춘다.

> 이 문서의 API 경로, JSON 필드명, enum 코드, 검증 범위는 모두 **1차 제안**이다. iOS 개발자와 응답 형태를 확인한 뒤 확정한다.

## 문서 표기

- **확정**: 팀 논의로 결정된 내용
- **제안**: 아직 최종 결정되지 않았으며, 단순한 구현을 위해 추천하는 내용
- **iOS 담당**: Spring 백엔드 API 범위에서 제외하고 iOS/Firebase에서 담당하는 내용
- **MVP 제외**: 해커톤 당일 구현하지 않는 내용

---

# 1. MVP 기능 범위

## 1.1 필수 기능

### 회원과 인증

- 아이디와 비밀번호로 회원가입 및 로그인
- 비밀번호는 BCrypt로 암호화해 저장
- 이름, 나이, 성별, 운동 종목 1개, 실력, 서울시 내 활동 지역 등록
- 내 프로필 조회 및 수정
- 프로필 사진은 선택 사항

### 추천

- 홈 화면에 추천 사용자 10명 제공
- 자기 자신, 현재 친구, 대기 중인 친구 요청 상대는 추천에서 제외
- 별도 지역 필터가 없으면 같은 구 사용자를 먼저 보여주고, 부족하면 다른 구 사용자를 제공
- 운동 종목 필터가 있으면 해당 종목 사용자만 제공
- 지역 필터가 있으면 해당 구 사용자만 제공
- 추천 카드 새로고침은 iOS가 이미 받은 10명 중 다음 사용자를 표시
- 10명을 모두 본 경우 iOS가 추천 API를 다시 호출
- 넘긴 사용자는 나중에 다시 추천될 수 있음

### 친구

- 친구 요청 보내기
- 받은 친구 요청 목록 조회
- 친구 요청 수락 및 거절
- 거절 후 재요청 가능
- 친구 목록 조회
- 친구 삭제
- 친구인 사용자에게만 채팅 화면 진입 허용

### 채팅

- **iOS 담당:** Firebase Cloud Firestore를 이용한 메시지 송수신
- Spring 백엔드는 채팅 메시지를 저장하거나 조회하지 않음
- 백엔드는 현재 친구 관계와 채팅방에서 사용할 사용자 정보만 제공

### 약속

- 친구끼리 날짜, 시간, 장소가 포함된 약속 생성
- 상대방 수락 없이 생성 즉시 확정
- 두 사용자 사이의 약속 목록 조회
- 약속 수정 및 삭제
- 프로필의 약속 횟수는 해당 사용자가 포함된 현재 약속 레코드 수

> **제안:** 화면 문구 `만난 횟수`보다 `약속 횟수`가 실제 계산 방식과 정확히 맞는다. 미래 약속도 횟수에 포함되기 때문이다.

### 후기

- 현재 친구인 상대에게 후기 작성
- 약속이 없어도 작성 가능
- 같은 친구에게 여러 번 작성 가능
- 별점은 필수이며 1~5점 정수
- 후기 글과 사진 1장은 선택
- 받은 후기 목록과 평균 별점을 공개 프로필에 표시
- 상대 프로필 또는 채팅방에서 후기 작성 화면으로 이동 가능
- 후기 수정 및 삭제는 구현하지 않음

### 이미지

- 프로필 사진과 후기 사진 업로드
- iOS는 이미지를 먼저 업로드하고, 반환받은 URL을 프로필 수정 또는 후기 작성 요청에 사용

## 1.2 있으면 좋은 기능

- 추천 결과의 간단한 랜덤 정렬
- Swagger에서 바로 사용할 수 있는 테스트 계정
- 친구 요청 또는 새 메시지 배지
- 후기 사진 모아보기
- 빈 목록, 로딩, 오류 상태에 대한 iOS 화면

---

# 2. 사용자 흐름

## 2.1 회원가입

1. 사용자가 이름, 나이, 성별, 아이디, 비밀번호를 입력한다.
2. 온보딩에서 운동 종목, 실력, 활동 지역을 선택한다.
3. iOS는 모든 값을 모아 회원가입 API를 한 번 호출한다.
4. 백엔드는 회원을 생성하고 JWT를 반환한다.
5. 사용자는 홈 화면으로 이동한다.
6. 프로필 사진은 회원가입 이후 선택적으로 등록한다.

## 2.2 추천과 친구 요청

1. iOS가 추천 API를 호출해 사용자 10명을 받는다.
2. iOS가 한 명씩 카드로 표시한다.
3. 새로고침 버튼을 누르면 다음 카드를 표시한다.
4. 친구 요청을 누르면 친구 요청 API를 호출한다.
5. 요청을 보낸 사용자는 이후 추천 결과에서 제외한다.
6. 현재 목록을 모두 사용하면 추천 API를 다시 호출한다.

## 2.3 받은 친구 요청

1. 사용자가 채팅 탭으로 이동한다.
2. iOS가 받은 친구 요청과 친구 목록을 각각 조회한다.
3. 받은 요청이 있으면 채팅 탭에 배지를 표시한다.
4. 사용자가 수락하면 친구 관계를 생성한다.
5. 사용자가 거절하면 요청을 거절 상태로 변경한다.
6. 거절된 상대는 나중에 다시 추천되거나 요청할 수 있다.

## 2.4 채팅

1. 사용자가 채팅 탭의 친구 목록에서 친구를 선택한다.
2. iOS가 두 사용자 ID로 채팅방 ID를 만든다.
3. iOS가 Firebase Cloud Firestore 채팅방을 열고 메시지를 처리한다.
4. 친구 관계가 삭제되면 iOS는 해당 사용자와의 새 메시지 전송을 막는다.

> **제안:** 채팅방 ID는 두 사용자 ID를 작은 순서대로 정렬해 `{작은ID}_{큰ID}`로 만든다. 예: `12_35`.

## 2.5 약속

1. 사용자가 채팅방의 달력 버튼을 누른다.
2. 날짜, 시간, 장소를 입력한다.
3. 약속을 생성하면 바로 확정된다.
4. 채팅방에서 두 사용자의 약속 목록을 조회한다.
5. 두 사용자 중 한 명이 약속을 수정하거나 삭제할 수 있다.

> **제안:** 수정 및 삭제 권한은 약속에 포함된 두 사용자 모두에게 허용한다. 별도의 승인 과정은 두지 않는다.

## 2.6 후기

1. 사용자가 친구의 프로필 또는 채팅방에서 `후기 보내기`를 누른다.
2. 별점 1~5점을 선택한다.
3. 후기 글과 사진 1장을 선택적으로 추가한다.
4. 사진이 있다면 이미지 업로드 API를 먼저 호출한다.
5. 반환된 이미지 URL과 함께 후기 작성 API를 호출한다.
6. 새 후기는 상대방 프로필의 평균 별점과 후기 목록에 반영된다.

## 2.7 친구 삭제

1. 사용자가 친구 삭제를 요청한다.
2. 백엔드는 현재 친구 관계만 삭제한다.
3. 기존 후기와 약속 데이터는 별도 정리 없이 그대로 둔다.
4. 두 사용자는 다시 친구 요청을 주고받을 수 있다.
5. 친구가 아니므로 iOS는 새 채팅 메시지 전송을 막는다.

> **제안:** 이것이 구현이 가장 단순한 삭제 정책이다. 기존 Firebase 메시지 삭제는 iOS/Firebase 범위에서 하지 않는다.

---

# 3. 필요한 데이터 모델

## 3.1 User

| 필드 | 타입 예시 | 필수 | 설명 |
|---|---|---:|---|
| id | BIGINT | O | 사용자 ID |
| login_id | VARCHAR(30) | O | 로그인 아이디, 유일값 |
| password_hash | VARCHAR(255) | O | BCrypt 해시 |
| name | VARCHAR(30) | O | 앱에 공개되는 이름 |
| age | INT | O | 나이 |
| gender | VARCHAR(20) | O | `MALE`, `FEMALE` |
| sport_id | BIGINT | O | 운동 종목 1개 |
| level | VARCHAR(20) | O | `BEGINNER`, `INTERMEDIATE`, `ADVANCED` |
| region_id | BIGINT | O | 서울시 구 단위 지역 |
| profile_image_url | VARCHAR(500) | X | 프로필 이미지 URL |
| created_at | DATETIME | O | 생성 시각 |
| updated_at | DATETIME | O | 수정 시각 |

> **제안:** 성별 선택지는 MVP에서 `MALE`, `FEMALE` 두 개로 제한한다. 다른 선택지가 필요하면 API 구현 전에 enum만 함께 확장한다.

## 3.2 Sport

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 운동 종목 ID |
| code | VARCHAR(30) | 고정 코드 |
| name | VARCHAR(30) | 한글 표시 이름 |

초기 데이터 **제안**:

| code | name |
|---|---|
| FITNESS | 헬스 |
| RUNNING | 러닝 |
| SOCCER | 축구 |
| BASKETBALL | 농구 |
| BADMINTON | 배드민턴 |
| TENNIS | 테니스 |
| CLIMBING | 클라이밍 |
| SWIMMING | 수영 |

## 3.3 Region

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 지역 ID |
| city | VARCHAR(30) | `서울특별시` |
| district | VARCHAR(30) | `양천구`, `강서구` 등 |

초기 데이터는 서울특별시 25개 구를 사용한다.

## 3.4 FriendRequest

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 친구 요청 ID |
| requester_id | BIGINT | 요청을 보낸 사용자 |
| receiver_id | BIGINT | 요청을 받은 사용자 |
| status | VARCHAR(20) | `PENDING`, `ACCEPTED`, `REJECTED` |
| created_at | DATETIME | 요청 시각 |
| responded_at | DATETIME | 수락 또는 거절 시각 |

거절 후 다시 요청하면 기존 행을 되돌리지 않고 새로운 요청 행을 생성한다.

## 3.5 Friendship

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 친구 관계 ID |
| user1_id | BIGINT | ID가 더 작은 사용자 |
| user2_id | BIGINT | ID가 더 큰 사용자 |
| created_at | DATETIME | 친구가 된 시각 |

- `(user1_id, user2_id)`에 유일 제약조건을 둔다.
- 친구 삭제는 이 행만 삭제한다.

## 3.6 Appointment

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 약속 ID |
| user1_id | BIGINT | 참여 사용자 1 |
| user2_id | BIGINT | 참여 사용자 2 |
| created_by | BIGINT | 약속을 생성한 사용자 |
| scheduled_at | DATETIME | 약속 날짜와 시간 |
| place | VARCHAR(100) | 약속 장소 |
| created_at | DATETIME | 생성 시각 |
| updated_at | DATETIME | 수정 시각 |

- 약속은 별도의 수락 상태를 갖지 않는다.
- 약속 삭제는 실제 행을 삭제하는 방식으로 단순화한다.
- 두 참여자 모두 수정 및 삭제할 수 있다.

## 3.7 Review

| 필드 | 타입 예시 | 설명 |
|---|---|---|
| id | BIGINT | 후기 ID |
| reviewer_id | BIGINT | 작성자 |
| reviewee_id | BIGINT | 후기를 받은 사용자 |
| rating | TINYINT | 1~5 정수 |
| content | VARCHAR(500) | 선택 후기 글 |
| image_url | VARCHAR(500) | 선택 이미지 URL 1개 |
| created_at | DATETIME | 작성 시각 |

- 같은 두 사용자 사이에 여러 후기를 허용한다.
- 작성 시점에 두 사용자가 친구인지 검사한다.
- 친구 관계가 삭제되어도 이미 작성된 후기는 유지한다.

## 3.8 Chat

MySQL 데이터 모델과 Spring API를 만들지 않는다.

- **iOS 담당:** Firebase Cloud Firestore의 채팅방 및 메시지 모델
- Spring 백엔드는 친구 목록에 `chatRoomId`를 제공

---

# 4. 추천 API 목록

## 인증 및 공통 데이터

| Method | Path | 설명 | 인증 |
|---|---|---|---:|
| POST | `/api/v1/auth/signup` | 회원가입 | X |
| POST | `/api/v1/auth/login` | 로그인 | X |
| GET | `/api/v1/metadata` | 운동 종목과 서울 지역 목록 | X |

## 사용자와 이미지

| Method | Path | 설명 | 인증 |
|---|---|---|---:|
| GET | `/api/v1/users/me` | 내 프로필 조회 | O |
| PATCH | `/api/v1/users/me` | 내 프로필 수정 | O |
| GET | `/api/v1/users/{userId}` | 사용자 공개 프로필 조회 | O |
| POST | `/api/v1/images` | 이미지 1장 업로드 | O |

## 추천과 친구

| Method | Path | 설명 | 인증 |
|---|---|---|---:|
| GET | `/api/v1/recommendations` | 추천 사용자 최대 10명 조회 | O |
| POST | `/api/v1/friend-requests` | 친구 요청 보내기 | O |
| GET | `/api/v1/friend-requests/received` | 받은 대기 요청 조회 | O |
| POST | `/api/v1/friend-requests/{requestId}/accept` | 친구 요청 수락 | O |
| POST | `/api/v1/friend-requests/{requestId}/reject` | 친구 요청 거절 | O |
| GET | `/api/v1/friends` | 친구 목록 조회 | O |
| DELETE | `/api/v1/friends/{friendUserId}` | 친구 삭제 | O |

## 약속

| Method | Path | 설명 | 인증 |
|---|---|---|---:|
| POST | `/api/v1/friends/{friendUserId}/appointments` | 약속 생성 | O |
| GET | `/api/v1/friends/{friendUserId}/appointments` | 두 사용자의 약속 목록 | O |
| PATCH | `/api/v1/appointments/{appointmentId}` | 약속 수정 | O |
| DELETE | `/api/v1/appointments/{appointmentId}` | 약속 삭제 | O |

## 후기

| Method | Path | 설명 | 인증 |
|---|---|---|---:|
| POST | `/api/v1/users/{userId}/reviews` | 해당 사용자에게 후기 작성 | O |
| GET | `/api/v1/users/{userId}/reviews` | 해당 사용자가 받은 후기 조회 | O |

---

# 5. 각 API의 요청 및 응답 예시

## 5.1 회원가입

`POST /api/v1/auth/signup`

> iOS는 회원가입 화면과 온보딩 화면에서 입력한 값을 모아 마지막 `시작하기` 버튼에서 한 번 요청한다.

### 요청

```json
{
  "loginId": "runner01",
  "password": "password123!",
  "name": "김민수",
  "age": 25,
  "gender": "MALE",
  "sportId": 2,
  "level": "INTERMEDIATE",
  "regionId": 5
}
```

### 응답 `201 Created`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 12,
    "name": "김민수",
    "profileImageUrl": null
  }
}
```

### 검증 규칙 제안

- `loginId`: 4~30자 영문 소문자, 숫자, 밑줄
- `password`: 8~50자
- `name`: 1~30자
- `age`: 14~100
- `sportId`, `regionId`: 실제 메타데이터에 존재해야 함

## 5.2 로그인

`POST /api/v1/auth/login`

### 요청

```json
{
  "loginId": "runner01",
  "password": "password123!"
}
```

### 응답 `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": 12,
    "name": "김민수",
    "profileImageUrl": "https://api.example.com/uploads/profile-12.jpg"
  }
}
```

## 5.3 메타데이터 조회

`GET /api/v1/metadata`

### 응답 `200 OK`

```json
{
  "sports": [
    {
      "id": 1,
      "code": "FITNESS",
      "name": "헬스"
    },
    {
      "id": 2,
      "code": "RUNNING",
      "name": "러닝"
    },
    {
      "id": 3,
      "code": "SOCCER",
      "name": "축구"
    }
  ],
  "regions": [
    {
      "id": 1,
      "city": "서울특별시",
      "district": "강남구"
    },
    {
      "id": 2,
      "city": "서울특별시",
      "district": "강서구"
    },
    {
      "id": 3,
      "city": "서울특별시",
      "district": "양천구"
    }
  ],
  "levels": [
    {
      "code": "BEGINNER",
      "name": "하"
    },
    {
      "code": "INTERMEDIATE",
      "name": "중"
    },
    {
      "code": "ADVANCED",
      "name": "상"
    }
  ],
  "genders": [
    {
      "code": "MALE",
      "name": "남성"
    },
    {
      "code": "FEMALE",
      "name": "여성"
    }
  ]
}
```

## 5.4 내 프로필 조회

`GET /api/v1/users/me`

### 응답 `200 OK`

```json
{
  "id": 12,
  "loginId": "runner01",
  "name": "김민수",
  "age": 25,
  "gender": "MALE",
  "profileImageUrl": "https://api.example.com/uploads/profile-12.jpg",
  "sport": {
    "id": 2,
    "code": "RUNNING",
    "name": "러닝"
  },
  "level": "INTERMEDIATE",
  "region": {
    "id": 3,
    "city": "서울특별시",
    "district": "양천구"
  },
  "friendCount": 7,
  "appointmentCount": 3,
  "averageRating": 4.5,
  "reviewCount": 4
}
```

후기가 없으면 다음처럼 반환한다.

```json
{
  "averageRating": null,
  "reviewCount": 0
}
```

## 5.5 내 프로필 수정

`PATCH /api/v1/users/me`

전달한 필드만 변경한다.

### 요청

```json
{
  "name": "김민수",
  "age": 26,
  "gender": "MALE",
  "sportId": 3,
  "level": "ADVANCED",
  "regionId": 2,
  "profileImageUrl": "https://api.example.com/uploads/8ad1-profile.jpg"
}
```

### 응답 `200 OK`

```json
{
  "id": 12,
  "name": "김민수",
  "age": 26,
  "gender": "MALE",
  "profileImageUrl": "https://api.example.com/uploads/8ad1-profile.jpg",
  "sport": {
    "id": 3,
    "code": "SOCCER",
    "name": "축구"
  },
  "level": "ADVANCED",
  "region": {
    "id": 2,
    "city": "서울특별시",
    "district": "강서구"
  }
}
```

## 5.6 사용자 공개 프로필 조회

`GET /api/v1/users/{userId}`

### 응답 `200 OK`

```json
{
  "id": 35,
  "name": "이서연",
  "age": 27,
  "gender": "FEMALE",
  "profileImageUrl": null,
  "sport": {
    "id": 6,
    "code": "TENNIS",
    "name": "테니스"
  },
  "level": "ADVANCED",
  "region": {
    "id": 1,
    "city": "서울특별시",
    "district": "강남구"
  },
  "friendCount": 12,
  "appointmentCount": 8,
  "averageRating": 4.8,
  "reviewCount": 10,
  "relationship": "FRIEND",
  "recentReviewImages": [
    "https://api.example.com/uploads/review-a.jpg",
    "https://api.example.com/uploads/review-b.jpg"
  ]
}
```

`relationship` 값:

- `NONE`: 관계 없음
- `OUTGOING_PENDING`: 내가 친구 요청을 보냄
- `INCOMING_PENDING`: 내가 친구 요청을 받음
- `FRIEND`: 현재 친구

## 5.7 이미지 업로드

`POST /api/v1/images`

`Content-Type: multipart/form-data`

### 요청 폼

| 이름 | 타입 | 필수 | 설명 |
|---|---|---:|---|
| file | binary | O | 이미지 파일 1장 |
| purpose | string | O | `PROFILE` 또는 `REVIEW` |

### 응답 `201 Created`

```json
{
  "imageUrl": "https://api.example.com/uploads/550e8400-e29b-41d4-a716.jpg"
}
```

### 업로드 정책 제안

- 최대 5MB
- JPEG와 PNG만 허용
- iOS의 HEIC 이미지는 업로드 전에 JPEG로 변환
- 파일명은 서버가 UUID로 변경
- 해커톤에서는 Spring 서버 로컬 디스크의 `uploads` 폴더에 저장
- 추후 S3로 변경하더라도 API 응답 형식은 그대로 유지

> 로컬 파일은 서버 재배포 시 사라질 수 있다. 해커톤 데모용 제안이며 배포 환경이 결정되면 S3 등의 외부 저장소를 검토한다.

## 5.8 추천 사용자 조회

`GET /api/v1/recommendations?sportId=3&regionId=2&size=10`

모든 쿼리 파라미터는 선택 사항이다.

| 파라미터 | 기본값 | 설명 |
|---|---:|---|
| sportId | 없음 | 지정하면 해당 운동 종목만 반환 |
| regionId | 없음 | 지정하면 해당 구만 반환 |
| size | 10 | 최대 10 |

정렬 및 제외 규칙:

1. 자기 자신 제외
2. 현재 친구 제외
3. 어느 방향이든 대기 중인 친구 요청 상대 제외
4. `sportId`가 있으면 해당 종목만 포함
5. `regionId`가 있으면 해당 지역만 포함
6. `regionId`가 없으면 내 지역 우선, 이후 서울의 다른 지역
7. 같은 우선순위 안에서는 랜덤 정렬 가능
8. 과거에 넘겼거나 거절한 사용자는 다시 포함될 수 있음

### 응답 `200 OK`

```json
{
  "items": [
    {
      "id": 35,
      "name": "이서연",
      "age": 27,
      "gender": "FEMALE",
      "profileImageUrl": null,
      "sport": {
        "id": 3,
        "code": "SOCCER",
        "name": "축구"
      },
      "level": "ADVANCED",
      "region": {
        "id": 2,
        "city": "서울특별시",
        "district": "강서구"
      },
      "averageRating": 4.8,
      "reviewCount": 10
    }
  ]
}
```

추천 대상이 없으면 오류가 아니라 빈 배열을 반환한다.

```json
{
  "items": []
}
```

## 5.9 친구 요청 보내기

`POST /api/v1/friend-requests`

### 요청

```json
{
  "receiverUserId": 35
}
```

### 응답 `201 Created`

```json
{
  "id": 101,
  "requesterUserId": 12,
  "receiverUserId": 35,
  "status": "PENDING",
  "createdAt": "2026-08-01T10:15:00"
}
```

다음 상황은 `409 Conflict`:

- 이미 친구
- 같은 방향의 대기 요청이 존재
- 상대가 나에게 보낸 대기 요청이 존재

상대가 보낸 요청이 이미 있으면 새 요청을 만들지 말고 받은 요청을 수락하도록 안내한다.

## 5.10 받은 친구 요청 조회

`GET /api/v1/friend-requests/received`

### 응답 `200 OK`

```json
{
  "items": [
    {
      "requestId": 101,
      "createdAt": "2026-08-01T10:15:00",
      "user": {
        "id": 35,
        "name": "이서연",
        "age": 27,
        "gender": "FEMALE",
        "profileImageUrl": null,
        "sport": {
          "id": 6,
          "code": "TENNIS",
          "name": "테니스"
        },
        "level": "ADVANCED",
        "region": {
          "id": 1,
          "city": "서울특별시",
          "district": "강남구"
        }
      }
    }
  ]
}
```

## 5.11 친구 요청 수락

`POST /api/v1/friend-requests/{requestId}/accept`

### 응답 `200 OK`

```json
{
  "requestId": 101,
  "status": "ACCEPTED",
  "friend": {
    "id": 35,
    "name": "이서연",
    "profileImageUrl": null,
    "chatRoomId": "12_35"
  }
}
```

백엔드는 한 트랜잭션에서 다음 작업을 수행한다.

1. 요청이 현재 사용자에게 온 `PENDING` 요청인지 검사
2. 요청을 `ACCEPTED`로 변경
3. `Friendship` 생성

## 5.12 친구 요청 거절

`POST /api/v1/friend-requests/{requestId}/reject`

### 응답 `200 OK`

```json
{
  "requestId": 101,
  "status": "REJECTED"
}
```

## 5.13 친구 목록 조회

`GET /api/v1/friends`

### 응답 `200 OK`

```json
{
  "items": [
    {
      "id": 35,
      "name": "이서연",
      "profileImageUrl": null,
      "sport": {
        "id": 6,
        "code": "TENNIS",
        "name": "테니스"
      },
      "region": {
        "id": 1,
        "city": "서울특별시",
        "district": "강남구"
      },
      "chatRoomId": "12_35",
      "friendSince": "2026-08-01T10:20:00"
    }
  ]
}
```

> Firebase의 마지막 메시지, 안 읽은 메시지 수, 마지막 메시지 시각은 iOS가 Firestore에서 합쳐서 표시한다.

## 5.14 친구 삭제

`DELETE /api/v1/friends/{friendUserId}`

### 응답 `204 No Content`

친구 관계만 삭제한다. 기존 약속, 후기, Firebase 메시지는 삭제하지 않는다.

## 5.15 약속 생성

`POST /api/v1/friends/{friendUserId}/appointments`

### 요청

```json
{
  "scheduledAt": "2026-08-01T18:30:00",
  "place": "양천구 목동운동장"
}
```

### 응답 `201 Created`

```json
{
  "id": 501,
  "scheduledAt": "2026-08-01T18:30:00",
  "place": "양천구 목동운동장",
  "createdByUserId": 12,
  "participants": [
    {
      "id": 12,
      "name": "김민수"
    },
    {
      "id": 35,
      "name": "이서연"
    }
  ],
  "createdAt": "2026-08-01T11:00:00"
}
```

- 현재 친구에게만 생성할 수 있다.
- 생성 즉시 확정된다.
- 모든 시간 문자열은 `Asia/Seoul` 기준의 ISO-8601 형식을 사용한다.

## 5.16 두 사용자 사이의 약속 목록

`GET /api/v1/friends/{friendUserId}/appointments`

### 응답 `200 OK`

```json
{
  "items": [
    {
      "id": 501,
      "scheduledAt": "2026-08-01T18:30:00",
      "place": "양천구 목동운동장",
      "createdByUserId": 12
    },
    {
      "id": 488,
      "scheduledAt": "2026-07-20T15:00:00",
      "place": "강서구 테니스장",
      "createdByUserId": 35
    }
  ]
}
```

정렬은 `scheduledAt` 내림차순을 사용한다.

## 5.17 약속 수정

`PATCH /api/v1/appointments/{appointmentId}`

전달한 필드만 변경한다.

### 요청

```json
{
  "scheduledAt": "2026-08-01T19:00:00",
  "place": "양천구 목동 파리공원"
}
```

### 응답 `200 OK`

```json
{
  "id": 501,
  "scheduledAt": "2026-08-01T19:00:00",
  "place": "양천구 목동 파리공원",
  "createdByUserId": 12,
  "updatedAt": "2026-08-01T11:10:00"
}
```

## 5.18 약속 삭제

`DELETE /api/v1/appointments/{appointmentId}`

### 응답 `204 No Content`

약속에 포함된 두 사용자 중 한 명만 삭제할 수 있다.

## 5.19 후기 작성

`POST /api/v1/users/{userId}/reviews`

`userId`는 후기를 받을 사용자 ID다.

### 요청

```json
{
  "rating": 5,
  "content": "러닝 자세를 친절하게 알려줬어요!",
  "imageUrl": "https://api.example.com/uploads/review-550e8400.jpg"
}
```

별점만 보내는 요청도 가능하다.

```json
{
  "rating": 5
}
```

### 응답 `201 Created`

```json
{
  "id": 701,
  "rating": 5,
  "content": "러닝 자세를 친절하게 알려줬어요!",
  "imageUrl": "https://api.example.com/uploads/review-550e8400.jpg",
  "reviewer": {
    "id": 12,
    "name": "김민수",
    "profileImageUrl": "https://api.example.com/uploads/profile-12.jpg"
  },
  "createdAt": "2026-08-01T21:30:00"
}
```

작성 시점에 현재 친구가 아니면 `403 Forbidden`을 반환한다.

## 5.20 받은 후기 조회

`GET /api/v1/users/{userId}/reviews?page=0&size=20`

### 응답 `200 OK`

```json
{
  "items": [
    {
      "id": 701,
      "rating": 5,
      "content": "러닝 자세를 친절하게 알려줬어요!",
      "imageUrl": "https://api.example.com/uploads/review-550e8400.jpg",
      "reviewer": {
        "id": 12,
        "name": "김민수",
        "profileImageUrl": "https://api.example.com/uploads/profile-12.jpg"
      },
      "createdAt": "2026-08-01T21:30:00"
    }
  ],
  "page": 0,
  "size": 20,
  "hasNext": false
}
```

---

# 6. 인증 방식과 공통 에러 응답

## 6.1 인증 방식

### 확정 및 제안

- **확정:** 아이디와 비밀번호 로그인
- **제안:** 로그인 성공 시 JWT Access Token 한 개만 발급
- **제안:** Access Token 만료 시간은 해커톤 편의를 위해 24시간
- **MVP 제외:** Refresh Token, 로그아웃 API, 이메일 인증, 전화번호 인증, 비밀번호 찾기

JWT가 필요한 요청은 다음 헤더를 사용한다.

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

로그아웃은 iOS가 저장한 토큰을 삭제하는 방식으로 처리한다.

## 6.2 비밀번호 저장

- 평문 비밀번호를 DB나 로그에 남기지 않는다.
- Spring Security의 BCryptPasswordEncoder를 사용한다.
- 로그인 실패 시 아이디가 없는지, 비밀번호가 틀렸는지 구분해 노출하지 않는다.

## 6.3 성공 응답

- 객체 또는 `items` 목록을 직접 반환한다.
- 불필요한 공통 `data` 래퍼는 두지 않는다.
- 삭제 성공은 `204 No Content`를 사용한다.

## 6.4 공통 에러 응답

```json
{
  "code": "VALIDATION_ERROR",
  "message": "입력값을 확인해주세요.",
  "fieldErrors": [
    {
      "field": "age",
      "reason": "나이는 14 이상이어야 합니다."
    }
  ],
  "timestamp": "2026-08-01T10:00:00"
}
```

`fieldErrors`가 필요 없는 오류에서는 빈 배열을 반환한다.

```json
{
  "code": "USER_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "fieldErrors": [],
  "timestamp": "2026-08-01T10:00:00"
}
```

## 6.5 주요 HTTP 상태와 에러 코드

| HTTP | code 예시 | 상황 |
|---:|---|---|
| 400 | `VALIDATION_ERROR` | 필수값 누락, 잘못된 enum이나 별점 |
| 400 | `INVALID_IMAGE` | 지원하지 않는 이미지 |
| 401 | `INVALID_CREDENTIALS` | 로그인 실패 |
| 401 | `UNAUTHORIZED` | 토큰 없음, 만료, 위조 |
| 403 | `NOT_FRIEND` | 친구 전용 기능 호출 |
| 403 | `APPOINTMENT_ACCESS_DENIED` | 관계없는 약속 수정·삭제 |
| 404 | `USER_NOT_FOUND` | 사용자 없음 |
| 404 | `FRIEND_REQUEST_NOT_FOUND` | 요청 없음 |
| 404 | `APPOINTMENT_NOT_FOUND` | 약속 없음 |
| 409 | `LOGIN_ID_ALREADY_EXISTS` | 아이디 중복 |
| 409 | `ALREADY_FRIENDS` | 이미 친구 |
| 409 | `FRIEND_REQUEST_ALREADY_PENDING` | 대기 요청 중복 |
| 413 | `IMAGE_TOO_LARGE` | 이미지 용량 초과 |
| 500 | `INTERNAL_SERVER_ERROR` | 예상하지 못한 서버 오류 |

---

# 7. 백엔드와 iOS의 협업 규칙

## 7.1 API 계약

- 모든 REST API는 `/api/v1`으로 시작한다.
- Swagger UI를 API 계약의 기준으로 사용한다.
- Swagger UI 제안 주소: `/swagger-ui/index.html`
- OpenAPI JSON 제안 주소: `/v3/api-docs`
- iOS가 사용하는 필드명과 enum을 바꿀 때는 먼저 공유한다.
- 구현되지 않은 API도 Swagger와 예시 JSON을 먼저 작성해 iOS가 병렬 개발할 수 있게 한다.

## 7.2 데이터 형식

- JSON 필드명은 `camelCase`
- DB 컬럼명은 `snake_case`
- 사용자, 지역, 운동 종목 ID는 정수
- 날짜와 시간은 ISO-8601 문자열
- 해커톤에서는 모든 사용자 시간을 `Asia/Seoul`로 해석
- 값이 없으면 임의의 빈 문자열보다 `null`을 사용
- 빈 목록은 `null`이 아니라 `[]`을 사용
- 실력, 성별, 상태는 문서에 적힌 enum 코드만 사용

## 7.3 화면과 API 연결

| iOS 화면 | 사용 API |
|---|---|
| 회원가입/온보딩 | `GET /metadata`, `POST /auth/signup` |
| 로그인 | `POST /auth/login` |
| 홈 추천 | `GET /recommendations` |
| 친구 요청 버튼 | `POST /friend-requests` |
| 채팅 탭 상단 요청 | `GET /friend-requests/received` |
| 요청 수락/거절 | `POST /friend-requests/{id}/accept`, `.../reject` |
| 채팅 탭 친구 목록 | `GET /friends` + Firestore 데이터 |
| 상대방 프로필 | `GET /users/{id}`, `GET /users/{id}/reviews` |
| 채팅방 | Firestore, 약속 API |
| 약속 달력 | 약속 생성/조회/수정/삭제 API |
| 후기 작성 | `POST /images`, `POST /users/{id}/reviews` |
| 내 프로필 | `GET /users/me`, `GET /users/{myId}/reviews` |
| 프로필 수정 | `POST /images`, `PATCH /users/me` |

## 7.4 Firebase 채팅 경계

- iOS가 Firestore 채팅방과 메시지를 생성, 조회, 전송한다.
- Spring 백엔드는 메시지 본문, 읽음 여부, 마지막 메시지를 관리하지 않는다.
- Spring의 친구 목록 응답에 `chatRoomId`를 포함한다.
- iOS는 백엔드 친구 목록에 있는 사용자에게만 채팅 진입과 전송 UI를 제공한다.
- 친구 삭제 후에는 새 메시지 전송을 막되 기존 Firestore 데이터는 그대로 둔다.

> **중요 제안:** 백엔드 JWT와 Firebase 인증은 자동으로 연결되지 않는다. 해커톤 전 iOS 개발자가 Firebase Authentication 및 Firestore Security Rules 사용 방식을 확인해야 한다. 데모에서 UI로만 친구 여부를 제한하면 보안상 완전한 제한은 아니며, 이는 MVP의 알려진 한계로 기록한다.

## 7.5 추천 화면 역할 분리

- 백엔드: 조건에 맞는 사용자 최대 10명을 반환
- iOS: 10명을 메모리에 저장하고 카드 한 장씩 표시
- iOS: 새로고침 버튼을 누르면 다음 배열 항목 표시
- iOS: 10명을 모두 사용하면 API 재호출
- 백엔드: 넘긴 사용자 이력을 저장하지 않음

## 7.6 개발 순서 제안

1. DTO와 Swagger 예시를 먼저 공유
2. `metadata`, 회원가입, 로그인 구현
3. 프로필과 추천 구현
4. 친구 요청과 친구 목록 구현
5. 약속과 후기 구현
6. 이미지 업로드 구현
7. iOS 연동 테스트

## 7.7 백엔드 2명 분담 제안

### 백엔드 A

- 공통 프로젝트 설정
- 인증/JWT
- 회원가입과 로그인
- 사용자 프로필
- 메타데이터
- 이미지 업로드

### 백엔드 B

- 추천
- 친구 요청 및 친구 관계
- 약속
- 후기와 평균 별점

두 영역이 공유하는 `User`, `Sport`, `Region` 엔티티와 응답 DTO 규칙은 구현 전에 함께 확정한다.

## 7.8 테스트용 약속

- 공용 테스트 계정 5~10개를 미리 삽입
- 서울의 서로 다른 구와 서로 다른 운동 종목을 섞어 생성
- 추천 제외, 친구 요청, 평균 별점을 확인할 데이터 포함
- iOS는 운영 서버 주소를 코드에 직접 여러 군데 쓰지 않고 환경 설정 한 곳에서 관리

---

# 8. 해커톤에서 제외해도 되는 기능

- 카카오, Apple 등 소셜 로그인
- 이메일 및 전화번호 인증
- 비밀번호 찾기와 변경
- Refresh Token과 서버 로그아웃
- 운동 종목 여러 개 등록
- GPS 및 실제 거리 계산
- 서울 외 지역
- AI 기반 추천 점수
- 넘긴 사용자 영구 제외
- 보낸 친구 요청 목록
- 사용자 차단과 신고
- 친구 요청 푸시 알림
- Spring 기반 실시간 채팅과 채팅 API
- 채팅 이미지, 읽음 표시, 입력 중 표시
- 약속 수락 및 거절
- 반복 약속과 캘린더 외부 연동
- 약속 완료 인증
- 후기 수정 및 삭제
- 후기 댓글과 좋아요
- 이미지 여러 장 업로드
- 이미지 리사이징과 썸네일 생성
- 관리자 페이지
- 회원 탈퇴

---

# 9. iOS 개발자에게 공유할 Notion용 요약

## 프로젝트 한 줄 소개

서울 안에서 운동 메이트를 추천받고, 친구가 된 사용자끼리 채팅과 운동 약속을 나눈 뒤 후기를 작성하는 앱입니다.

## iOS가 알아야 할 핵심 규칙

1. 로그인은 `loginId + password`이고, 로그인 후 받은 JWT를 모든 인증 API의 `Authorization: Bearer {token}` 헤더에 넣습니다.
2. 운동 종목, 지역, 실력, 성별 선택지는 `GET /api/v1/metadata`에서 받습니다.
3. 추천 API는 최대 10명을 반환합니다. 카드 한 장씩 넘기는 동작은 iOS에서 처리합니다.
4. 현재 친구, 자기 자신, 대기 중인 친구 요청 상대는 추천에서 제외됩니다.
5. 받은 친구 요청과 친구 목록은 별도 API입니다. 채팅 탭에서 두 결과를 함께 표시합니다.
6. 채팅 메시지는 iOS/Firebase가 담당합니다. Spring 백엔드에는 메시지 API가 없습니다.
7. 친구 목록 응답의 `chatRoomId`를 Firestore 채팅방 ID로 사용합니다.
8. 약속은 친구끼리만 만들 수 있고 생성 즉시 확정됩니다.
9. 후기는 친구에게 여러 번 쓸 수 있으며 별점만 보내도 됩니다.
10. 이미지가 있다면 먼저 `/images`에 업로드한 후 반환된 URL을 프로필 또는 후기 요청에 넣습니다.

## 공통 Base URL

```text
개발: 추후 공유
API prefix: /api/v1
Swagger UI: /swagger-ui/index.html
```

## 공통 인증 헤더

```http
Authorization: Bearer {accessToken}
Content-Type: application/json
```

이미지 업로드만 `multipart/form-data`를 사용합니다.

## 주요 enum

```text
gender: MALE | FEMALE
level: BEGINNER | INTERMEDIATE | ADVANCED
friend request status: PENDING | ACCEPTED | REJECTED
relationship: NONE | OUTGOING_PENDING | INCOMING_PENDING | FRIEND
image purpose: PROFILE | REVIEW
```

## API 체크리스트

- [ ] 회원가입
- [ ] 로그인
- [ ] 메타데이터
- [ ] 내 프로필 조회
- [ ] 내 프로필 수정
- [ ] 상대 프로필 조회
- [ ] 이미지 업로드
- [ ] 추천 사용자 10명
- [ ] 친구 요청 보내기
- [ ] 받은 요청 목록
- [ ] 친구 요청 수락
- [ ] 친구 요청 거절
- [ ] 친구 목록
- [ ] 친구 삭제
- [ ] 약속 생성
- [ ] 약속 목록
- [ ] 약속 수정
- [ ] 약속 삭제
- [ ] 후기 작성
- [ ] 후기 목록

## 아직 최종 확정되지 않은 항목

- 실제 백엔드 Base URL과 배포 환경
- MySQL 배포 위치
- 이미지의 최종 저장소
- Firebase Authentication 및 Firestore Security Rules 방식
- 화면에서 `만난 횟수`를 `약속 횟수`로 변경할지 여부
- 성별 enum에 추가 선택지를 둘지 여부

---

# 해커톤 구현 직전 최종 확인 항목

아래 항목만 팀에서 확인한 뒤 이 문서의 상태를 `확정`으로 변경한다.

1. iOS 개발자가 Firestore를 실제 채팅 저장소로 사용하는지
2. Firebase Authentication과 보안 규칙을 어떻게 설정할지
3. 테스트 서버 Base URL
4. 이미지 로컬 저장을 데모 환경에서 유지할 수 있는지
5. `MALE`, `FEMALE` 외 성별 선택지가 필요한지
6. 프로필의 `만난 횟수` 문구를 `약속 횟수`로 바꿀지
