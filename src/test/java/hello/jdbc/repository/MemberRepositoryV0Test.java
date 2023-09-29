package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 테스트 코드를 사용해서 JDBC로 회원을 데이터베이스에 등록
 */
@Slf4j
class MemberRepositoryV0Test {

    // 우리가 테스트할 거 MemberRepositoryV0
    MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        // JDBC 개발 - 등록
        // 회원 저장 - save()
        Member member = new Member("memberV4", 10000);
        repository.save(member);

        // JDBC 개발 - 조회
        // 회원 조회 - findById()
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember); // 검증을 눈으로 하고 싶을 때
        // // member와 findMember는 다른 인스턴스이다.
        log.info("member == findMember = {}", member == findMember); // false
        log.info("member.equals(findMember) = {}", member.equals(findMember)); // true
        assertThat(findMember).isEqualTo(member);
    }
}