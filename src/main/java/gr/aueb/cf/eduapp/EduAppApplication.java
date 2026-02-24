package gr.aueb.cf.eduapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EduAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduAppApplication.class, args);
	}

}
