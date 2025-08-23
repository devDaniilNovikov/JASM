package dn.jasm.configuration.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI openAPIClient() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription("Local server");
        Contact contact = new Contact();

        Info info = new Info()
                .title("Маркетплейс")
                .version("1.0.0")
                .description("Документация для API маркетплейса")
                .contact(new Contact()
                        .name("Daniil")
                        .email("novikovdanila7@gmail.com"));
        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }




    }

