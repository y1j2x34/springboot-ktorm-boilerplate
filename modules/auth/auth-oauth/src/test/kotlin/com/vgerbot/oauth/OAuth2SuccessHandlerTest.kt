package com.vgerbot.oauth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class OAuth2SuccessHandlerTest {

    @Test
    fun `should default to root redirect target`() {
        val handler = OAuth2SuccessHandler(
            OAuth2Properties()
        )

        assertEquals("/", ReflectionTestUtils.getField(handler, "defaultTargetUrl"))
    }

    @Test
    fun `should use configured redirect target`() {
        val handler = OAuth2SuccessHandler(
            OAuth2Properties(
                successRedirectUri = "/dashboard"
            )
        )

        assertEquals("/dashboard", ReflectionTestUtils.getField(handler, "defaultTargetUrl"))
    }
}
