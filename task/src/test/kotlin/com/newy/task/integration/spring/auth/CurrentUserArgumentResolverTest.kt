package com.newy.task.integration.spring.auth

import com.newy.task.integration.helper.WebMvcTestHelper
import com.newy.task.spring.auth.resolver.CurrentUserArgumentResolver
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import kotlin.test.assertEquals

@WebMvcTestHelper
@DisplayName("CurrentUserArgumentResolver 등록 테스트")
class CurrentUserArgumentResolverTest(
    @Autowired private val requestMappingHandlerAdapter: RequestMappingHandlerAdapter,
) {
    @Test
    fun `CurrentUserArgumentResolver 는 한 번만 등록되어야 한다`() {
        val resolverCount = requestMappingHandlerAdapter.argumentResolvers
            ?.count { it is CurrentUserArgumentResolver }

        assertEquals(1, resolverCount)
    }
}
