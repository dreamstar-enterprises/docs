package com.example.authorizationserver

import com.example.authorizationserver.props.ClientSecurityProperties
import com.example.authorizationserver.props.ServerProperties
import com.example.authorizationserver.props.SpringDataProperties
import com.example.authorizationserver.props.SpringSessionProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    ServerProperties::class,
    ClientSecurityProperties::class,
    SpringDataProperties::class,
    SpringSessionProperties::class
)
internal class AuthorizationServerApplication

fun main(args: Array<String>) {
    // run SpringBoot application
    runApplication<AuthorizationServerApplication>(*args)

}
