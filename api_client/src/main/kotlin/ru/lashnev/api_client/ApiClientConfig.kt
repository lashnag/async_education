package ru.lashnev.api_client

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.Customizer.withDefaults
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableWebSecurity
class ApiClientConfig {
    @Bean
    fun webClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
        val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        return WebClient.builder()
            .apply(oauth2Client.oauth2Configuration())
            .build()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorizeRequests -> authorizeRequests.anyRequest().authenticated() }
            .oauth2Login { oauth2Login -> oauth2Login.loginPage("/oauth2/authorization/task-manager-client-oidc") }
            .oauth2Client(withDefaults())

        http.securityMatcher("/task_manager/**")
            .authorizeHttpRequests { authorize -> authorize.anyRequest().hasAuthority("SCOPE_task-manager.read") }
            .authorizeHttpRequests { authorize -> authorize.anyRequest().hasAuthority("SCOPE_task-manager.read") }
            .oauth2ResourceServer { it.jwt(withDefaults()) }
        return http.build()
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientRepository: OAuth2AuthorizedClientRepository,
    ): OAuth2AuthorizedClientManager {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .authorizationCode()
            .refreshToken()
            .build()
        val authorizedClientManager = DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository)
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
        return authorizedClientManager
    }
}
