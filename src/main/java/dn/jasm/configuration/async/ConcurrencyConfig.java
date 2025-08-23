package dn.jasm.configuration.async;


import static com.ea.async.Async.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ConcurrencyConfig {


    @Bean
    public ExecutorService executorService(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean("taskExecutor")
    public Executor taskExecutor(){
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setQueueCapacity(25);
        taskExecutor.setThreadNamePrefix("JasmAsyncJobs-");
        taskExecutor.setVirtualThreads(true);
        taskExecutor.initialize();
        return taskExecutor;
    }





}
