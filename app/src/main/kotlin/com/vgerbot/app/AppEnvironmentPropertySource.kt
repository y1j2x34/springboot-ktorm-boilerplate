package com.vgerbot.app

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component

@Component
class AppEnvironmentPropertySource: EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        val patternResolver = PathMatchingResourcePatternResolver()
        val moduleConfigLocations = patternResolver.getResources("classpath*:application-*.yml")
        val yamlPropertyLoader = YamlPropertySourceLoader()
        moduleConfigLocations.forEach {
            val name = it.filename
                ?.replace("application-", "")
                ?.replace(".yml", "") ?: return
            val sources = yamlPropertyLoader.load(name, it)
            sources.forEach { source ->
                environment?.propertySources?.addLast(source)
            }
        }
    }

}