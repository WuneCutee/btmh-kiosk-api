package vn.baotinmanhhai.kioskapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KioskApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KioskApiApplication.class, args);
    }
}
