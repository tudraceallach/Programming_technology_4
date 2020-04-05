package ru.example.accounts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import ru.example.accounts.backend.config.AccountConfiguration;

/**
 * The  Accounts application.
 */
@SpringBootApplication
@EnableAutoConfiguration
@Import(AccountConfiguration.class)
public class AccountsApplication {

	/**
	 * The entry point of application.
	 *
	 * @param args the input arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AccountsApplication.class, args);
	}
}
