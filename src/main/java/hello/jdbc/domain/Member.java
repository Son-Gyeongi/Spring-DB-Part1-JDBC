package hello.jdbc.domain;

import lombok.Data;

// JDBC 개발 - 등록
// JDBC를 통해서 회원 객체를 데이터베이스에 저장하는 코드를 짜야한다.
@Data
public class Member {

    private String memberId; // 회원 id
    private int money; // 회원이 소지한 금액

    // 기본 생성자
    public Member() {
    }

    public Member(String memberId, int money) {
        this.memberId = memberId;
        this.money = money;
    }
}
