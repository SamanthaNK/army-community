package com.armycommunity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ArmyCommunityApplication {
	public static void main(String[] args) {
		SpringApplication.run(ArmyCommunityApplication.class, args);
	}

}
