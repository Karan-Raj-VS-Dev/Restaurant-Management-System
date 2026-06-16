import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { createOperationalProxy } from "../../operational-proxy";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: createOperationalProxy(["auth", "customer", "employee", "property", "table", "catalog", "inventory", "order", "kitchen", "billing", "payment", "insights"])
  }
});
