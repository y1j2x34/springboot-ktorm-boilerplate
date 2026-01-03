# 字典模块 API 使用指南

## 快速开始

### 1. 添加依赖

在你的模块的 `build.gradle.kts` 中添加：

```kotlin
dependencies {
    implementation(project(":dict:dict-api"))      // 如果只需要使用接口
    implementation(project(":dict:dict-core"))     // 如果需要完整功能
}
```

### 2. 基本使用流程

```
1. 创建字典类型 (DictType)
   ↓
2. 为字典类型添加数据项 (DictData)
   ↓
3. 在业务中查询和使用字典数据
```

## REST API 调用示例

### 字典类型管理

#### 1. 创建字典类型

```bash
curl -X POST http://localhost:8080/dict/types \
  -H "Content-Type: application/json" \
  -d '{
    "dictCode": "user_status",
    "dictName": "用户状态",
    "dictCategory": "system",
    "valueType": "STRING",
    "isTree": false,
    "description": "用户账号状态定义",
    "status": true,
    "sortOrder": 1
  }'
```

**响应示例：**
```json
{
  "id": 1,
  "dictCode": "user_status",
  "dictName": "用户状态",
  "dictCategory": "system",
  "valueType": "STRING",
  "validationRule": null,
  "validationMessage": null,
  "isTree": false,
  "description": "用户账号状态定义",
  "status": true,
  "sortOrder": 1,
  "createdTime": "2024-01-01T10:00:00",
  "updatedTime": "2024-01-01T10:00:00"
}
```

#### 2. 查询所有字典类型

```bash
curl http://localhost:8080/dict/types
```

#### 3. 根据编码查询字典类型

```bash
curl http://localhost:8080/dict/types/code/user_status
```

#### 4. 按分类查询字典类型

```bash
curl http://localhost:8080/dict/types?category=system
```

#### 5. 按状态查询字典类型

```bash
curl http://localhost:8080/dict/types?status=true
```

#### 6. 更新字典类型

```bash
curl -X PUT http://localhost:8080/dict/types/1 \
  -H "Content-Type: application/json" \
  -d '{
    "dictName": "用户状态（已更新）",
    "description": "更新后的描述"
  }'
```

#### 7. 删除字典类型

```bash
curl -X DELETE http://localhost:8080/dict/types/1
```

### 字典数据管理

#### 1. 创建字典数据

```bash
curl -X POST http://localhost:8080/dict/data \
  -H "Content-Type: application/json" \
  -d '{
    "dictTypeId": 1,
    "dictCode": "user_status",
    "dataValue": "active",
    "dataLabel": "正常",
    "parentId": 0,
    "level": 1,
    "isDefault": true,
    "status": true,
    "sortOrder": 1
  }'
```

**响应示例：**
```json
{
  "id": 1,
  "dictTypeId": 1,
  "dictCode": "user_status",
  "dataValue": "active",
  "dataLabel": "正常",
  "parentId": 0,
  "level": 1,
  "isDefault": true,
  "status": true,
  "sortOrder": 1,
  "createdTime": "2024-01-01T10:05:00",
  "updatedTime": "2024-01-01T10:05:00"
}
```

#### 2. 根据字典编码查询数据

```bash
# 查询所有数据（包括停用的）
curl http://localhost:8080/dict/data/code/user_status

# 只查询启用的数据
curl http://localhost:8080/dict/data/code/user_status?activeOnly=true
```

**响应示例：**
```json
[
  {
    "id": 1,
    "dictTypeId": 1,
    "dictCode": "user_status",
    "dataValue": "active",
    "dataLabel": "正常",
    "parentId": 0,
    "level": 1,
    "isDefault": true,
    "status": true,
    "sortOrder": 1
  },
  {
    "id": 2,
    "dictTypeId": 1,
    "dictCode": "user_status",
    "dataValue": "inactive",
    "dataLabel": "停用",
    "parentId": 0,
    "level": 1,
    "isDefault": false,
    "status": true,
    "sortOrder": 2
  }
]
```

#### 3. 查询树形结构数据

```bash
curl http://localhost:8080/dict/data/code/region/tree
```

