package dn.jasm.configuration;

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
    public OpenAPI openAPIClient(){
        Server server = new Server();
        server.setUrl(serverPort);
        server.setDescription("local.env");

        Server prodServer = new Server();
        prodServer.setUrl("http://prod.url");
        prodServer.setDescription("prod.url");

        Contact contact = new Contact();
        contact.setName("Daniil Novikov");
        contact.setEmail("novikovdanila7@gmail.com");

        License license = new License().name("license.prod").url(serverPort+"/license");

        Info info = new Info()
                .title("Mp Api2")
                .version("1")
                .description("Api for mp")
                .license(license)
                .contact(contact);
        return new OpenAPI().info(info)
                .servers(List.of(server,prodServer));



    }
}
