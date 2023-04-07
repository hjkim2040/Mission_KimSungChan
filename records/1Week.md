# 1Week_김성찬.md

## Title: [1Week] 김성찬

### 미션 요구사항 분석 & 체크리스트

---
미션

- 호감상대 삭제



### 체크리스트

---

- [x] UI 작업
  - [x] 호감 목록 리스트 
  - [x] 호감 목록 삭제 버튼
- [ ] 삭제 버튼 처리
  - [x] 삭제 버튼을 누르면 해당 항목 삭제
  - [ ] 삭제 전 해당 항목에 대한 소유권 체크
  - [x] 삭제 후 호감 목록 페이지로 이동
  - [x] rq.redirectWithMsg 함수 사용


### 1주차 미션 요약

---

**[접근 방법]**

- 삭제 기능이 작동하는 것에 중점을 두고 구현을 했습니다.
- 호감 목록 UI는 daisyUI를 참고하여 개발하였습니다. (daisyUi의 Button과 Table 참고)
- 미션을 진행하기 전에 점프 투 스프링부트 게시판 만들기와 그램그램 프로젝트를 보면서 어떻게 기능을 구현해야 될까 고민을 했습니다.
- 최우선 목표는 일단 삭제기능이 제대로 작동하게 만드는 것이였고, UI는 간단하게 구현해 봤습니다.
- `LikeablePersonController`에 `GetMapping`을 통해서 삭제가 실패하면 뒤로가기가 실행되게 구현했습니다.
- `rq.redirectWithMsg` 함수를 사용하여 삭제 후 호감 목록 페이지로 이동하고 메세지가 출력되게 했습니다.
- `LikeablePersonService`에 `LikeablePersonRepository`에서 `id`를 찾아 삭제하는 기능을 구현했습니다.
- 삭제에 성공하면 성공 코드와 메세지가 나오게 했습니다.




**[특이사항]**

- 삭제 전 해당 항목에 대한 소유권 체크 기능을 구현 하지 못했습니다.
- 삭제 기능이 잘 작동하기는 하지만 코드를 알맞게 작성한건지, 더 효율적인 코드가 어떤것이 있을지 궁금증이 생겼습니다.
- 추후 리팩토링 시, 어떤 부분을 추가적으로 진행하고 싶은지에 대해 구체적으로 작성해주시기 바랍니다.
- 추후 리팩토링 시 구현하지 못한 소유권 체크과 선택 미션인 구글 로그인을 구현해 보고 싶고, 강사님 코드를 참고하여 제가 작성한 코드를 다듬어 보고 싶습니다.

**[추가, 보완 사항]**

