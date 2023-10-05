package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 적용2
 * 트랜잭션 - 파라미터 연동, 풀(pool)을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    // 계좌이체 로직 작성
    // from 보내는 곳 to 받는 곳 money 얼마 보낼건가
    // fromId 회원을 조회해서 toId 회원에게 money만큼의 돈을 계좌이체하는 로직이다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 데이터 소스 필요, 데이터 소스에서 커넥션 받아온다.
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false); // false(수동커밋) - 트랜잭션 시작
            // 비즈니스 로직 (트랜잭션 처리하는 코드와 분리하기 위해서 메서드 만들었다.)
            bizLogic(con, fromId, toId, money);
            // 정상 로직 수행 된 후 커넥션에서 커밋을 해줘야 한다.
            con.commit(); // 성공시 커밋
        } catch (Exception e) {
            con.rollback(); // 실패시 커넥션에서 롤백 해준다.
            throw new IllegalStateException(); // 예외 던지기
        } finally {
            release(con);
        }
    }

    // 비즈니스 로직
    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        // 회원 꺼내기
        Member fromMember = memberRepository.findById(con, fromId);
        Member toMember = memberRepository.findById(con, toId);

        // 트랜잭션이 없으면 기본적으로 autocommit으로 돈다. update 수행할 때마다 자동 커밋이 된다.
        // from의 돈을 깍는다.
        memberRepository.update(con, fromId, fromMember.getMoney() - money);
        validation(toMember); // 예외 발생 상황 확인
        // to의 돈을 올린다.
        memberRepository.update(con, toId, toMember.getMoney() + money);
        // 트랜잭션 - 적용2 / 커밋, 롤백 판단해서 처리해야 한다.(트랜잭션 종료)
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) { // 예외 발생 상황 확인
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); // 자동 커밋, 커넥션 풀 고려
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }
}
