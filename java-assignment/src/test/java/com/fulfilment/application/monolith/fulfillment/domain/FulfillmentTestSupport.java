package com.fulfilment.application.monolith.fulfillment.domain;

import com.fulfilment.application.monolith.fulfillment.domain.event.FulfillmentChangedEvent;
import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentEventPublisher;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentReferenceResolver;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** In-memory fakes of the fulfilment ports, shared by the unit tests. */
public final class FulfillmentTestSupport {

  private FulfillmentTestSupport() {}

  public static FulfillmentAssignment assignment(long store, long product, String warehouse) {
    return new FulfillmentAssignment(store, product, warehouse);
  }

  public static class InMemoryStore implements FulfillmentStore {
    public final List<FulfillmentAssignment> data = new ArrayList<>();
    private long nextId = 1;

    @Override
    public FulfillmentAssignment findAssignmentById(Long id) {
      return data.stream().filter(a -> a.id.equals(id)).findFirst().orElse(null);
    }

    @Override
    public List<FulfillmentAssignment> findByStore(Long storeId) {
      return data.stream().filter(a -> a.storeId.equals(storeId)).toList();
    }

    @Override
    public List<FulfillmentAssignment> findByStoreAndProduct(Long storeId, Long productId) {
      return data.stream()
          .filter(a -> a.storeId.equals(storeId) && a.productId.equals(productId))
          .toList();
    }

    @Override
    public List<FulfillmentAssignment> findByWarehouse(String code) {
      return data.stream().filter(a -> a.warehouseBusinessUnitCode.equals(code)).toList();
    }

    @Override
    public boolean exists(Long storeId, Long productId, String code) {
      return data.stream()
          .anyMatch(
              a ->
                  a.storeId.equals(storeId)
                      && a.productId.equals(productId)
                      && a.warehouseBusinessUnitCode.equals(code));
    }

    @Override
    public void create(FulfillmentAssignment assignment) {
      assignment.id = nextId++;
      data.add(assignment);
    }

    @Override
    public void remove(FulfillmentAssignment assignment) {
      data.remove(assignment);
    }
  }

  /** Everything exists unless removed from the sets. */
  public static class FakeReferences implements FulfillmentReferenceResolver {
    public Set<Long> stores = Set.of(1L, 2L, 3L);
    public Set<Long> products = Set.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);
    public Set<String> warehouses = Set.of("W1", "W2", "W3", "W4");

    @Override
    public boolean storeExists(Long storeId) {
      return stores.contains(storeId);
    }

    @Override
    public boolean productExists(Long productId) {
      return products.contains(productId);
    }

    @Override
    public boolean activeWarehouseExists(String code) {
      return warehouses.contains(code);
    }
  }

  public static class RecordingPublisher implements FulfillmentEventPublisher {
    public final List<FulfillmentChangedEvent> events = new ArrayList<>();

    @Override
    public void publish(FulfillmentChangedEvent event) {
      events.add(event);
    }
  }
}
