package hello.jdbc.connection;

// JDBC 이해 - 데이터베이스 연결
// 커넥션 상수 만들기
// 객체 생성 못하게 abstract로 만든다.
public abstract class ConnectionConst {
    // 데이터베이스 정보들 - h2 데이터 베이스 접근
    public static final String URL = "jdbc:h2:tcp://localhost/~/test2";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}
