package com.zokudo.sor.config;

import com.google.common.collect.Lists;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableSwagger2
public class ApplicationConfig {

    @Value("${swagger.enable}")
    private String isSwaggerEnable;


    @Bean
    public Client client() {
        return ClientBuilder
                .newBuilder()
                .register(new LoggingFeature(Logger.getLogger(this.getClass().getName()),
                        Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, null))
                .build();
    }

    @Bean
    public Docket productApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(metaData()).securitySchemes(Lists.newArrayList(new BasicAuth("basicAuth")))
                .enable(Boolean.parseBoolean(isSwaggerEnable));
    }

    private ApiInfo metaData() {
        return new ApiInfo(
                "SOR Mircroservice",
                "SOR Mircroservice",
                "2.0",
                "Terms of service",
                new Contact("Autobots", "https://www.autobots.com", ""),
                "Version 2.0",
                "",
                Collections.emptyList());
    }


}
