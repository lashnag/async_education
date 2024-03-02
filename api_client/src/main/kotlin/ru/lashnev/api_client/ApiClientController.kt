package ru.lashnev.api_client

import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.util.*


@RestController
class ApiClientController(private val webClient: WebClient) {
    @GetMapping(value = ["/api_client/task_list"])
    fun getTasks(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        return webClient
            .get()
            .uri("http://localhost:8090/task_manager/list?principal=${authorizedClient.principalName}")
            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    @GetMapping(value = ["/api_client/create_task"])
    fun createNewTask(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient,
        taskDescription: String,
    ): String? {
        val bodyValues: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyValues.add("principal", authorizedClient.principalName)
        bodyValues.add("taskDescription", taskDescription)

        return webClient
            .put()
            .uri("http://localhost:8090/task_manager/create_task")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(bodyValues))
            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    @GetMapping(value = ["/api_client/close_task"])
    fun closeTask(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient,
        taskUUID: UUID,
    ): String? {
        val bodyValues: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyValues.add("principal", authorizedClient.principalName)
        bodyValues.add("taskUUID", taskUUID.toString())

        return webClient
            .post()
            .uri("http://localhost:8090/task_manager/close_task")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(bodyValues))
            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }

    @GetMapping(value = ["/api_client/reassign"])
    fun reassign(
        @RegisteredOAuth2AuthorizedClient("task-manager-client-authorization-code") authorizedClient: OAuth2AuthorizedClient
    ): String? {
        val bodyValues: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyValues.add("principal", authorizedClient.principalName)

        return webClient
            .post()
            .uri("http://localhost:8090/task_manager/reassign")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromFormData(bodyValues))
            .attributes(ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()
    }
}
