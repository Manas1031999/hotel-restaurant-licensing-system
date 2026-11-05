package in.wb.nhls.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/api/v1/health","/api/v1/businesses","/api/v1/businesses/**","/api/v1/license-applications/**", "/api/licenses/**","/api/payments/**","/api/v1/audit-logs","/api/v1/audit-logs/**").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }
}
