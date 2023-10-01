package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    // DataSource 예제1 - DriverManager
    @Test
    void driverManager() throws SQLException {
        // 커넥션 획득 - 커넥션 2개 생성
        // 실제 DB에서 서로 다른 커넥션 가져온다.
        Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("1. connection = {}, class = {}", con1, con1.getClass());
        log.info("2. connection = {}, class = {}", con2, con2.getClass());
    }

    // 이번에는 스프링이 제공하는 '데이터 소스가 적용이 된' DriverManager를 사용하는
    // DriverManagerDataSource 사용해보자.
    @Test
    void dataSourceDriverManager() {
        // DriverManager에서 바로 쓴다.
        // DriverManagerDataSource(스프링에서 제공) - 항상 새로운 커넥션을 획득한다, DataSource를 구현했다.
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
//        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection con1 = dataSource.getConnection();
        Connection con2 = dataSource.getConnection();
        log.info("connection={}, class={}", con1, con1.getClass());
        log.info("connection={}, class={}", con2, con2.getClass());
    }
}
