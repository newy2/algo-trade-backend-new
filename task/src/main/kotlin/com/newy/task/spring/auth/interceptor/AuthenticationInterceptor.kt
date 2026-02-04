package com.newy.task.spring.auth.interceptor

import com.newy.task.spring.auth.annotation.LoginRequired
import com.newy.task.spring.auth.model.LoginUser
import com.newy.task.spring.auth.repository.LoginUserJpaRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val userRepository: LoginUserJpaRepository
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 1. 컨트롤러 메서드인지 확인
        if (handler !is HandlerMethod) return true

        // 2. 헤더 정보 확인 및 DB 조회 (이전과 동일)
        val userIdHeader = request.getHeader("X-User-Id")
        val user = userIdHeader?.toLongOrNull()?.let { userRepository.findByIdOrNull(it) }

        if (user != null) {
            request.setAttribute("loginUser", LoginUser(id = user.id, nickname = user.nickname))
        }

        // 3. 마킹(@LoginRequired) 확인
        val isLoginRequired = handler.hasMethodAnnotation(LoginRequired::class.java) ||
                handler.beanType.isAnnotationPresent(LoginRequired::class.java)

        // 4. 마킹은 되어 있는데 유저 정보가 없으면 컷!
        if (isLoginRequired && request.getAttribute("loginUser") == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "인증이 필요한 API입니다.")
            return false
        }

        return true
    }
}