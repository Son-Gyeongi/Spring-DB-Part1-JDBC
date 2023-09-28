package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JDBC 개발 - 등록
 * 테스트 코드를 사용해서 JDBC로 회원을 데이터베이스에 등록
 */
class MemberRepositoryV0Test {

    // 우리가 테스트할 거 MemberRepositoryV0
    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // 회원 저장
        Member member = new Member("memberV0", 10000);
        repository.save(member);
    }
}