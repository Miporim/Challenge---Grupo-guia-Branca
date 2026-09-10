package br.com.fiap.aguiabranca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import br.com.fiap.aguiabranca.security.JwtAccessDeniedHandler;
import br.com.fiap.aguiabranca.security.JwtAuthenticationEntryPoint;
import br.com.fiap.aguiabranca.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

/**
 * Sessão STATELESS, CSRF desabilitado, DSL com lambdas (obrigatório no
 * Security 7) — ver seção 5. A autorização fina por role/posse fica nos
 * services via {@code @PreAuthorize}; aqui só a regra grossa de rota
 * pública x autenticada.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/api/auth/register",
            "/api/auth/login",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DECISION: não há AuthenticationManager/UserDetailsService — login e
    // emissão do JWT são feitos manualmente em AuthService, e o filtro JWT
    // povoa o contexto direto com um UsernamePasswordAuthenticationToken
    // carregando (userId, authorities), sem passar pelo provider chain.
}
