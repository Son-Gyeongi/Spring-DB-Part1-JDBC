package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

/**
 * JDBC 개발 - 등록
 * JDBC - DriverManager 사용해서 저장해보자
 */
@Slf4j
public class MemberRepositoryV0 {

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
            return member;
        } catch (SQLException e) {
            log.info("db error", e); // 확인용 로그
            throw e; // 예외를 밖으로 던진다.
        } finally {
            // 중요. 시간 역순으로 클로즈 해준다.
            // 외부 리소르를 쓰는 거다. 실제 TCP/IP connection에 걸려서 쓰는거다.
            close(con, pstmt, null);
        }
    }

    // Connection 닫아주기 - 사용한 자원들을 다 닫아줘야 한다.
    private void close(Connection con, Statement stmt, ResultSet rs) {
        // Statement는 sql을 그대로 넣는거다. PreparedStatement는 파라미터를 바인딩 할 수 있다.(PreparedStatement는 Statement를 상속받았다.)
        // ResultSet는 결과를 Select쿼리로 조회할 때 반환되는 거다.

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e); // error정보를 남겨준다.
            }
        }

        // 코드의 안전성을 위해서 - close할 때 예외가 발생할 경우
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e); // error정보를 남겨준다.
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e); // error정보를 남겨준다.
            }
        }
    }

    // Connection 가져오기
    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
