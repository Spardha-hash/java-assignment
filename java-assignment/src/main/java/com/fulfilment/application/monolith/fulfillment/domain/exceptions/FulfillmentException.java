package com.fulfilment.application.monolith.fulfillment.domain.exceptions;

/** Base type for business-rule failures in the fulfilment domain. */
public abstract class FulfillmentException extends RuntimeException {
  protected FulfillmentException(String message) {
    super(message);
  }
}
