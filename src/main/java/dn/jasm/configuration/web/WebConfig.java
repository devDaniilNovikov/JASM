package dn.jasm.configuration.web;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dn.jasm.controller.CardController;
import jakarta.servlet.annotation.WebListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.web.exchanges.HttpExchangesProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.support.ServletRequestHandledEvent;
import software.amazon.awssdk.core.interceptor.InterceptorContext;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private static final String URL_FOR_HANDLE = "/api/v1/charge/create";

    @Bean
    public RestClient restClient(){
        return RestClient.builder()
                .build();
    }

    @Bean("objectsMapper")
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.disable(DeserializationFeature.EAGER_DESERIALIZER_FETCH,
                DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY);
        return objectMapper;
    }

    @EventListener
    public void handleServletEvent(ServletRequestHandledEvent event){
        if (event.getRequestUrl()
                .equals(URL_FOR_HANDLE) &&
                event.getStatusCode() == 200){
            log.info("[Handle servlet event: {}, time: {}, ip: {}]",
                    event.getMethod(),
                    event.getProcessingTimeMillis(),
                    event.getClientAddress());
        }
    }

}
