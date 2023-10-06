package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 문제 해결 - 트랜잭션 매니저1
 * 트랜잭션 - 트랜잭션 매니저
 * 문제 - DataSource를 직접 사용하는 것 (JDBC 관련된 것을 쓴다.)
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    // 계좌이체 로직 작성
    // from 보내는 곳 to 받는 곳 money 얼마 보낼건가
    // fromId 회원을 조회해서 toId 회원에게 money만큼의 돈을 계좌이체하는 로직이다.
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            // 비즈니스 로직 (트랜잭션 처리하는 코드와 분리하기 위해서 메서드 만들었다.)
            bizLogic(fromId, toId, money);
            // 정상 로직 수행 된 후 커넥션에서 커밋을 해줘야 한다.
            transactionManager.commit(status); // 성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); // 실패시 커넥션에서 롤백 해준다.
            throw new IllegalStateException(); // 예외 던지기
        }
//        finally {
        // 더이상 release()를 내가 할 필요없다. 트랜잭션은 커밋디거나 롤백될 때 릴리즈 알아서 하면 된다.
        // transactionManager가 해준다.
//            release(con);
//        }
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
