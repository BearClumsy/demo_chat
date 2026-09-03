package com.example.demo_chat.rag;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Stage 5 of the RAG pipeline: tracks which of an intent's required slots are still missing. */
@Component
public class SlotFillingService {

  /**
   * @param intent the confirmed intent
   * @param slots the slots collected so far for this dialogue
   * @return the required slots that haven't been filled yet, in declaration order
   */
  public List<String> missingSlots(IntentDefinition intent, Map<String, String> slots) {
    var filled = slots == null ? Map.<String, String>of() : slots;
    return intent.requiredSlots().stream().filter(slot -> !filled.containsKey(slot)).toList();
  }
}
