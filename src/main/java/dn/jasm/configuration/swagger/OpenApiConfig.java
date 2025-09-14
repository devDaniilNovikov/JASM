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

    @Value("${open-api.contact.name}")
    private String contactName;

    @Value("${open-api.contact.email}")
    private String email;

    @Value("${open-api.info.description}")
    private String description;

    @Value("${open-api.info.title}")
    private String title;

    @Value("${open-api.info.version}")
    private String apiVersion;

    @Value("${open-api.server.description}")
    private String serverDescription;


    @Bean
    public OpenAPI openAPIClient() {
        Server server = new Server();
        server.setUrl("http://localhost:" + serverPort);
        server.setDescription(serverDescription);
        Contact contact = new Contact();
        contact.setName(contactName);
        contact.setEmail(email);
        Info info = new Info()
                .title(title)
                .version(apiVersion)
                .description(description)
                .contact(contact);
        return new OpenAPI().info(info)
                .servers(List.of(server));
    }




    }

