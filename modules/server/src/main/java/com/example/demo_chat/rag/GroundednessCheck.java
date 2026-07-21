package com.example.demo_chat.rag;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** Structured output of {@link ResponseValidator}: whether a generated answer is grounded. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GroundednessCheck(boolean grounded, String reasoning) {}
