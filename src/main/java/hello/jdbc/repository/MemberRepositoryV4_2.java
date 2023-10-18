package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 스프링과 문제 해결 (예외 처리, 반복) - 스프링 예외 추상화 적용
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    // 먼저 DataSource 사용하려면 의존관계 주입을 받아야 한다.
    // DataSource(커넥션을 획득하는 방법을 추상화하는 인터페이스) - 애플리케이션 코드를 변경할 필요없다.
    private final DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        // 에러코드를 기반으로 스프링이 잡은 예외계층으로 변환해서 넣어주겠다.
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
        // dataSource 넣어주는 이유 - 어떤 DB를 쓰는지 알아서 찾아서 쓴다.
    }

    // 회원 저장
    @Override // 인터페이스 사용시 @Override 사용하는 게 좋다. 컴파일러가 구현된 게 안 맞으면 오류를 내준다.
    public Member save(Member member) {
        // sql 작성
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection con = null; // connection이 있어야 연결할 수 있다.
        PreparedStatement pstmt = null; // PreparedStatement를 가지고 데이터베이스에 쿼리를 날린다.

        try {
            // connection 가져오기
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId()); // values 뒤에 ?인 파라미터 바인딩하기
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate(); // 실행 - 쿼리가 실제 데이터베이스에 실행이 된다, Statement 를 통해 준비된 SQL을 커넥션을 통해 실제 데이터베이스에 전달
            // executeUpdate()는 데이터 변경할 때 사용
            return member;
        } catch (SQLException e) {
            // 스프링이 제공하는 방대한 예외 계층으로 다 바뀐다.
            throw exTranslator.translate("save", sql, e); // "save" 작업에서 발생했다.
        } finally {
            // 중요. 시간 역순으로 클로즈 해준다.
            // 외부 리소르를 쓰는 거다. 실제 TCP/IP connection에 걸려서 쓰는거다.
            close(con, pstmt, null);
        }
    }

    // 회원 조회
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?"; // 회원 한명 조회

        // 밖에 Connection 선언하는 이유 try/catch 문에서 finally에서 con을 호출해야해서
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);// con을 통해서 prepareStatement()를 얻어야 한다.
            pstmt.setString(1, memberId);

            // 실행
            // rs - select 쿼리의 결과를 담고 있는 통이다.
            rs = pstmt.executeQuery();// select 시 사용
            // 데이터 꺼내기
            if (rs.next()) { // rs.next() - 실제 데이터가 있는 곳에서 시작한다, 한번은 호출해야 한다. 데이터가 있으면 true
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else { // 데이터가 없을 때
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }
        } catch (SQLException e) {
            // 스프링이 제공하는 방대한 예외 계층으로 다 바뀐다.
            throw exTranslator.translate("findById", sql, e); // "findById" 작업에서 발생했다.
        } finally { // 리소스 close 해주기
            close(con, pstmt, rs);
        }
    }

    // 회원 변경
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리를 실행하고 영향받은 row수를 반환
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    // 회원 삭제
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    // Connection 닫아주기 - 사용한 자원들을 다 닫아줘야 한다.
    private void close(Connection con, Statement stmt, ResultSet rs) {
        // Statement는 sql을 그대로 넣는거다. PreparedStatement는 파라미터를 바인딩 할 수 있다.(PreparedStatement는 Statement를 상속받았다.)
        // ResultSet는 결과를 Select쿼리로 조회할 때 반환되는 거다.

        /**
         * JdbcUtils 편의 메서드
         * 스프링은 JDBC를 편리하게 다룰 수 있는 JdbcUtils 라는 편의 메서드를 제공한다.
         * JdbcUtils 을 사용하면 커넥션을 좀 더 편리하게 닫을 수 있다
         */
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
//        JdbcUtils.closeConnection(con);
    }

    // dataSource에서 Connection 가져오기
    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection = {}, class = {}", con, con.getClass());
        return con;
    }
}
