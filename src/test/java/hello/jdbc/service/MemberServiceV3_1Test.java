package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 문제 해결 - 트랜잭션 매니저1
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
class MemberServiceV3_1Test {

    // 상수 몇가지 정리
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 memberRepository;
    private MemberServiceV3_1 memberService;

    // 값 세팅
    @BeforeEach // 각각이 테스트가 실행되기 전에 호출
    void before() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        // memberRepository가 dataSource 필요해서 가져왔다.
        memberRepository = new MemberRepositoryV3(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        // JDBC 기술을 사용하므로, JDBC용 트랜잭션 매니저( DataSourceTransactionManager )를 선택해서 서비스에 주입한다.
        // 트랜잭션 매니저는 데이터소스를 통해 커넥션을 생성하므로 DataSource 가 필요하다.
        memberService = new MemberServiceV3_1(transactionManager, memberRepository);
    }

    // 리소스 정리 - db에 저장된 값 삭제(초기화)
    @AfterEach // 각각이 테스트가 끝난 후에 호출
    void after() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    // 정상 이체 로직 작성
    @Test
    @DisplayName("정상 이체") // 테스트 실행 시 나오는 이름
    void accountTransfer() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(memberA); // DB에 저장
        memberRepository.save(memberB); // DB에 저장

        // when / memberA 돈을 memberB에게 2000원 이체한다.
        log.info("START TX"); // accountTransfer() 여기서는 같은 커넥션을 쓴다. 같은 커넥션이 내부에서 재사용된다.
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX"); // accountTransfer() 여기서는 같은 커넥션을 쓴다.

        // then / 검증
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberB = memberRepository.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    // 이체중 예외 발생
    // memberA 가 이체하는 중 예외가 발생해서 rollback이 된다.
    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        // given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(memberA);
        memberRepository.save(memberEx);

        // when
        // 검증에서 예외가 터지는 걸 검증해야 한다.
        // memberService.accountTransfer()를 수행한 결과가 예외가 터져서 IllegalStateException가 되면 성공
        // 트랜잭션이 없으면 기본적으로 autocommit으로 돈다.
        assertThatThrownBy(() -> memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        // then
        Member findMemberA = memberRepository.findById(memberA.getMemberId());
        Member findMemberEx = memberRepository.findById(memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}