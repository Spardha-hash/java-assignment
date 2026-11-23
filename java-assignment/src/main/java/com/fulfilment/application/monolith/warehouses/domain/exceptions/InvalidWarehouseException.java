package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** A business validation failed (duplicate code, unknown location, capacity exceeded, ...). */
public class InvalidWarehouseException extends WarehouseException {
  public InvalidWarehouseException(String message) {
    super(message);
  }
}
