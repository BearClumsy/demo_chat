package com.example.demo_chat.rag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SlotFillingServiceTest {

  private final SlotFillingService slotFillingService = new SlotFillingService();

  private static final IntentDefinition INTENT =
      new IntentDefinition(
          "order_status",
          List.of("Where is my order?"),
          List.of("order_id", "email"),
          "Orders ship within 2 business days.",
          "Answer only about order status.",
          true,
          "Order {order_id} for {email} is on its way.",
          "Please contact a human agent.");

  @Test
  void nullSlotsMeansEveryRequiredSlotIsMissing() {
    // Cassandra materialises an empty map column as null on read.
    assertThat(slotFillingService.missingSlots(INTENT, null)).containsExactly("order_id", "email");
  }

  @Test
  void filledSlotsAreExcludedInDeclarationOrder() {
    assertThat(slotFillingService.missingSlots(INTENT, Map.of("order_id", "12345")))
        .containsExactly("email");
  }

  @Test
  void noMissingSlotsWhenAllFilled() {
    assertThat(
            slotFillingService.missingSlots(
                INTENT, Map.of("order_id", "12345", "email", "a@example.com")))
        .isEmpty();
  }
}
