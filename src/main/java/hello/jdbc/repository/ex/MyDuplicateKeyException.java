package hello.jdbc.repository.ex;

/**
 * 스프링과 문제 해결 - 데이터 접근 예외 직접 만들기
 * 키 중복 예외를 직접 만든다. - 데이터 중복인 경우에만 예외를 던진다.
 * MyDbException 상속 받는다. - 데이터베이스에서 발생하는 오류다라는 카테고리로 묶을 수 있다.
 */
public class MyDuplicateKeyException extends MyDbException {

    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
