package ru.lashnev.task_manager

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class ResourceServerConfig {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.securityMatcher("/task_manager/**")
            .authorizeHttpRequests { authorize -> authorize.anyRequest().hasAuthority("SCOPE_task-manager.read") }
            .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
        return http.build()
    }
}
