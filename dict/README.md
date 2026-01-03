# 字典模块 (Dict Module)

字典模块提供了系统字典数据管理功能，支持简单键值对和树形结构的字典数据。

## 模块结构

```
dict/
├── dict-api/          # API 接口层
│   ├── model/         # 实体模型
│   ├── dto/           # 数据传输对象
│   ├── dao/           # DAO 接口
│   └── service/       # 服务接口
└── dict-core/         # 核心实现层
    ├── dao/           # DAO 实现
    ├── service/       # 服务实现
    └── controller/    # REST 控制器
```

## 核心功能

### 1. 字典类型管理 (DictType)

字典类型定义了字典的基本信息和规则：

- **dictCode**: 字典编码（唯一标识）
- **dictName**: 字典名称
- **dictCategory**: 字典分类
- **valueType**: 值类型（STRING、INTEGER、DECIMAL、DATE、BOOLEAN）
- **validationRule**: 值校验规则（JSON 格式）
- **isTree**: 是否树形结构
- **status**: 启用/停用状态

### 2. 字典数据管理 (DictData)

字典数据是字典类型下的具体值：

- **dictCode**: 字典编码（关联字典类型）
- **dataValue**: 字典值（实际存储的值）
- **dataLabel**: 字典标签（显示文本）
- **parentId**: 父级ID（树形结构使用）
- **level**: 层级深度
- **isDefault**: 是否默认值
- **status**: 启用/停用状态
- **sortOrder**: 排序顺序

## API 接口

### 字典类型接口

#### 创建字典类型
```http
POST /dict/types
Content-Type: application/json

{
  "dictCode": "user_status",
  "dictName": "用户状态",
  "dictCategory": "system",
  "valueType": "STRING",
  "isTree": false,
  "description": "用户账号状态定义",
  "status": true,
  "sortOrder": 1
}
```

#### 更新字典类型
```http
PUT /dict/types/{id}
Content-Type: application/json

{
  "dictName": "用户状态（更新）",
  "description": "更新后的描述"
}
```

#### 删除字典类型
```http
DELETE /dict/types/{id}
```

#### 查询字典类型
```http
# 根据 ID 查询
GET /dict/types/{id}

# 根据编码查询
GET /dict/types/code/{code}

# 查询所有
GET /dict/types

# 根据分类查询
GET /dict/types?category=system

# 根据状态查询
GET /dict/types?status=true
```

### 字典数据接口

#### 创建字典数据
```http
POST /dict/data
Content-Type: application/json

{
  "dictTypeId": 1,
  "dictCode": "user_status",
  "dataValue": "active",
  "dataLabel": "正常",
  "parentId": 0,
  "level": 1,
  "isDefault": true,
  "status": true,
  "sortOrder": 1
}
```

#### 更新字典数据
```http
PUT /dict/data/{id}
Content-Type: application/json

{
  "dataLabel": "激活",
  "sortOrder": 2
}
```

#### 删除字典数据
```http
DELETE /dict/data/{id}
```

#### 查询字典数据
```http
# 根据 ID 查询
GET /dict/data/{id}

# 根据字典编码查询所有数据
GET /dict/data/code/{code}

# 根据字典编码查询启用的数据
GET /dict/data/code/{code}?activeOnly=true

# 根据字典编码查询树形结构数据
GET /dict/data/code/{code}/tree

# 根据字典编码和父ID查询
GET /dict/data/code/{code}/parent/{parentId}

# 根据字典类型ID查询
GET /dict/data/type/{typeId}

# 根据字典编码和值查询
GET /dict/data/code/{code}/value/{value}

# 获取默认值
GET /dict/data/code/{code}/default
```

## 使用示例

### 1. 简单字典使用

```kotlin
// 1. 创建字典类型
val createTypeDto = CreateDictTypeDto(
    dictCode = "gender",
    dictName = "性别",
    dictCategory = "common",
    valueType = "STRING",
    isTree = false,
    status = true
)
dictTypeService.createDictType(createTypeDto)

// 2. 添加字典数据
val createDataDto = CreateDictDataDto(
    dictTypeId = 1,
    dictCode = "gender",
    dataValue = "male",
    dataLabel = "男",
    isDefault = false,
    status = true,
    sortOrder = 1
)
dictDataService.createDictData(createDataDto)

// 3. 查询字典数据
val genderList = dictDataService.getActiveDictDataByCode("gender")
```

