package com.example.fitbit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableScheduling
class FitbitApplicationTests {

	@Value("${server.servlet.session.timeout}")
	private Duration sessionTimeout;

	@Test
	void contextLoads() {
	}

	/**
	 * 実験・授業中の再ログイン発生を抑えるため、ブラウザセッションが約1週間保持されることを確認する。
	 */
	@Test
	void keepsBrowserSessionForOneWeek() {
		assertThat(sessionTimeout).isEqualTo(Duration.ofDays(7));
	}

}
