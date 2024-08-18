package com.example.bff.auth.mappers.converters

import org.springframework.core.convert.converter.Converter
import org.springframework.security.core.GrantedAuthority

/**********************************************************************************************************************/
/***************************************************** INTERFACE ******************************************************/
/**********************************************************************************************************************/

internal interface ClaimSetAuthoritiesConverter : Converter<Map<String, Any>, Collection<GrantedAuthority>> {
}

/**********************************************************************************************************************/
/**************************************************** END OF KOTLIN ***************************************************/
/**********************************************************************************************************************/