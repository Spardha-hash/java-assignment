package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import com.fulfilment.application.monolith.fulfillment.domain.ports.FulfillmentStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

/** Database adapter of the {@link FulfillmentStore} port. */
@ApplicationScoped
public class FulfillmentRepository
    implements FulfillmentStore, PanacheRepository<DbFulfillmentAssignment> {

  @Override
  public FulfillmentAssignment findAssignmentById(Long id) {
    DbFulfillmentAssignment entity = findById(id);
    return entity == null ? null : entity.toFulfillmentAssignment();
  }

  @Override
  public List<FulfillmentAssignment> findByStore(Long storeId) {
    return list("storeId", storeId).stream()
        .map(DbFulfillmentAssignment::toFulfillmentAssignment)
        .toList();
  }

  @Override
  public List<FulfillmentAssignment> findByStoreAndProduct(Long storeId, Long productId) {
    return list("storeId = ?1 and productId = ?2", storeId, productId).stream()
        .map(DbFulfillmentAssignment::toFulfillmentAssignment)
        .toList();
  }

  @Override
  public List<FulfillmentAssignment> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode).stream()
        .map(DbFulfillmentAssignment::toFulfillmentAssignment)
        .toList();
  }

  @Override
  public boolean exists(Long storeId, Long productId, String warehouseBusinessUnitCode) {
    return count(
            "storeId = ?1 and productId = ?2 and warehouseBusinessUnitCode = ?3",
            storeId,
            productId,
            warehouseBusinessUnitCode)
        > 0;
  }

  @Override
  public void create(FulfillmentAssignment assignment) {
    DbFulfillmentAssignment entity = new DbFulfillmentAssignment();
    entity.storeId = assignment.storeId;
    entity.productId = assignment.productId;
    entity.warehouseBusinessUnitCode = assignment.warehouseBusinessUnitCode;

    persist(entity);

    assignment.id = entity.id;
  }

  @Override
  public void remove(FulfillmentAssignment assignment) {
    deleteById(assignment.id);
  }
}
