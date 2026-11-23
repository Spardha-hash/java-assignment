package com.fulfilment.application.monolith.fulfillment.domain;

import static com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.assignment;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.FakeReferences;
import com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.InMemoryStore;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentConflictException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.InvalidFulfillmentException;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.validators.FulfillmentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FulfillmentValidatorTest {

  private InMemoryStore store;
  private FakeReferences references;
  private FulfillmentValidator validator;

  @BeforeEach
  void setUp() {
    store = new InMemoryStore();
    references = new FakeReferences();
    validator = new FulfillmentValidator(store, references);
  }

  private void existing(long storeId, long productId, String warehouse) {
    store.create(assignment(storeId, productId, warehouse));
  }

  @Test
  void acceptsAValidAssignment() {
    assertDoesNotThrow(() -> validator.validateForAssignment(assignment(1, 1, "W1")));
  }

  @Test
  void rejectsIncompleteRequests() {
    assertThrows(InvalidFulfillmentException.class, () -> validator.validateRequest(null));
    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateRequest(new FulfillmentAssignment(null, 1L, "W1")));
    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateRequest(new FulfillmentAssignment(1L, null, "W1")));
    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateRequest(new FulfillmentAssignment(1L, 1L, " ")));

    FulfillmentAssignment withId = assignment(1, 1, "W1");
    withId.id = 9L;
    assertThrows(InvalidFulfillmentException.class, () -> validator.validateRequest(withId));
  }

  @Test
  void rejectsUnknownReferences() {
    assertThrows(
        FulfillmentNotFoundException.class,
        () -> validator.validateReferences(assignment(99, 1, "W1")));
    assertThrows(
        FulfillmentNotFoundException.class,
        () -> validator.validateReferences(assignment(1, 99, "W1")));
    assertThrows(
        FulfillmentNotFoundException.class,
        () -> validator.validateReferences(assignment(1, 1, "NOPE")));
  }

  @Test
  void rejectsDuplicates() {
    existing(1, 1, "W1");

    assertThrows(
        FulfillmentConflictException.class,
        () -> validator.validateNotDuplicate(assignment(1, 1, "W1")));
  }

  @Test
  void productCanHaveAtMostTwoWarehousesPerStore() {
    existing(1, 1, "W1");
    existing(1, 1, "W2");

    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateLimits(assignment(1, 1, "W3")));
    // another store is not affected
    assertDoesNotThrow(() -> validator.validateLimits(assignment(2, 1, "W3")));
  }

  @Test
  void storeCanHaveAtMostThreeWarehouses() {
    existing(1, 1, "W1");
    existing(1, 2, "W2");
    existing(1, 3, "W3");

    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateLimits(assignment(1, 4, "W4")));
    // a warehouse already serving the store is not a new one
    assertDoesNotThrow(() -> validator.validateLimits(assignment(1, 4, "W1")));
  }

  @Test
  void warehouseCanStoreAtMostFiveProducts() {
    for (long product = 1; product <= 5; product++) {
      existing(product % 3 + 1, product, "W1"); // spread over stores to avoid the store limit
    }

    assertThrows(
        InvalidFulfillmentException.class,
        () -> validator.validateLimits(assignment(1, 6, "W1")));
    // a product the warehouse already stores is not a new product
    assertDoesNotThrow(() -> validator.validateLimits(assignment(3, 1, "W1")));
  }
}
