package ru.lashnev.async_education

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AsyncEducationApplication

fun main(args: Array<String>) {
    runApplication<AsyncEducationApplication>(*args)
}
