---
name: daily-report
description: Generate today's work-log entry summarizing code changes and save it to the Obsidian wiki's Daily notes folder
invocation: user
---

Create or update today's entry in the project's Obsidian wiki at `docs/wiki/Daily/<YYYY-MM-DD>.md`.

## Gathering what changed

- If this is a git repository, prefer `git log --since=midnight --oneline` and `git diff` against the
  start of today as the source of truth for what changed.
- If it isn't a git repo yet (or git shows nothing useful), fall back to summarizing the current
  conversation's session work instead.
- Don't just restate the diff mechanically — a diff/git log already shows *what* changed. Capture the
  *why*: decisions made, root causes of bugs, dead ends, and tradeoffs. Skip anything a reader could
  already infer from the code itself.

## Format

- Group entries by topic (e.g. infrastructure/tooling, data layer, API, docs), not flat chronological
  order — easier to scan later.
- Bullet points, not prose. Low ceremony — this only stays useful if it's cheap to write.
- Where an entry relates to an existing `docs/wiki/Features/*.md` or `docs/wiki/Infrastructure/**/*.md`
  page, reference it with a `[[wikilink]]`.
- Match the structure of prior entries in `docs/wiki/Daily/` if any exist, for consistency.

## Saving

- Write to `docs/wiki/Daily/<YYYY-MM-DD>.md` (today's date). If that file already exists, extend it
  rather than overwriting — ask the user if it's unclear whether new content should merge with or
  replace what's there.
- Ensure it's linked from `docs/wiki/index.md`'s Map of Content, under a `### Daily` section (create
  that section if it doesn't exist yet).