### 2. 树形字典使用

```kotlin
// 1. 创建树形字典类型
val regionTypeDto = CreateDictTypeDto(
    dictCode = "region",
    dictName = "地区",
    dictCategory = "common",
    valueType = "STRING",
    isTree = true,  // 启用树形结构
    status = true
)
dictTypeService.createDictType(regionTypeDto)

// 2. 添加省级数据
val provinceDto = CreateDictDataDto(
    dictTypeId = 2,
    dictCode = "region",
    dataValue = "440000",
    dataLabel = "广东省",
    parentId = 0,  // 根节点
    level = 1,
    status = true
)
dictDataService.createDictData(provinceDto)

// 3. 添加市级数据
val cityDto = CreateDictDataDto(
    dictTypeId = 2,
    dictCode = "region",
    dataValue = "440100",
    dataLabel = "广州市",
    parentId = provinceId,  // 父节点ID
    level = 2,
    status = true
)
dictDataService.createDictData(cityDto)

// 4. 查询树形结构
val regionTree = dictDataService.getDictDataTreeByCode("region")
```

### 3. 在业务中使用

```kotlin
// 获取用户状态字典
val statusList = dictDataService.getActiveDictDataByCode("user_status")

// 获取默认状态
val defaultStatus = dictDataService.getDefaultDictDataByCode("user_status")

// 根据值获取标签
val status = dictDataService.getDictDataByCodeAndValue("user_status", "active")
println("状态标签: ${status?.dataLabel}")  // 输出: 状态标签: 正常
```

## 数据库表结构

### dict_type 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| dict_code | VARCHAR(100) | 字典编码（唯一） |
| dict_name | VARCHAR(200) | 字典名称 |
| dict_category | VARCHAR(100) | 字典分类 |
| value_type | VARCHAR(50) | 值类型 |
| validation_rule | VARCHAR(500) | 校验规则 |
| validation_message | VARCHAR(500) | 校验失败提示 |
| is_tree | TINYINT(1) | 是否树形结构 |
| description | TEXT | 描述 |
| status | TINYINT(1) | 状态 |
| sort_order | INT | 排序 |
| created_by | INT | 创建人 |
| created_time | DATETIME | 创建时间 |
| updated_by | INT | 更新人 |
| updated_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

### dict_data 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键ID |
| dict_type_id | BIGINT | 字典类型ID |
| dict_code | VARCHAR(100) | 字典编码 |
| data_value | VARCHAR(500) | 字典值 |
| data_label | VARCHAR(500) | 字典标签 |
| parent_id | BIGINT | 父级ID |
| level | INT | 层级 |
| is_default | TINYINT(1) | 是否默认 |
| status | TINYINT(1) | 状态 |
| sort_order | INT | 排序 |
| created_by | INT | 创建人 |
| created_time | DATETIME | 创建时间 |
| updated_by | INT | 更新人 |
| updated_time | DATETIME | 更新时间 |
| remark | VARCHAR(500) | 备注 |

## 初始化数据

数据库已预置以下示例字典：

1. **user_status**: 用户状态（正常、停用、锁定、已删除）
2. **gender**: 性别（男、女、未知）
3. **region**: 地区（北京市、上海市、广东省及下级城市）
4. **order_status**: 订单状态（待支付、已支付、已发货、已完成、已取消）

## 注意事项

1. **唯一性约束**: 同一字典编码下，字典值必须唯一
2. **级联删除**: 删除字典类型时，会级联删除其下所有字典数据
3. **树形结构**: 
   - `parentId = 0` 表示根节点
   - `level` 从 1 开始，表示层级深度
   - 支持无限层级
4. **默认值**: 同一字典编码下，建议只设置一个默认值
5. **状态控制**: 停用的字典数据不会在 `getActiveDictDataByCode` 中返回

## 扩展功能建议

1. **缓存支持**: 可以添加 Redis 缓存来提升查询性能
2. **国际化**: 支持多语言字典标签
3. **版本控制**: 记录字典数据的变更历史
4. **权限控制**: 集成 RBAC 模块，控制字典的管理权限
5. **数据校验**: 根据 `validationRule` 实现值的自动校验
6. **导入导出**: 支持字典数据的批量导入和导出

## 依赖模块

- **core**: 基础 DAO 和工具类
- **Spring Boot Web**: REST API 支持
- **Ktorm**: 数据库 ORM 框架

