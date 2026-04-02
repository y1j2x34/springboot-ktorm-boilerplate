/// <reference types="@solidjs/start/env" />

declare const process: NodeJS.Process & {
    env: {
        NODE_ENV: 'production' | 'development'
        PROD: boolean
        DEV: boolean
    }
}
