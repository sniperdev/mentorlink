package com.sniperdev.mentorlink_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI mentorLinkOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("MentorLink API")
                                .description("Backend API for MentorLink mentoring platform")
                                .version("1.0.0")
                                .contact(
                                        new Contact().name("Sniperdev").email("biuro@sniperdev.pl")
                                )
                );
    }
}
