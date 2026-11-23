package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** Base type for business-rule failures in the Warehouse domain. */
public abstract class WarehouseException extends RuntimeException {
  protected WarehouseException(String message) {
    super(message);
  }
}
