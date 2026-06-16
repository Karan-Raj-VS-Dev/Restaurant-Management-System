import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    globals: true,
    setupFiles: ["./vitest.setup.ts"],
    include: [
      "packages/**/*.test.tsx",
      "apps/**/*.test.tsx"
    ],
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "lcov"],
      reportsDirectory: "./coverage",
      include: [
        "packages/ui/src/components.tsx",
        "apps/pos-ui/src/components/OrderComposer.tsx",
        "apps/kitchen-ui/src/components/TicketLane.tsx"
      ]
    }
  }
});
