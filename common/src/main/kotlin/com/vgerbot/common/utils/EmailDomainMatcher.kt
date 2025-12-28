package com.vgerbot.common.utils

import java.util.regex.Pattern

/**
 * 邮箱域名匹配工具类
 * 
 * 支持多种匹配模式：
 * 1. 精确匹配：example.com
 * 2. 大括号扩展：comp.{com,cn} -> 匹配 comp.com 或 comp.cn
 * 3. 多个域名用逗号分隔：example.com,test.com
 * 4. 通配符：*.example.com -> 匹配任意子域名
 * 
 * 示例配置：
 * - "example.com" -> 只匹配 example.com
 * - "comp.{com,cn}" -> 匹配 comp.com 和 comp.cn
 * - "*.example.com" -> 匹配 sub.example.com, test.example.com 等
 * - "example.com,test.com" -> 匹配 example.com 或 test.com
 * - "*.{example,test}.com" -> 匹配 sub.example.com, sub.test.com 等
 */
object EmailDomainMatcher {
    
    /**
     * 从邮箱地址提取域名
     * 
     * @param email 邮箱地址
     * @return 域名（小写），如果格式不正确则返回 null
     */
    fun extractDomain(email: String): String? {
        val atIndex = email.indexOf('@')
        // 检查 @ 符号是否存在，以及 @ 前后是否都有内容
        if (atIndex <= 0 || atIndex == email.length - 1) {
            return null
        }
        return email.substring(atIndex + 1).lowercase().trim()
    }
    
    /**
     * 检查邮箱是否匹配配置的域名模式
     * 
     * @param email 邮箱地址
     * @param configuredDomains 配置的域名模式（逗号分隔）
     * @return 是否匹配
     */
    fun matches(email: String, configuredDomains: String?): Boolean {
        val domain = extractDomain(email) ?: return false
        return matchesDomain(domain, configuredDomains)
    }
    
    /**
     * 检查域名是否匹配配置的域名模式
     * 
     * @param domain 域名（已转换为小写）
     * @param configuredDomains 配置的域名模式（逗号分隔）
     * @return 是否匹配
     */
    fun matchesDomain(domain: String, configuredDomains: String?): Boolean {
        if (configuredDomains.isNullOrBlank()) {
            return false
        }
        
        // 分割多个域名配置（逗号分隔）
        val domainPatterns = splitPatterns(configuredDomains)
        
        // 尝试匹配任意一个模式
        return domainPatterns.any { pattern ->
            matchesSinglePattern(domain, pattern)
        }
    }
    
    /**
     * 分割域名模式配置
     * 需要考虑大括号内的逗号不应该被分割
     * 
     * @param configuredDomains 配置的域名模式
     * @return 分割后的模式列表
     */
    private fun splitPatterns(configuredDomains: String): List<String> {
        val patterns = mutableListOf<String>()
        val current = StringBuilder()
        var braceDepth = 0
        
        for (char in configuredDomains) {
            when (char) {
                '{' -> {
                    braceDepth++
                    current.append(char)
                }
                '}' -> {
                    braceDepth--
                    current.append(char)
                }
                ',' -> {
                    if (braceDepth > 0) {
                        // 大括号内的逗号不分割
                        current.append(char)
                    } else {
                        // 大括号外的逗号用于分割
                        val pattern = current.toString().trim().lowercase()
                        if (pattern.isNotEmpty()) {
                            patterns.add(pattern)
                        }
                        current.clear()
                    }
                }
                else -> current.append(char)
            }
        }
        
        // 添加最后一个模式
        val lastPattern = current.toString().trim().lowercase()
        if (lastPattern.isNotEmpty()) {
            patterns.add(lastPattern)
        }
        
        return patterns
    }
    
    /**
     * 匹配单个域名模式
     * 
     * @param domain 域名
     * @param pattern 模式
     * @return 是否匹配
     */
    private fun matchesSinglePattern(domain: String, pattern: String): Boolean {
        // 将模式转换为正则表达式
        val regex = patternToRegex(pattern)
        return regex.matcher(domain).matches()
    }
    
    /**
     * 将域名模式转换为正则表达式
     * 
     * 支持的语法：
     * - * : 匹配任意字符（不包括点）
     * - {option1,option2} : 匹配任意一个选项
     * - 其他字符：精确匹配
     * 
     * 示例：
     * - "example.com" -> 精确匹配 example.com
     * - "*.example.com" -> 匹配 sub.example.com, test.example.com 等
     * - "comp.{com,cn}" -> 匹配 comp.com 或 comp.cn
     * - "*.{example,test}.com" -> 匹配 sub.example.com, sub.test.com 等
     * 
     * @param pattern 域名模式
     * @return 正则表达式 Pattern
     */
    fun patternToRegex(pattern: String): Pattern {
        if (pattern.isBlank()) {
            return Pattern.compile("^$")
        }
        
        val regex = StringBuilder("^")
        var i = 0
        
        while (i < pattern.length) {
            when (val char = pattern[i]) {
                '*' -> {
                    // 星号匹配任意字符（不包括点）
                    regex.append("[^.]+")
                    i++
                }
                '{' -> {
                    // 处理大括号扩展
                    val closeIndex = pattern.indexOf('}', i)
                    if (closeIndex > i) {
                        val options = pattern.substring(i + 1, closeIndex)
                            .split(',')
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }
                            .map { Pattern.quote(it) }
                            .joinToString("|")
                        
                        if (options.isNotEmpty()) {
                            regex.append("(?:").append(options).append(")")
                        }
                        i = closeIndex + 1
                    } else {
                        // 没有找到闭合的大括号，按字面意义处理
                        regex.append(Pattern.quote(char.toString()))
                        i++
                    }
                }
                '.' -> {
                    // 转义点号
                    regex.append("\\.")
                    i++
                }
                else -> {
                    // 其他字符按字面意义匹配
                    regex.append(Pattern.quote(char.toString()))
                    i++
                }
            }
        }
        
        regex.append("$")
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE)
    }
    
    /**
     * 获取所有可能的域名展开列表（用于调试和测试）
     * 
     * 注意：如果使用了通配符 *，则无法展开所有可能的值
     * 
     * @param pattern 域名模式
     * @return 展开后的域名列表（如果包含通配符则返回原模式）
     */
    fun expandPattern(pattern: String): List<String> {
        // 如果包含通配符，无法完全展开
        if (pattern.contains('*')) {
            return listOf(pattern)
        }
        
        // 如果不包含大括号，直接返回
        if (!pattern.contains('{') || !pattern.contains('}')) {
            return listOf(pattern)
        }
        
        // 递归展开大括号
        return expandBraces(pattern)
    }
    
    /**
     * 递归展开大括号模式
     */
    private fun expandBraces(pattern: String): List<String> {
        val braceStart = pattern.indexOf('{')
        if (braceStart < 0) {
            return listOf(pattern)
        }
        
        val braceEnd = pattern.indexOf('}', braceStart)
        if (braceEnd < 0) {
            return listOf(pattern)
        }
        
        val prefix = pattern.substring(0, braceStart)
        val suffix = pattern.substring(braceEnd + 1)
        val options = pattern.substring(braceStart + 1, braceEnd)
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val results = mutableListOf<String>()
        for (option in options) {
            val expanded = prefix + option + suffix
            // 递归处理剩余的大括号
            results.addAll(expandBraces(expanded))
        }
        
        return results
    }
}

