package com.example.demo_chat.rag;

/** Status of a chat's RAG dialogue session (see {@code docs/wiki/Plan/dialogue-state.md}). */
public enum DialogueStatus {
  NEW,
  SLOT_FILLING,
  READY_TO_ANSWER,
  ANSWERED,
  OUT_OF_SCOPE,
  /** Intent matched and an answer was generated, but it failed the output-side guardrail. */
  ESCALATED
}
