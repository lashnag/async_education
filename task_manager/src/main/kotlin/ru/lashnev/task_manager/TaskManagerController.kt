package ru.lashnev.task_manager

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient


@RestController
class TaskManagerController(private val webClient: WebClient) {
    @GetMapping("/task_manager/list")
    fun getArticles(): Array<String> {
        return arrayOf("Task 1", "Task 2")
    }

    @GetMapping(value = ["/task_list"])
    fun getArticles(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient
    ): Array<String>? {
        return webClient
            .get()
            .uri("http://127.0.0.1:8080/task_manager/list")
            .attributes(oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(Array<String>::class.java)
            .block()
    }
}
