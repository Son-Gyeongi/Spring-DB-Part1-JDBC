package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 문제 해결 - 트랜잭션 AOP 적용
 * 트랜잭션 - @Transactional AOP 적용
 */
@Slf4j
public class MemberServiceV3_3 {

    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 계좌이체 로직 작성
    // from 보내는 곳 to 받는 곳 money 얼마 보낼건가
    // fromId 회원을 조회해서 toId 회원에게 money만큼의 돈을 계좌이체하는 로직이다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 비즈니스 로직
        bizLogic(fromId, toId, money);
    }

    // 비즈니스 로직
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        // 회원 꺼내기
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        // 트랜잭션이 없으면 기본적으로 autocommit으로 돈다. update 수행할 때마다 자동 커밋이 된다.
        // from의 돈을 깍는다.
        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember); // 예외 발생 상황 확인
        // to의 돈을 올린다.
        memberRepository.update(toId, toMember.getMoney() + money);
        // 트랜잭션 - 적용2 / 커밋, 롤백 판단해서 처리해야 한다.(트랜잭션 종료)
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) { // 예외 발생 상황 확인
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
