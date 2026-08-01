#!/usr/bin/env node
// Validates the knowledge-base intent files without booting the application, so a content-only PR
// gets a fast gate instead of a full Gradle build. The rules mirror what the runtime expects from
// IntentDefinition / IntentDefinitionRegistry, so a file that passes here loads at startup.
//
//   node scripts/validate-intents.mjs [intentsDir]

import { readFileSync, readdirSync } from "node:fs";
import { basename, join, resolve } from "node:path";

const DEFAULT_DIR = "modules/server/src/main/resources/knowledge-base/intents";

// snake_case here because IntentDefinition is annotated with SnakeCaseStrategy.
const STRING_FIELDS = [
  "intent_id",
  "knowledge_snippet",
  "system_instruction",
  "answer_template",
  "escalation_fallback",
];
const ARRAY_FIELDS = ["canonical_questions", "required_slots"];
const BOOLEAN_FIELDS = ["allowed"];
const KNOWN_FIELDS = new Set([...STRING_FIELDS, ...ARRAY_FIELDS, ...BOOLEAN_FIELDS]);

const errors = [];
const seenIntentIds = new Map();
const seenQuestions = new Map();

function fail(file, message) {
  errors.push(`${file}: ${message}`);
}

function validate(file, intent) {
  for (const field of STRING_FIELDS) {
    if (typeof intent[field] !== "string" || intent[field].trim() === "") {
      fail(file, `"${field}" is required and must be a non-empty string`);
    }
  }
  for (const field of ARRAY_FIELDS) {
    if (!Array.isArray(intent[field])) {
      fail(file, `"${field}" is required and must be an array`);
    }
  }
  for (const field of BOOLEAN_FIELDS) {
    if (typeof intent[field] !== "boolean") {
      fail(file, `"${field}" is required and must be a boolean`);
    }
  }
  for (const field of Object.keys(intent)) {
    if (!KNOWN_FIELDS.has(field)) {
      fail(file, `unknown field "${field}" — the runtime record would ignore it`);
    }
  }

  if (Array.isArray(intent.canonical_questions) && intent.canonical_questions.length === 0) {
    fail(file, `"canonical_questions" must list at least one phrasing`);
  }

  const intentId = intent.intent_id;
  if (typeof intentId === "string") {
    const stem = basename(file, ".json");
    if (intentId !== stem) {
      fail(file, `"intent_id" is "${intentId}" but the filename says "${stem}"`);
    }
    const duplicate = seenIntentIds.get(intentId);
    if (duplicate) {
      // The registry collects intents into a map keyed by intent_id, so a duplicate would make
      // startup fail with an IllegalStateException rather than silently overwrite.
      fail(file, `duplicate "intent_id" — already declared in ${duplicate}`);
    } else {
      seenIntentIds.set(intentId, file);
    }
  }

  for (const question of intent.canonical_questions ?? []) {
    if (typeof question !== "string" || question.trim() === "") {
      fail(file, `"canonical_questions" contains an empty entry`);
      continue;
    }
    const key = question.trim().toLowerCase();
    const duplicate = seenQuestions.get(key);
    if (duplicate && duplicate !== file) {
      // Two intents claiming the same phrasing make retrieval a coin flip.
      fail(file, `canonical question "${question}" is also claimed by ${duplicate}`);
    } else {
      seenQuestions.set(key, file);
    }
  }

  if (typeof intent.answer_template === "string" && Array.isArray(intent.required_slots)) {
    const placeholders = [...intent.answer_template.matchAll(/\{(\w+)\}/g)].map((m) => m[1]);
    for (const placeholder of placeholders) {
      if (!intent.required_slots.includes(placeholder)) {
        fail(
          file,
          `"answer_template" uses {${placeholder}} but it is not listed in "required_slots"`,
        );
      }
    }
  }
}

const dir = resolve(process.argv[2] ?? DEFAULT_DIR);
let files;
try {
  files = readdirSync(dir)
    .filter((name) => name.endsWith(".json"))
    .sort();
} catch (error) {
  console.error(`Cannot read intents directory ${dir}: ${error.message}`);
  process.exit(1);
}

if (files.length === 0) {
  console.error(`No intent files found in ${dir}`);
  process.exit(1);
}

for (const name of files) {
  const path = join(dir, name);
  let intent;
  try {
    intent = JSON.parse(readFileSync(path, "utf8"));
  } catch (error) {
    fail(name, `invalid JSON — ${error.message}`);
    continue;
  }
  validate(name, intent);
}

if (errors.length > 0) {
  console.error(`Knowledge base validation failed (${errors.length} problem(s)):`);
  for (const error of errors) {
    console.error(`  - ${error}`);
  }
  process.exit(1);
}

console.log(`Knowledge base OK — ${files.length} intent(s) validated in ${dir}`);
