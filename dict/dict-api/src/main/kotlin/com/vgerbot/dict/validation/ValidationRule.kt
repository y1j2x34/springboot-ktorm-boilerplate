package com.vgerbot.dict.validation

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * 校验规则基类
 * 使用 JSON 多态序列化
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = RegexValidationRule::class, name = "regex"),
    JsonSubTypes.Type(value = RangeValidationRule::class, name = "range"),
    JsonSubTypes.Type(value = LengthValidationRule::class, name = "length"),
    JsonSubTypes.Type(value = EnumValidationRule::class, name = "enum"),
    JsonSubTypes.Type(value = NumberValidationRule::class, name = "number"),
    JsonSubTypes.Type(value = DateRangeValidationRule::class, name = "dateRange"),
    JsonSubTypes.Type(value = EmailValidationRule::class, name = "email"),
    JsonSubTypes.Type(value = PhoneValidationRule::class, name = "phone"),
    JsonSubTypes.Type(value = UrlValidationRule::class, name = "url")
)
sealed class ValidationRule {
    abstract fun validate(value: String): Boolean
    abstract fun getDefaultMessage(): String
}

/**
 * 正则表达式校验
 * 示例: {"type": "regex", "pattern": "^[a-zA-Z0-9]+$"}
 */
data class RegexValidationRule(
    val pattern: String
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        return value.matches(Regex(pattern))
    }
    
    override fun getDefaultMessage(): String {
        return "值不符合正则表达式规则: $pattern"
    }
}

/**
 * 数值范围校验
 * 示例: {"type": "range", "min": 0, "max": 100}
 */
data class RangeValidationRule(
    val min: Double? = null,
    val max: Double? = null
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        val numValue = value.toDoubleOrNull() ?: return false
        return (min == null || numValue >= min) && (max == null || numValue <= max)
    }
    
    override fun getDefaultMessage(): String {
        return when {
            min != null && max != null -> "值必须在 $min 到 $max 之间"
            min != null -> "值必须大于等于 $min"
            max != null -> "值必须小于等于 $max"
            else -> "无效的数值"
        }
    }
}

/**
 * 长度校验
 * 示例: {"type": "length", "minLength": 2, "maxLength": 50}
 */
data class LengthValidationRule(
    val minLength: Int? = null,
    val maxLength: Int? = null
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        val length = value.length
        return (minLength == null || length >= minLength) && 
               (maxLength == null || length <= maxLength)
    }
    
    override fun getDefaultMessage(): String {
        return when {
            minLength != null && maxLength != null -> "长度必须在 $minLength 到 $maxLength 之符之间"
            minLength != null -> "长度必须大于等于 $minLength"
            maxLength != null -> "长度必须小于等于 $maxLength"
            else -> "长度不符合要求"
        }
    }
}

/**
 * 枚举值校验
 * 示例: {"type": "enum", "values": ["active", "inactive", "pending"]}
 */
data class EnumValidationRule(
    val values: List<String>
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        return value in values
    }
    
    override fun getDefaultMessage(): String {
        return "值必须是以下之一: ${values.joinToString(", ")}"
    }
}

/**
 * 数字校验（整数或小数）
 * 示例: {"type": "number", "integerOnly": true, "positive": true}
 */
data class NumberValidationRule(
    val integerOnly: Boolean = false,
    val positive: Boolean = false,
    val negative: Boolean = false
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        val numValue = if (integerOnly) {
            value.toLongOrNull()?.toDouble()
        } else {
            value.toDoubleOrNull()
        } ?: return false
        
        return when {
            positive -> numValue > 0
            negative -> numValue < 0
            else -> true
        }
    }
    
    override fun getDefaultMessage(): String {
        return when {
            integerOnly && positive -> "必须是正整数"
            integerOnly && negative -> "必须是负整数"
            integerOnly -> "必须是整数"
            positive -> "必须是正数"
            negative -> "必须是负数"
            else -> "必须是有效数字"
        }
    }
}

/**
 * 日期范围校验
 * 示例: {"type": "dateRange", "format": "yyyy-MM-dd", "minDate": "2020-01-01", "maxDate": "2030-12-31"}
 */
data class DateRangeValidationRule(
    val format: String = "yyyy-MM-dd",
    val minDate: String? = null,
    val maxDate: String? = null
) : ValidationRule() {
    override fun validate(value: String): Boolean {
        return try {
            val dateFormat = java.text.SimpleDateFormat(format)
            dateFormat.isLenient = false
            val date = dateFormat.parse(value)
            
            val minDateObj = minDate?.let { dateFormat.parse(it) }
            val maxDateObj = maxDate?.let { dateFormat.parse(it) }
            
            (minDateObj == null || !date.before(minDateObj)) && 
            (maxDateObj == null || !date.after(maxDateObj))
        } catch (e: Exception) {
            false
        }
    }
    
    override fun getDefaultMessage(): String {
        return "日期格式必须为 $format" + when {
            minDate != null && maxDate != null -> "，且在 $minDate 到 $maxDate 之间"
            minDate != null -> "，且不早于 $minDate"
            maxDate != null -> "，且不晚于 $maxDate"
            else -> ""
        }
    }
}

/**
 * 邮箱校验
 * 示例: {"type": "email"}
 */
class EmailValidationRule : ValidationRule() {
    override fun validate(value: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return value.matches(Regex(emailRegex))
    }
    
    override fun getDefaultMessage(): String {
        return "必须是有效的邮箱地址"
    }
}

/**
 * 手机号校验（支持中国大陆）
 * 示例: {"type": "phone"}
 */
class PhoneValidationRule : ValidationRule() {
    override fun validate(value: String): Boolean {
        val phoneRegex = "^1[3-9]\\d{9}$"
        return value.matches(Regex(phoneRegex))
    }
    
    override fun getDefaultMessage(): String {
        return "必须是有效的手机号码"
    }
}

/**
 * URL 校验
 * 示例: {"type": "url"}
 */
class UrlValidationRule : ValidationRule() {
    override fun validate(value: String): Boolean {
        val urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
        return value.matches(Regex(urlRegex))
    }
    
    override fun getDefaultMessage(): String {
        return "必须是有效的URL地址"
    }
}



