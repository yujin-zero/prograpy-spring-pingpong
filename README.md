# 🏓 PingPong Game API

## 📌 프로젝트 개요
PingPong 게임을 위한 RESTful API를 제공하는 **Spring Boot 기반 프로젝트**입니다.  
유저는 방을 생성하고, 참가 및 나가기, 팀 변경이 가능하며, 게임 시작 후 자동 종료됩니다.

---

## 🛠️ 기술 스택
- **Backend:** Java 17, Spring Boot, Spring Data JPA, Lombok  
- **Database:** H2 Database  
- **API 문서화:** Swagger (SpringDoc OpenAPI)  
- **비동기 처리:** `@Async`, CompletableFuture  
- **트랜잭션 관리:** Spring `@Transactional`  
- **빌드 & 배포:** Gradle  

---

## 🚀 주요 기능  

### ✅ **공통 API**
- [x] **헬스 체크 API (`GET /health`)** - 서버 상태 확인  
- [x] **데이터 초기화 API (`POST /init`)** - 기존 데이터 삭제 후 더미 유저 생성  

### ✅ **유저 API**
- [x] **유저 전체 조회 (`GET /user`)** - 페이징 지원  

### ✅ **방(Room) 관련 API**
- [x] **방 생성 (`POST /room`)**  
- [x] **방 전체 조회 (페이징 지원, `GET /room`)**  
- [x] **방 상세 조회 (`GET /room/{roomId}`)**  
- [x] **방 참가 (`POST /room/attention/{roomId}`)**  
- [x] **방 나가기 (`POST /room/out/{roomId}`)**  
- [x] **팀 변경 (`PUT /team/{roomId}`)**  

### ✅ **게임 API**
- [x] **게임 시작 (`PUT /room/start/{roomId}`)**  
- [x] **게임 자동 종료 (60초 후 `FINISH` 상태로 변경)**  

---

## 📖 API 명세서  
Swagger 문서를 통해 API 요청 및 응답 형식을 확인할 수 있습니다.  

