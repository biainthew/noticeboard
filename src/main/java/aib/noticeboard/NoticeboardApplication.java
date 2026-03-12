package aib.noticeboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NoticeboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeboardApplication.class, args);
    }

}
