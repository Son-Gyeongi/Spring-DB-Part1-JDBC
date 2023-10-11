package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 자바 예외 이해 - 언체크 예외 기본 이해
 * // 체크 예외든 언체크 예외든 모든 예외는 잡거나 던진거나 둘 중에 하나를 한다.
 * // 그런데 그게 컴파일러가 체크를 하냐 안 하냐의 차이가 있다.
 */
@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch() {
        Service service = new Service();
        service.callCatch();
    }

    @Test
    void unchecked_throw() {
        Service service = new Service();
//        service.callThrow(); // 예외 발생
        Assertions.assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * 언체크 예외 만들기
     * RuntimeException을 상속받는 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    // 서비스, 레파지토리 가상의 코드를 만들어보자.
    /**
     * Unchecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                // 예외 처리 로직
                log.info("예외 처리, message = {}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않아도 된다. 자연스럽게 상위로 넘어간다.
         * 체크 예외와 다르게 throws 예외 선언을 하지 않아도 된다.
         * throws MyUncheckedException 생략되어 있다.
         */
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
//        public void call() throws MyUncheckedException { // throws 예외 선언 생략할 수 있다.
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
