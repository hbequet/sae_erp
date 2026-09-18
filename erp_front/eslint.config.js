import js from "@eslint/js";
import globals from "globals";
import pluginVue from "eslint-plugin-vue";
import json from "@eslint/json";
import css from "@eslint/css";
import { defineConfig } from "eslint/config";

export default defineConfig([
  {
    ignores: ["**/node_modules/**", "**/dist/**", "**/*.md"]
  },

  {
    files: ["**/*.{js,mjs,cjs,vue}"],
    plugins: {
      js
    },
    languageOptions: {
      globals: globals.browser
    },
    rules: {
      ...js.configs.recommended.rules
    }
  },

  ...pluginVue.configs["flat/essential"].map((config) => ({
    ...config,
    files: ["**/*.{vue,js,mjs,cjs}"]
  })),

  {
    files: ["**/*.json"],
    plugins: { json },
    language: "json/json",
    extends: ["json/recommended"]
  },
  {
    files: ["**/*.jsonc"],
    plugins: { json },
    language: "json/jsonc",
    extends: ["json/recommended"]
  },
  {
    files: ["**/*.json5"],
    plugins: { json },
    language: "json/json5",
    extends: ["json/recommended"]
  },

  {
    files: ["**/*.css"],
    plugins: { css },
    language: "css/css",
    extends: ["css/recommended"]
  }
]);