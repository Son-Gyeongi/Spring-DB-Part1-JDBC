package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 자바 예외 이해 - 언체크 예외 활용
@Slf4j
public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }
    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() {
            throw new RuntimeConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                throw new RuntimeSQLException(e); // 예외 변환, RuntimeSQLException 예외로 바꿔서 던질거다.
            }
        }

        // SQL 실행한다는 가정
        public void runSQL() throws SQLException {
            throw new SQLException("ex");
        }
    }

    // 예외 만들기
    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(String message) {
            super(message);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }

        // 왜 발생했는지 이전 예외를 같이 넣을 수 있다.
        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