- 피어리뷰를 통해 받은 피드백 내용과 정답코드를 기반으로 리팩토링을 진행했습니다.

  **1. 호감 삭제를 위한 테스트 케이스 만들기**
  - ```LikeablePersonControllerTests```
  ```
    @Test
    @DisplayName("호감 상대 삭제")
    @WithUserDetails("user3")
    void t006() throws Exception {
    //WHEN
    ResultActions resultActions = mvc
    .perform(
    delete("/likeablePerson/1") // delete 방식으로 요청
    .with(csrf())
    )
    .andDo(print());

        //THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete")) // delete 메소드 사용
                .andExpect(status().is3xxRedirection()) // 300번대 에러는 리다이렉션 메세지
                .andExpect(redirectedUrlPattern("/likeablePerson/list**")); // /likeablePerson/list로 시작하는? 포함된? url로 리다이렉트

        assertThat(likeablePersonService.findById(1L).isPresent()).isEqualTo(false); // Long 유형의 id가 1인 LikeablePerson이 존재하지 않아야 한다
    }
  ```
  ```
    @Test
    @DisplayName("호감삭제(없는거 삭제, 삭제가 안되어야 함)")
    @WithUserDetails("user3")
    void t007() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        delete("/likeablePerson/100") // 없는 호감 목록 삭제 시도 
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is4xxClientError()) // 4xx번대 에러가 나와야 한다
        ;
    }
  ```
  ```
    @Test
    @DisplayName("호감삭제(권한이 없는 경우, 삭제가 안됨)")
    @WithUserDetails("user2")
    void t008() throws Exception {
        // WHEN
        ResultActions resultActions = mvc
                .perform(
                        delete("/likeablePerson/1") // 1번째 호감 목록 삭제
                                .with(csrf())
                )
                .andDo(print());

        // THEN
        resultActions
                .andExpect(handler().handlerType(LikeablePersonController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().is4xxClientError()) // 403 FORBIDDEN 오류가 발생해야한다. 클라이언트가 콘텐츠에 접근할 권리를 가지고 있지 않다는 의미
        ;

        assertThat(likeablePersonService.findById(1L).isPresent()).isEqualTo(true); // Long 유형의 id가 1인 LikeablePerson이 존재해야 한다
                                                                                    // 권한은 없지만 존재는 해야 한다
    }
  ```

  **2. 호감 삭제 요청을 GET방식에서 DELETE방식으로 변경**
  **3. 컨트롤러에서 서비스를 이용해 권한 체크**
  
  - ```LikeablePersonService```
  ```
  public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다."); // likeablePerson이 없으면 실패 메세지 출력

        if (!Objects.equals(actor.getInstaMember().getId(), likeablePerson.getFromInstaMember().getId())) 
            return RsData.of("F-2", "권한이 없습니다."); // 인스타 회원의 id 와 호감을 표시한 사람의 id가 다르다면 실패 메세지 출력

        return RsData.of("S-1", "삭제가능합니다.");
    }
  ```
  - ```LikeablePersonController```
  ```
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}") // 요청 방식을 DELETE 방식으로 변경, URL이 짧아졌다(@GetMapping("/delete/{id}") -> @DeleteMapping("/{id}"))
    public String delete(@PathVariable Long id) {

        LikeablePerson likeablePerson = likeablePersonService.findById(id).orElse(null);

        RsData canActorDeleteRsData = likeablePersonService.canActorDelete(rq.getMember(), likeablePerson); // 권한 체크

        if (canActorDeleteRsData.isFail()) return rq.historyBack(canActorDeleteRsData); // 권한이 없다면 뒤로가기

        RsData deleteRs = likeablePersonService.delete(likeablePerson); // 삭제 요청

        if (deleteRs.isFail()) return rq.historyBack(deleteRs); // 삭제 실패 시 뒤로가기

        return rq.redirectWithMsg("/likeablePerson/list", deleteRs); // 삭제 성공하면 /likeablePerson/list?msg=메시지 리다이렉트

    }
  ```
  **4. 구글 로그인 기능**
  - git에 올라오지 않은(숨겨놓은) ```application-oauth.yml``` 에 추가
  ```
  spring:
    security:
      oauth2:
        client:
          registration:
              google:
              clientId: 구글 클라이언트 아이디
              client-secret: 구글 클라이언트 시크릿 키
              redirect-uri: '{baseUrl}/{action}/oauth2/code/{registrationId}'
              client-name: Google
              scope: profile
  ```
  **5. OneToMany 도입하여 양방향 관계 맺기**

  - ```InstaMember``` 
  ```
    @OneToMany(mappedBy = "fromInstaMember", cascade = {CascadeType.ALL})
    @OrderBy("id desc") // 내림차순 정렬
    @LazyCollection(LazyCollectionOption.EXTRA) // .size() 또는 .contains()로 접근할 때, 컬렉션 전체를 초기화하지 않고 그 값만 가져온다.
    @Builder.Default // @Builder 가 있으면 ` = new ArrayList<>();` 가 작동하지 않는다. 그래서 이걸 붙여야 한다.
    private List<LikeablePerson> fromLikeablePeople = new ArrayList<>(); // 호감을 표시하는 사람 리스트
      
        
    @OneToMany(mappedBy = "toInstaMember", cascade = {CascadeType.ALL})
    @OrderBy("id desc") // 내림차순 정렬
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Builder.Default // @Builder 가 있으면 ` = new ArrayList<>();` 가 작동하지 않는다. 그래서 이걸 붙여야 한다.
    private List<LikeablePerson> toLikeablePeople = new ArrayList<>(); 호감을 받는 사람 리스트
    
    public void addFromLikeablePerson(LikeablePerson likeablePerson) {
        fromLikeablePeople.add(0, likeablePerson); // 호감을 표시하는 사람 추가
    }
    
    public void addToLikeablePerson(LikeablePerson likeablePerson) {
        toLikeablePeople.add(0, likeablePerson); // 호감을 받는 사람 추가
    }
  ```
  1. 특정 엔티티를 영속 상태로 만들 경우, 연관된 엔티티도 함께 영속 상태로 만들고 싶을 경우 영속성 전이를 사용하는데요.
  2. JPA에서는 영속성 전이를 Cascade옵션을 통해서 설정하고 관리할 수 있습니다.
  3. CascadeType.ALL = CascadeType.REMOVE + CascadeType.PERSIST
  4. CascadeType.PERSIST: 엔티티를 영속화할 때, 연관된 엔티티도 함께 유지
  5. CascadeType.REMOVE: 엔티티를 제거할 때, 연관된 엔티티도 모두 제거
  
  - ```LikeablePersonController```
  ```
  @PreAuthorize("isAuthenticated()")
  @GetMapping("/list")
  public String showList(Model model) {
  InstaMember instaMember = rq.getMember().getInstaMember();
    
    // 인스타인증을 했는지 체크
    if (instaMember != null) {
     List<LikeablePerson> likeablePeople = instaMember.getFromLikeablePeople(); // 해당 인스타회원이 좋아하는 사람들 목록
     model.addAttribute("likeablePeople", likeablePeople);
    }
  ```
  - ```LikeablePersonService```
  ```
  @Transactional
  public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
      if (member.hasConnectedInstaMember() == false) {
          return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
      }
      if (member.getInstaMember().getUsername().equals(username)) {
          return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
      }

      InstaMember fromInstaMember = member.getInstaMember();
      InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();

      LikeablePerson likeablePerson = LikeablePerson
              .builder()
              .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
              .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
              .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
              .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
              .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
              .build();

      likeablePersonRepository.save(likeablePerson); // 저장

      // 너가 좋아하는 호감표시 생겼어.
      fromInstaMember.addFromLikeablePerson(likeablePerson);

      // 너를 좋아하는 호감표시 생겼어.
      toInstaMember.addToLikeablePerson(likeablePerson);

      return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);
    }
  ```
  **6. 양방향의 부작용을 해결하기 위한 작업, @ToString**
  - ```LikeablePerson```
  ```
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EntityListeners(AuditingEntityListener.class)
    @ToString
    @Entity
    @Getter
    
    public class LikeablePerson {
    
        @Id
        @GeneratedValue(strategy = IDENTITY)
        private Long id;
        
        @CreatedDate
        private LocalDateTime createDate;
        
        @LastModifiedDate
        private LocalDateTime modifyDate;
    
        @ManyToOne
        @ToString.Exclude // 양방향을 걸면, 여기에 달아주는게 보통이다. 이렇게 해야 무한재귀가 실행되지 않는다.
        private InstaMember fromInstaMember; // 호감을 표시한 사람(인스타 멤버)
        private String fromInstaMemberUsername; // 혹시 몰라서 기록
    
        @ManyToOne
        @ToString.Exclude // 양방향을 걸면, 여기에 달아주는게 보통이다. 이렇게 해야 무한재귀가 실행되지 않는다.
        private InstaMember toInstaMember; // 호감을 받은 사람(인스타 멤버)
        private String toInstaMemberUsername; // 혹시 몰라서 기록
    
        private int attractiveTypeCode; // 매력포인트(1=외모, 2=성격, 3=능력)
    
        public String getAttractiveTypeDisplayName() {
            return switch (attractiveTypeCode) {
                case 1 -> "외모";
                case 2 -> "성격";
                default -> "능력";
            };
        }
    }
  ```
 