**响应示例（树形结构）：**
```json
[
  {
    "id": 1,
    "dataValue": "440000",
    "dataLabel": "广东省",
    "parentId": 0,
    "level": 1,
    "children": [
      {
        "id": 2,
        "dataValue": "440100",
        "dataLabel": "广州市",
        "parentId": 1,
        "level": 2,
        "children": []
      },
      {
        "id": 3,
        "dataValue": "440300",
        "dataLabel": "深圳市",
        "parentId": 1,
        "level": 2,
        "children": []
      }
    ]
  }
]
```

#### 4. 根据编码和值查询

```bash
curl http://localhost:8080/dict/data/code/user_status/value/active
```

#### 5. 获取默认值

```bash
curl http://localhost:8080/dict/data/code/user_status/default
```

#### 6. 根据父节点查询子节点

```bash
curl http://localhost:8080/dict/data/code/region/parent/1
```

#### 7. 更新字典数据

```bash
curl -X PUT http://localhost:8080/dict/data/1 \
  -H "Content-Type: application/json" \
  -d '{
    "dataLabel": "激活",
    "sortOrder": 10
  }'
```

#### 8. 删除字典数据

```bash
curl -X DELETE http://localhost:8080/dict/data/1
```

## 在代码中使用

### Kotlin 示例

```kotlin
import com.vgerbot.dict.service.DictDataService
import com.vgerbot.dict.service.DictTypeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    fun registerUser(username: String, gender: String) {
        // 验证性别值是否有效
        val genderData = dictDataService.getDictDataByCodeAndValue("gender", gender)
        
        if (genderData == null || !genderData.status) {
            throw IllegalArgumentException("无效的性别值: $gender")
        }
        
        // 继续用户注册逻辑...
        println("用户性别: ${genderData.dataLabel}")
    }
    
    fun getUserStatusOptions(): List<Map<String, String>> {
        // 获取所有可用的用户状态
        return dictDataService.getActiveDictDataByCode("user_status")
            .map { mapOf(
                "value" to it.dataValue,
                "label" to it.dataLabel
            )}
    }
}
```

### Java 示例

```java
import com.vgerbot.dict.service.DictDataService;
import com.vgerbot.dict.model.DictData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class OrderService {
    
    @Autowired
    private DictDataService dictDataService;
    
    public void updateOrderStatus(Long orderId, String status) {
        // 验证订单状态是否有效
        DictData statusData = dictDataService.getDictDataByCodeAndValue("order_status", status);
        
        if (statusData == null || !statusData.getStatus()) {
            throw new IllegalArgumentException("无效的订单状态: " + status);
        }
        
        // 更新订单状态...
        System.out.println("订单状态更新为: " + statusData.getDataLabel());
    }
    
    public List<Map<String, String>> getOrderStatusOptions() {
        return dictDataService.getActiveDictDataByCode("order_status")
            .stream()
            .map(data -> Map.of(
                "value", data.getDataValue(),
                "label", data.getDataLabel()
            ))
            .collect(Collectors.toList());
    }
}
```

## 前端集成示例

### Vue.js 示例

```javascript
// 字典服务
export const dictService = {
  // 获取字典数据
  async getDictData(dictCode) {
    const response = await fetch(`/dict/data/code/${dictCode}?activeOnly=true`);
    return await response.json();
  },
  
  // 获取树形字典数据
  async getDictTree(dictCode) {
    const response = await fetch(`/dict/data/code/${dictCode}/tree`);
    return await response.json();
  },
  
  // 获取字典标签
  async getDictLabel(dictCode, value) {
    const response = await fetch(`/dict/data/code/${dictCode}/value/${value}`);
    const data = await response.json();
    return data.dataLabel;
  }
};

// 在组件中使用
export default {
  data() {
    return {
      genderOptions: [],
      selectedGender: ''
    }
  },
  async mounted() {
    // 加载性别选项
    this.genderOptions = await dictService.getDictData('gender');
  },
  methods: {
    async submitForm() {
      // 提交前验证字典值
      const genderLabel = await dictService.getDictLabel('gender', this.selectedGender);
      console.log('选择的性别:', genderLabel);
    }
  }
}
```

### React 示例

