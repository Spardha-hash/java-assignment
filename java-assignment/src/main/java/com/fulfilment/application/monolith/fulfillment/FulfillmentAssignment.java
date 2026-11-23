package com.fulfilment.application.monolith.fulfillment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * States that a Warehouse (identified by its business unit code, which survives warehouse
 * replacement) fulfils a Product for a Store.
 */
@Entity
@Table(
    name = "fulfillment_assignment",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"storeId", "productId", "warehouseBusinessUnitCode"}))
public class FulfillmentAssignment {

  @Id @GeneratedValue public Long id;

  public Long storeId;

  public Long productId;

  public String warehouseBusinessUnitCode;

  public FulfillmentAssignment() {}
}
