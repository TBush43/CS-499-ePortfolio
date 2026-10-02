# CS 499 Computer Science ePortfolio

This repository contains Thomas Bush's CS 499 capstone ePortfolio. It documents the enhancement of the original **OnWeight** Android application into **OnWeight Coach**, a jiu-jitsu coaching prototype for fictional student profiles, session notes, attendance, goals, and follow-up decisions.

## Portfolio

The [ePortfolio site](https://tbush43.github.io/CS-499-ePortfolio/) is published from [`docs/`](docs/). The repository and site are public. No real student data belongs in this prototype because it has no coach sign-in or database encryption.

## Capstone enhancement areas

1. **Software engineering and design** - Replace the original screens with a coach workflow and separate activities, validation, and repository logic, supported by unit tests.
2. **Algorithms and data structures** - Rank students for coach review using hash maps and a bounded priority queue, with reasons and deterministic tests.
3. **Databases** - Persist students, session notes, attendance, and goals in related SQLite tables with foreign keys, indexes, parameterized lookups, and tests. Version 1 is the first persistent schema; a future schema change will require a migration.

## Repository structure

- `artifacts/original/` - Original CS 360 OnWeight source and archive
- `artifacts/enhanced/` - Software design enhancement
- `artifacts/algorithm/` - Algorithms and data structures enhancement
- `artifacts/database/` - Database enhancement
- `docs/` - GitHub Pages ePortfolio
- `planning/` - Module One plan and milestone narratives
- `submissions/` - Original-and-enhanced code packages
- `professional-self-assessment/` - Final professional self-assessment

## Current status

- Original CS 360 artifact preserved
- Code review video published
- Three enhancements and their narratives published for review
- Database enhancement awaiting instructor feedback and an on-device test pass
- Final professional self-assessment still to come
