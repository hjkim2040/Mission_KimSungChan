# 3Week_김성찬.md

## Title: [3Week] 김성찬

### 미션 요구사항 분석 & 체크리스트

---
미션

- 네이버클라우드플랫폼을 통한 배포(도메인 없이, IP로 접속)
- 호감표시/호감사유변경 후, 개별 호감표시건에 대해서, 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 작업


### 체크리스트

---

- `https://서버IP:포트/` 형태로 접속이 가능해야 한다
  - 네이버 클라우드 플랫폼에서 설정한 포트와 공인 IP로 접속
- 운영서버에서는 각종 소셜로그인, 인스타 아이디 연결이 되어야 한다
  - 페이스북, 구글, 카카오, 인스타 아이디로 로그인이 가능 해야 한다
- 호감표시를 한 후 개별호감표시건에 대해서, 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 작업
- 호감사유변경을 한 후 개별호감표시건에 대해서, 3시간 동안은 호감취소와 호감사유변경을 할 수 없도록 작업
  - `LikeablePersonService`의 `cancel`메소드와 `modifyAttractive`메소드에 호감취소 및 호감사유변경이 가능한지 `isModifyUnlocked()`를 통해 판단하고 불가능 하다면 오류 메세지가 출력되도록 코드 작성 


### 3주차 미션 요약

---

**[접근 방법]**

