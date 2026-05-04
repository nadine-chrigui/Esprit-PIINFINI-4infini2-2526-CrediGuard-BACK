package tn.esprit.pi_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PiBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PiBackApplication.class, args);
    }

}
