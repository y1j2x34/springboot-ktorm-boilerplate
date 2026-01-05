package com.vgerbot.app.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Flux
import java.time.Duration

/**
 * Server-Sent Events Controller
 * Provides REST API for server-sent events streaming
 */
@Tag(name = "SSE", description = "Server-Sent Events APIs")
@Controller
@RequestMapping("event")
class SseController {
    @Operation(summary = "Stream hello world", description = "Stream hello world messages via Server-Sent Events")
    @ApiResponse(responseCode = "200", description = "SSE stream started")
    @GetMapping("hello-world")
    fun streamHelloWorld() = Flux.fromArray(arrayOf(1,2,3,4,5))
        .map { sequence ->
            ServerSentEvent.builder<String>()
                .id(sequence.toString())
                .event("data")
                .data("Hello world $sequence !")
                .build()
        }
}