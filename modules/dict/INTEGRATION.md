# 如何在应用中集成字典模块

本文档说明如何在现有应用中集成和使用字典模块。

## 前提条件

- ✅ 字典模块已在 `settings.gradle.kts` 中注册
- ✅ 数据库表已创建（运行 `database/init.sql`）
- ✅ 字典模块已成功编译

## 集成步骤

### 1. 添加模块依赖

在你的应用模块（如 `app/build.gradle.kts`）中添加字典模块依赖：

```kotlin
dependencies {
    // 其他依赖...
    
    // 添加字典模块
    implementation(project(":dict:dict-core"))
    
    // 如果只需要使用接口和模型（作为库使用）
    // implementation(project(":dict:dict-api"))
}
```

### 2. 确保数据库初始化

确保应用启动前已执行数据库初始化脚本：

```bash
# 如果使用 Docker Compose
cd database
docker-compose up -d

# 或手动执行 SQL
mysql -u root -p < database/init.sql
```

### 3. 在代码中使用字典服务

#### 方式一：通过依赖注入使用（推荐）

```kotlin
import com.vgerbot.dict.service.DictDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class YourBusinessService {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    fun yourBusinessMethod() {
        // 使用字典数据
        val statusList = dictDataService.getActiveDictDataByCode("user_status")
        // 业务逻辑...
    }
}
```

#### 方式二：通过 REST API 使用

前端或其他服务可以直接调用 REST API：

```javascript
// JavaScript/TypeScript 示例
const response = await fetch('/dict/data/code/user_status?activeOnly=true');
const statusList = await response.json();
```

### 4. 应用启动配置

如果你的应用使用了组件扫描，确保扫描到字典模块的包：

```kotlin
@SpringBootApplication(scanBasePackages = [
    "com.vgerbot.app",
    "com.vgerbot.dict",  // 添加这行
    // 其他包...
])
class YourApplication

fun main(args: Array<String>) {
    runApplication<YourApplication>(*args)
}
```

### 5. 验证集成

启动应用后，访问以下 URL 验证字典模块是否正常工作：

```bash
# 查询字典类型
curl http://localhost:8080/dict/types

# 查询字典数据
curl http://localhost:8080/dict/data/code/user_status
```

如果返回数据，说明集成成功！

## 常见集成场景

### 场景 1：用户管理模块集成

```kotlin
// user-core/src/main/kotlin/.../service/UserServiceImpl.kt

import com.vgerbot.dict.service.DictDataService

@Service
class UserServiceImpl : UserService {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    fun validateUserStatus(status: String): Boolean {
        // 验证状态值是否有效
        val statusData = dictDataService.getDictDataByCodeAndValue("user_status", status)
        return statusData != null && statusData.status
    }
    
    fun getUserStatusOptions(): List<Map<String, String>> {
        // 获取用户状态选项供前端使用
        return dictDataService.getActiveDictDataByCode("user_status")
            .map { mapOf("value" to it.dataValue, "label" to it.dataLabel) }
    }
}
```

### 场景 2：前端表单集成

```vue
<!-- UserForm.vue -->
<template>
  <el-form>
    <el-form-item label="性别">
      <el-select v-model="form.gender">
        <el-option
          v-for="item in genderOptions"
          :key="item.dataValue"
          :label="item.dataLabel"
          :value="item.dataValue"
        />
      </el-select>
    </el-form-item>
    
    <el-form-item label="地区">
      <el-cascader
        v-model="form.region"
        :options="regionTree"
        :props="{ value: 'dataValue', label: 'dataLabel', children: 'children' }"
      />
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, onMounted } from 'vue';

const genderOptions = ref([]);
const regionTree = ref([]);

onMounted(async () => {
  // 加载性别字典
  const genderRes = await fetch('/dict/data/code/gender?activeOnly=true');
  genderOptions.value = await genderRes.json();
  
  // 加载地区树形字典
  const regionRes = await fetch('/dict/data/code/region/tree');
  regionTree.value = await regionRes.json();
});
</script>
```

### 场景 3：数据转换工具类

