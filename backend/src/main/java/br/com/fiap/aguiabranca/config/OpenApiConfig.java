package br.com.fiap.aguiabranca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Esquema {@code bearerAuth} para o botão Authorize funcionar no Swagger —
 * ver seção 6. Todo o fluxo precisa poder ser demonstrado a partir daqui,
 * sem Postman.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI aguiaBrancaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Águia Branca API")
                        .version("v1")
                        .description("API de gestão de inovação — Challenge Águia Branca"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}
