# AGENTS.md — Backpacked fork

Fork of MrCrayfish/Backpacked with an es_MX translation and Gravestones integration, for the
studio's events (Chillcraft 2: Minecraft 1.21.1 + Fabric, Java 21). Fork details (branches,
changes, build, pulling from upstream) are in [`FORK.md`](FORK.md). Read it before touching anything.

## Rules

1. **Work only on the `chillcraft/*` branches.** The `multiloader/*` branches are upstream copies.
2. **Minimal changes to the original code:** our own code goes in new files (`integration/`) plus a
   few registration lines. This keeps pulling from upstream cheap.
3. **Only the `fabric` subproject is built** (`TARGET_LOADER=fabric`).
4. **No secrets in the repo.** The GitHub Packages token goes in `~/.gradle/gradle.properties`
   (locally) or in GitHub Actions secrets (CI).
5. **LGPL 2.1 license:** the repo is public on purpose.
6. **Language:** everything committed to git is in English: code, comments, docs, commit messages,
   PR titles and descriptions. The only exception is the translation itself (`es_mx.json`).
   Conversation with the user is in Spanish.
7. When changing something documented, update `FORK.md` in the same commit.

## Git

- **Conventional Commits, single line**, in English: `feat: ...`, `fix(lang): ...`.
  **No body and no `Co-authored-by`.** Full conventions in the `CONTRIBUTING.md` of the
  organization's `.github` repo.
- PRs use `.github/pull_request_template.md`. Squash merge with the PR title as the message.
- Do not rewrite published history: pull upstream with merge, not rebase.

## Related projects

Sibling folders in `D:\modpacks\`: `chillcraft2` (the event; records which jar version the modpack
uses), `chillcoin`, `bossify`, `3d-models`, `.github`.

## About the user

Event organizer and senior JavaScript developer, with no prior experience in Java or modding:
explain where the Java/Gradle/Fabric execution model differs from Node/JS, without explaining basic
programming.