```kotlin
import com.vgerbot.dict.service.DictDataService
import org.springframework.stereotype.Component

@Component
class DictConverter {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    /**
     * 将字典值转换为标签（用于数据展示）
     */
    fun valueToLabel(dictCode: String, value: String): String {
        return dictDataService.getDictDataByCodeAndValue(dictCode, value)?.dataLabel ?: value
    }
    
    /**
     * 批量转换
     */
    fun valuesToLabels(dictCode: String, values: List<String>): List<String> {
        return values.map { valueToLabel(dictCode, it) }
    }
    
    /**
     * 获取字典选项（用于下拉框）
     */
    fun getOptions(dictCode: String): List<Map<String, Any>> {
        return dictDataService.getActiveDictDataByCode(dictCode)
            .map { mapOf(
                "value" to it.dataValue,
                "label" to it.dataLabel,
                "extra" to it.remark
            )}
    }
}
```

## 性能优化建议

### 1. 添加缓存层

```kotlin
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CachedDictService {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    @Cacheable(value = ["dictData"], key = "#dictCode")
    fun getCachedDictData(dictCode: String): List<DictData> {
        return dictDataService.getActiveDictDataByCode(dictCode)
    }
}

// application.yml 配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

### 2. 前端字典预加载

```javascript
// 应用启动时预加载常用字典
const COMMON_DICTS = ['gender', 'user_status', 'order_status'];

export const preloadDicts = async () => {
  const promises = COMMON_DICTS.map(async (code) => {
    const res = await fetch(`/dict/data/code/${code}?activeOnly=true`);
    const data = await res.json();
    localStorage.setItem(`dict_${code}`, JSON.stringify(data));
  });
  
  await Promise.all(promises);
};
```

## 数据库配置

确保你的应用配置中包含正确的数据库连接：

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/spring-boot-kt?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 权限控制（可选）

如果需要限制字典管理权限，可以集成 RBAC 模块：

```kotlin
import com.vgerbot.common.annotation.RequirePermission

@RestController
@RequestMapping("/dict/types")
class DictTypeController {
    
    @RequirePermission("dict:create")
    @PostMapping
    fun createDictType(@RequestBody dto: CreateDictTypeDto): ResponseEntity<Any> {
        // ...
    }
    
    @RequirePermission("dict:update")
    @PutMapping("/{id}")
    fun updateDictType(@PathVariable id: Long, @RequestBody dto: UpdateDictTypeDto): ResponseEntity<Any> {
        // ...
    }
    
    @RequirePermission("dict:delete")
    @DeleteMapping("/{id}")
    fun deleteDictType(@PathVariable id: Long): ResponseEntity<Any> {
        // ...
    }
    
    // 查询接口通常不需要权限限制
    @GetMapping
    fun getAllDictTypes(): ResponseEntity<List<DictType>> {
        // ...
    }
}
```

## 故障排查

### 问题 1：找不到 DictDataService bean

**原因**: 应用没有扫描到字典模块的包

**解决**: 在 `@SpringBootApplication` 注解中添加 `scanBasePackages`

### 问题 2：数据库表不存在

**原因**: 数据库初始化脚本未执行

**解决**: 手动执行 `database/init.sql`

### 问题 3：API 返回 404

**原因**: dict-core 模块未被应用依赖

**解决**: 在应用的 `build.gradle.kts` 中添加 `implementation(project(":dict:dict-core"))`

### 问题 4：外键约束错误

**原因**: 旧版本的 SQL 脚本可能引用了错误的表名

**解决**: 确保使用最新的 `init.sql`（外键应引用 `dict_type` 而非 `sys_dict_type`）

## 下一步

- 阅读 [README.md](./README.md) 了解更多功能
- 查看 [API-GUIDE.md](./API-GUIDE.md) 学习 API 使用
- 参考 [DictUsageExample.kt](./dict-core/src/main/kotlin/com/vgerbot/dict/example/DictUsageExample.kt) 查看示例代码

## 技术支持

如果遇到问题：
1. 检查应用日志
2. 验证数据库连接
3. 确认模块依赖配置
4. 查看常见问题文档

