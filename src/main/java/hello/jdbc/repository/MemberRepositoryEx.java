package hello.jdbc.repository;

import hello.jdbc.domain.Member;

import java.sql.SQLException;

// 스프링과 문제 해결 (예외 처리, 반복) - 체크 예외와 인터페이스
public interface MemberRepositoryEx {
    Member save(Member member) throws SQLException;

    Member findById(String memberId) throws SQLException;

    void update(String memberId, int money) throws SQLException;

    void delete(String memberId) throws SQLException;
}
