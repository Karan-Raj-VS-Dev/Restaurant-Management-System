import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { createOperationalProxy } from "../../operational-proxy";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5180,
    proxy: createOperationalProxy(["auth", "insights", "order", "billing", "employee"])
  }
});
