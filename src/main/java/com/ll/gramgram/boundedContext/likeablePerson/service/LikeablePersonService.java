package com.ll.gramgram.boundedContext.likeablePerson.service;

import com.ll.gramgram.base.appConfig.AppConfig;
import com.ll.gramgram.base.rsData.RsData;
import com.ll.gramgram.boundedContext.instaMember.entity.InstaMember;
import com.ll.gramgram.boundedContext.instaMember.service.InstaMemberService;
import com.ll.gramgram.boundedContext.likeablePerson.entity.LikeablePerson;
import com.ll.gramgram.boundedContext.likeablePerson.repository.LikeablePersonRepository;
import com.ll.gramgram.boundedContext.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeablePersonService {
    private final LikeablePersonRepository likeablePersonRepository;
    private final InstaMemberService instaMemberService;
    private final AppConfig appConfig;

    @Transactional
    public RsData<LikeablePerson> like(Member member, String username, int attractiveTypeCode) {
        if ( member.hasConnectedInstaMember() == false ) {
            return RsData.of("F-2", "먼저 본인의 인스타그램 아이디를 입력해야 합니다.");
        }

        if (member.getInstaMember().getUsername().equals(username)) {
            return RsData.of("F-1", "본인을 호감상대로 등록할 수 없습니다.");
        }


        InstaMember fromInstaMember = member.getInstaMember();
        InstaMember toInstaMember = instaMemberService.findByUsernameOrCreate(username).getData();


        List<LikeablePerson> existingLikes = likeablePersonRepository.findByFromInstaMemberIdAndToInstaMemberId(fromInstaMember.getId(), toInstaMember.getId());



        if(!existingLikes.isEmpty()){
            LikeablePerson existingLike = existingLikes.get(0);
            if (existingLike.getAttractiveTypeCode() != attractiveTypeCode) {
                String oldAttractiveType = existingLike.getAttractiveTypeDisplayName();
                existingLike.setAttractiveTypeCode(attractiveTypeCode);
                likeablePersonRepository.save(existingLike);
                String newAttractiveType = existingLike.getAttractiveTypeDisplayName();
                return RsData.of("S-2", "%s에 대한 호감사유를 %s에서 %s으로 변경합니다.".formatted(username,oldAttractiveType,newAttractiveType), existingLike);
            } else {
                return RsData.of("F-1","중복으로 호감표시를 할 수 없습니다.");
            }
        }

        List<LikeablePerson> allFromInstaMember = likeablePersonRepository.findByFromInstaMemberId(fromInstaMember.getId());

        if (allFromInstaMember.size() >= AppConfig.getLikeablePersonFromMax()){
            return RsData.of("F-1","11명 이상의 호감상대를 등록 할 수 없습니다.");
        }

        LikeablePerson likeablePerson = LikeablePerson
                .builder()
                .fromInstaMember(fromInstaMember) // 호감을 표시하는 사람의 인스타 멤버
                .fromInstaMemberUsername(member.getInstaMember().getUsername()) // 중요하지 않음
                .toInstaMember(toInstaMember) // 호감을 받는 사람의 인스타 멤버
                .toInstaMemberUsername(toInstaMember.getUsername()) // 중요하지 않음
                .attractiveTypeCode(attractiveTypeCode) // 1=외모, 2=능력, 3=성격
                .build();

        likeablePersonRepository.save(likeablePerson); // 저장
        fromInstaMember.addFromLikeablePerson(likeablePerson);
        toInstaMember.addToLikeablePerson(likeablePerson);

        return RsData.of("S-1", "입력하신 인스타유저(%s)를 호감상대로 등록되었습니다.".formatted(username), likeablePerson);






    }


    public List<LikeablePerson> findByFromInstaMemberId(Long fromInstaMemberId) {
        return likeablePersonRepository.findByFromInstaMemberId(fromInstaMemberId);
    }
    public Optional<LikeablePerson> findById(Long id) {
        return likeablePersonRepository.findById(id);
    }

    @Transactional
    public RsData delete(LikeablePerson likeablePerson) {

        likeablePersonRepository.delete(likeablePerson);
        String toInstaMemberUsername = likeablePerson.getToInstaMember().getUsername();
        return RsData.of("S-1", "%s님에 대한 호감을 취소하였습니다.".formatted(toInstaMemberUsername));
    }
    public RsData canActorDelete(Member actor, LikeablePerson likeablePerson) {
        if (likeablePerson == null) return RsData.of("F-1", "이미 삭제되었습니다.");

        // 수행자의 인스타계정 번호
        long actorInstaMemberId = actor.getInstaMember().getId();
        // 삭제 대상의 작성자(호감표시한 사람)의 인스타계정 번호
        long fromInstaMemberId = likeablePerson.getFromInstaMember().getId();

        if (actorInstaMemberId != fromInstaMemberId) {
            return RsData.of("F-1", "권한이 없습니다.");
        }
        return RsData.of("S-1", "삭제가능합니다.");
    }


}
