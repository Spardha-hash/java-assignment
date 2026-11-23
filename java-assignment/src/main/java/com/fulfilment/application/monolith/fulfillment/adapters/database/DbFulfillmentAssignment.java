package com.fulfilment.application.monolith.fulfillment.adapters.database;

import com.fulfilment.application.monolith.fulfillment.domain.models.FulfillmentAssignment;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** JPA representation of a {@link FulfillmentAssignment}. */
@Entity
@Table(
    name = "fulfillment_assignment",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"storeId", "productId", "warehouseBusinessUnitCode"}))
public class DbFulfillmentAssignment {

  @Id @GeneratedValue public Long id;

  public Long storeId;

  public Long productId;

  public String warehouseBusinessUnitCode;

  public FulfillmentAssignment toFulfillmentAssignment() {
    FulfillmentAssignment assignment =
        new FulfillmentAssignment(storeId, productId, warehouseBusinessUnitCode);
    assignment.id = id;
    return assignment;
  }
}
