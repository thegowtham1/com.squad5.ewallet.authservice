package com.squad5.ewallet.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EWallet {

	public static void main(String[] args) {
		SpringApplication.run(EWallet.class, args);
		System.out.println("Started");
	}

}
