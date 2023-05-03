# 2Week_김성찬.md

## Title: [2Week] 김성찬

### 미션 요구사항 분석 & 체크리스트

---
미션

- 호감표시 할 때 예외처리 케이스 3가지를 추가로 처리
  - 한명의 인스타회원이 다른 인스타회원에게 중복으로 호감표시를 할 수 없습니다.
  - 한명의 인스타회원이 11명 이상의 호감상대를 등록 할 수 없습니다.
  - 케이스 4 가 발생했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우에는 성공으로 처리한다.<br></br>
- 네이버 로그인 기능 구현
  - 개인의 이름과 성별등과 같은 정보 포함하면 안됨

### 체크리스트

---

- 중복 호감 표시했을 때 오류 메세지 출력
  - rq.historyBack 사용<br></br>
- 한명의 인스타회원이 11명 이상의 호감상대를 등록했을 때 오류 메세지 출력
  - rq.historyBack 사용<br></br>
- 중복 호감 표시했을 때 기존의 사유와 다른 사유로 호감을 표시하는 경우 새로운 호감 표시 등록 대신 사유 수정
  - 호감 변경 메세지 출력<br></br>
- 네이버 로그인 가능하게 구현
  - member테이블의 username에 이름과 성별, 이메일 등이 포함 되지 않게 구현 


### 2주차 미션 요약

---

**[접근 방법]**

- fromInstaMemberId 및 toInstaMemberId를 가진 LikeablePerson 객체가 이미 존재하는지 확인
- existingLikes는 이러한 객체의 목록을 저장
- 목록이 비어 있지 않은 경우, 즉 fromInstaMemberId 및 toInstaMemberId가 동일한 LikeablePerson 객체가 이미 존재하는 경우 오류 메시지 출력
- LikeablePersonRepository에서 findByFromInstaMemberId 메서드를 호출하여 회원이 이미 10개 이상의 호감 상대를 가지고 있는지 확인
- 반환된 리스트의 크기가 10보다 크면 11개 이상의 호감을 가질 수 없음을 알리는 오류 메시지를 출력
- 중복된 호감 표시를 발견했을 때 existingLikes리스트에서 LikeablePerson객체를 검색 
- LikeablePerson의 호감사유가 existingLikes의 호감 사유와 다른지 확인
- 그 이유가 다르면 LikeablePerson 객체의 호감 사유를 변경하고 변경 사항을 리포지토리에 저장한 후 성공 메시지를 출력
- 호감 사유가 같으면 원래 오류 메시지를 출력하여 동일한 인스타멤버에게 여러 번 호감 표시를 할 수 없음을 알림
- 네이버 로그인도 구글과 카카오 로그인 구현 과정을 따라했음

**[특이사항]**

- 네이버 로그인을 하면 이름과 이메일이 같이 출력된다.(member테이블의 username에 이메일과 이름이 포함이 됨)
- NAVER__{id=gi4gQt0xSt5d-dzfb7iV9qwpQvyK2VPBrcJt9b4OYlg, email=~~~~~~, name=김성찬}
- 네이버 로그인 api 설정에서 회원 이름과 연락처 이메일 주소를 전부 체크 해제 했는데도 변함이 없음
```
# application.yml

          naver:
            clientId: 
            client-secret: 
            redirect-uri: '{baseUrl}/{action}/oauth2/code/{registrationId}'
            authorization-grant-type: authorization_code
            client-name: Naver
            scope:
          
        provider:
          naver:
            authorization-uri: https://nid.naver.com/oauth2.0/authorize
            token-uri: https://nid.naver.com/oauth2.0/token
            user-info-uri: https://openapi.naver.com/v1/nid/me
            user-name-attribute: response  
```


**[추가, 보완 사항]**

