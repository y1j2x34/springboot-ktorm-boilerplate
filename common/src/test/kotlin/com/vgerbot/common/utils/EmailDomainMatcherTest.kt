package com.vgerbot.common.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * EmailDomainMatcher 工具类单元测试
 */
class EmailDomainMatcherTest {
    
    @Test
    fun `test extractDomain - valid email`() {
        assertEquals("example.com", EmailDomainMatcher.extractDomain("user@example.com"))
        assertEquals("test.com", EmailDomainMatcher.extractDomain("admin@test.com"))
        assertEquals("sub.example.com", EmailDomainMatcher.extractDomain("user@sub.example.com"))
    }
    
    @Test
    fun `test extractDomain - invalid email`() {
        assertNull(EmailDomainMatcher.extractDomain("invalid-email"))
        assertNull(EmailDomainMatcher.extractDomain("@example.com"))
        assertNull(EmailDomainMatcher.extractDomain("user@"))
        assertNull(EmailDomainMatcher.extractDomain(""))
    }
    
    @Test
    fun `test matches - exact match`() {
        assertTrue(EmailDomainMatcher.matches("user@example.com", "example.com"))
        assertTrue(EmailDomainMatcher.matches("admin@test.com", "test.com"))
        assertFalse(EmailDomainMatcher.matches("user@other.com", "example.com"))
    }
    
    @Test
    fun `test matches - brace expansion`() {
        // 单个大括号扩展
        assertTrue(EmailDomainMatcher.matches("user@comp.com", "comp.{com,cn}"))
        assertTrue(EmailDomainMatcher.matches("user@comp.cn", "comp.{com,cn}"))
        assertFalse(EmailDomainMatcher.matches("user@comp.net", "comp.{com,cn}"))
        
        // 多个大括号扩展（需要通配符 * 匹配子域名部分）
        assertTrue(EmailDomainMatcher.matches("user@sub.example.com", "*.{example,test}.com"))
        assertTrue(EmailDomainMatcher.matches("admin@sub.test.com", "*.{example,test}.com"))
        assertFalse(EmailDomainMatcher.matches("user@example.com", "*.{example,test}.com"))
    }
    
    @Test
    fun `test matches - wildcard`() {
        assertTrue(EmailDomainMatcher.matches("user@sub.example.com", "*.example.com"))
        assertTrue(EmailDomainMatcher.matches("admin@test.example.com", "*.example.com"))
        assertFalse(EmailDomainMatcher.matches("user@example.com", "*.example.com"))
        assertFalse(EmailDomainMatcher.matches("user@other.com", "*.example.com"))
    }
    
    @Test
    fun `test matches - multiple patterns`() {
        val patterns = "example.com,test.com,comp.{com,cn}"
        
        assertTrue(EmailDomainMatcher.matches("user@example.com", patterns))
        assertTrue(EmailDomainMatcher.matches("user@test.com", patterns))
        assertTrue(EmailDomainMatcher.matches("user@comp.com", patterns))
        assertTrue(EmailDomainMatcher.matches("user@comp.cn", patterns))
        assertFalse(EmailDomainMatcher.matches("user@other.com", patterns))
    }
    
    @Test
    fun `test matches - case insensitive`() {
        assertTrue(EmailDomainMatcher.matches("user@Example.COM", "example.com"))
        assertTrue(EmailDomainMatcher.matches("user@COMP.COM", "comp.{com,cn}"))
    }
    
    @Test
    fun `test matchesDomain - exact match`() {
        assertTrue(EmailDomainMatcher.matchesDomain("example.com", "example.com"))
        assertFalse(EmailDomainMatcher.matchesDomain("test.com", "example.com"))
    }
    
    @Test
    fun `test matchesDomain - null or empty patterns`() {
        assertFalse(EmailDomainMatcher.matchesDomain("example.com", null))
        assertFalse(EmailDomainMatcher.matchesDomain("example.com", ""))
        assertFalse(EmailDomainMatcher.matchesDomain("example.com", "   "))
    }
    
    @Test
    fun `test patternToRegex - exact match`() {
        val pattern = EmailDomainMatcher.patternToRegex("example.com")
        assertTrue(pattern.matcher("example.com").matches())
        assertFalse(pattern.matcher("test.com").matches())
        assertFalse(pattern.matcher("sub.example.com").matches())
    }
    
