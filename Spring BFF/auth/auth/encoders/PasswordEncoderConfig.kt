package com.example.authorizationserver.auth.encoders

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import java.security.SecureRandom

/**********************************************************************************************************************/
/******************************************************* CONFIGURATION ************************************************/
/**********************************************************************************************************************/

@Configuration
internal class PasswordEncoderConfig() {

    @Bean
    fun passwordEncoder(): PasswordEncoder {

        // BCrypt parameters
        val bcryptParams = BCryptPasswordEncoder(
            10,
            SecureRandom.getInstanceStrong()
        )

        // SCrypt parameters
        val scryptParams = SCryptPasswordEncoder(
            16384,   // CPU cost parameter
            8,    // Memory cost parameter
            1,   // Parallelization parameter
            32,    // Key length parameter
            16     // Salt length parameter
        )

        // map of password encoders
        val encoders: Map<String, PasswordEncoder> = mapOf(
            "bcrypt" to bcryptParams,
            "scrypt" to scryptParams
            // optionally, add a "noop" encoder for testing or migration purposes
            // "noop" to NoOpPasswordEncoder.getInstance()
        )

        // default encoder used
        return DelegatingPasswordEncoder("bcrypt", encoders)
    }

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/