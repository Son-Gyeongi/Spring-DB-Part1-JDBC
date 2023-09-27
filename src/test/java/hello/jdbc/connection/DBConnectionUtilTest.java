package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.*;

// JDBC 이해 - 데이터베이스 연결
@Slf4j
// 해당 테스트는 public 안 붙여도 된다.
class DBConnectionUtilTest {

    @Test
    void connection() {
        Connection connection = DBConnectionUtil.getConnection();
        // 데이터베이스랑 연결 잘 됐는지 검증하기
        assertThat(connection).isNotNull(); // null이 아니면 성공
    }
}
