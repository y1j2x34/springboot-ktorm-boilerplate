# 字典模块文档索引

欢迎使用字典模块！以下是文档导航：

## 📚 文档列表

### 1. [README.md](./README.md) - **从这里开始** ⭐
- 模块概览
- 核心功能介绍
- 基本使用示例
- 数据库表结构
- 初始化数据说明

### 2. [API-GUIDE.md](./API-GUIDE.md) - **API 详细指南** 📖
- 完整的 REST API 调用示例
- Kotlin/Java 代码集成示例
- Vue.js/React 前端集成示例
- 高级用法和最佳实践
- 性能优化建议
- 常见问题解答

### 3. [SUMMARY.md](./SUMMARY.md) - **开发总结** 📝
- 模块完成情况
- 技术架构说明
- 构建验证结果
- 后续扩展建议

### 4. [VALIDATION.md](./VALIDATION.md) - **数据校验功能** 🛡️
- 9种内置校验规则详解
- 完整使用流程和示例
- 实战案例（用户注册、商品信息等）
- 最佳实践和性能优化
- 自定义校验规则扩展

### 5. [INTEGRATION.md](./INTEGRATION.md) - **集成指南** 🔧
- 应用集成步骤
- 常见集成场景
- 性能优化建议
- 故障排查

## 🚀 快速开始

### 3 步完成字典集成

1. **添加依赖**
```kotlin
// 在你的模块 build.gradle.kts 中添加
dependencies {
    implementation(project(":dict:dict-core"))
}
```

2. **创建字典**
```bash
curl -X POST http://localhost:8080/api/dict/types \
  -H "Content-Type: application/json" \
  -d '{"dictCode": "gender", "dictName": "性别", "isTree": false}'
```

3. **使用字典**
```kotlin
@Autowired
lateinit var dictDataService: DictDataService

val genderList = dictDataService.getActiveDictDataByCode("gender")
```

## 📂 代码示例

- **完整示例代码**: [dict-core/src/main/kotlin/com/vgerbot/dict/example/DictUsageExample.kt](./dict-core/src/main/kotlin/com/vgerbot/dict/example/DictUsageExample.kt)
- **简单字典示例**: 见 README.md 的"使用示例 → 1. 简单字典使用"
- **树形字典示例**: 见 README.md 的"使用示例 → 2. 树形字典使用"

## 🔗 API 端点速查

### 字典类型
- `POST /api/dict/types` - 创建
- `GET /api/dict/types/code/{code}` - 查询
- `PUT /api/dict/types/{id}` - 更新
- `DELETE /api/dict/types/{id}` - 删除

### 字典数据
- `POST /api/dict/data` - 创建
- `GET /api/dict/data/code/{code}` - 查询列表
- `GET /api/dict/data/code/{code}/tree` - 查询树形结构
- `GET /api/dict/data/code/{code}/default` - 查询默认值
- `PUT /api/dict/data/{id}` - 更新
- `DELETE /api/dict/data/{id}` - 删除

详细说明请查看 [API-GUIDE.md](./API-GUIDE.md)

## 💡 使用场景

- ✅ 系统枚举管理（用户状态、订单状态等）
- ✅ 基础数据管理（性别、民族、学历等）
- ✅ 树形数据管理（地区、部门、分类等）
- ✅ 动态表单选项（下拉框、单选框数据源）
- ✅ 国际化标签管理

## 🛠️ 技术栈

- Kotlin 1.9.23
- Spring Boot
- Ktorm ORM
- MySQL 数据库
- RESTful API

## 📞 问题反馈

如果遇到问题或有建议，请：
1. 查看 [README.md](./README.md) 的"注意事项"部分
2. 查看 [API-GUIDE.md](./API-GUIDE.md) 的"常见问题"部分
3. 检查 [SUMMARY.md](./SUMMARY.md) 了解模块架构

## 🎯 下一步

根据你的需求选择：

- **初次使用**: 阅读 [README.md](./README.md)
- **API 集成**: 查看 [API-GUIDE.md](./API-GUIDE.md)
- **了解架构**: 阅读 [SUMMARY.md](./SUMMARY.md)
- **查看示例**: 运行 [DictUsageExample.kt](./dict-core/src/main/kotlin/com/vgerbot/dict/example/DictUsageExample.kt)

