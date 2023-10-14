package hello.jdbc.repository.ex;

// 스프링과 문제 해결 (예외 처리, 반복) - 런타임 예외 적용
// SQLException을 Runtime 예외로 감싸서 던질거다.
public class MyDbException extends RuntimeException { // RuntimeException는 언체크 예외이다.
    public MyDbException() {
    }

    public MyDbException(String message) {
        super(message);
    }

    public MyDbException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDbException(Throwable cause) {
        super(cause);
    }
}
