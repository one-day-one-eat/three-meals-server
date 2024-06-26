package team.sparta.onehouronemeal.infra.security.jwt

import io.jsonwebtoken.ExpiredJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import team.sparta.onehouronemeal.infra.security.UserPrincipal

@Component
class JwtAuthenticationFilter(
    private val jwtPlugin: JwtPlugin
) : OncePerRequestFilter() {

    companion object {
        private val BEARER_PATTERN = Regex("^Bearer (.+?)$")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val jwt = request.getBearerToken()

        if (jwt != null) {
            jwtPlugin.validateToken(jwt)
                .onSuccess {
                    val userId = it.payload.subject.toLong()
                    val role = it.payload.get("role", String::class.java)
                    val type = it.payload.get("type", String::class.java)

                    if (type != JwtPlugin.ACCESS_TOKEN_TYPE) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token type")
                        return
                    }

                    val principal = UserPrincipal(
                        id = userId,
                        roles = setOf(role)
                    )

                    val authentication = JwtAuthenticationToken(
                        principal = principal,
                        details = WebAuthenticationDetailsSource().buildDetails(request)
                    )

                    SecurityContextHolder.getContext().authentication = authentication
                }
                .onFailure { exception ->
                    when (exception) {
                        is ExpiredJwtException -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired")
                        }

                        else -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token")
                        }
                    }

                    return
                }
        }

        filterChain.doFilter(request, response)
    }

    private fun HttpServletRequest.getBearerToken(): String? {
        val headerValue = this.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return BEARER_PATTERN.find(headerValue)?.groupValues?.get(1)
    }
}