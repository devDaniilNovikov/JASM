package dn.jasm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.data.map.repository.config.EnableMapRepositories;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@EnableKafka
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class JasmApplication {

    public static void main(String[] args) {
        SpringApplication.run(JasmApplication.class, args);
    }


}