```jsx
import { useState, useEffect } from 'react';

function UserForm() {
  const [genderOptions, setGenderOptions] = useState([]);
  const [selectedGender, setSelectedGender] = useState('');
  
  useEffect(() => {
    // 加载性别字典
    fetch('/dict/data/code/gender?activeOnly=true')
      .then(res => res.json())
      .then(data => setGenderOptions(data));
  }, []);
  
  return (
    <div>
      <select value={selectedGender} onChange={e => setSelectedGender(e.target.value)}>
        <option value="">请选择性别</option>
        {genderOptions.map(option => (
          <option key={option.dataValue} value={option.dataValue}>
            {option.dataLabel}
          </option>
        ))}
      </select>
    </div>
  );
}
```

## 高级用法

### 1. 树形级联选择

```kotlin
// 省市区三级联动
fun getCascadeRegions(provinceCode: String? = null, cityCode: String? = null): List<DictData> {
    return when {
        cityCode != null -> {
            // 查询区/县
            val city = dictDataService.getDictDataByCodeAndValue("region", cityCode)
            dictDataService.getDictDataByCodeAndParent("region", city?.id ?: 0)
        }
        provinceCode != null -> {
            // 查询市
            val province = dictDataService.getDictDataByCodeAndValue("region", provinceCode)
            dictDataService.getDictDataByCodeAndParent("region", province?.id ?: 0)
        }
        else -> {
            // 查询省
            dictDataService.getDictDataByCodeAndParent("region", 0)
        }
    }
}
```

### 2. 字典值转换器

```kotlin
// 批量转换字典值为标签
fun convertValuesToLabels(dictCode: String, values: List<String>): List<String> {
    return values.mapNotNull { value ->
        dictDataService.getDictDataByCodeAndValue(dictCode, value)?.dataLabel
    }
}

// 使用示例
val statusValues = listOf("active", "inactive", "locked")
val statusLabels = convertValuesToLabels("user_status", statusValues)
// 结果: ["正常", "停用", "锁定"]
```

### 3. 缓存优化

```kotlin
import org.springframework.cache.annotation.Cacheable

@Service
class CachedDictService {
    
    @Autowired
    lateinit var dictDataService: DictDataService
    
    @Cacheable("dictData", key = "#dictCode")
    fun getCachedDictData(dictCode: String): List<DictData> {
        return dictDataService.getActiveDictDataByCode(dictCode)
    }
}
```

## 常见问题

### Q1: 如何处理字典数据变更？

**A:** 建议使用"软删除"方式，即修改 `status` 字段为 false，而不是直接删除数据，以保持历史数据的完整性。

### Q2: 树形字典最多支持多少层级？

**A:** 理论上支持无限层级，但建议控制在 3-5 层以内，过深的层级会影响查询性能和用户体验。

### Q3: 如何确保字典值的唯一性？

**A:** 数据库已设置唯一约束 `uk_dict_code_value (dict_code, data_value)`，同一字典编码下不能有重复的值。

### Q4: 前端如何缓存字典数据？

**A:** 推荐使用 localStorage 或 sessionStorage 缓存常用字典，减少网络请求：

```javascript
// 字典缓存工具
const dictCache = {
  get(dictCode) {
    const cached = localStorage.getItem(`dict_${dictCode}`);
    if (cached) {
      const data = JSON.parse(cached);
      // 检查缓存是否过期（例如24小时）
      if (Date.now() - data.timestamp < 24 * 60 * 60 * 1000) {
        return data.value;
      }
    }
    return null;
  },
  
  set(dictCode, value) {
    localStorage.setItem(`dict_${dictCode}`, JSON.stringify({
      value: value,
      timestamp: Date.now()
    }));
  }
};
```

## 性能优化建议

1. **使用缓存**: 对频繁访问的字典数据启用缓存（Redis 或本地缓存）
2. **批量查询**: 避免在循环中单独查询字典，应批量获取
3. **索引优化**: 数据库已为常用查询字段创建索引
4. **前端缓存**: 前端应用启动时一次性加载常用字典数据
5. **按需加载**: 树形字典支持按父节点懒加载子节点

## 安全注意事项

1. **权限控制**: 建议集成 RBAC 模块，限制字典的增删改权限
2. **输入验证**: 创建字典数据时应验证 `dataValue` 的格式
3. **SQL 注入防护**: 使用 Ktorm 的参数化查询，避免 SQL 注入
4. **XSS 防护**: 前端显示字典标签时应进行 HTML 转义

