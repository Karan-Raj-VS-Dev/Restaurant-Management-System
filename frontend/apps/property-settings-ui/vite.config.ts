import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import { createOperationalProxy } from "../../operational-proxy";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5179,
    proxy: createOperationalProxy(["auth", "property", "employee", "table", "catalog", "inventory", "billing"])
  }
});
