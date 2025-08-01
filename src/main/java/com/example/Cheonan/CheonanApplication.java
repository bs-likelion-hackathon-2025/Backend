package com.example.Cheonan;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CheonanApplication {
	static {
		// .env 로딩
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()   // 없을 경우 무시
				.load();

		// 시스템 환경변수로 등록
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);
	}

	public static void main(String[] args) {
		SpringApplication.run(CheonanApplication.class, args);
	}

}

