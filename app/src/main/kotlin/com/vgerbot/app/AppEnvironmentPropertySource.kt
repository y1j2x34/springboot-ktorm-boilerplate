package com.vgerbot.app

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import kotlin.math.log

@Component
class AppEnvironmentPropertySource : EnvironmentPostProcessor {
    val logger = LoggerFactory.getLogger(AppEnvironmentPropertySource::class.java)
    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        val patternResolver = PathMatchingResourcePatternResolver()
        val activeProfiles = environment?.activeProfiles?.plus("default") ?: arrayOf("dev", "default")
        val moduleConfigLocations = patternResolver.getResources("classpath*:application-*.yml")
        val yamlPropertyLoader = YamlPropertySourceLoader()
        val reg = Regex("application-((${activeProfiles.joinToString("|")})(-\\w+)?).yml")
        moduleConfigLocations.forEach {
            val filename = it.filename ?: return@forEach;
            val matchResult = reg.matchEntire(filename);
            val resourceName = matchResult?.groupValues?.getOrNull(1) ?: return@forEach
            if (resourceName.isEmpty()) return@forEach

            val sources = yamlPropertyLoader.load(resourceName, it)
            sources.forEach { source ->
                logger.info("load external source: {}", source);
                environment?.propertySources?.addLast(source)
            }
        }
    }

}