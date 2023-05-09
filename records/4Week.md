# 4Week_김성찬.md

## Title: [4Week] 김성찬

### 미션 요구사항 분석 & 체크리스트

---
미션

- 네이버클라우드플랫폼을 통한 배포, 도메인, HTTPS 까지 적용
- 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 성별 필터링기능 구현
- 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 호감사유 필터링기능 구현
- 내가 받은 호감리스트(/usr/likeablePerson/toList)에서 정렬기능


### 체크리스트

---

- `https://도메인/` 형태로 접속이 가능해야 한다
- 운영서버에서는 각종 소셜로그인, 인스타 아이디 연결이 되어야 한다
  - 페이스북, 구글, 카카오, 인스타 아이디로 로그인이 가능 해야 한다
- 내가 받은 호감리스트에서 특정 성별을 가진 사람에게서 받은 호감만 필터링해서 볼 수 있아야 한다
- 내가 받은 호감리스트에서 특정 호감사유의 호감만 필터링해서 볼 수 있어야 한다
- 최신순(기본)
  - 가장 최근에 받은 호감표시를 우선적으로 표시
- 날짜순
  - 가장 오래전에 받은 호감표시를 우선적으로 표시
- 인기 많은 순
  - 가장 인기가 많은 사람들의 호감표시를 우선적으로 표시
- 인기 적은 순
  - 가장 인기가 적은 사람들의 호감표시를 우선적으로 표시
- 성별순
  - 여성에게 받은 호감표시를 먼저 표시하고, 그 다음 남자에게 받은 호감표시를 후에 표시
  - 2순위 정렬조건으로는 최신순
- 호감사유순
  - 외모 때문에 받은 호감표시를 먼저 표시하고, 그 다음 성격 때문에 받은 호감표시를 후에 표시, 마지막으로 능력 때문에 받은 호감표시를 후에 표시
  - 2순위 정렬조건으로는 최신순

### 3주차 미션 요약

---

**[접근 방법]**

```agsl
if (gender != null) {
     likeablePeopleStream = likeablePeopleStream.filter(lp -> lp.getFromInstaMember().getGender().equalsIgnoreCase(gender));
            }
```

- if (gender != null): 이 블록 내부의 코드는 성별 변수가 null이 아닌 경우에만 실행됩니다. 성별 변수는 메서드 매개변수로 전달되며, 발신자의 성별에 따라 '마음에 들어요' 목록을 필터링하는 데 사용됩니다.
- lp -> lp.getFromInstaMember().getGender().equalsIgnoreCase(gender):  이 함수는 LikeablePerson 객체 lp를 입력으로 받아 lp의 성별과 제공된 성별을 비교하여 부울 값을 반환합니다.

```agsl
if (attractiveTypeCode != 0) {
      likeablePeopleStream = likeablePeopleStream.filter(lp -> lp.getAttractiveTypeCode() == attractiveTypeCode);
            }
```

- if (attractiveTypeCode != 0): 이 블록 내부의 코드는 매력적인 유형 코드가 0이 아닌 경우에만 실행됩니다. 매력적인 유형 코드 변수는 메서드 매개변수로 전달되며, 매력적인 유형 코드(호감 이유)에 따라 호감 가는 사람의 목록을 필터링하는 데 사용됩니다.
- lp -> lp.getAttractiveTypeCode() == attractiveTypeCode: 이 함수는 LikeablePerson 객체 lp를 입력으로 받아 lp의 매력적인 유형 코드와 제공된 매력적인 유형 코드의 비교를 기반으로 부울 값을 반환합니다.

```agsl
switch (sortCode) {
                case 1:
                     likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getCreateDate).reversed());
                    break;
                case 2:
                     likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing(LikeablePerson::getCreateDate));
                    break;
                case 3:
                     likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getLikes()).reversed());
                    break;
                case 4:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getLikes()));
                    break;
                case 5:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparing((LikeablePerson lp) -> lp.getFromInstaMember().getGender()).thenComparing(Comparator.comparing(LikeablePerson::getCreateDate).reversed()));
                    break;
                case 6:
                    likeablePeopleStream = likeablePeopleStream.sorted(Comparator.comparingInt(LikeablePerson::getAttractiveTypeCode).thenComparing(Comparator.comparing(LikeablePerson::getCreateDate).reversed()));
                    break;

            }
```

case 1(최신순): 내림차순(가장 최근 것부터)으로 각 LikeablePerson 객체의 createDate 속성을 기준으로 정렬됩니다. reversed() 메서드는 순서를 반전시키는 데 사용됩니다.

case 2(날짜 순서): 각 LikeablePerson 객체의 createDate 속성을 기준으로 오름차순(가장 오래된 것부터)으로 정렬됩니다.

case 3(인기 많은 순서): 각 LikeablePerson 개체의 fromInstaMember에 있는 총 좋아요 수를 기준으로 내림차순(가장 인기 있는 것부터)으로 정렬됩니다.

case 4(인기 없는 순서): 각 LikeablePerson 개체의 fromInstaMember가 가진 총 좋아요 수를 기준으로 오름차순(가장 인기가 적은 것부터)으로 정렬됩니다.

case 5(성별 순서): 각 호감 가는 사람 개체의 fromInstaMember의 성별에 따라 정렬됩니다. 여성의 호감이 먼저 표시되고 그다음에 남성의 호감이 표시됩니다. 보조 정렬 기준은 내림차순(최신순)으로 createDate 속성을 사용합니다.

case 6(호감 사유): 각 LikeablePerson 개체의 매력 유형 코드(호감 이유)를 기준으로 오름차순으로 정렬됩니다. 외모(코드 1)를 기준으로 한 호감도가 가장 먼저 표시되고, 성격(코드 2)을 기준으로 한 호감도, 마지막으로 능력(코드 3)을 기준으로 한 호감도가 그 뒤를 따릅니다. 보조 정렬 기준은 내림차순(최신순)으로 createDate 속성을 사용합니다.




**[특이사항]**

- 



**[추가, 보완 사항]**

- 
