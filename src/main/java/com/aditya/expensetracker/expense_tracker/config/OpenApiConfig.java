package com.aditya.expensetracker.expense_tracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components; 
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme; 

@Configuration
public class OpenApiConfig {
	
    @Value("${app.contact.email}")
    private String contactEmail;

    @Bean
    public OpenAPI expenseTrackerOpenAPI() {
    	
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .name("Bearer Authentication")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .info(new Info()
                        .title("Expense Tracker API")
                        .description("REST API for managing expenses, income, and authentication.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Aditya Shukla")
                                .email(contactEmail)));
    }
}