    @Test
    fun `test patternToRegex - wildcard`() {
        val pattern = EmailDomainMatcher.patternToRegex("*.example.com")
        assertTrue(pattern.matcher("sub.example.com").matches())
        assertTrue(pattern.matcher("test.example.com").matches())
        assertFalse(pattern.matcher("example.com").matches())
        assertFalse(pattern.matcher("other.com").matches())
    }
    
    @Test
    fun `test patternToRegex - brace expansion`() {
        val pattern = EmailDomainMatcher.patternToRegex("comp.{com,cn,net}")
        assertTrue(pattern.matcher("comp.com").matches())
        assertTrue(pattern.matcher("comp.cn").matches())
        assertTrue(pattern.matcher("comp.net").matches())
        assertFalse(pattern.matcher("comp.org").matches())
    }
    
    @Test
    fun `test patternToRegex - complex pattern`() {
        val pattern = EmailDomainMatcher.patternToRegex("*.{example,test}.{com,cn}")
        assertTrue(pattern.matcher("sub.example.com").matches())
        assertTrue(pattern.matcher("admin.example.cn").matches())
        assertTrue(pattern.matcher("user.test.com").matches())
        assertTrue(pattern.matcher("api.test.cn").matches())
        assertFalse(pattern.matcher("example.com").matches())
        assertFalse(pattern.matcher("test.org").matches())
    }
    
    @Test
    fun `test expandPattern - exact match`() {
        val expanded = EmailDomainMatcher.expandPattern("example.com")
        assertEquals(listOf("example.com"), expanded)
    }
    
    @Test
    fun `test expandPattern - with wildcard`() {
        val expanded = EmailDomainMatcher.expandPattern("*.example.com")
        assertEquals(listOf("*.example.com"), expanded)
    }
    
    @Test
    fun `test expandPattern - brace expansion`() {
        val expanded = EmailDomainMatcher.expandPattern("comp.{com,cn}")
        assertTrue(expanded.containsAll(listOf("comp.com", "comp.cn")))
        assertEquals(2, expanded.size)
    }
    
    @Test
    fun `test expandPattern - multiple braces`() {
        val expanded = EmailDomainMatcher.expandPattern("{example,test}.{com,cn}")
        assertTrue(expanded.containsAll(listOf(
            "example.com", "example.cn",
            "test.com", "test.cn"
        )))
        assertEquals(4, expanded.size)
    }
    
    @Test
    fun `test complex real world scenarios`() {
        // 测试场景1：公司多个域名
        val companyPatterns = "company.{com,cn},corp.company.com"
        assertTrue(EmailDomainMatcher.matches("employee@company.com", companyPatterns))
        assertTrue(EmailDomainMatcher.matches("employee@company.cn", companyPatterns))
        assertTrue(EmailDomainMatcher.matches("employee@corp.company.com", companyPatterns))
        assertFalse(EmailDomainMatcher.matches("external@gmail.com", companyPatterns))
        
        // 测试场景2：支持所有子域名
        val subdomainPattern = "*.company.com"
        assertTrue(EmailDomainMatcher.matches("user@dev.company.com", subdomainPattern))
        assertTrue(EmailDomainMatcher.matches("user@api.company.com", subdomainPattern))
        assertFalse(EmailDomainMatcher.matches("user@company.com", subdomainPattern))
        
        // 测试场景3：多个公司域名组合
        val multiCompanyPatterns = "companya.{com,cn},companyb.com,*.test.com"
        assertTrue(EmailDomainMatcher.matches("user@companya.com", multiCompanyPatterns))
        assertTrue(EmailDomainMatcher.matches("user@companya.cn", multiCompanyPatterns))
        assertTrue(EmailDomainMatcher.matches("user@companyb.com", multiCompanyPatterns))
        assertTrue(EmailDomainMatcher.matches("user@sub.test.com", multiCompanyPatterns))
        assertFalse(EmailDomainMatcher.matches("user@other.com", multiCompanyPatterns))
    }
    
    @Test
    fun `test edge cases`() {
        // 空大括号
        assertFalse(EmailDomainMatcher.matches("user@example.com", "example.{}"))
        
        // 不匹配的大括号
        assertTrue(EmailDomainMatcher.matches("user@example.{com", "example.{com"))
        
        // 空格处理
        assertTrue(EmailDomainMatcher.matches("user@example.com", " example.com , test.com "))
        
        // 大括号内的空格
        assertTrue(EmailDomainMatcher.matches("user@comp.com", "comp.{com, cn, net}"))
    }
}

