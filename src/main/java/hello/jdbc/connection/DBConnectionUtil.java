package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

/**
 * JDBC 이해 - 데이터베이스 연결
 * 애플리케이션이랑 데이터베이스 연결해보자
 * 데이터베이스랑 연결하는 코드
 */
@Slf4j
public class DBConnectionUtil {

    // jdbc 표준 인터페이스가 제공하는 Connection (java.sql.Connection)
    public static Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection = {}, class = {}", connection, connection.getClass()); // 객체정보, 클래스 타입 정보 출력
            return connection;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
