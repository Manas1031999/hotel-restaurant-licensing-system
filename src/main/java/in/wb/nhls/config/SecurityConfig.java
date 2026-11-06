package in.wb.nhls.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {

    private static final String APPLICANT = "APPLICANT";
    private static final String REVIEWER = "REVIEWER";
    private static final String APPROVER = "APPROVER";
    private static final String ADMIN = "ADMIN";

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            JsonSecurityErrorHandler errorHandler) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                // খোলা
                .requestMatchers("/api/v1/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login").permitAll()

                // শুধু ADMIN
                .requestMatchers("/api/v1/users/**", "/api/v1/audit-logs/**", "/api/v1/notifications/**").hasRole(ADMIN)

                // Business
                .requestMatchers(HttpMethod.POST, "/api/v1/businesses").hasAnyRole(APPLICANT, ADMIN)
                .requestMatchers(HttpMethod.PUT, "/api/v1/businesses/**").hasAnyRole(APPLICANT, ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/v1/businesses/**").hasRole(ADMIN)

                // Application: কোন status কে দিতে পারবে, তা service-এ আরও খুঁটিয়ে দেখা হয়
                .requestMatchers(HttpMethod.POST, "/api/v1/license-applications").hasRole(APPLICANT)
                .requestMatchers(HttpMethod.PUT, "/api/v1/license-applications/*/review").hasAnyRole(REVIEWER, APPROVER)
                .requestMatchers(HttpMethod.GET, "/api/v1/license-applications").hasAnyRole(REVIEWER, APPROVER, ADMIN)

                // License
                .requestMatchers(HttpMethod.POST, "/api/licenses/generate/**").hasAnyRole(APPROVER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/licenses/renew/**").hasAnyRole(APPLICANT, ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/licenses/expire").hasRole(ADMIN)

                // Payment
                .requestMatchers("/api/payments/**").hasAnyRole(APPLICANT, ADMIN)

                // বাকি সব GET: login করা যে কেউ
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint(errorHandler)
                .accessDeniedHandler(errorHandler))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(errorHandler)
                .accessDeniedHandler(errorHandler));

        return http.build();
    }
}
