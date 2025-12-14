package dn.jasm;


import dn.jasm.exception.ApplicationStartRunningException;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.context.annotation.Configuration;
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
@EnableBatchProcessing
@EnableTransactionManagement
public class JasmApplication {

    public static void main(String[] args) {
        SpringApplication.run(JasmApplication.class, args);
    }


    @Configuration
    public static class ApplicationGlobalConfig extends AbstractFailureAnalyzer<ApplicationStartRunningException> {
        @Override
        protected FailureAnalysis analyze(Throwable rootFailure,
                                          ApplicationStartRunningException cause) {
            return new FailureAnalysis("Application was down on start-time cause",
                    "Check your start-configuration", cause);
        }
    }
}