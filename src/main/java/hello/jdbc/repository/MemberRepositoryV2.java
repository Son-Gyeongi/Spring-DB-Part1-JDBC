package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 적용2
 * JDBC - ConnectionParam 커넥션을 파라미터로 넘기는 예제
 */
@Slf4j
public class MemberRepositoryV2 {

    // 먼저 DataSource 사용하려면 의존관계 주입을 받아야 한다.
    // DataSource(커넥션을 획득하는 방법을 추상화하는 인터페이스) - 애플리케이션 코드를 변경할 필요없다.
    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 회원 저장
    public Member save(Member member) throws SQLException {
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
            log.error("db error", e); // 확인용 로그
            throw e; // 예외를 밖으로 던진다.
        } finally {
            // 중요. 시간 역순으로 클로즈 해준다.
            // 외부 리소르를 쓰는 거다. 실제 TCP/IP connection에 걸려서 쓰는거다.
            close(con, pstmt, null);
        }
    }

    // 회원 조회
    public Member findById(String memberId) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally { // 리소스 close 해주기
            close(con, pstmt, rs);
        }
    }
    // 트랜잭션 - 적용2
    // 회원 조회
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?"; // 회원 한명 조회

        // 밖에 PreparedStatement 선언하는 이유 try/catch 문에서 finally에서 pstmt을 호출해야해서
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
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
            log.error("db error", e);
            throw e;
        } finally { // 리소스 close 해주기
            // connection은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con); // 여기서 커넥션 닫으면 안된다. 서비스에서 종료해야 한다.
        }
    }

    // 회원 변경
    public void update(String memberId, int money) throws SQLException {
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
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }
    // 트랜잭션 - 적용2
    // 회원 변경
    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate(); // 쿼리를 실행하고 영향받은 row수를 반환
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            // connection은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con); // 여기서 커넥션 닫으면 안된다. 서비스에서 종료해야 한다.
        }
    }

    // 회원 삭제
    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
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
        JdbcUtils.closeConnection(con);
    }

    // dataSource에서 Connection 가져오기
    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection = {}, class = {}", con, con.getClass());
        return con;
    }
}
