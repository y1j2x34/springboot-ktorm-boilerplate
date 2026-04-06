import { defineConfig } from "vite";
import { nitroV2Plugin as nitro } from "@solidjs/vite-plugin-nitro-2";
import { solidStart } from "@solidjs/start/config";
import tailwindcss from "@tailwindcss/vite";


export default defineConfig(({ mode }) => {
  return {
    plugins: [
      tailwindcss({
        
      }),
      solidStart({
        ssr: false
      }),
      nitro()
    ],
    environments: {
      
    },
    appType: 'mpa',
    devtools: true,
    server: {
      proxy: {
        "/oauth2": {
          target: "http://localhost:8082",
          changeOrigin: false,
          xfwd: true
        },
        "/login/oauth2": {
          target: "http://localhost:8082",
          changeOrigin: false,
          xfwd: true,
        },
        "/api": {
          target: "http://localhost:8082",
          changeOrigin: false,
          rewrite: (path) => path.replace(/^\/api/, ""),
        }
      }
    }
  }
});