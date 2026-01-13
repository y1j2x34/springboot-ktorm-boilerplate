rootProject.name = "sprintboot-kt"

// Application entry point
include("app")

// Infrastructure layer
include("infrastructure")
include("infrastructure:infrastructure-core")
include("infrastructure:infrastructure-common")
include("infrastructure:infrastructure-security")
include("infrastructure:infrastructure-redis")

// Modules layer
include("modules")

// Auth modules
include("modules:auth")
include("modules:auth:auth-jwt")
include("modules:auth:auth-oauth")
include("modules:auth:auth-wechat")
include("modules:auth:auth-oauth2-server")

// Business modules
include("modules:user")
include("modules:user:user-api")
include("modules:user:user-core")

include("modules:tenant")
include("modules:tenant:tenant-api")
include("modules:tenant:tenant-core")

include("modules:authorization")
include("modules:authorization:authorization-api")
include("modules:authorization:authorization-core")

include("modules:dict")
include("modules:dict:dict-api")
include("modules:dict:dict-core")

include("modules:captcha")

include("modules:postgrest-query")
include("modules:postgrest-query:postgrest-query-api")
include("modules:postgrest-query:postgrest-query-core")

include("modules:dynamic-table")
include("modules:dynamic-table:dynamic-table-api")
include("modules:dynamic-table:dynamic-table-core")

include("modules:scheduler")
include("modules:scheduler:scheduler-api")
include("modules:scheduler:scheduler-core")

include("modules:notification")
include("modules:notification:notification-api")
include("modules:notification:notification-core")

include("modules:async-task")
include("modules:async-task:async-task-api")
include("modules:async-task:async-task-core")
