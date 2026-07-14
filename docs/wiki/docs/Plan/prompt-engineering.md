# Prompt Engineering and Guardrails

[← Back to README](README.md) · [RAG pipeline](rag-pipeline.md)

## Principle behind assembling the final prompt

The prompt is always assembled from three strictly separated parts — the
model must not infer facts that aren't present in the retrieved content.

```
┌───────────────────────────────────────────┐
│ SYSTEM: role + hard scope constraints       │
├───────────────────────────────────────────┤
│ CONTEXT: retrieved knowledge_snippet(s)     │
├───────────────────────────────────────────┤
│ USER STATE: filled slots (order_id...)      │
├───────────────────────────────────────────┤
│ USER MESSAGE: normalized query              │
└───────────────────────────────────────────┘
```

## Example of an assembled prompt

```
[SYSTEM]
You are a support assistant. Answer ONLY based on the text in the CONTEXT
block. Do not invent facts. Do not go beyond the "refund_status" topic.
If there isn't enough information, say that clarification from a human
agent is needed.

[CONTEXT]
Refunds are processed within 3-5 business days from the moment the request
is confirmed.

[USER STATE]
order_id: 48213

[USER MESSAGE]
Refund status for an order
```

## Input-side guardrails

- **Explicit scope instruction** — the system prompt always explicitly
  names the topic and forbids going beyond it (see the example above).
- **Whitelist, not blacklist** — it's easier to guarantee safety by
  allowing a specific topic than by enumerating everything forbidden.
- **Fixed enum for intent classification** — the LLM cannot "invent" a new
  intent, only pick from a given list (see [intent-matching.md](intent-matching.md)).

## Output-side guardrails

An additional lightweight pass after generation, before sending to the user:

```
answer + context → LLM (or NLI classifier) →
   "is the answer grounded in the context: yes/no"
```

If "no" — the answer is not sent; a fallback escalation to a human agent
occurs instead of showing a potentially hallucinated response.

## Handling "sensitive" messages

If the normalized query looks like a financial/legal/medical consultation
rather than a fact from the knowledge base, that topic should be excluded
from the allowed set from the start (not marked `allowed: true` in the
Vector Store), rather than filtered after the fact by the prompt.

## Related documents

- [Intent matching and slot filling](intent-matching.md)
- [Vector Store schema](../data/vector-store-schema.md)
