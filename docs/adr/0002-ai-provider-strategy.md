# 2. AI provider strategy — Gemini REST client with a deterministic mock

- Status: accepted
- Date: 2026-10-22

## Context

The product uses Google Gemini for several workflows (resume assistance, job
description generation, candidate screening, cover letters, natural-language job
search). The preferred integration was Spring AI's Gemini support.

At project start, Spring AI has **no GA release compatible with Spring Boot 4**.
Spring AI 1.0 GA targets Spring Boot 3.3+, and the Boot-4-compatible Spring AI
2.0 line is still in milestones. Rule 1.5 of the project brief requires using a
documented alternative when a library has no Boot-4-compatible release.

A second requirement is that the whole system must run on a laptop **without any
API key**.

## Decision

Introduce an `AiClient` interface in `ai-service` with two implementations,
selected by configuration:

1. **`GeminiAiClient`** — calls the Gemini REST API directly using Spring's
   `RestClient` (`generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`).
   Prompts live in `src/main/resources/prompts/`; structured JSON responses are
   parsed into DTOs with one retry on malformed output; requests have timeouts.
   Active when `GEMINI_API_KEY` is set.
2. **`MockAiClient`** — a deterministic, realistic client that produces
   plausible output from the input (scores, summaries, skill lists, cover
   letters) with no network calls. Active when `GEMINI_API_KEY` is blank; logs a
   single startup warning. This is the default for local development and CI.

Selection is done with a Spring `@ConditionalOnProperty` / bean factory keyed on
the presence of `GEMINI_API_KEY`.

## Consequences

- No dependency on Spring AI; one less compatibility risk against Boot 4.
- Tests run fully offline against `MockAiClient`; a real key activates Gemini
  with no code change.
- If Spring AI ships a stable Boot-4 release later, migration is isolated to the
  `GeminiAiClient` implementation behind the `AiClient` interface.
