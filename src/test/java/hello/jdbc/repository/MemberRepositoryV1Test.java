package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// DataSource 적용
@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repository;

    @BeforeEach
        // 각 테스트가 실행되기 전에 한번 호출
    void beforeEach() {
        // 기본 DriverMavager를 통한 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        // -> 성능이 느려진다. 그래서 커넥션 풀을 사용하자. Hikari로 해보자

        // 커넥션 풀링 - HikariDataSource 생성
        // 커넥션을 쓰고 close할 때 반환한다. 커넥션 풀 사용시 conn0 커넥션이 재사용 된 것을 확인할 수 있다.
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // memberRepositoryV1 객체 생성
        repository = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        // 회원 저장 - save()
        Member member = new Member("memberV100", 10000);
        repository.save(member);

        // 회원 조회 - findById()
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember); // 검증을 눈으로 하고 싶을 때
        // // member와 findMember는 다른 인스턴스이다.
        log.info("member == findMember = {}", member == findMember); // false
        log.info("member.equals(findMember) = {}", member.equals(findMember)); // true
        assertThat(findMember).isEqualTo(member);

        // 회원 변경 - update() / money 10000 -> 20000
        repository.update(member.getMemberId(), 20000);
        // 검증
        Member updateMember = repository.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        // 회원 삭제 - delete()
        repository.delete(member.getMemberId());
        // 검증
//        Member deletedMember = repository.findById(member.getMemberId()); // 삭제해서 NoSuchElementException 예외가 뜬다.
        assertThatThrownBy(() -> repository.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class); // NoSuchElementException 예외가 터져야 정상

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}