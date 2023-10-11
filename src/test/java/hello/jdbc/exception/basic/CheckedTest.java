package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * 자바 예외 이해 - 체크 예외 기본 이해
 * Exception 과 그 하위 예외는 모두 컴파일러가 체크하는 체크 예외이다. 단 RuntimeException 은 예외로 한다.
 * 체크 예외는 잡아서 처리하거나, 또는 밖으로 던지도록 선언해야한다. 그렇지 않으면 컴파일 오류가 발생한다.
 */
@Slf4j
public class CheckedTest {

    // 정상 동작
    @Test
    void checked_catch() {
        Service service = new Service();
        service.callCatch();
        // service.callCatch() 에서 예외를 처리했기 때문에 테스트 메서드까지 예외가 올라오지 않는다.
    }

    // 예외를 잡지 않고 던질거다.
    @Test
//    void checked_throw() throws MyCheckedException { // 예외를 밖으로 던지면 테스트 실패하게 된다.
    void checked_throw() {
        Service service = new Service();
//        service.callThrow();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
        // service.callThrow()로직을 호출하면 MyCheckedException 예외가 터져야 정상
    }

    /**
     * Exception을 상속받은 예외는 체크 예외가 된다.
     * 체크 예외 만들어보자.
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    // 서비스, 레파지토리 가상의 코드를 만들어보자.
    /**
     * Checked 예외는
     * 예외를 잡아서 처리하거나, 던지거나 둘중 하나를 필수로 선택해야 한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message = {}", e.getMessage(), e); // e는 StackTrace 출력
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야 한다.
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException {
            // call() 호출하면 MyCheckedException 예외가 터진다.
            throw new MyCheckedException("ex");
        }
    }
}
