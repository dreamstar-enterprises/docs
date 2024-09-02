package com.frontiers.bff

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BFFApplication

fun main(args: Array<String>) {

    // run SpringBoot application
    runApplication<BFFApplication>(*args)

}
