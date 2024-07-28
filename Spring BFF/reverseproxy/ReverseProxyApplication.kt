package com.example.reverseproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReverseProxyApplication

fun main(args: Array<String>) {
    runApplication<ReverseProxyApplication>(*args)
}
