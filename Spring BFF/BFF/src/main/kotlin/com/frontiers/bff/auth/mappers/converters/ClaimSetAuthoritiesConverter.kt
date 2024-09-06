package com.frontiers.bff.auth.mappers.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority

/**********************************************************************************************************************/
/***************************************************** INTERFACE ******************************************************/
/**********************************************************************************************************************/

// adapted from:
// https://github.com/ch4mpy/spring-addons/blob/master/spring-addons-starter-oidc/src/main/java/com/c4_soft/springaddons/security/oidc/starter/ClaimSetAuthoritiesConverter.java

internal interface ClaimSetAuthoritiesConverter : Converter<Map<String, Any>, Collection<GrantedAuthority>> {
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/