package com.fibank.cashdesk;

import com.fibank.cashdesk.config.BanknotesDenominationsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BanknotesDenominationsConfig.class)
public class CashDeskApplication {

	public static void main(String[] args) {
		SpringApplication.run(CashDeskApplication.class, args);
	}

}
