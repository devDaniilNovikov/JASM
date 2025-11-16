package dn.jasm;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableKafka
@EnableAsync
@EnableScheduling
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableRetry
@EnableRabbit
@EnableBatchProcessing
@EnableTransactionManagement
public class JasmApplication {

    public static void main(String[] args) {
        SpringApplication.run(JasmApplication.class, args);
    }


}
