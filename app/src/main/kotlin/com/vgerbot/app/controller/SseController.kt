package com.vgerbot.app.controller

import org.springframework.http.codec.ServerSentEvent
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Flux
import java.time.Duration

@Controller
@RequestMapping("event")
class SseController {
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