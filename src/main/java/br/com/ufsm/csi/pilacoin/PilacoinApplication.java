package br.com.ufsm.csi.pilacoin;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class PilacoinApplication {

	public static void main(String[] args) {
		SpringApplication.run(PilacoinApplication.class, args);
	}

}
