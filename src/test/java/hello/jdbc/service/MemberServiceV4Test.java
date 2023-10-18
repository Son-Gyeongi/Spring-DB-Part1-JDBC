package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
import hello.jdbc.repository.MemberRepositoryV4_2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 스프링과 문제 해결 (예외 처리, 반복) - 런타임 예외 적용
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository 인터페이스 의존
 */
@Slf4j
@SpringBootTest // junit 연동 되어있다. spring을 띄우면 필요한 spring 빈을 다 등록하고 스프링 빈의 의존관계를 주입받을 수 있다.
class MemberServiceV4Test {

    // 상수 몇가지 정리
    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    @Autowired // 의존관계 주입 받아서 사용
    private MemberRepository memberRepository;
    @Autowired // 의존관계 주입 받아서 사용
    private MemberServiceV4 memberService;

    /**
     * 스프링 빈에 등록해줘야 한다.
     * @TestConfiguration - 테스트 안에서 내부 설정 클래스를 만들어서 사용하면서 이 에노테이션을 붙이면,
     * 스프링 부트가 자동으로 만들어주는 빈들에 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
     */
    @TestConfiguration
    static class TestConfig {
        /**
         * 데이터소스와 트랜잭션 매니저를 스프링 빈으로 등록하는 코드가 생략되었다. 따라서 스프링 부트가
         * application.properties 에 지정된 속성을 참고해서 데이터소스와 트랜잭션 매니저를 자동으로 생성해
         * 준다.
         */

        private final DataSource dataSource; // 스프링이 자동으로 만든 DataSource
        // 커넥션 정보는 application.properties에 있다.

        // 코드에서 보는 것 처럼 생성자를 통해서 스프링 부트가 만들어준 데이터소스 빈을 주입 받을 수도 있다.
        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        // 빈 등록 - 인터페이스에 대한 구현체 등록
        @Bean
        MemberRepository memberRepository() {
//            return new MemberRepositoryV4_1(dataSource);
            /**
             * 스프링과 문제 해결 (예외 처리, 반복) - 스프링 예외 추상화 적용
             * SQLExceptionTranslator 추가
             */
            return new MemberRepositoryV4_2(dataSource);
        }

        @Bean
        MemberServiceV4 memberServiceV4() {
            return new MemberServiceV4(memberRepository());
        }
    }

    // 리소스 정리 - db에 저장된 값 삭제(초기화)
    @AfterEach // 각각이 테스트가 끝난 후에 호출
    void after() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }

    // 프록시 적용 되었는지 확인
    @Test
    void AopCheck() {
        // memberService, memberRepository 클래스 정보 확인
        log.info("memberService class = {}", memberService.getClass());
        log.info("memberRepository class = {}", memberRepository.getClass());
        // 테스트를 위와 같이 눈으로만 확인할 수 없다.
        assertThat(AopUtils.isAopProxy(memberService)).isTrue(); // AopProxy인지 물어본다.
        assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
    }

    // 정상 이체 로직 작성
    @Test
    @DisplayName("정상 이체") // 테스트 실행 시 나오는 이름
    void accountTransfer() {
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
    void accountTransferEx() {
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