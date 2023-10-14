package hello.jdbc.repository;

import hello.jdbc.domain.Member;

// 스프링과 문제 해결 (예외 처리, 반복) - 런타임 예외 적용
public interface MemberRepository {
    Member save(Member member);

    Member findById(String memberId);

    void update(String memberId, int money);

    void delete(String memberId);
}
