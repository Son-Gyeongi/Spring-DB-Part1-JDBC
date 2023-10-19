package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

/**
 * 스프링과 문제 해결 (예외 처리, 반복) - JDBC 반복 문제 해결/JdbcTemplate (템플릿 콜백 패턴)
 * JdbcTemplate 사용
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository {

    private final JdbcTemplate template;

    public MemberRepositoryV5(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    // 회원 저장
    @Override // 인터페이스 사용시 @Override 사용하는 게 좋다. 컴파일러가 구현된 게 안 맞으면 오류를 내준다.
    public Member save(Member member) {
        // sql 작성
        String sql = "insert into member(member_id, money) values (?, ?)";
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;
    }

    // 회원 조회
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?"; // 회원 한명 조회
        return template.queryForObject(sql, memberRowMapper(), memberId);// queryForObject() : 1건 조회
        // memberRowMapper() - 쿼리 결과를 어떻게 멤버로 만들건지 매핑정보 넣어줘야 한다.
    }

    // 쿼리 결과를 어떻게 멤버로 만들건지 매핑정보 넣어줘야 한다.
    private RowMapper<Member> memberRowMapper() {
        // 람다 문법, rs(resultSet)/rowNum(몇번째로 들어오는지)
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    // 회원 변경
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";
        template.update(sql, money, memberId);
    }

    // 회원 삭제
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";
        template.update(sql, memberId);
    }

    // 커넥션 닫기 및 동기화 하기 안해도 된다.
}
