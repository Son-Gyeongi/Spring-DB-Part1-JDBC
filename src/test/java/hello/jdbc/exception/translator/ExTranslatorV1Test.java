package hello.jdbc.exception.translator;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import hello.jdbc.repository.ex.MyDuplicateKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 * 스프링과 문제 해결 - 데이터 접근 예외 직접 만들기
 * 예외를 전환하는 V1 테스트
 */
public class ExTranslatorV1Test {

    // 3. 테스트
    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new Repository(dataSource);
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId"); // 같은 ID 저장 시도
    }

    // 2. MyDuplicateKeyException를 처리하는 서비스 코드
    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        // 회원가입 로직
        public void create(String memberId) {
            try {
                // 회원 만들기
                repository.save(new Member(memberId, 0));
                log.info("saveId = {}", memberId);
            } catch (MyDuplicateKeyException e) { // memberId가 중복일 때
                log.info("키 중복, 복구 시도");
                // 새로운Id 생성
                String retryId = generateNewId(memberId);
                log.info("retryId = {}", retryId);
                repository.save(new Member(retryId, 0)); // 다시 저장
            } catch (MyDbException e) {
                log.info("데이터 접근 계층 예외 발생", e);
                throw e;
            }
        }

        // 키 생성
        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }

    // 1. repository 만들기
    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;

        public Member save(Member member) {
            String sql = "insert into member(member_id, money) values(?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection(); // DataSource에서 커넥션 가져온다.
                pstmt = con.prepareStatement(sql);// 커넥션에서 prepareStatement 해서 sql 넘긴다.
                pstmt.setString(1, member.getMemberId());
                pstmt.setInt(2, member.getMoney());
                pstmt.executeUpdate();
                return member;
            } catch (SQLException e) {
                /**
                 * 이제 더이상 SQLException을 밖에 던지지 않을거다.
                 */
                // h2 db 인 경우
                if (e.getErrorCode() == 23505) { // 키 중복인 경우
                    throw new MyDuplicateKeyException(e); // 이렇게 하면 서비스에서 잡아서 복구할 수 있다.
                }
                throw new MyDbException(e); // SQLException 예외 제외하고 나머지 예외의 경우
            } finally {
                // 리소스 close
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }
}
