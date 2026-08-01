package com.gdghajithon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String RECOMMENDATIONS_PATH = "/api/users/recommendations";
    private static final Set<String> RECOMMENDATION_FILTERS = Set.of("sportIds", "regionIds");

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, bearerAuth))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME));
    }

    @Bean
    public OpenApiCustomizer recommendationFilterCustomizer() {
        return openApi -> openApi.getPaths()
                .get(RECOMMENDATIONS_PATH)
                .getGet()
                .getParameters()
                .stream()
                .filter(parameter -> RECOMMENDATION_FILTERS.contains(parameter.getName()))
                .forEach(parameter -> {
                    parameter.setStyle(io.swagger.v3.oas.models.parameters.Parameter.StyleEnum.FORM);
                    parameter.setExplode(false);
                });
    }
}
