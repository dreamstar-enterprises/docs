package com.example.authorizationserver.auth.security.userservice

import com.example.authorizationserver.api.entities.user.UserEntity
import com.example.authorizationserver.auth.security.objects.user.DocDbUser
import com.example.authorizationserver.auth.security.roles.RoleAuthConfig
import com.example.authorizationserver.auth.security.virtualthreads.VirtualThreadManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**********************************************************************************************************************/
/********************************************** SERVICE IMPLEMENTATION ************************************************/
/**********************************************************************************************************************/

/**********************************************************************************************************************/
/* USER DETAILS - MANAGER (extends USER SERVICE) */
/**********************************************************************************************************************/

    @Service
    internal class DocDbUserDetailsManagerImpl(
    @Autowired
        private val passwordEncoder: PasswordEncoder,
        private val virtualThreadManager: VirtualThreadManager,
    ) : DocDbUserDetailsManager {

        // find username in database, and get other security details, from User's Profile
        override fun loadUserByUsername(userEmail: String): DocDbUser {
            val userEntity = virtualThreadManager.fetchUserEntity(userEmail)
            if (userEntity == null) {
                throw UsernameNotFoundException("User not found: $userEmail")
            } else {
                return createDocDBUserDetails(userEntity)
            }
        }

        // create DocDBUserDetails object
        private fun createDocDBUserDetails(userEntity: UserEntity): DocDbUser {

            // get authorities
            val authorities = RoleAuthConfig().getAuthorities(userEntity.securityInformation.roles)

            // return DocDBuserDetails object
            return DocDbUser(
                userEntity._id.toString(),
                userEntity.contactInformation.email,
                passwordEncoder.encode(userEntity.securityInformation.password),
                authorities,
                userEntity.securityInformation.isAccountNonExpired,
                userEntity.securityInformation.isAccountNonLocked,
                userEntity.securityInformation.isCredentialsNonExpired,
                userEntity.securityInformation.isEnabled,
            )
        }

//        override fun createUser(user: UserDetails): Mono<Void> {
//            return mono {
//                val encodedUser = User.builder()
//                    .username(user.username)
//                    .password(passwordEncoder.encode(user.password))
//                    .authorities(user.authorities)
//                    .build()
//                userRepo.save(encodedUser)
//            }.then()
//        }
//
//        override fun updateUser(user: UserDetails): Mono<Void> {
//            return mono {
//                val existingUser = userRepo.getUserByEmail(user.username)
//                existingUser?.let {
//                    val updatedUser = it.copy(
//                        securityInformation = it.securityInformation.copy(
//                            password = passwordEncoder.encode(user.password)
//                        ),
//                        contactInformation = it.contactInformation.copy(
//                            email = user.username
//                        ),
//                        roles = user.authorities.map { it.authority }
//                    )
//                    userRepo.save(updatedUser)
//                } ?: throw UsernameNotFoundException("User not found: ${user.username}")
//            }.then()
//        }
//
//        override fun deleteUser(username: String): Mono<Void> {
//            return mono {
//                userRepo.deleteByUsername(username)
//            }.then()
//        }
//
//        override fun changePassword(oldPassword: String, newPassword: String): Mono<Void> {
//            return mono {
//                val username = // Retrieve current username from security context
//                val user = userRepo.getUserByEmail(username)
//                user?.let {
//                    if (passwordEncoder.matches(oldPassword, it.securityInformation.password)) {
//                        val updatedUser = it.copy(
//                            securityInformation = it.securityInformation.copy(
//                                password = passwordEncoder.encode(newPassword)
//                            )
//                        )
//                        userRepo.save(updatedUser)
//                    } else {
//                        throw BadCredentialsException("Invalid old password")
//                    }
//                } ?: throw UsernameNotFoundException("User not found: $username")
//            }.then()
//        }
//
//        override fun userExists(username: String): Mono<Boolean> {
//            return mono {
//                userRepo.getUserByEmail(username) != null
//            }
//        }
    }

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/