```agsl
@Configuration
public class AppConfig {
    @Getter
    private static long likeablePersonFromMax;
    @Value("${custom.likeablePerson.from.max}")
    public void setLikeablePersonFromMax(long likeablePersonFromMax){
        AppConfig.likeablePersonFromMax = likeablePersonFromMax;
    }
    @Getter
    private static long likeablePersonModifyCoolTime;

    @Value("${custom.likeablePerson.modifyCoolTime}")
    public void setLikeablePersonModifyCoolTime(long likeablePersonModifyCoolTime) {
        AppConfig.likeablePersonModifyCoolTime = likeablePersonModifyCoolTime;
    }
    public static LocalDateTime genLikeablePersonModifyUnlockDate() {
        return LocalDateTime.now().plusSeconds(likeablePersonModifyCoolTime);
    }
}
```
- `AppConfig`클래스에서 `@Value` 어노테이션을 통해 `application.yml`에 있는 `modifyCoolTime`값을 가져옴
- `genLikeablePersonModifyUnlockDate()` 메소드는 현재 시간에서 `likeablePersonModifyCoolTime`을 더한 값을 반환
```agsl
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@Getter
@Setter
public class LikeablePerson extends BaseEntity {
    private LocalDateTime modifyUnlockDate;


    @ManyToOne
    @ToString.Exclude
    private InstaMember fromInstaMember; 
    private String fromInstaMemberUsername; 
    @ManyToOne
    @ToString.Exclude
    private InstaMember toInstaMember; 
    private String toInstaMemberUsername; 
    private int attractiveTypeCode; 

    public boolean isModifyUnlocked() {
        return modifyUnlockDate.isBefore(LocalDateTime.now());
    }

    // 초 단위에서 올림
    public String getModifyUnlockDateRemainStrHuman() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH시mm분");
        return modifyUnlockDate.format(formatter);
    }
```
- `modifyUnlockDate`가 현재 시간 전이면 `isModifyUnlocked()`메소드는 `true`를 반환
- `getModifyUnlockDateRemainStrHuman()` 메서드는 `HH시mm분` 형식의 시간을 표시하도록 `modifyUnlockDate`의 형식을 지정
```agsl
@Transactional
    public RsData<LikeablePerson> like(Member actor, String username, int attractiveTypeCode) {
        RsData canLikeRsData = canLike(actor, username, attractiveTypeCode);

        if (canLikeRsData.isFail()) return canLikeRsData;

        if (canLikeRsData.getResultCode().equals("S-2")) return modifyAttractive(actor, username, attractiveTypeCode);


        InstaMember fromInstaMember = actor.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();


        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(actor.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .modifyUnlockDate(AppConfig.genLikeablePersonModifyUnlockDate())
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장
        fromInstaMember.addFromLikeablePerson(likeablePerson);
        toInstaMember.addToLikeablePerson(likeablePerson);

        publisher.publishEvent(new EventAfterLike(this, likeablePerson));

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }
```
- 새 `LikeablePerson` 인스턴스를 생성할 때 `genLikeablePersonModifyUnlockDate()` 메서드를 사용하도록 `LikeablePersonService` 클래스의 `like()`메소드를 수정
- `like()` 메서드를 사용하여 새 `LikeablePerson` 인스턴스를 생성할 때, 이 인스턴스는 `genLikeablePersonModifyUnlockDate()` 메서드를 사용하여 `modifyUnlockDate` 필드를 설정
- `genLikeablePersonModifyUnlockDate()` 메서드를 사용하면 `modifyUnlockDate`가 현재 시간에 `AppConfig` 클래스의 `likeablePersonModifyCoolTime` 값을 더한 값으로 설정
```agsl
@Transactional
    public RsData cancel(LikeablePerson likeablePerson) {
        if (likeablePerson.isModifyUnlocked()) {

            publisher.publishEvent(new EventBeforeCancelLike(this, likeablePerson));

            likeablePerson.getFromInstaMember().removeFromLikeablePerson(likeablePerson);

            likeablePerson.getToInstaMember().removeToLikeablePerson(likeablePerson);


            likeablePersonRepository.delete(likeablePerson);
            String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
            return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(toInstaMemberUsername));
        }else{
            String unlockDateRemain = likeablePerson.getModifyUnlockDateRemainStrHuman();
            return RsData.of("F-1","최소할 수 없습니다. %s 에 다시 시도해주세요.".formatted(unlockDateRemain));
        }
    }
```
```agsl
@Transactional
    public RsData<LikeablePerson> modifyAttractive(Member actor, LikeablePerson likeablePerson, int attractiveTypeCode) {
        if (likeablePerson.isModifyUnlocked()) {
            RsData canModifyRsData = canModifyLike(actor, likeablePerson);

            if (canModifyRsData.isFail()) {
                return canModifyRsData;
            }
            String oldAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();
            String username = likeablePerson.getToInstaMember().getUsername();

            modifyAttractionTypeCode(likeablePerson, attractiveTypeCode);

            likeablePerson.setModifyUnlockDate(AppConfig.genLikeablePersonModifyUnlockDate());
            
            String newAttractiveTypeDisplayName = likeablePerson.getAttractiveTypeDisplayName();

            return RsData.of("S-3", "%s님에 대한 호감사유를 %s에서 %s(으)로 변경합니다.".formatted(username, oldAttractiveTypeDisplayName, newAttractiveTypeDisplayName), likeablePerson);
        }else{
            String unlockDateRemain = likeablePerson.getModifyUnlockDateRemainStrHuman();
            return RsData.of("F-1", "호감사유 변경이 불가능합니다. %s 에 다시 시도해주세요.".formatted(unlockDateRemain));
        }
    }
```
- `if (likeablePerson.isModifyUnlocked())`을 사용하여 쿨타임이 지났을 경우에만 수정 및 취소 할 수 있도록 함
- 호감사유 변경 후 쿨타임이 갱신 되도록 구현
- 쿨타임이 아직 활성화 상태인 경우 수정 및 취소 작업이 다시 수행 가능한 시간과 함께 오류 메시지를 반환
- 네이버 클라우드 플랫폼을 이용한 배포 가이드를 따라 서버 설정
- 수행 가능한 미션을 전부 진행하고 서버에 git clone을 해서 빌드 준비
- 도커를 이용해 배포
- `http://서버IP:포트/`에 접속해 서버 및 배포가 잘 되는지 확인
- 운영서버에서 각종 소셜로그인이 잘 연결 되는지 확인
- 도메인 구매 후 IP부여(도메인 이름 : mywk.xyz)



**[특이사항]**

- 



**[추가, 보완 사항]**

- 강사님의 해설 코드를 참고하여 리팩토링
- 리팩토링 버전으로 다시 배포
