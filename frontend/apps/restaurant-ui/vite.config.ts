import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { createOperationalProxy } from "../../operational-proxy";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5176,
    proxy: createOperationalProxy(["auth", "property", "table", "catalog", "employee", "inventory", "insights", "order", "billing"])
  }
});
