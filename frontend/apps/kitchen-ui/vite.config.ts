import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { createOperationalProxy } from "../../operational-proxy";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5174,
    proxy: createOperationalProxy(["auth", "kitchen", "inventory", "insights"])
  }
});
