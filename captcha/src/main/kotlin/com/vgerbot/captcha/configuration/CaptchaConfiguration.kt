package com.vgerbot.captcha.configuration

import com.anji.captcha.model.common.Const
import com.anji.captcha.service.CaptchaCacheService
import com.anji.captcha.service.CaptchaService
import com.anji.captcha.service.impl.CaptchaServiceFactory
import com.anji.captcha.util.ImageUtils
import com.anji.captcha.util.StringUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.util.Base64Utils
import org.springframework.util.FileCopyUtils
import java.util.*

@Configuration
class CaptchaConfiguration {

    @Bean(name = ["AjCaptchaCacheService"])
    fun captchaCacheService(): CaptchaCacheService {
        return CaptchaServiceFactory.getCache("local")
    }

    @Bean
    @DependsOn("AjCaptchaCacheService")
    fun captchaService(): CaptchaService {
        val config = Properties()

        config[Const.CAPTCHA_CACHETYPE] = "local"
        config[Const.CAPTCHA_WATER_MARK] = "<watermark>";
        config[Const.CAPTCHA_FONT_TYPE] = "宋体"
        config[Const.CAPTCHA_TYPE] = "default"
        config[Const.CAPTCHA_INTERFERENCE_OPTIONS] = "0"
        config[Const.ORIGINAL_PATH_JIGSAW] = ""
        config[Const.ORIGINAL_PATH_PIC_CLICK] = ""
        config[Const.CAPTCHA_SLIP_OFFSET] = "5"
        config[Const.CAPTCHA_AES_STATUS] = "true"
        config[Const.CAPTCHA_WATER_FONT] = "宋体"
        config[Const.CAPTCHA_CACAHE_MAX_NUMBER] = "1000"
        config[Const.CAPTCHA_TIMING_CLEAR_SECOND] = "180"
        if ((StringUtils.isNotBlank(config.getProperty(Const.ORIGINAL_PATH_JIGSAW))
                    && config.getProperty(Const.ORIGINAL_PATH_JIGSAW)
                .startsWith("classpath:"))
            || (StringUtils.isNotBlank(config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK))
                    && config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK)
                .startsWith("classpath:"))
        ) {
            config[Const.CAPTCHA_INIT_ORIGINAL] = "true"
            initializeBaseMap(
                config.getProperty(Const.ORIGINAL_PATH_JIGSAW),
                config.getProperty(Const.ORIGINAL_PATH_PIC_CLICK)
            )
        }
        return CaptchaServiceFactory.getInstance(config)
    }

    companion object {
        private fun initializeBaseMap(jigsaw: String, picClick: String) {
            ImageUtils.cacheBootImage(
                getResourcesImagesFile(
                    "$jigsaw/original/*.png"
                ),
                getResourcesImagesFile("$jigsaw/slidingBlock/*.png"),
                getResourcesImagesFile("$picClick/*.png")
            )
        }

        fun getResourcesImagesFile(path: String?): Map<String?, String> {
            val imgMap: MutableMap<String?, String> = HashMap()
            val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver()
            try {
                val resources = resolver.getResources(path!!)
                for (resource in resources) {
                    val bytes = FileCopyUtils.copyToByteArray(resource.inputStream)
                    val string = Base64Utils.encodeToString(bytes)
                    val filename = resource.filename
                    imgMap[filename] = string
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return imgMap
        }
    }
}
