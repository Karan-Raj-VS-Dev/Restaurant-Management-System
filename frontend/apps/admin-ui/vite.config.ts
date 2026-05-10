import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

const proxy = {
  "/services/auth": {
    target: "http://localhost:9001",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/auth/, "")
  },
  "/services/property": {
    target: "http://localhost:9004",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/property/, "")
  },
  "/services/inventory": {
    target: "http://localhost:9007",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/inventory/, "")
  },
  "/services/insights": {
    target: "http://localhost:9017",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/insights/, "")
  },
  "/services/order": {
    target: "http://localhost:9008",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/order/, "")
  },
  "/services/billing": {
    target: "http://localhost:9010",
    changeOrigin: true,
    rewrite: (path: string) => path.replace(/^\/services\/billing/, "")
  }
};

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5175,
    proxy
  }
});
