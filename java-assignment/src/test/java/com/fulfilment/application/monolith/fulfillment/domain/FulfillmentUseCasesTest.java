package com.fulfilment.application.monolith.fulfillment.domain;

import static com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.assignment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.FakeReferences;
import com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.InMemoryStore;
import com.fulfilment.application.monolith.fulfillment.domain.FulfillmentTestSupport.RecordingPublisher;
import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.FulfillmentNotFoundException;
import com.fulfilment.application.monolith.fulfillment.domain.exceptions.InvalidFulfillmentException;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.AssignFulfillmentUseCase;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.ListFulfillmentUseCase;
import com.fulfilment.application.monolith.fulfillment.domain.usecases.RemoveFulfillmentUseCase;
import com.fulfilment.application.monolith.fulfillment.domain.validators.FulfillmentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FulfillmentUseCasesTest {

  private InMemoryStore store;
  private RecordingPublisher publisher;
  private AssignFulfillmentUseCase assign;
  private RemoveFulfillmentUseCase remove;
  private ListFulfillmentUseCase list;

  @BeforeEach
  void setUp() {
    store = new InMemoryStore();
    publisher = new RecordingPublisher();
    var validator = new FulfillmentValidator(store, new FakeReferences());
    assign = new AssignFulfillmentUseCase(store, validator, publisher);
    remove = new RemoveFulfillmentUseCase(store, publisher);
    list = new ListFulfillmentUseCase(store);
  }

  @Test
  void assignStoresTheAssociationAndPublishesAnEvent() {
    FulfillmentAssignment created = assign.assign(assignment(1, 1, "W1"));

    assertNotNull(created.id);
    assertEquals(1, store.data.size());
    assertEquals(1, publisher.events.size());
    assertEquals(FulfillmentChangedEvent.Type.ASSIGNED, publisher.events.get(0).type());
  }

  @Test
  void assignRejectsInvalidRequestsAndPublishesNothing() {
    assertThrows(InvalidFulfillmentException.class, () -> assign.assign(null));
    assertThrows(
        FulfillmentNotFoundException.class, () -> assign.assign(assignment(99, 1, "W1")));

    assertTrue(store.data.isEmpty());
    assertTrue(publisher.events.isEmpty());
  }

  @Test
  void listReturnsOnlyTheAssociationsOfTheStore() {
    assign.assign(assignment(1, 1, "W1"));
    assign.assign(assignment(2, 1, "W1"));

    assertEquals(1, list.listByStore(1L).size());
    assertEquals(0, list.listByStore(3L).size());
  }

  @Test
  void removeDeletesTheAssociationAndPublishesAnEvent() {
    FulfillmentAssignment created = assign.assign(assignment(1, 1, "W1"));

    remove.remove(created.id);

    assertTrue(store.data.isEmpty());
    assertEquals(FulfillmentChangedEvent.Type.REMOVED, publisher.events.get(1).type());
  }

  @Test
  void removeOfUnknownAssociationFails() {
    assertThrows(FulfillmentNotFoundException.class, () -> remove.remove(123L));
    assertThrows(FulfillmentNotFoundException.class, () -> remove.remove(null));
  }
}
