package ru.lashnev.api_client

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@RestController
class ApiClientController(private val webClient: WebClient) {
    @GetMapping(value = ["/task_list"])
    fun getArticles(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient
    ): Array<String>? {
        return webClient
            .get()
            .uri("http://localhost:8090/task_manager/list")
            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(Array<String>::class.java)
            .block()
    }
}
