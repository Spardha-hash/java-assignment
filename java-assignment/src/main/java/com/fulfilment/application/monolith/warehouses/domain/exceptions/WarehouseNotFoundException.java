package com.fulfilment.application.monolith.warehouses.domain.exceptions;

/** The requested (active) warehouse does not exist. */
public class WarehouseNotFoundException extends WarehouseException {
  public WarehouseNotFoundException(String message) {
    super(message);
  }
}
