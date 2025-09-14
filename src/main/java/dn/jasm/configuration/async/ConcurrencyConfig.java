package dn.jasm.configuration.async;


import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Configuration
public class ConcurrencyConfig {


    @Bean
    public ExecutorService executorService() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean("taskExecutor")
    public ThreadFactory taskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.setThreadNamePrefix("JasmAsyncJobs-");
        taskExecutor.setVirtualThreads(true);
        taskExecutor.initialize();
        return taskExecutor;
    }





}
