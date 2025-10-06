package dn.jasm.configuration.web;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Slf4j
@Configuration
public class WebConfig {

    private static final String URL_FOR_HANDLE = "/api/v1/charge/create";

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .build();
    }

    @EventListener
    @Async
    public void handleServletEvent(ServletRequestHandledEvent event){
        if (event.getRequestUrl().equals(URL_FOR_HANDLE) && event.getStatusCode() == 200){
            log.info("Handle servlet event: {}, time: {}, ip: {}",
                    event.getMethod(),
                    event.getProcessingTimeMillis(),
                    event.getClientAddress());
        }
    }

}
