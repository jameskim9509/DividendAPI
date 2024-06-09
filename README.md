# Dividend API
## 배당금 정보 API

- ## 요구사항
1. 매일 0시 0분 0초 현재 등록된 모든 회사들에 대한 배당금 정보 스크래핑 및 저장
2. 사용자(ADMIN, USER)별 권한(READ_ROLE, WRITE_ROLE) 부여 및 권한별 URL 요청 제한 <br>
   ( 이후 JWT 토큰을 활용한 지속적 인증 수행 )
3. 사용자별 요청에 대해 배당금 정보 스크래핑 후 저장 또는 저장된 배당금 정보 조회 및 삭제 ( REST API구현 )
4. 키워드에 대한 회사명 자동완성 텍스트 지원 <br>
( ex) "NV" 키워드에 대해 "NVIDIA Corporation (NVDA)"등 "NV"로 시작하는 회사명 리스트 반환 )

- ## 기술 요구사항
1. 스레드풀을 이용한 스케줄러 관리 및 배당금 정보 스크랩( At finance.yahoo.com ) 구현
2. JPA Spring Data를 사용해 Repository 구현
3. 사용자 회원가입 시( post /auth/signup ) <br>
   전달된 권한 정보를 통해 해당 권한을 가진 유저 저장 및 passwordEncoder를 통한 패스워드 암호화
4. 사용자 로그인 시 ( post /auth/signin ) <br>
   사용자 검증 및 JWT 토큰 생성 및 지속적 인증 구현 ( JWT Authentication Filter 구현 )
5. 조회된 배당금 정보에 대해 Redis 캐시 서버에 저장 및 재조회 속도 향상
6. Trie 자료구조를 통한 회사명 저장 및 조회 성능 향상
7. 자동완성 텍스트 조회 시 Trie 자료구조 내 검색 및 해당 키워드로 시작하는 문자열 반환
8. Logback 설정을 통한 log파일 생성
9. custom error 및 error handler 통한 일관성 있는 예외 처리

* ## Spring Boot 개발환경
  * Intellij IDE
  * 내장 tomcat
  * embeded h2 Database
  * Redis server
  * postman

* ## DB 테이블 <br>
![DB_cap.PNG](./)

* ## Postman 사용 예
  * 로그인 <br>
  ![postman_cap1(signin).png](./)
  * 자동완성 <br>
  ![postman_cap2(autocomplete).png](./)
  * 배당금 조회 <br>
  ![postman_cap3(getDividendList).png](./)
