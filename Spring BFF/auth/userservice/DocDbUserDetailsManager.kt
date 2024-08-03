package com.example.authorizationserver.auth.userservice

import org.springframework.security.core.userdetails.UserDetailsService

/**********************************************************************************************************************/
/************************************************* SERVICE INTERFACE **************************************************/
/**********************************************************************************************************************/

internal interface DocDbUserDetailsManager : UserDetailsService {

    //        fun createUser(user: UserDetails): Mono<Void>
    //        fun updateUser(user: UserDetails): Mono<Void>
    //        fun deleteUser(username: String): Mono<Void>
    //        fun changePassword(oldPassword: String, newPassword: String): Mono<Void>
    //        fun userExists(username: String): Mono<Boolean>

}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/